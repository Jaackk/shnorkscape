# Standard HP bars and ordinary melee hitmarks in 950

2026-09-10. Scope: prove the small standard HP-bar mapping needed by the engine, and identify what an ordinary typed melee hit would require. No claim of a complete combat port.

## Reproduction and source

Run `python -B tools/verify_950_hitbars.py --output protocol-analysis/hitbars-hitmarks-950-evidence.json` from the isolated test folder, using the project Python environment. The tool opens the original 910 packed cache strictly with `rb`, validates every sector header and reference-table/file boundary it reads, and compares logical files against both the 947 and 950 flat caches. It changes only the requested evidence JSON. The checked result passed; Java tests have been added but are left for the main combined build.

Native addresses below refer to `OpenNXT/data/clients/950/win64/original/rs2client.exe`, SHA-256 `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`. Use `tools/dis950.py range/bytes` to reproduce their instructions.

## Standard HP-bar identity: proven

The native bar sink `0x14037C6F0` loads its definition through the configuration type at `0x140C70480`, whose value is 72. The corresponding loader is installed at `0x1402AD4C1..0x1402AD5AD`. These are logical files in config index 2, group 72.

| ID | Exact 910 EntityHitBar selection | SHA-256 of the logical definition, identical in 910/947/950 |
|---|---|---|
| 0 | Entity size below 3 | `f0e4c0c5b3c4714f07a60b0d55c2c91e7b0ce3497fd6231f870b8de73bf52cc6` |
| 4 | Entity size 3 or 4 | `8aa4da3217bfdd3df984661522f237cd2e8bf104dc2d3a280b43d66d0d2ef661` |
| 3 | Entity size at least 5 | `f1c04433edb7b57f65f9c975782075768c94ccca7e7eb66449beb3d7d4e807a4` |

All three definitions are 29 bytes. This is an actual comparison with 910 data, not numeric ID agreement or an inference from the 947 writer. Their sprite references, lifetime/fade settings and priorities remain unchanged. This does not establish every other bar ID or all referenced sprite artwork as unchanged.

`Entity.addHitBars()` queues `new EntityHitBar(this)`. The exact class computes `min(hp,maxHp) * 255 / maxHp`, or zero when maxHp is zero; `getToPercentage()` returns that same percentage, `getDelay()` is zero, and `display(Player)` always returns true in this source. Thus its current behavior is safe for the player frame's shared snapshot. **The scale is 0..255, not 0..100 or 0..256.**

The new `Native950Hitbars` adapter admits only `bar.getClass() == EntityHitBar.class`, then IDs 0/3/4 and valid percentage values. It preserves the actual bar's getter semantics, snapshots immutable player/NPC typed values, counts refused entries, caps the encoded list at 255 entries and does not clear the engine queue. It deliberately does not admit subclasses, timers, adrenaline or custom bars. A custom bar may have viewer-dependent visibility even if its ID happens to match.

The existing legacy percentage calculation uses `int` multiplication. The adapter refuses out-of-range results rather than wrapping them into a byte; it does not repair all possible overflow within that legacy method. Normal size/HP fixtures and zero/full/over-max endpoints are covered. Any future general percentage utility should use `long` arithmetic and explicitly preserve which entity the bar belongs to.

### Wire fields and timing

The 950 reader consumes a smart bar ID, smart transition-cycle value, and then a smart delay. Cycle 32767 removes the bar immediately. For an update, it reads first fill byte and reads a second fill byte only for nonzero cycle. Zero cycle reuses the first fill. The native player call at `0x14012D733..0x14012D773` adds the delay to the current client cycle and forwards the separate cycle value. The proof here uses **cycle=0, delay=0**, exactly matching the standard legacy EntityHitBar; it does not guess positive-duration semantics for timer bars.

950 then requires a signed-null smart size field. Sending size=-1 suppresses both optional quantity bytes. The 910 writer had no corresponding size field: copying its byte sequence would be incomplete.

For a half-full bar, floor(50*255/100)=127:

- Player mask, no hits: `40 80 ff 00 00 00 81 00`. Bar count negated; first fill negated; final `00` is size=-1.
- NPC mask, no hits: `00 00 20 00 01 00 00 00 ff 00`. Bar count plain; first fill adds 128; final `00` is size=-1.

