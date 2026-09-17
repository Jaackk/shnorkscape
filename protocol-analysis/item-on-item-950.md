# Native950 selected item on item

Client opcode69 has fixed18-byte body, registered at `0x14000af00` (`edx=0x45`, `r8d=edx-0x33`) to descriptor `0x140e94730`. Sender `0x1400e2c8d` uses that exact descriptor. Evidence records the paired unmodified950 WIN64 client SHA256, raw bytes and instruction lists in `item-on-item-950-evidence.json`.

| Bytes | Meaning | Encoding |
|---|---|---|
|0..1|selected source slot|BEu16, low byte+128|
|2..4|selected source item ID|u24 bytes16,0,8|
|5..8|target component hash|i32 bytes8,0,24,16|
|9..10|target slot|plain BEu16|
|11..14|source component hash|i32 bytes8,0,24,16|
|15..17|target item ID|plain LEu24|

The source triple is independently established by selection setter `0x14019f491..0x14019f50b`: source component fields+0x18/+0x1a combine into its interface/component hash and are stored at selection state+0x25c; component+0x1c becomes selection slot+0x260; the virtual item getter at+0xb0 becomes selected item+0x264. The same item getter supplies item IDs to the already verified and live-used drag sender at `0x1401ac02f`.

The target path at `0x1400e2950` first checks selection-active state+0x218. It looks up the target component using menu hash+0x50 and slot+0x4c through `0x140390f60`, saving those exact hash and slot values for the body. Target item ID is read from that resolved component with the same virtual+0xb0 getter. The source component is looked up from the stored selected hash/slot before dispatch. Therefore this packet is selected item/UI use on another item, independently of the other target variants or drag/swap.

`Native950Actions.ItemOnItemAction` preserves the endpoints, mapping only0xFFFF slots and0xFFFFFF item IDs to-1. The world must still verify both interface components are its inventory grid, both slots currently contain the exact reported items, and the requested combination is supported. No sender field authorizes inventory mutation or possession.

Four independent tests cover a literal asymmetric fixture with wide IDs and hashes, tinderbox590 targeting logs1511, native absent sentinels, exact framing and distinction from drag12. Other target-family packets90/19/112 remain unimplemented; this milestone does not silently reinterpret them.
