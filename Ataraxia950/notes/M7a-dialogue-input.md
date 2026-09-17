# M7a: shared dialogue and Withdraw-X input

This milestone adds native 947 presentation and response handling to the existing
910 dialogue and bank implementations. The final build, installation and native
Banker, quantity-input and player-portrait checks have passed.
The later map-close cleanup regression and its replacement build are recorded
in the follow-up below; the original installation evidence is retained.

## Delivered features

- `Native947Dialogues` renders NPC and player speech with portraits, requested
  animations, Continue, plain messages, and one-to-five option lists. It uses the
  existing `DialogueManager` and dialogue subclasses for stage transitions.
- Banker Talk opens the existing greeting, then offers bank access or Never
  mind. The bank choice reaches the existing `Bank.openBank` path. The native
  branch exposes these two implemented services; the legacy branch retains its
  original menu.
- Response validation accepts only the current page's enabled Continue or
  visible option row, with a static slot of -1. It consumes the page response
  before entering dialogue code. Hidden rows, wrong components, stale closed
  pages and duplicate responses without a newly rendered page are rejected.
- Dialogue close removes its attachment and response state even when the legacy
  close-interface flag is set. Native interface-manager presence checks follow
  the actual rendered page. Movement, changed context and lost NPC reach retire
  the active interaction through the native coordinator.
- Bank Withdraw-X opens an owned quantity prompt and accepts the native signed
  64-bit count reply. A validated amount is capped against current stock and
  inventory capacity, then handed to the existing `Bank.withdrawItem` path.
- Quantity ownership captures the bank session, bank identity, target slot/item
  and a copied snapshot of every bank slot and amount. Replies are consumed
  before validation; changed bank state, lost access, invalid counts and
  unsolicited replies cannot withdraw items.
- Quantity cancellation removes only the owned prompt and preserves the bank.
  Dialogue, modal, movement, bank-action and disconnect lifecycle changes
  retire requests. Generic legacy input callbacks are not enabled by this work.
- Dialogue opening explicitly restores the shared wrapper after quantity
  cancellation. This fixes the native case where a later Banker greeting was
  open on the server but remained hidden after Escape from Withdraw-X.

Deposit-X, shops, collection-box dialogue and the rest of the broader interface
migration remain outside M7a.

### Generic NPC dialogue follow-up — validated

The Cook check exposed a shared NPC metadata lookup that still tried to read
legacy definitions for a native NPC. A bounded follow-up makes `NPC.getName`
and the new `hasMenuOption` read strict metadata from the paired 947 cache.
Five shared option-1 name/menu reads and the SlidingTilesRoom classification
use those accessors. General `getDefinitions` and combat remain gated; this is
not a general NPC-definition or quest-system port.

Six passing `Native947NpcMenuTest` checks cover the metadata accessors. The new
`Native947CookDialogueSmoke` follows actual data-spawned NPC 278 through
encrypted option-1 opcode 88, Continue and Yes responses on opcode 15, then
player speech interface 1191 and player-head packet 81. It cancels before quest
changes. The compiled and installed smoke, final build and native Cook/player
dialogue checks have passed. The scope remains the shared dialogue path, not
acceptance of the Cook's full quest.

## Verified bindings and evidence

Ordinary dialogue attaches at **1477:750**, the 3505 attachment from Dialogue Box
struct 21303. Its wrapper is 1477:747. NPC speech uses 1184, player speech 1191,
options 1188 and plain messages 1186. Continue targets are 15 on speech and 8 on
messages; option rows are 8/13/18/23/28. Script 5589 lays out visible choices and
script 1364 refreshes the dialogue wrapper and native modal hooks.

NPC portraits use the bounded cache script 2374, with 16429 applying the requested
animation. The newly confirmed native **server 81, IF_SETPLAYERHEAD** supplies the
local player's portrait. Its four-byte hash body has byte order B, A, D, C for a
logical hash A, B, C, D. This binding was traced through the original 947-3
descriptor, vtable and parser to the model kind 3 and local appearance values.

