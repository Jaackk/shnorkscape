# Combat QA review: a3b1935

## Tested build

- Session: `session-20260919-013423-360-jaxa`
- Commit: `a3b19355355e7e3f135e6c625c57414f3800939d`
- JAR SHA-256: `447fbada0f3d9b9452f86955f294cb636c61c85ae83be88844c727ecedca4922`
- Duration: 357,478 ms (ticks 776-1374)
- Persistent gameplay state changed by recorder: no

## Coverage and observations

- 64 ability executions covered 20 named melee, magic, and ranged abilities; nine were manual and 55 Revolution-selected.
- Queueing, replacement, cooldown rejection, scheduled multi-hits, channels, and all three saved action bars produced structured events.
- No exception, disconnect, contradictory GCD, or collapsed follow-up-hit cadence occurred.
- Action bars were visibly populated and switched. Several melee/ranged sequences visibly animated, so presentation is not globally broken.
- Automatic markers found Berserk and Death's Swiftness with `animation=none` and no presentation attempt. Their server effects started and expired, but presentation remains incomplete.
- Dragon Breath, Wild Magic, and Asphyxiate had animations but no resolved graphic/target graphic. Mechanics executed; magic presentation remains incomplete.
- There were no manual `;;bug` markers. Prayer and potion paths were not exercised. Magic selection/autocast state was recorded, but the screenshot backlog prevents reliable visual correlation.

## Recorder findings

- 368 PNGs (2.20 GiB) were saved, 343 captures were dropped, and 64 storyboard groups were requested.
- Every JSONL line parsed; no telemetry event was dropped.
- Capture was asynchronous, so gameplay remained stable, but one PowerShell process per frame could not keep up.
- Queue lag: median 46,674 ms, p90 75,324 ms, maximum 79,493 ms. Only five frames began within 1.5 seconds of their event.
- Most later filenames therefore identify the request, not the rendered moment. Item Browser and settings frames appearing under combat filenames prove the miscorrelation.
- Raw unknown client opcodes were incorrectly promoted to `unhandled-combat-context-input` solely because they occurred near combat. They are not evidence of a combat failure.

## Changes from this review

- Capture queue reduced from 96 to 3 and delayed jobs from 160 to 32.
- Normal captures older than 2.5 seconds and high-priority captures older than 7 seconds are dropped as stale before starting.
- Each ordinary ability now requests one frame at 150 ms; channels retain one end frame. Per-write action-bar screenshots and raw unknown-frame anomaly screenshots were removed.
- Capture request/start/completion timestamps and queue lag are now recorded.
- Non-world-thread state sampling falls back to the last safe snapshot instead of emitting an unusable exception-only state.
- Unknown raw frames remain in JSONL but are no longer labelled combat anomalies without a decoded combat action.

## Unresolved presentation

- Berserk and Death's Swiftness: confirmed server effects, missing verified revision-950 animation/status presentation.
- Dragon Breath, Wild Magic, and Asphyxiate: confirmed execution with missing verified graphic/projectile presentation.
- Native buff/status icons and combat-ready stance remain unresolved. No 876 ID or speculative 950 client script was copied.
- Future screenshots must be judged by recorded `queueLagMs`; stale drops are preferable to false visual evidence.

## Representative evidence

- `000001-qa-000000-session-start-0ms.png`: baseline game/UI state.
- `000006-qa-000004-revolution-anomaly-supported-ability-no-animation-0ms.png`: retained with the explicit warning that its event timing is stale.
- `000181-qa-000044-attack-request-action-bar-visual-sync-120ms.png`: representative populated action bars.
- `000253-qa-000047-revolution-anomaly-magic-ability-no-visual-effect-0ms.png`: representative magic-presentation investigation, timing stale.
- `000485-qa-000091-revolution-ability-executed-250ms.png`: proves screenshot/event miscorrelation and preserves Item Browser V1 baseline.
- `000513-qa-000097-revolution-ability-executed-0ms.png`: preserves the V1 custom-quantity baseline and screenshot backlog evidence.
- `000711-qa-000129-drag-drag-100ms.png`: end-of-session UI baseline.

All other session PNGs were visually reviewed through 23 chronological contact sheets and classified redundant or stale. The JSONL timeline and index remain the authoritative technical record.
