import unittest
from unittest.mock import patch
from check_restart_gate import compare
from check_application_gate import ROOT


class RestartGateTest(unittest.TestCase):
    def test_same_process_rejected(self):
        data = {'pid': 7}
        with patch('check_restart_gate.source', return_value=({}, {}, {})), \
                patch('check_restart_gate.load', return_value=(data, {})):
            with self.assertRaisesRegex(ValueError, 'separate client'):
                compare('a', 'b')

    @unittest.skipUnless((ROOT/'logs/workspace-static-v4-41956-A.json').exists(), 'Private live evidence')
    def test_reviewed_restart(self):
        result = compare(ROOT/'logs/workspace-static-v4-3800-A.json',
                         ROOT/'logs/workspace-static-v4-41956-A.json')
        self.assertTrue(result['exactRestartReadbackPassed'])
        self.assertEqual(result['entries'], 912)
        self.assertEqual(result['changedIdsAcrossRestart'], [])