Quantity input uses **1418 at 1477:749, then 1469 at 1418:2**. These are sibling
attachments of the ordinary dialogue host. Script 17396 selects input mode 17;
opcode 16 carries its signed 64-bit result. Script 1548 and explicit subinterface
closes tear down the prompt. Input Escape emits 41; global CLOSE_MODAL 55 remains
a separate signal. Withdraw-X is option 6, whose client script predicts amount
zero, so its opening click must claim the exact current item rather than using
the immediate-withdrawal prediction exceptions.

The wire has no quantity request identifier. A reply with no active request is
rejected, but it cannot identify an old prompt after a later prompt has opened.
Only one request may be active, and context changes cancel it.

Detailed proof, pins, reproduction tools and acceptance status:

- [Dialogue evidence](../../OpenNXT/data/prot/947/generated/native947-3/verified/ui/DIALOGUES_M7A.md)
- [Quantity-input evidence](../../OpenNXT/data/prot/947/generated/native947-3/verified/ui/QUANTITY_INPUT.md)
- [Dialogue derivation output](../../OpenNXT/data/prot/947/generated/native947-3/verified/ui/dialogues-m7a-evidence.txt)
- [Quantity logical-file fixtures](../../OpenNXT/data/prot/947/generated/native947-3/verified/ui/quantity-input-files.json)

Both helpers verify their paired-cache pins before acquiring presentation or
request ownership. The dialogue verifier covers 82 fully decoded component files
and its scripts; quantity tests independently change each pinned file and
require rejection. Existing unsupported packet bindings are not promoted by
analogy to an earlier revision.

## Tests — passed

The first targeted run passed **47 tests**, including dialogue presentation,
quantity ownership, response routing and Banker integration. The wrapper-fix
build then passed 590 tests. After the six NPC metadata checks were added, final
full Ship passed **528 engine tests plus 68 frontend tests, 596 total**, with no
failures, errors or skips.

The expanded real-cache smoke passed against both compiled classes and the
installed distribution. It exercised encrypted inbound/outbound framing, the
shared gameplay path, a real checkpoint and reconnect. Both runs restored the
exact resulting quantities of **7 in inventory and 993 in the bank**, with no
stale quantity prompt.

- Targeted build/test log: `logs/m7a-targeted-tests.log` at the project root.
- Compiled encrypted checkpoint/reconnect smoke: `logs/m7a-engine-smoke.log`.
- Initial installed-distribution smoke: `logs/m7a-installed-smoke.log`.
- Initial full build: `logs/m7a-ship.log`.
- Transition-fix build and tests: `logs/m7a-transition-ship.log`.
- Transition-fix installed smoke: `logs/m7a-transition-installed-smoke.log`.
- NPC metadata engine tests: `logs/m7a-npc-menu-tests.log`.
- Compiled actual-Cook dialogue smoke: `logs/m7a-cook-engine-smoke.log`.
- Final full build: `logs/m7a-final-ship.log`.
- Installed actual-Cook dialogue smoke: `logs/m7a-cook-installed-smoke.log`.
- Final installed bank/checkpoint smoke: `logs/m7a-final-installed-smoke.log`.

All log paths are relative to the project root.

Automated packet and engine checks do not establish native rendering, keyboard
focus or successful in-game interaction.

## Ship — installed

Final full Ship completed successfully and installed both the wrapper fix and
the bounded shared NPC metadata fix. Its rollback snapshot is
`backups/install-2026-09-08-142431` at the project root and contains **112 JARs**.
The earlier transition-build snapshot is `backups/install-2026-09-08-135748`;
the pre-M7a rollback remains `backups/install-2026-09-08-131917`. The final server
is **PID 24808**, created at **2026-09-08T19:25:49.4063840Z**. Both the installed
Cook dialogue smoke and final bank/checkpoint smoke passed; the latter restored
the exact smoke-test totals of **7 in inventory and 993 in the bank**.

## Native UI acceptance — passed

Observed in the native client:

