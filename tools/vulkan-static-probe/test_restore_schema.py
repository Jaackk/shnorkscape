import copy
import unittest
from restore_schema import schema,plan

class RestoreSchemaTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):cls.s=schema()
    def fixture(self):
        items=[dict(id=i,found=True,variantTag=0,int32=0) for i in self.s['ids']]
        next(i for i in items if i['id']==8372)['int32']=6
        return dict(version=1,account='account-a',session='session-a',schemaSha256=self.s['schemaSha256'],items=items)
    def test_deterministic_and_scope(self):
        self.assertEqual(schema(),self.s)
        self.assertEqual(len(self.s['ids']),912)
        self.assertEqual({v['id'] for v in self.s['slotMetadata'].values()},{3295,3380,5215,5352,2912})
    def test_plan_is_not_executable_and_masks_sibling_flags(self):
        e=self.fixture()
        next(i for i in e['items'] if i['id']==3295)['int32']=-1
        p=plan(e,'account-a','session-a',self.s)
        self.assertFalse(p['executable'])
        self.assertEqual(p['stages'][4]['values'][0],[19035,31])
        self.assertTrue(p['activeMatrixRetainedNotReplayed'])
    def test_absent_not_zero(self):
        e=self.fixture()
        next(i for i in e['items'] if i['id']==3296).update(found=False,variantTag=None,int32=None)
        p=plan(e,'account-a','session-a',self.s)
        self.assertNotIn(3296,[i for i,v in p['stages'][0]['values']])
    def test_account_and_session(self):
        for a,s in [('account-b','session-a'),('account-a','old-session')]:
            with self.assertRaises(ValueError):plan(self.fixture(),a,s,self.s)
    def test_incomplete_v3_and_bad_records(self):
        e=self.fixture();e['items']=e['items'][:-5]
        with self.assertRaises(ValueError):plan(e,'account-a','session-a',self.s)
        for update in ({'id':999999},{'variantTag':1},{'int32':True},{'int32':2**31},{'found':False}):
            e=self.fixture();e['items'][0].update(update)
            with self.assertRaises(ValueError):plan(e,'account-a','session-a',self.s)
    def test_duplicate_schema_and_selection(self):
        for field,value in [('schemaSha256','bad'),('version',2)]:
            e=self.fixture();e[field]=value
            with self.assertRaises(ValueError):plan(e,'account-a','session-a',self.s)
        e=self.fixture();e['items'][1]=copy.deepcopy(e['items'][0])
        with self.assertRaises(ValueError):plan(e,'account-a','session-a',self.s)
        e=self.fixture();next(i for i in e['items'] if i['id']==8372)['int32']=9
        with self.assertRaises(ValueError):plan(e,'account-a','session-a',self.s)

if __name__=='__main__':unittest.main()
