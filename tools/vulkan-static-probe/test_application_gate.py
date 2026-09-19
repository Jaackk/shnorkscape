import copy
import unittest
from unittest.mock import patch
import check_application_gate as gate

class ApplicationTests(unittest.TestCase):
    def test_native_load_visibility_is_proven_not_a_blanket_ignore(self):
        self.assertEqual(gate.custom_visibility_constants(),[[1032,8],[1033,8],[1034,8],[1035,8]])

    def test_live_application_37768(self):
        paths=[gate.ROOT/('logs/workspace-static-v4-37768-'+p+'.json') for p in 'ABC']
        if not all(p.exists() for p in paths):self.skipTest('Local private runtime evidence')
        report=gate.compare(paths)
        self.assertTrue(report['structuralApplicationPassed'])
        self.assertEqual(report['stageMismatchIds'],[])
        self.assertEqual(report['customMatrixChangedDuringApply'],[])
        self.assertEqual(report['unexplainedApplicationFields'],[])

    def test_approved_local_source(self):
        if not (gate.ROOT/'logs/workspace-static-v4-35192-A.json').exists():
            self.skipTest('Private fixture is local only')
        schema,items,report=gate.source()
        self.assertEqual(len(items),912)
        self.assertEqual(report['custom1VsActive']['equalFields'],712)
        self.assertFalse(items[6056]['found'])

    def test_stage_comparison_preserves_absence_masks_and_active(self):
        if not (gate.ROOT/'logs/workspace-static-v4-35192-A.json').exists():
            self.skipTest('Private fixture is local only')
        schema,fixture,report=gate.source()
        a=copy.deepcopy(fixture)
        ids={r[1] for slot in ('6','7','12','13') for row in schema['slots'][slot].values() for r in row}
        for i in ids:
            a[i].update(found=False,variantTag=None,int32=None)
        b=copy.deepcopy(a)
        for i in ids|set(schema['integerMetadataIds']):
            if fixture[i]['found']: b[i]=copy.deepcopy(fixture[i])
        c=copy.deepcopy(b)
        data=[dict(pid=999,thread=1,imageSha256=gate.IMAGE,tick=t) for t in (1,2,3)]
        with patch.object(gate,'source',return_value=(schema,fixture,report.copy())),patch.object(gate,'load',side_effect=list(zip(data,(a,b,c)))),patch.object(gate,'controls'):
            self.assertEqual(gate.compare(['A','B','C'])['stageMismatchIds'],[])
        b[6056].update(found=True,variantTag=0,int32=0)
        b[2912]['int32']^=32
        with patch.object(gate,'source',return_value=(schema,fixture,report.copy())),patch.object(gate,'load',side_effect=list(zip(data,(a,b,c)))),patch.object(gate,'controls'):
            self.assertEqual(gate.compare(['A','B','C'])['stageMismatchIds'],[2912,6056])

if __name__=='__main__':unittest.main()