- Banker Talk displayed the greeting and NPC portrait. Continue and the option
  menu worked; selecting Bank opened the bank.
- Entering 7 into Withdraw-X completed the transfer. Escape on a second prompt
  closed the input and preserved the bank.
- Initial testing found that a later Talk could leave its greeting hidden after
  quantity cancellation. The final opener now shows wrapper 1477:747 after the
  cancellation callback; two automated regressions cover both cancellation
  orders.
- After a fresh login to the wrapper-fix installation, **9 in inventory and 991 in
  the bank** persisted from the earlier transactions and restart. Withdraw-X →
  Escape → bank X → Talk displayed the complete greeting and NPC portrait,
  confirming the wrapper fix in the native client.
- Maximizing the open greeting to **1707×1067** and restoring to **856×511** kept
  it complete. Space advanced to the two options; number 2 selected Never mind
  and closed the conversation.

- After manual sign-in to the final installation, the actual Cook's Talk menu
  routed through path walking and opened his greeting and visible portrait.
  Space advanced to Yes/No; keyboard 1 selected Yes and opened player speech
  interface 1191 with the name X, the visible helmet portrait and the text
  “Sure, What do you need help with?” This passed at **1707×1067**.
- Testing stopped at that first player reply, before continuing the quest.
  Opening the world map cancelled the conversation, and Escape restored the
  game world with no remaining dialogue.

## Final runtime health

The read-only snapshot in
`logs/ui-debug/result-43cd4e83-9aa2-4c9f-a38f-10615fc187e4.txt` recorded an active
session, a closed map, **0 world tick failures**, **0 strict facade hits** and
**0 scheduler failures**. It also recorded **60 unhandled frames** (last opcode
2) and **41 dropped facade calls**. Retired legacy bonus varps, varcs, music and
other deferred traffic remain unsupported; the clean failure counters do not
mean that every client operation or legacy feature is implemented.

The native client was left beside the Cook at **3208,3215**, at **1707×1067**,
with no dialogue open.

## Follow-up: black world after closing the map

The user subsequently reported that clicking the Cook left the HUD visible over
a black world. The earlier live check had ended after map close and missed the
next NPC interaction. The old `containsWorldMapInterface()` treated any
attachment at 1477:30 as the 910 map. Modern map close restores scene 1482 at
that same slot, so the next `NPCHandler` call to `Player.stopAll(false)` removed
the scene and attempted a rejected restore at legacy slot 1477:23.

Native map presence and removal now use the session's `Native947WorldMap`
owner. Its existing idempotent close restores the scene at 1477:30; shared
cleanup leaves that scene alone. Three additional state regressions cover
initial and restored scenes, repeated player cleanup and delegated map removal.
The real-cache Cook smoke now opens/closes the map, talks to the Cook again,
and checks both scene bookkeeping and the absence of another scene-close packet.

The expanded probe failed against the previous installation in
`logs/cook-black-before-fix.log`. It passed against the replacement installation
in `logs/cook-black-fixed-installed-smoke.log`. The replacement full build,
`logs/cook-black-fix-ship.log`, passed **531 engine + 68 frontend = 599 tests**
with zero failures, errors or skips. Server **PID 10044** started at
**2026-09-08T19:40:56.8702390Z**; rollback snapshot
`backups/install-2026-09-08-143918` contains **112 JARs**.

Native verification passed after manual login to this installation at
**1707×1067**: open the world map, wait for it to render, close it with Escape,
then directly left-click the Cook. Path walking opened his greeting and portrait
while the world remained visible. Space opened Yes/No, and keyboard 2 selected
No and closed the dialogue with the world still visible. The client was left
logged in with the existing panel positions unchanged.

The final read-only snapshot,
`logs/ui-debug/result-5704e06a-1c4e-4458-8018-7f65f9e42035.txt`, recorded an active
session, a closed map, **0 world tick failures**, **0 strict facade hits** and
**0 scheduler failures**. It also recorded **71 unhandled frames** (last opcode
56) and **44 dropped facade calls**; deferred legacy traffic remains unsupported.
