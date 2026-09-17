# Native950 world-object mutations

Derived from the unmodified paired950 WIN64 client, SHA256 `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`. Raw parser, registration and mutation-sink instructions are pinned in `world-objects-950-evidence.json`.

| Meaning | Opcode | Frame/body | Native parser | Ordinary wire fields |
|---|---:|---|---|---|
| LOC_ADD_CHANGE | 11 | variable-byte,6-byte ordinary body | `0x1401130b0` | plain packed offset; object ID bytes16,24,0,8; shape/rotation+128 |
| LOC_DEL | 26 | fixed2 | `0x140113f30` |128-minus packed offset; shape/rotation+128 |

Packed offset is `(xWithinZone << 4) | yWithinZone`. Shape/rotation is `(shape << 2) | rotation`. Both packets use the already verified zone prefix96 and current native256-tile scene origin. The ordinary writer accepts Region shapes0..22 and rotations0..3. Native parser-generated secondary wall shapes23/24 are not emitted independently.

The add parser at `0x1401132ad` stores the decoded object ID in mutation field+0x4c, writes animation=-1 at+0x60, and sets operation2 at+0x68. Delete writes object ID=-1 and operation3. The shared native sink `0x14035777a` checks operations2..4: a nonnegative object ID calls scene creation/replacement `0x1403863b0`; ID=-1 calls scene removal `0x1403871c0`. This verifies meaning independently of registration order or an older packet label.

The old draft label for947 packet51 pointed at950 opcode75 as a possible object-add packet. That candidate was rejected: it actually writes the decoded ID in animation field+0x60, sets operation1, and invokes an existing object's animation method at vtable+0x130. Sending it for a tree stump would animate an object instead of replacing it. Opcode75 remains unexposed.

The add frame is variable because bit7 of decoded shape appends an optional map-placement transform. `0x14010d000` reads a plain flags byte followed by signed BE shorts: flag1 has four quaternion components divided by32768; flags2/4/8 have X/Y/Z translations; flag16 has one uniform scale divided by128, otherwise flags32/64/128 have individual scales. The existing strict Region cache decoder already reads this exact structure through ObjectData, but used to discard it. Native Region loading now retains the original raw transform on WorldObject, its copies and tile-based replacements. The writer checks the exact flag-dependent byte count and republishes it on stump changes and original-tree restoration. No transforms are guessed from an animation or definition ID.

`Native950ObjectsView` reads existing Region spawned/removed-original lists and resolves each effective slot with `getObjectWithSlot`. Region continues owning objects, clipping and collision; WorldTasksManager continues owning fire expiration and tree restoration. The view stores only immutable per-connection publication snapshots. It sends no updates for unchanged state, restores a cached tree when its replacement disappears, deletes a fire when an originally empty slot expires, and publishes existing mutations to a newly arriving viewer.

Before a scene rebuild it restores the previous scene baseline using the previous origin, then republishes current mutations after the rebuild. This also covers overlapping rebuilds and distant teleports. Native facade Region broadcasts now defer to this projection; raw legacy zone streams and player-private object mutation helpers remain sealed. Player-only object overlays and dynamic/rotated regions are outside this milestone.

The original `World.spawnTempGroundObject` timer is retained for firemaking. In native mode it checks exact object identity before removing a fire, so an old timer cannot delete its replacement or create duplicate ashes. The returned ownerless ashes FloorItem is explicitly marked for native projection and pickup; its original public ground-item expiration remains60seconds (the old5-argument call's hiddenTime180 did not affect already-public items).

Validation added: literal independent11/26 wire fixtures, variable-frame width, mixed32bit ID order, shape/rotation boundaries, offsets, transform length/flag checks; tree/stump/restoration, fire/delete, removed baseline, late viewers, same-slot replacements, separate object slots, scene/plane clipping, immutable snapshots, rebuild origins and owner-thread checks. Compilation/runtime and live visual results are recorded by the milestone acceptance, not inferred from these source tests.
