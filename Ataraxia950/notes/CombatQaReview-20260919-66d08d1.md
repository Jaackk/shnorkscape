# Combat QA review: 66d08d1

## Tested build

- Session: `session-20260919-021657-031-jaxa`
- Commit: `66d08d17561b43e9daf0589f02d574962c8ff005`
- JAR SHA-256: `ef1adbf528e08abc0266e423026d68962265502ebb67571be66fd40176a8c622`
- Duration: 258,062 ms (ticks 157-587)
- Persistent gameplay state changed by recorder: no

## Coverage

- 54 executions covered 20 named melee, magic and ranged abilities: 13 manual and 41 Revolution-selected.
- Bars 1, 2 and 3 were visibly populated and switched during the session.
- The timeline covered manual queueing/replacement, GCD rejection, multi-hit scheduling and channel endings.
- No server exception, session disconnect, reconnect, collapsed channel cadence or contradictory GCD was recorded.
- Prayer, native spell selection/autocast and potion paths were not exercised. There were no manual `;;bug` markers.

## Visual findings

- Melee and ranged animations were visible across the screenshot sequence. Combat and action-bar presentation were not globally broken.
- The nine `magic-ability-no-visual-effect` markers are mostly false positives. Dragon Breath, Wild Magic and other magic executions visibly produced purple/gold animation-integrated effects even though their execution records had no separate graphic or target-graphic ID. The anomaly rule only understands explicit graphic fields.
- `000107-qa-000081-revolution-anomaly-magic-ability-no-visual-effect-0ms.png` is a clear example: Dragon Breath has an obvious purple effect despite the automatic marker.
- `000138-qa-000093-revolution-anomaly-magic-ability-no-visual-effect-0ms.png` shows an abnormally large black/gold model or effect obscuring the scene during Dragon Breath at tick 479 (animation 36639). One frame is insufficient to decide whether this is a sequence asset, equipment interaction or camera intersection, so no mapping change is justified from this session alone.
- Death's Swiftness is the one confirmed presentation defect. At tick 550 the server applied the self effect for 50 ticks and a 100-tick cooldown, but resolved `animation=none` and sent no graphic/target graphic. `000222-qa-000135-ability-request-anomaly-supported-ability-no-animation-0ms.png` shows no distinct cast or status presentation.
- The character still returns to idle between attacks. This session adds no safe revision-950 evidence for changing stance ownership.

## Recorder quality

- All 2,566 JSONL records parsed. The finalized index reports 2,021 indexed events, 133 PNGs saved, 134 capture drops, zero screenshot failures and zero dropped telemetry events.
- Capture queue lag improved substantially: median 835 ms, p90 1,822 ms, p95 2,102 ms and maximum 2,430 ms; 102 of 133 completed captures began within 1.5 seconds.
- Drops were fail-open: 117 queue-full, four evicted for higher-priority evidence and 13 stale-before-capture. Gameplay remained smooth.
- The remaining high capture/drop ratio is acceptable for diagnosis but should be considered when requesting broad automatic storyboards.

## Conclusions

- Server combat remained stable throughout this session.
- Queue, GCD, channel timing and all three action bars have strong evidence of normal operation in the covered scenarios.
- Death's Swiftness presentation remains confirmed missing.
- The giant Dragon Breath visual needs one focused marked live reproduction before changing cache-backed presentation data.
- Automatic magic-visual anomalies must not be treated as proof of an invisible ability when a cache-backed animation can contain its own effect.
