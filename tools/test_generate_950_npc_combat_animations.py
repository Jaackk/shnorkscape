import unittest
from generate_950_npc_combat_animations import compatibility, sequence

# One literal opcode1 frame, duration3, low7, high9; no real cache is needed.
FRAME = bytes.fromhex('010001000300070009')

class SequenceBindingTest(unittest.TestCase):
    def test_identical_complete_definition_is_accepted(self):
        kind, parsed, reason = compatibility(FRAME+b'\0', FRAME+b'\0')
        self.assertEqual('identical-definition', kind)
        self.assertEqual(3, parsed['durationCycles'])
        self.assertIsNone(reason)

    def test_complete_frame_binding_allows_unrelated_priority_change(self):
        kind, _, _ = compatibility(FRAME+b'\x05\x01\0', FRAME+b'\x05\x02\0')
        self.assertEqual('identical-frame-duration-binding', kind)

    def test_new_frame_source_cannot_be_accepted_from_equal_missing_opcode1(self):
        old = bytes.fromhex('1900011a0000000300')
        new = bytes.fromhex('1900021a0000000300')
        self.assertIsNone(compatibility(old, new)[0])

    def test_new_duration_base_is_not_hidden_by_equal_opcode1(self):
        self.assertIsNone(compatibility(FRAME+b'\0', FRAME+bytes.fromhex('1a0000000400'))[0])
        self.assertEqual(7, sequence(FRAME+bytes.fromhex('1a0000000400'))['durationCycles'])

    def test_changed_frame_or_duration_cannot_pass(self):
        for index in (4,6,8):
            changed = bytearray(FRAME); changed[index] ^= 1
            self.assertIsNone(compatibility(FRAME+b'\0', bytes(changed)+b'\0')[0])

    def test_short_item_sentinels_do_not_consume_following_fields(self):
        raw = bytes.fromhex('07ffff')+FRAME+b'\0'
        self.assertEqual(3, sequence(raw, modern=False)['durationCycles'])
        self.assertEqual(3, sequence(raw)['durationCycles'])

    def test_truncated_and_trailing_bytes_are_rejected(self):
        for raw in (FRAME[:-1], FRAME, FRAME+b'\0\0'):
            with self.assertRaises(ValueError): sequence(raw)

if __name__ == '__main__': unittest.main()
