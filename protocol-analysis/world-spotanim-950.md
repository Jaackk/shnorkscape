# Native950 world spot animation

Exact client SHA256: fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36.
Server descriptor197 has fixed15 bytes. Parser0x1400efb10 independently identifies the tile branch with target >=0x40000000; plane=(target>>28)&3, x=(target>>14)&16383, y=target&16383. It converts absolute tiles to fine coordinates, obtains terrain height, and inserts the graphic in the world effect manager at state+0x19988. Graphic65535 takes that manager's remove-at-coordinate path. No entity mask or moving caster attachment is used.

|Bytes|Field/reader|
|---|---|
|0-3|Target: b2<<24, b3<<16, b0<<8, b1|
|4|Rotation low3; flag high bit; writer zero|
|5-7|Offsets: b6<<16, b5<<8, b7; X=(v&2047)-1023, Y=((v>>11)&2047)-1023; writer neutral|
|8|Entity slot, decoded b+128; ignored by tile branch; writer zero encoded128|
|9-10|Signed height, BE low-byte-minus128; writer zero|
|11-12|Graphic, LE low-byte-minus128;65535 removes|
|13-14|Delay low15 bits, LE low-byte-minus128; high flag bit; writer zero|

Sunshine: local Vernox830 EffectsManager names19866/3856. Exact950 named Azure Sunshine39861 and Iaian Sunset46100 both bind player pose19866. Azure's effect sequence19865 equals exact950 Graphic3856.emoteId; base model84388 remains distinct from cosmetic model127576. These are cross-revision candidates corroborated by exact950 typed assets, not nearby-number guesses. Assets are hash pinned. World presentation remains LIVE TEST PENDING.

Removal is reference-counted by coordinate across owners. One owner's expiry/death/logout cannot remove another owner's active shared-coordinate combat area. Persistent replay to a newly arriving viewer remains PARTIAL.

Death's Swiftness uses graphic8996, sequence36601, model139366: the named standard ranged effect. Exact950 definitions and the corresponding beta949.1 group/model bytes match. Cosmetic preview sequence35073 and the old3869 effect are not selected. Player pose19879 retains its dedicated named identity.
