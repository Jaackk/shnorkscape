# P0 ability input transition gate (2026-09-22)

The live problem remains unresolved: a keyboard-bound ability can work initially and stop later, sometimes with missing native yellow feedback. This candidate adds no action-bar reset, remount, gameplay change, or replacement visual. It only adds opt-in `;;bugtest` boundary events and prolonged offline regressions.

## Candidate boundary

- Source checkpoint: commit this document and code together.
- Staged JAR: `dist/staged-update/ataraxia-950-1.0-UNTRACKED.jar`, SHA-256 `631FFD999D0F5D44C65E88214E22769A4EEA2DC1662FA6B3AA3B639E517EBFFB`.
- Installer: `Apply Staged Update.cmd --check-only` validates it without installing. The normal installer refuses running Java/client processes and snapshots players/workspace before replacement.
- The running/deployed JAR remains `92E32A56D3C2BC05190CF3EE9E97FAA3CB0462A02C673753643B7F6F0CD6FD7F` until Jack chooses a restart. Do not call this diagnostic live until then.
- Pre-edit backup: `backups/pre-edit-20260922-212004-465`; its runtime JAR is the rollback candidate. Previous staged JAR copied to `previous-staged-ataraxia-950-1.0-UNTRACKED.jar` in that directory.

## What the diagnostic distinguishes

While Bug Test is enabled, `Native950BugTest.inboundFrame` records only valid 950 IF_BUTTON opcode/option/length, never payload. This is after framing on Netty's event loop and before transport decode/queue. Existing `input/interface` and `interface/button-dispatch` events are after world-thread drain. New `action-bar/pre-dispatch-rejected` identifies the early player gate. New `action-bar/activation-rejected` records mount, option, locked and dead gates; `activation-empty` records a resolved empty slot. Existing `combat/ability-click`, `ability-request`, `ability-rejected`, `ability-queued`, `visual-sync`, and `visual-write-*` provide the later boundary. No chat text, keycode, credential, or arbitrary packet payload is added.

The source change uses the already-existing opt-in inbound observer, so it does not replace the workspace frame observer or record all packets. Exact 950 IF_BUTTON options are 18/122/89/100/81/126/49/66/31/59. The regression checks option classification and 160 decoded keypress/slot-to-config resolutions over four saved-bar transitions; a separate 220-tick combat fixture tests repeated ready manual ability admission through bar, Revolution and target transitions. These tests cannot simulate the Vulkan keybinding or prove yellow feedback.

## One short live gate after operator-controlled install/restart

1. Jack and Nooby close both clients. Use `Stop.cmd`, run `Apply Staged Update.cmd`, then `Play.cmd` only when they are ready. Confirm staged hash is now the deployed runtime hash and the server process began afterward. Do not edit Jaxa's save.
2. On Jaxa, equip a suitable known-good ability and target. Enter `;;bugtest`; mark `;;bug p0-baseline`.
3. Press one bound ability key 2-3 times while it is legal/ready and click the same action-bar slot once. Mark `;;bug p0-working`. Note whether yellow press feedback appears. Do not infer an ability failure solely from GCD/cooldown refusal.
4. Perform one transition from the handoff matrix, initially `;;bar 2` then `;;bar 1`. Repeat the same key and click. If still good, try native bar selector, `;;copy nooby`, one drag/rearrangement, Powers open/close, target death/change, combat exit/re-entry, equipment switch, Revolution toggle, channel/queue completion and two-player same-NPC combat, marking each transition separately. Stop at the FIRST good-to-bad transition; no need to exhaust the matrix once it fails.
5. Immediately mark `;;bug p0-dead-after-<transition>` while the failure is still present. Press the same key once, click the same slot once, then disable `;;bugtest`. Record whether chat/world movement still accepts input and whether either press shows the yellow square. Avoid sharing passwords or raw private logs.

Inspect the resulting local `logs/bugtest/session-.../timeline.jsonl` interval, not general unknown opcode traffic:

| First missing event | What that means | Next targeted check |
| --- | --- | --- |
| No `if-button-ingress` for key, but click has one | The client did not emit a valid IF_BUTTON for the key | Native focus/keybind/slot identity after that exact transition; no server scheduler change yet |
| Neither key nor click has ingress while other actions work | Client component/event-mask/input state likely changed | Compare native mount/mask/visible slot at transition; do not blindly remount |
| Ingress exists, no `input/interface` | Decode/transport queue/drop boundary | Check frame length, terminal failure, queue capacity and session draining |
| `input/interface` exists but no `button-dispatch` | Pre-dispatch/closed/inactive gate | Inspect `pre-dispatch-rejected`, session readiness, player state |
| `button-dispatch` exists, `activation-rejected` or `activation-empty` | Server mount/option/slot/binding state | Compare active bar, child vs wrapper ownership, packed slot and sync event |
| `ability-click` and `ability-request` exist with refusal | Scheduler/equipment/target/GCD/channel/adrenaline gate | Use precise refusal and tick state; fix only if incorrect |
| Ability executes but yellow/cooldown absent | Native visual acknowledgement | Correlate 6570 and client script/UI ownership; sent packets alone are insufficient |

The server cannot observe a physical keypress that the client never sends. If ingress is absent, the visually observed good-to-bad transition is the required evidence; do not call the scheduler the cause. This is a measurement checkpoint, NOT a P0 fix or authorization to proceed to P1-P6.
