import copy
import unittest
from check_setter_gate import verify,CONTROLS,IMAGE

class SetterGateTests(unittest.TestCase):
    def fixture(self):
        snapshots=[];controls=[]
        for n,phase in enumerate('ABC'):
            d=dict(pid=10,thread=20,imageSha256=IMAGE,tick=n,phase=phase)
            items={3296:dict(found=n>0,variantTag=0 if n else None,int32=[None,4097,0][n]),
                   3297:dict(found=False,variantTag=None,int32=None)}
            snapshots.append((d,items))
            controls.append(dict(pid=10,thread=20,phase=phase,nativeWrites=False,
                controls=[dict(id=i,found=True,stable=True,variantTag=0,int32=v) for i,v in CONTROLS.items()]))
        return snapshots,controls
    def test_exact_readback(self):
        self.assertEqual(verify(*self.fixture())['readbackGate'],'passed')
    def test_zero_is_not_absence(self):
        s,c=self.fixture();s[2][1][3296].update(found=False,variantTag=None,int32=None)
        with self.assertRaises(ValueError):verify(s,c)
    def test_wrong_value_control_and_unrelated_change(self):
        s,c=self.fixture();s[1][1][3296]['int32']=1
        with self.assertRaises(ValueError):verify(s,c)
        s,c=self.fixture();c[1]['controls'][0]['int32']=1
        with self.assertRaises(ValueError):verify(s,c)
        s,c=self.fixture();s[2][1][3297].update(found=True,variantTag=0,int32=0)
        with self.assertRaises(ValueError):verify(s,c)
    def test_wrong_image_and_session(self):
        s,c=self.fixture();s[2][0]['pid']=11
        with self.assertRaises(ValueError):verify(s,c)
        s,c=self.fixture()
        for d,items in s:d['imageSha256']='bad'
        with self.assertRaises(ValueError):verify(s,c)

if __name__=='__main__':unittest.main()
