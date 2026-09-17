#!/usr/bin/env python3
"""Compare legacy and modern spotanim definitions without opening writable cache handles."""
import argparse, bz2, hashlib, json, lzma, zlib
from functools import lru_cache
from pathlib import Path

class Reader:
    def __init__(self, data): self.data, self.pos = data, 0
    def integer(self, n=4, signed=False):
        out = int.from_bytes(self.data[self.pos:self.pos+n], "big", signed=signed)
        self.pos += n
        assert self.pos <= len(self.data)
        return out
    def smart(self, fmt):
        return self.integer(4) & 0x7fffffff if fmt >= 7 and self.data[self.pos] & 128 else self.integer(2)

def unpack(raw):
    kind, size = raw[0], int.from_bytes(raw[1:5], "big")
    if kind == 0: return raw[5:5+size]
    expected, compressed = int.from_bytes(raw[5:9], "big"), raw[9:9+size]
    if kind == 1: out = bz2.decompress(b"BZh1" + compressed)
    elif kind == 2: out = zlib.decompress(compressed, 31)
    elif kind == 3:
        prop = compressed[0]
        decoder = lzma.LZMADecompressor(format=lzma.FORMAT_RAW, filters=[dict(
            id=lzma.FILTER_LZMA1, dict_size=int.from_bytes(compressed[1:5], "little"),
            pb=prop//45, lp=(prop%45)//9, lc=prop%9)])
        out = decoder.decompress(compressed[5:], max_length=expected)
    else: raise ValueError("Unknown container compression " + str(kind))
    assert len(out) == expected
    return out

class Cache:
    def __init__(self, path, flat): self.path, self.flat = Path(path), flat
    @lru_cache(None)
    def raw(self, index, group):
        if self.flat: return (self.path/str(index)/(str(group)+".dat")).read_bytes()
        with (self.path/("main_file_cache.idx"+str(index))).open("rb") as f:
            f.seek(group*6); h=f.read(6)
        assert len(h)==6
        size, sector = int.from_bytes(h[:3], "big"), int.from_bytes(h[3:], "big")
        assert size > 0
        out, part = bytearray(), 0
        with (self.path/("main_file_cache.dat2m" if index==40 else "main_file_cache.dat2")).open("rb") as f:
            while len(out)<size:
                assert sector > 0
                f.seek(sector*520); block=f.read(520)
                width=4 if group>65535 else 2; header=width+6
                assert int.from_bytes(block[:width], "big")==group
                assert int.from_bytes(block[width:width+2], "big")==part
                assert block[width+5]==index
                sector=int.from_bytes(block[width+2:width+5], "big")
                out.extend(block[header:header+min(520-header,size-len(out))]); part+=1
        return bytes(out)
    @lru_cache(None)
    def reference(self, index):
        r=Reader(unpack(self.raw(255,index))); fmt=r.integer(1)
        assert fmt in (5,6,7)
        if fmt>=6:r.integer()
        flags=r.integer(1); count=r.smart(fmt); ids=[]; total=0
        for _ in range(count):total+=r.smart(fmt);ids.append(total)
        r.pos+=count*(4+(4 if flags&1 else 0)+(4 if flags&8 else 0)+(64 if flags&2 else 0)+(8 if flags&4 else 0)+4)
        counts=[r.smart(fmt) for _ in ids]; out={}
        for group,n in zip(ids,counts):
            total=0; files=[]
            for _ in range(n):total+=r.smart(fmt);files.append(total)
            out[group]=files
        return out
    @lru_cache(None)
    def archive(self, index, group):
        ids=self.reference(index)[group]; data=unpack(self.raw(index,group))
        if len(ids)==1:return {ids[0]:data}
        if not ids:return {}
        chunks=data[-1]; table=Reader(data); table.pos=len(data)-1-chunks*len(ids)*4
        end=table.pos; out={k:bytearray() for k in ids}; pos=0
        for _ in range(chunks):
            size=0
            for key in ids:
                size+=table.integer(signed=True); assert size>=0
                out[key].extend(data[pos:pos+size]); pos+=size
        assert pos==end
        return {k:bytes(v) for k,v in out.items()}
    def definitions(self, index):
        return {group*256+file:blob for group in self.reference(index)
                for file,blob in self.archive(index,group).items()}

def sha(blob): return hashlib.sha256(blob).hexdigest()

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--legacy", required=True)
    parser.add_argument("--modern", required=True)
    parser.add_argument("--resource", required=True)
    parser.add_argument("--report", required=True)
    args=parser.parse_args()
    legacy, modern = Cache(args.legacy,False), Cache(args.modern,True)
    old,new = legacy.definitions(21),modern.definitions(21)
    # Empty/terminator-only configs do not establish an effect's identity.
    same={i:sha(new[i]) for i in sorted(old.keys() & new.keys()) if old[i]==new[i] and len(new[i])>1}
    changed=sorted(i for i in old.keys() & new.keys() if old[i]!=new[i])
    missing=sorted(old.keys()-new.keys())
    added=sorted(new.keys()-old.keys())
    representatives={str(i):dict(same=i in same,legacyHex=old[i].hex(),modernHex=new[i].hex(),
                     legacySha256=sha(old[i]),modernSha256=sha(new[i]))
                     for i in [3,12,53,56,85,93,94,111,184,200,299,369,436,1576,1577,2000]}
    dependencies={}
    for id in (94,184,436,1576):
        # These four raw definitions start with the model and sequence opcodes (1 and 2).
        r=Reader(new[id]); fields={}
        for _ in range(2):
            op=r.integer(1); assert op in (1,2)
            fields[op]=r.smart(7)
        model,sequence=fields[1],fields[2]
        row=dict(modelId=model,sequenceId=sequence)
        for key,index,group,file in (("sequence",20,sequence>>7,sequence&127),("model47",47,model,0)):
            a=legacy.archive(index,group)[file]; b=modern.archive(index,group)[file]
            row[key]=dict(same=a==b,legacyLength=len(a),modernLength=len(b),legacySha256=sha(a),modernSha256=sha(b))
        dependencies[str(id)]=row
    report=dict(format=1,legacyRevision=910,revision=950,index=21,legacyPath=str(legacy.path),modernPath=str(modern.path),
                legacyDefinitions=len(old),modernDefinitions=len(new),verifiedDefinitions=len(same),
                changedDefinitions=len(changed),missingDefinitions=len(missing),newDefinitions=len(added),
                legacyReferenceSha256=sha(legacy.raw(255,21)),modernReferenceSha256=sha(modern.raw(255,21)),
                changedIds=changed,missingIds=missing,newIds=added,representatives=representatives,
                representativeDependencies=dependencies,
                limitation="Exact spotanim config equality does not prove unchanged rendered models, sequences or sounds.")
    resource=Path(args.resource); reportPath=Path(args.report)
    resource.parent.mkdir(parents=True,exist_ok=True);reportPath.parent.mkdir(parents=True,exist_ok=True)
    resource.write_text("# Generated by tools/verify_950_player_effects.py; exact nonempty 910/950 index21 definition equality.\n"
                        +"format=1\nlegacyRevision=910\nrevision=950\nindex=21\n"
                        +"".join("id.%d=%s\n"%(i,h) for i,h in same.items()),encoding="ascii")
    reportPath.write_text(json.dumps(report,indent=2)+"\n",encoding="utf-8")
    print(json.dumps({k:v for k,v in report.items() if not isinstance(v,(dict,list))},indent=2))
if __name__=="__main__":main()
