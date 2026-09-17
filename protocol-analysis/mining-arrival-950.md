# Mining arrival / animation audit, 2026-09-12

Read-only audit of the isolated 950 runtime, paired cache and original 950 WIN64 client. No client or cache changes.

## Footprint and reach

The user's live clicks were copper rocks 113146 at (3229,3148) and 113148 at (3230,3147). Actual index16 definitions:

- 113146: 1x1, models73989/74043/73987.
- 113148: 2x2, models74154/74182/73986.
- Both: name Copper rock, options Mine/null/null/Prospect/null; shape10; clip2; access0; XYZ offsets0; XYZ scale128; no transforms.

Live log movement reaches (3229,3149) and (3230,3149), respectively: the correct northern edge of each footprint. Shrinking footprints or requiring distance1 from the southwest origin would be incorrect for the second object. Native950Mining.inReach already checks rotation-aware footprint adjacency, empty server walking queue and zero-length original ObjectStrategy route.

## Sequence / client evidence

The isolated temp/RockDistanceProbe.java uses the deployed engine's strict decoder against actual index20 data. Bronze32540, iron32548 and steel32552 all have:

- opcode9 absent; opcode24 absent; therefore opcode9 resolves0 in both server and client.
- opcode10=1; opcode11=0; duration1200ms.
- trace: 15@0 10@1 11@3 6@5 7@8 1@11 2@200 13@203 18@288 119@289 119@293 119@297 120@301 120@308 120@315 120@322.

The original950 client proves the movement/animation interaction:

1. SeqType decoder0x14035a725 stores opcode9 at +0xa60;0x14035a74a stores opcode10 at +0xa64.
2. Constructor uses sentinels4/3; postdecode0x14035aef0 resolves missing opcode9 to0 when opcode24 resource is absent. Thus the mining sequences' opcode9=0 is not a guess based on obsolete decoder defaults.
3. 0x140320d20 is an action-animation movement blocker. It reads actor+0x2c0. Positive selects active sequence+0xa60 at0x140320dbf; otherwise +0xa64 at0x140320dcb. Equality to0 returns true. The alternate active-animation representation calls the same test at0x1402f5050.
4. Its sole direct caller0x14031fa76 branches on true at0x14031fa7d, increments movement state+0x3c, and skips the path/position update calls at0x14031faeb and0x14031fb5a.
5. Separately, 0x140323f40 cancels movement-interruptible sequences whose opcode10=1. This is consistent with mining being interrupted by walking, and does not make opcode9 safe while preexisting movement remains queued.

Inference: sending the first mining animation alongside the final server movement update can start a sequence before the client has interpolated the final queued step(s), holding the visible character short of its authoritative position. Server distance assertions alone cannot catch this. A stationary update boundary before starting the original Mining action lets the final movement arrive before the blocking sequence. Do not alter paired cache flags or blindly reduce object footprints.

Required regression: walk/run from several tiles away, assert final movement update contains no mining sequence or bars, and then a later stationary update starts the original action with normal bars. Repeat clicking a different rock during mining, cancellation during approach, and already adjacent clicks. The real client's interpolation still requires manual confirmation.
