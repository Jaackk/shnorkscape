# Ribbon and modern-mode logical cache files

`inspected-ribbon-files.json` and `inspected-modern-mode-files.json` contain the
raw logical files extracted from the selected local 947 cache on 2026-09-08.
Keys are `index/group/file`; values are lowercase hexadecimal bytes. Extraction
uses the existing `inspect_native947_interface_slots.archive()` reader, including
JS5 decompression and multi-file splitting. These are not generated from the
runtime hash tables.

The 45-file ribbon fixture includes five static components, eight client-variable
bit definitions, nineteen scripts, three enums and ten structs. Scripts include
the actual icon builder 13845, click dispatcher 5588, standalone tab rebuilders
8361/8362/8363, position/size helpers 13268/11145 and layout savers 8707/8708.
The nine-file mode fixture contains server-variable bit definitions
27168, 27169, 22875, 39917, 49044 and 60098, plus tutorial checks 15532,
15534 and 734. The distinction matters: the ribbon writes use server
opcodes 115 (small) and 55 (large) to modify client domain 2; mode assertions use
opcode 50 and domain 0. Settings stores 138, which requires the existing large
writer because the native small form accepts only 0..127. Its literal payload
is `00 8a 00 00 20 55`; the small zero terminator remains a separate write.
Client bit 21816 (parent 4116, bit 0) is initialized with display bit 42113.
The pinned native checkbox script 2755 toggles 21816 and then copies it to
42113; forcing only the display bit would leave a previous disabled preference
out of sync and make the first click appear unchanged.

The ordinary-world login profile sends server bits 39917=98, 49044=100 and
60098=1 before mounting the panels. Script 2755 calls 15532(0), which rejects
editing if 39917<98, 49044<100, or both varp 12314<=0 and bit 60098 is false
(15534 calls 734, a comparison against 1). Varp 12314 is the active Leagues
selector, so this profile leaves it alone. The third bit is domain 0, parent
1264 bit 21; sending a varbit preserves that parent's unrelated state.
This marks the existing direct-to-world session outside the tutorial; it does
not implement tutorial or quest progression. The mode fixture pins the exact
definitions and check scripts, while the bootstrap golden records the six
mode/onboarding writes before panel onLoad. Native editing still requires live
acceptance after installation.

`Native947RibbonTest` rejects a one-byte change to any pinned file, checks the
client-variable packet transforms independently, and simulates previously saved
ribbon settings to ensure the zero terminator prevents old buttons from leaking
into the selected four-panel plus Settings list. It decodes enum 13319 to prove
Settings button 137 is dynamic actor 7 (not its fifth displayed position), and
enum 13321 to establish management ID 9. The literal event packet assertion
permits only operation 1 on `1431:0`, slots 7..7; the other four shortcuts retain
their native toggles. The tests also pin the 224-by-48 bottom-right geometry,
eight-pixel inset, and save/rebuild script arguments and ordering, including
clearing the twelve implemented/inactive chat
tab bars before saving them into working layout 9 and preset 8. The ribbon
fixture also includes enum 7716, ribbon minimum struct 38883, eight inactive slot structs and their shared
minimum-size struct 21320. Hidden 100-by-135 rectangles avoid script 8701's
uninitialized all-zero layout sentinel; that script is already pinned by the
equipment binding verifier and its separate raw fixture.
These tests do not establish native rendering, click
behaviour or the client-owned interface-mode bit; those still require the live
client check.
