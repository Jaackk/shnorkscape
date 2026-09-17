# Native 950 ground items

Derived against the unmodified local WIN64 client `OpenNXT/data/clients/950/win64/original/rs2client.exe`, SHA256 `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`.

`ground-items-950-evidence.json` records the original parser bytes, instruction addresses, SHA256 hashes, descriptor registration instructions and concrete ground-pile mutation sinks. This verifies meaning through the read fields and mutated structure, not registration order. Live rendering is separate acceptance.

| Packet | Opcode | Size | Native parser | Wire order |
|---|---:|---:|---|---|
| UPDATE_ZONE_PARTIAL_FOLLOWS | 96 | 3 | `0x140141040` | signed zone delta Y; negated signed zone delta X; plane+128 |
| OBJ_ADD | 51 | 6 | `0x140140b80` | item ID LE u24; 128-packed offset; amount BE u16 |
| OBJ_DEL | 109 | 4 | `0x140140ab0` | 128-packed offset; item ID LE u24 |
| OBJ_COUNT | 70 | 8 | `0x140115020` | packed offset; item ID BE u24; old amount BE u16; new amount BE u16 |

The packed offset is `(offsetX << 4) | offsetY`, where both offsets are 0..7. Zone deltas are in 8-tile units relative to the current rebuilt scene origin. The prefix writes plane at `0x140c6e900`, X at `0x140c6e904`, and Y at `0x140c6e908`. It reads the client origin from scene fields +0x698 (X) and +0x69c (Y), and adds the signed deltas multiplied by eight. OBJ parsers add the local offsets to these stored coordinates. A prefix selects a zone; it does not clear its contents.

The descriptor registrations independently give opcodes and sizes: 96/3 at `0x14000d550`, 51/6 at `0x14000cce0`, 109/4 at `0x14000d7c0`, and 70/8 at `0x14000d070`. OBJ_COUNT's bound dispatcher at `0x1401408f0` forwards to the parser at `0x140115020`.

OBJ_ADD calls `0x1403aaa30`, which resolves a tile-keyed pile, then appends a 0x90-byte entry whose first fields are item ID and quantity. It does not combine existing equal IDs. OBJ_DEL calls `0x1403aacf0`, which removes the first matching ID from the pile and destroys the tile entry when empty. OBJ_COUNT searches the same vector and changes every entry whose ID and old quantity match. This last detail means count updates are ambiguous if the server sends duplicate equal-count entries of the same ID on one tile.

The add/count quantities are genuinely unsigned 16-bit values. Do not truncate a larger server item amount into these writers. Stack splitting, aggregation and reliable resynchronization belong to the ground-item service. These packet helpers refuse quantities outside 1..65535; removal is explicit. IDs use all three bytes and reject negative values and the 0xFFFFFF absent sentinel. Ground options already decode their three-byte IDs in `Native950Actions`.

Ground options 1..6 use inbound opcodes 127,103,22,56,52,113, each 8 bytes. Native senders `0x1400e60ee` and `0x1400e5ad9` send 128-minus flags, absolute X BE u16, absolute Y BE u16 and item ID BE u24. Both the modifier and second flag are supported. Standard Take is option 3 (opcode22), corresponding to `groundOptions[2]`. World validation, ownership, path reachability, pickup capacity and state mutation remain server responsibilities.

Implementation is in the four `Native950Packets` writers and `Native950Protocol.ServerPacket` constants. Seven `Native950GroundPacketsTest` tests pin literal wire fixtures, opcode/size framing, distinct endian orders, signed zone boundaries, wide IDs, quantities and invalid coordinates. The legacy raw `createWorldTileStream` remains sealed: promoting these packets does not authorize any old serializer bytes.

To reproduce disassembly, run `tools/dis950.py range <parser VA> <byte count>` with the existing AstraNXT virtual-environment Python. The evidence JSON includes exact byte counts as half the bodyHex length.

## Scene origin and publication

The scene-origin calculation is verified through the native rebuild path. At `0x1400f6fcb` the client reads width from scene field +0x6a0, shifts right four, subtracts that value from decoded chunkX/chunkY, and shifts each result left three. At `0x1400f7160` it passes the resulting pair to `0x14011b1d0`; that function stores both directly at +0x698/+0x69c. The constructor at `0x140117640` initializes width to256 at `0x1401177c9`; its caller at `0x1400221d2` stores that exact instance into `[client+0x19898]` at `0x1400221f2`. Native950Packets therefore declares `SCENE_SIZE=256`, yielding `(loadedChunk-16)*8`. The retained Player.getMapSize()==0 override describes the server's104-tile interest/collision window, not this wire scene. Using that legacy radius for zone packets would shift ground items by80tiles. GroundItemsView and its published-target gate now use the verified native constant while preserving the existing server loading radius. A unit regression explicitly uses a native Player with server mapSize0 and accepts the correct256-tile published origin; the real-cache probe pins prefix `10 f0 80` for the banker tile3217,3258.

`Native950GroundItemsView` polls only loaded Region storage once per world output phase. It admits marked native FloorItems, applies private-owner and current-scene/plane visibility, and sends one aggregated entry per tile/ID. Authoritative stack quantities, item attributes, timers and ownership remain in the original FloorItems. Rendering saturates at65535; taking or expiring part of a larger real stack cannot wrap the display or discard items. Exact count changes use OBJ_COUNT because this projection guarantees one matching entry per tile/ID.

The client updates ground-pile bounds during REBUILD_NORMAL and may retain overlapping piles. Therefore `Native950Session` first calls `beforeRebuild()` to remove every previously published entry using the previous scene origin, then queues the rebuild, then republishes desired entries using the new scene origin. This also avoids aliasing during distant teleports. Plane changes and late login are handled by the same projection. Old `PacketDispatcher.sendGroundItem/sendRemoveGroundItem` notifications defer to this phase instead of emitting immediate duplicate adds/removes.

The view exposes `canTake(viewer,itemId,x,y)` for the interaction router. It requires that this viewer was sent that exact pile and that the loaded scene origin and plane still match. The gameplay pickup transaction must separately re-read the real Region/FloorItem, enforce account/controller/owner rules, path reachability and capacity, then commit the item transfer. Twelve view tests cover private/public transitions, duplicate-stack aggregation and overflow, pickup/expiry, clipping, rebuild/teleport, invalid legacy items, published-target admission and the owner-thread guard. These are server and byte-level proofs; client rendering is verified separately in live acceptance.