Full read derivations remain in [player masks](player-masks-950-derived.md) and [NPC masks](npc-masks-950-derived.md). The helper's seven tests cover these independent byte literals, endpoint ratios, size-based IDs, refusal of viewer-dependent subclasses, snapshot immutability and oversized counts.

### Verification lifecycle

Call `Native950Hitbars.verifyCache()` during native startup before admitting bar updates. It pins all three selected definitions and caches a successful result by the `Cache.STORE` object identity; repeated successful checks do not rehash. A changed store needs verification again. Strict mismatches throw; permissive research mode returns false and should leave bars refused. `from(...)` is a pure adapter intended for focused tests. The live bridge must call `fromRunningCache(Entity)`, which verifies the running JVM on first nonempty queue, reuses the successful store check, and refuses all queued bars if the cache is absent or permissive verification fails. A standalone preflight process cannot populate the running server's verification state.

## Ordinary melee hitmark identity: investigated, not yet bridged

The native hit sink `0x14037C0D0` resolves config type global `0x140C70348 = 46`, installed through `0x1402AD3C0..0x1402AD4AC`. The definition reader is `0x14033DA40` and its morph resolver is `0x14033E3E0`.

The 910 `HitLook.MELEE_DAMAGE` base value is 132, but `Hit.getMark(viewer,victim)` increments it for an ordinary noncritical melee hit:

| Viewer relation | Ordinary noncritical melee wrapper |
|---|---:|
| Viewer is source or victim | 133 |
| Uninvolved observer | 150 |

**Do not use raw HitLook value 132 as the ordinary melee packet type.** The 950 definition at 132 has been repurposed to a `+%1 miscut!` message. Also do not reuse the current viewer-independent player snapshot to publish the same typed hitmark to everyone.

The selected wrappers 133 and 150 retain their 947 morph semantics in 950. Their definition opcode 17 (u16 varbit) became opcode 21 (u24 varbit), without changing the selected varbit or target table. Full consumption and normalized fields agree. The old 910 wrappers have the same selector/target structure but different duration/replacement defaults; those differences are recorded in the evidence JSON.

- Wrapper 133 uses varbit 36893: selector 0 chooses definition 0; selectors 1..7 choose the existing alternate styles; selectors 16..30 choose definition 106.
- Wrapper 150 uses varbit 36896: selector 0 chooses definition 14; alternate-style entries are preserved; most selectors 16..30 choose definition 116. Some entries select empty definition 186 or no definition (-1). These are intentional client-selected visibility/style effects.

The helper records every referenced target and compares decoded definitions across the three revisions. Native opcode 21 reads the three-byte varbit at `0x14033DF93..0x14033DFD4`, then the u16 varp and target list; resolver `0x14033E418..0x14033E4E4` evaluates the varbit/varp and chooses the entry or final fallback.

### Damage scaling and viewer state must be resolved before integration

The native text formatter `0x14033E1B0` computes:

`displayed number = wire damage * definition numerator / definition denominator`

The multiplication is at `0x14033E1FF`; signed integer division is at `0x14033E218`. Definition opcodes 19/20 set numerator/denominator at object offsets +0xB0/+0xB4. Defaults are both one.

- Standard selected definitions 0 and 14 use 1:1 display scale.
- Legacy-looking definitions 106 and 116 explicitly divide by ten.

Consequently, the current fallback's `damage / 10` is not a universal typed-hit conversion. The engine removes `Hit.getDamage()` directly from engine HP, while this port sends current life points as engine HP multiplied by ten. A modern standard 1:1 hit style must be reconciled with that same unit, and legacy styles must remain consistent with the viewer's selected HP/hitsplat display mode. Do not call the old `getDamageDisplay(viewer)` or divide again without checking the resulting units.

The existing untyped fallback carries only a byte, so values over 255 remain refused; the proven typed form can carry smart damage through 32767. This investigation does not authorize clamping larger damage silently, mapping critical/dual/soaked/missed hits broadly, or guessing style varbits. A next typed-hit milestone should establish one explicit viewer mode and style, align the displayed HP loss and hitsplat number, and then add the viewer-aware frame bridge. The present implementation enables only the proven standard HP bars.
