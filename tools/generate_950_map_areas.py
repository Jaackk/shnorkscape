"""Read-only cache derivation of native950 map-area membership; writes only the generated resource/report.

WIN64 0x14032e690 reads index23/group3/file=(squareX | squareY<<7).
Its RLE expands24-bit labels into64 entries (chunkX*8+chunkY).
0x1403162dc compares each label with area-definition +0x38, the opcode2
label in index2/group83. It is an identity, not an arbitrary display colour.
"""
import argparse, hashlib, json, struct
from pathlib import Path
import cache950 as C

ROOT=Path(__file__).resolve().parents[1]

def digest(files):
    h=hashlib.sha256()
    for fid,raw in sorted(files.items()):
        h.update(struct.pack(">II",fid,len(raw))); h.update(raw)
    return h.hexdigest()

def expand(raw):
    out=[]; p=0
    while p<len(raw):
        if p+3>len(raw): raise ValueError("Truncated area label")
        label=int.from_bytes(raw[p:p+3],"big"); p+=3
        count=raw[p] if p<len(raw) else 64-len(out)
        if p<len(raw): p+=1
        if count<1 or len(out)+count>64: raise ValueError("Invalid area run")
        out.extend([label]*count)
    if len(out)!=64: raise ValueError("Expected64 area chunks")
    return out

def main():
    configs=C.archive(2,83); maps=C.archive(23,3)
    labels={}
    for fid,raw in sorted(configs.items()):
        if len(raw)<5 or raw[0]!=2 or raw[-1]!=0: raise ValueError("Area label must start config: %s"%fid)
        label=int.from_bytes(raw[1:4],"big")
        if label in labels: raise ValueError("Ambiguous area label")
        labels[label]=fid
    squares=[]; unknown=set(); mixed=0
    for fid,raw in sorted(maps.items()):
        chunks=expand(raw); runs=[]
        for label in chunks:
            if label not in labels:
                if label!=0: raise ValueError("Unknown nonzero area label: %x"%label)
                unknown.add(fid)
            area=labels.get(label,-1)
            if runs and runs[-1][0]==area: runs[-1][1]+=1
            else: runs.append([area,1])
        if len(runs)>1: mixed+=1
        squares.append([fid]+[v for run in runs for v in run])
    lodestones=[]
    for row in json.loads((ROOT/"Ataraxia950/resources/native950/lodestones-950.json").read_text())["destinations"]:
        x,y=row["arrival"]["x"],row["arrival"]["y"]
        fid=(x>>6)|((y>>6)<<7); raw=maps[fid]; label=expand(raw)[((x>>3)&7)*8+((y>>3)&7)]
        lodestones.append({"name":row["name"],"arrival":row["arrival"],"mapFile":fid,"label":"%06x"%label,"areaType":labels[label]})
    result={"revision":950,"default":474,"source":"index23/group3 64-chunk labels joined with opcode2 in index2/group83", "bindings":[{"index":2,"group":83,"files":len(configs),"sha256":digest(configs)},{"index":23,"group":3,"files":len(maps),"sha256":digest(maps)}],"areas":[[fid,label] for label,fid in sorted(labels.items(),key=lambda e:e[1])],"squares":squares}
    target=ROOT/"Ataraxia950/resources/native950/map-areas-950.json"
    target.write_text(json.dumps(result,separators=(",",":"))+"\n",encoding="utf-8")
    report={"method":result["source"],"bindings":result["bindings"],"areaDefinitions":len(configs),"mapSquares":len(maps),"mixedSquares":mixed,"unassignedLabelZeroSquares":sorted(unknown),"lodestones":lodestones,"binaryEvidence":{"archiveDescriptor":"0x1402af9bd: index23", "mapKeyAndRequest":"0x14032e6b6 / 0x14032e7dd: group3, file=(y<<7)|x", "labelRle":"0x14032e900..0x14032ea1d", "chunkOrderAndLabelComparison":"0x1403162d1..0x1403162e1; outerX increments8 at0x140316436"}}
    (ROOT/"protocol-analysis/map-areas-950-evidence.json").write_text(json.dumps(report,indent=2)+"\n",encoding="utf-8")
    print("Derived %d areas, %d map squares (%d mixed); all29 lodestones resolved"%(len(configs),len(maps),mixed))
    for row in lodestones: print("%-20s area %-3d label %s"%(row["name"],row["areaType"],row["label"]))

if __name__=="__main__": main()
