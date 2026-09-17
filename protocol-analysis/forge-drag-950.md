# Forge dragging: paired950 cache evidence

The Forge content uses the native large central interface slot. The former mount at1477:735 uses the small central slot1007. Manually enlarging that small host is temporary: native drag completion calls8304 →8387 →8390 →8391, and8391 restores the host to512×334 (or the small370×256 preset). This matches the clipped512×334 live result reported on2026-09-12.

## Durable mount

- enum7716[1047] → struct40393, named Central Interface (Large).
- Its params3503/3505/3506 identify wrapper1477:724, contenthost1477:726, and overlay1477:727.
- The cache wrapper is800×600 and centered; host726 fills the wrapper.
- Forge37:17 is800×484 and centers inside its host. Preserve these cache dimensions.
- Smithing/Smelting frame script2600 calls8421, which calls3934 to discover the actual mount. Mounting at726 automatically selects1047, including its native dragging hooks.
- Frame hooks route through20528 →3927 →8412; client-owned onDrag8301 and onDragComplete8304 operate the slot. No server inventory-drag emulation is required.

## Open and close order

Keep the existing state writes, open37 on1477:726, registration, show1477:724, frame2600, category2586, event masks, refresh,1364, and generation-fenced deferred redraw. Remove all old11145 size overrides and small-slot8389 reset calls. No replacement sizing script is required.

Close only the owned subinterface at726, unregister37, hide724, and run1364. Leave1477:732/735 alone. This preserves ordinary Production/Toolbelt modal dimensions and avoids moving another modal while closing Forge.

The native large wrapper starts centered; do not force19986 on each open unless a deliberate recenter policy is desired.19986 only sets centered position and cannot fix the small-slot resize contract. Native drag may retain a chosen window position.

## Modal ownership and input

1364 calls8074 to resolve struct40393.host726 and explicitly handles large-modal visibility. Its large-host branch registers modal input context18 and calls20393(1,5); closing the last central modal releases that state. No special new global lock/input setting is necessary.

Legacy7808 is a one-string forwarding call to7839 and does not initialize Forge geometry.8178 conditionally refreshes another root component via8179; it is not needed to establish the large-slot contract. Keep1364 instead of replaying unrelated global refresh scripts.

## Evidence and verification

Run tools/verify_950_forge_drag.py, optionally with --output protocol-analysis/forge-drag-950-evidence.json. It reads the paired cache, verifies pinned bytes and precise struct/layout/call-chain assertions, and emits full normalized instructions for the relevant scripts. It does not change cache or gameplay data.

Header decoding intentionally stops before newer format9/11 listener encodings. Hooks are established from decoded scripts and their mount discovery chain rather than the old910 component decoder's empty listener arrays.

The small-slot reset is conditional only on key1007:8391 instructions17–60. enum7720 currently contains only0→struct21330(370×256), so no varc3678 choice can supply800×484. The recommended1047 mount skips that special branch and uses the normal slot wrapper/host relationships.

Client validation should include a title drag followed by a delayed screenshot, category/quantity selection, window resize, close→reopen, and close→ordinary Logs/Craft production. Static packet assertions cannot prove a client-local drag succeeded. Root reported the former small-host defect reproduced as exactly512×334; the large-host fix still needs live confirmation when this note is created.

## Live outcome

After deployment, the user signed in and confirmed: "dragging works now without breaking it". A read-only client capture showed the complete Smelting title, material/product grids, details, quantity control and Begin Project button. The updated server and client remain running.
