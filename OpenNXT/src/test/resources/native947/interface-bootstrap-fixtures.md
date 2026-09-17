# Native 947 interface bootstrap fixtures

These files pin the **Kotlin handoff's** unframed opcode and payload sequence. The
later Ataraxia equipment, skills, action-bar and stat bursts have their own engine
tests; these fixtures do not claim to represent that combined login stream.

`interface-bootstrap-baseline.txt` was independently encoded from the pre-change
`Ataraxia947Handoff.interfaceBootstrap()` and the committed cache inventory at
`data/prot/947/generated/native947-3/verified/ui/slot-inventory-7716.json`. It has
85 packets: one root open, five child opens, three wrapper shows, 72 wrapper hides,
one backpack event-settings packet, one chat filter varbit and two chat scripts.
It is the extraction regression baseline, not the current visibility policy.

`interface-bootstrap-modern.txt` preserves all of those bytes and their order,
with three explicit policy additions (96 packets total):

- After the root open and before child opens, set varbits 27168, 27169 and 22875
  to zero (modern combat, interface mode and skin).
- At the end of the hide burst, hide the previously omitted inert root slots
  45, 46, 1049, 1050, 1051, 1052 and 1053. Their wrappers are 1477 components
  532, 489, 574, 650, 570, 682 and 566 respectively.
- After the backpack event settings, enable only operation 1 (Toggle Run) on
  static component 1465:14, using slot range -1..-1 and mask 2. Operation 2
  (Rest) remains deferred. The two preceding changes follow `UI-DECISIONS.md`.

The test verifies the inventory SHA-256, checks every opcode and payload, and
does not load a game cache or start either server. Adding a slot to the resource
inventory cannot modify the explicit hide list. Missing or off-root selected
addresses fail before the packet list can be returned to the login handler.

Ribbon attachment is a separate opt-in argument, false by default. Its test
checks the exact visibility/attachment delta, without claiming an observed
native click or supplying an inherited 910 event mask.
