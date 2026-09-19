import copy
import hashlib
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch
import snapshot_schema as generator
from compare_snapshots import compare,field,load

class SnapshotTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.raw=(generator.HERE/'snapshot-schema.json').read_bytes()
        cls.schema=json.loads(cls.raw)
    def test_pinned_deterministic_generation(self):
        self.assertEqual(generator.encoded(generator.generate()),self.raw)
        self.assertEqual(generator.encoded(generator.generate()),self.raw)
        self.assertEqual(len(self.schema['ids']),907)
        self.assertEqual(len(self.schema['descriptorIds']),900)
        header=(generator.HERE/'snapshot-schema.h').read_text()
        self.assertIn(hashlib.sha256(self.raw).hexdigest(),header)
        self.assertIn('{'+','.join(map(str,self.schema['ids']))+'}',header)
        for rows in self.schema['slots'].values():
            self.assertEqual(len(rows),90)
            self.assertEqual(len({ref[1] for row in rows.values() for ref in row}),180)
    def test_pin_failure(self):
        name='cache/2/69.dat'
        with patch.dict(generator.PINS,{name:'0'*64}):
            with self.assertRaisesRegex(ValueError,'hash mismatch'):
                generator.generate()
    def test_truncated_evidence(self):
        with self.assertRaises(ValueError):generator.container(b'\2\0')
    def make_items(self):
        return {i:dict(id=i,found=True,stable=True,variantTag=0,int32=0) for i in self.schema['ids']}
    def test_comparison_and_missing_not_zero(self):
        a=self.make_items();b=copy.deepcopy(a);c=copy.deepcopy(a)
        self.assertTrue(compare(a,b,c,self.schema)['custom1BvsActiveC']['completeEquality'])
        ref=self.schema['slots']['6']['1000'][0]
        b[ref[1]].update(found=False,variantTag=None,int32=None)
        result=compare(a,b,c,self.schema)
        self.assertIsNone(field(b,ref))
        self.assertFalse(result['custom1BvsActiveC']['completeEquality'])
        self.assertIn(ref[1],result['changedIdsAtoB'])
    def test_signed_parent_fields(self):
        self.assertEqual(field({1:dict(found=True,int32=-1)},[0,1,20,31]),4095)
    def test_type_scope_and_stability_refusal(self):
        items=self.make_items()
        data=dict(status='snapshot-stable',phase='A',nativeWrites=False,
                  schemaSha256=hashlib.sha256(self.raw).hexdigest(),items=list(items.values()))
        with tempfile.TemporaryDirectory() as temp:
            path=Path(temp)/'a.json'
            path.write_text(json.dumps(data))
            load(path,'A',self.schema,data['schemaSha256'])
            data['items'][0]['variantTag']=1
            path.write_text(json.dumps(data))
            with self.assertRaises(ValueError):load(path,'A',self.schema,data['schemaSha256'])
            data['items'].pop()
            path.write_text(json.dumps(data))
            with self.assertRaises(ValueError):load(path,'A',self.schema,data['schemaSha256'])

if __name__ == '__main__':unittest.main()
