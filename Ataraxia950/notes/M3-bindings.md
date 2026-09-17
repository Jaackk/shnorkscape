# M3 - binding table and facade promotion (validation record)

Written 2026-09-07 for milestone M3 (stats, vitals, run energy, skills tab),
area A: the wire side. Paths are relative to `Ataraxia947/`. Every id below comes
from a **CONFIRMED** row of
`OpenNXT/data/prot/947/generated/native947-3/verified/ui/UI-EVIDENCE-SUMMARY.md`
(topic detail in `SKILLS_TAB.md`, `ORBS_AND_VARS.md` and their `-verification.md`
companions) or from a cache scan reproduced here and named in section 3. Nothing
under `OpenNXT/` was modified.

---

## 1. What went into `resources/native947/ui-bindings-947.json`

### 1.1 Slots (enum 7716 key -> struct -> params 3505 attach / 3503 wrapper)

| Slot | Key | Struct | Attach | Wrapper | Note |
|---|---|---|---|---|---|
| `skills` | 0 | 21293 | 1477:300 | 1477:298 | new |
| `action_bar` | 1003 | 21277 | 1477:70 | 1477:67 | new |
| `minimap` | 1004 | 21278 | 1477:94 | **1477:92** | wrapper added; the pre-M3 table had only the attach |

All three re-resolved against the real cache at
`C:\Users\developer\Desktop\rs3cache\cache` before being written, with the loader's
own enum/struct path (`enumInt(7716, key)` then `structInt(struct, 3505/3503)`);
each resolved to exactly the hash the evidence pins, and each attach/wrapper is a
component of root 1477 as a full 32-bit hash, never the legacy `& 0xfff` mask.

### 1.2 Interfaces

| Name | Id | minComponents | Named components | Pins |
|---|---|---|---|---|
| `skills` | 1466 | 15 | root 0, current_level 4, base_level 5, cells 7, bottom_bar 9, total_level 11, combat_level 12 | file 0 + components 4, 9, 11, 12 |
| `action_bar` | 1430 | 269 | root 0, hp_bar 7, prayer_bar 14, summoning_bar 20, auto_retaliate_icon 54, adrenaline_bar 55, auto_retaliate_button 57 | file 0 + components 7, 14, 20, 54, 55, 57 |
| `minimap` | 1465 | 43 | viewport 0, run_orb 14, run_orb_ring 15, **energy_bar 17**, run_orb_icon 18, energy_text 19 | file 0 (already pinned) + components 14, 15, 17, 18, 19 |

The energy bar is `1465:17`, not `1465:18`: `ORBS_AND_VARS.md` s10.1 corrects the
910 assumption. `1465:18` is the orb icon, repainted by script 1741.

Components 1466:4, 1466:5 and 1466:7 are byte-identical files
(`f66a5fe2...`), so only 4 is pinned; pinning all three would assert the same
digest three times without adding a check.

Every SHA-256 pin was computed from the real cache with the loader's own
`Native947CacheReader.Flat.sha256(index, group, file)`. The method was
cross-checked before use: the digests it produced for 1430:0/7/14/20/54/55/57 and
1465:0/14/15/17/18/19 are byte-identical to the independent verifier's
`ui/orbs-verifier-interfaces.json`, so the 1466 digests it produced (for which
the verifier recorded no per-file digest) are trustworthy by the same method.

**Trap avoided:** `SKILLS_TAB.md` s9.2 records a "group sha256 `872cb6b3...`" for
3/1466. That is the digest of the whole group, **not** of file 0, which is what
the loader checks. File 0 is `6933909c...`. Pinning the group digest would have
rejected the table against the very cache it was derived from.

### 1.3 Vars

| Name | Kind | Id | Base varp | Bits | maxValue |
|---|---|---|---|---|---|
| `hitpoints` | varbit | 1668 | 659 | 1..15 | 32767 |
| `prayer_points` | varbit | 16736 | 3274 | 0..14 | 32767 |
| `summoning_points` | varbit | 41524 | 8040 | 0..14 | 32767 |
| `virtual_levels` | varbit | 19007 | 458 | 30..30 | 1 |
| `adrenaline` | varp | 679 | - | - | 1000 |
| `run_toggle` | varp | 463 | - | - | 4 |
| `auto_retaliate` | varp | 462 | - | - | 1 |

Encoding, from the readers rather than from prose: hitpoints is life points
**x 100** (script 2915 builds the maximum as `curlvl(3) x 100` through 16856),
prayer is points **x 10** (5256 divides by 10), summoning is points **x 10**
(873 divides by 10). Auto-retaliate is inverted: **1 means OFF** (8131 selects
the "disabled" text for 1).

`ui-bindings-947.json` now declares the base varp and bit range for **every**
varbit, including the four bank/chat ones that predate M3, and the loader rejects
the table when a varbit has moved. That closed a real gap: a varbit is written by
recomputing its parent varp, so a definition that silently moved to another varp
would have corrupted an unrelated player variable instead of failing. The four
pre-M3 entries needed their true bases filled in
(45189 = varp 8958[0..3], 45141 = 110[27..30], 45158 = 7755[4], 18797 = 1772[0]);
the test fake previously carried invented bases for them.

Every range was re-derived from the cache with the loader's own decoder rather
than copied from the doc, and matches `ui/orbs-verifier-interfaces.json`
`varbitDefs2_69` where that file covers the id. Varbit 19007 is not in that file;
the cache decodes it as varp 458 bits 30..30, exactly as `SKILLS_TAB.md` s6 says.

### 1.4 Scripts

29 readers added, all pinned by SHA-256: 1315, 1316, 1432, 1522, 1534, 1740,
1741, 2141, 2306, 2761, 2915, 4708, 5256, 7081, 8109, 8110, 8122, 8123, 8124,
8125, 8129, 8130, 8131, 8391, 8488, 8489, 11849, 11868, 12294.

None of them is *invoked* by the server. They are pinned because every semantic
claim M3 depends on is a claim about one of these files (that UPDATE_STAT carries
the boosted level, that prayer is in tenths, that 1 means auto-retaliate off,
that the run orb follows varp 463 through varc 119 with no server varc write). If
a re-dumped cache changes one of them, the table is rejected before the M3 vars
are driven at a client that no longer reads them the same way.

### 1.5 Deliberately NOT bound (CANDIDATE)

Skills panel **320** (both 1466 and 320 carry the same 8488 hook; needs a live
test), the skills wrapper open sequence, XP tracker slot **1015**, XP popup slot
**1026**, run-click gate varc **1413**, struct 21293 params 3499/3500. The
binding test asserts their absence so nobody adds them without the live test.

---

## 2. Facade promotion (`Native947PacketDispatcher`)

| Method | Was | Now | Packet |
|---|---|---|---|
| `sendSkillLevel` | NO-OP + counter | REAL | UPDATE_STAT 66 |
| `sendConfigByFile` / `sendConfigByFile1/2` / `sendVarBit` | REAL for 0..255, **STRICT above** | REAL for every value | VARBIT_SMALL 50 / VARBIT_LARGE 71 |
| `sendGlobalConfig` / `Small` / `Large`, `sendCSVarInteger` | NO-OP + counter | REAL | CLIENT_SETVARC_SMALL 1 / _LARGE 112 |
| `sendGlobalString`, `sendCSVarString` | NO-OP + counter | REAL | CLIENT_SETVARCSTR_SMALL 67 / _LARGE 15 |
| `sendRunEnergy` | NO-OP | REAL | UPDATE_RUNENERGY 116 |
| `refreshWeight` | NO-OP | REAL | UPDATE_RUNWEIGHT 108 |

Still STRICT and untouched: the zone-relative prefix, GE, social/clan, the
active-interface openers, and the input-box varcs.

**A verified packet is not a licence to write a 910 id.** Every promoted method
resolves through `Native947IdMap` first, so an id the binding table does not
declare raises `UnboundIdException` inside the writer body and becomes a counted
drop with nothing on the wire. `sendConfigByFile` in particular now reaches
VARBIT_LARGE, which is exactly what a still-unbound varbit could have exploited.

Three semantic points, each from a named reader:

* **UPDATE_STAT carries the boosted level.** `Skills.getLevel` is the boosted or
  drained value and `Skills.getLevelForXp` is the base; the packet sends the
  former, because the client derives the base itself from the experience (script
  11849 over enum 10866) and colours the cell by comparing op `0x4D8` (current)
  with op `0x14` (definition-capped base) in script 8489. Total level (2761 ->
  4708) and combat level (1432) are also client-side. Sending the base level
  would make every boost invisible and every drain look permanent.
* **Experience is clamped at 200,000,000 points.** The writer multiplies by ten
  and the client's setter `0x140369C10` clamps a flagged entry at 2,000,000,000
  tenths. Ataraxia's own `Skills.MAXIMUM_EXP` is 2,000,000,000 **points**, ten
  times what the wire can carry, and `experience * 10` would overflow a Java int
  above 214,748,364. The facade clamps rather than letting the writer reject the
  packet: a clamped bar is what the client would display anyway, whereas a
  rejected packet leaves the skill blank.
* **Skill index 0..26 only.** Enum 680 has 29 entries and 0..26 are identical to
  `Skills.SKILL_NAME`; 27 Archaeology and 28 Necromancy have no Ataraxia data at
  all (`level[]` and `xp[]` are length 27). The client does **not** bounds-check
  the skill index, so anything outside 0..26 is a counted drop rather than a byte
  written into a real stat slot.

  *Consequence of leaving 27 and 28 unsent, stated for the record:* they stay at
  the client's initialiser defaults (base 1, current 1, experience 0). Because
  the client computes the total level itself (2761 -> 4708 sums enum 680,
  including 27 and 28), the displayed total is **2 higher** than the sum of the
  27 skills Ataraxia models. Combat level (1432) is unaffected at level 1. This
  was not observed live - no client was run for this milestone.

### 2.1 Discarded-id bookkeeping

Before M3 every id-carrying discard was a counted NO-OP, and
`Counters.noopIds(method)` was the inventory M2b acceptance (c) checked against
`notes/P5-router.md` section 3. After the promotion those families discard as
counted **drops**, and no method is left in the id-carrying NO-OP tier, so that
inventory would have gone silently empty.

`Native947PacketDispatcher.drop` therefore records its first argument into the
same histogram `noop` uses, and `Counters` gained the clearer aliases
`discardedIds(method)` / `discardedIdsByMethod()`. The old names are kept because
`Native947Session.Snapshot` and `Native947InteractionsSmoke` print them, so the
smoke's `discarded no-op ids` line still carries the full inventory - the same
ids, now under the drop tier rather than the no-op tier. That category shift is
the expected difference in the smoke output, and it is the only one.

---

## 3. `notes/P5-router.md` section 3: the M3 subset, retired

Method: every index-12 script (20,577 groups, 20,577 decoded) was scanned for the
6-byte `[op u16 BE][operand i32 BE]` form of cs2 `getvar` (0x1ca) and `setvar`
(0x195), with the operand `domain << 24 | id << 8 | flag` for domain 0 (varp) and
domain 2 (varc) and flag 0..3. The method was validated against four independent
controls from `ORBS_AND_VARS.md` before any conclusion was drawn from it, and
reproduced every one exactly: varp 463 readers `[1315, 1316, 6995, 7017, 7971]`,
varp 462 readers plus writers `[8129, 9876]`, varp 455 readers `[2526, 2778]`,
and varc 1413's thirteen readers with no writer. (The same scan is not valid for
varbits: cs2 `getvarbit` 0x30 / `setvarbit` 0xa2 take a **3-byte** operand, so
the varbit half of the scan produced false negatives and was discarded; varbit
readers are taken from the evidence docs instead.)

| Ids | 947 reader found | Decision |
|---|---|---|
| varps **711, 712, 713, 714, 715, 716, 717, 718, 3561, 3562, 3563, 3596** | yes, exactly one each: 8461 (715, 717, 3561, 1037-1039), 8463 (716, 718, 3562, 1037-1039), 8465 (711-714, 3563, 3596) | **retired** |
| varc **779** | yes: 1516, 1517, 1608, 3503, 6452, 8280, 8484, 18274 | **retired** |
| varc **2911** | yes: 8286 | **retired** |
| varcs **5886..5906** (21 ids) | yes: 19 and 17690 read them, and **12915 writes all 21 client-side** | **retired** |

Reasons, per group:

* **The bonus varps (711-718, 3561-3563, 3596).** All three readers are the
  equipment-bonus panel: each opens by loading container 94 (worn equipment) and
  immediately branches on the component hash `102170649` = **1559:25**, then on
  slot 742. Interface 1559 is not bound, has no CONFIRMED row anywhere in the UI
  evidence, and M3 never opens it. Binding the varps would put 910 bonus values
  into live player variables whose 947 readers we have located but whose expected
  encoding we have not verified - the "never invent a transform" rule. They are
  recorded here for M13 (equipment stats panel), which must verify the encoding
  against 8461/8463/8465 before binding them. Until then they are counted drops
  on every wear/remove, which is loud but harmless.
* **varc 779.** `GlobalPlayerUpdater.buildAppearenceData`'s 910 varc. A native
  player takes the P4 appearance branch, so the value the 910 emitters around it
  compute is not the one the native body uses; there is no CONFIRMED mapping from
  the 910 meaning to what 1516/1517/8484 expect.
* **varc 2911.** One reader, 8286, which resolves struct 21301 param 3503 - the
  all-chat slot wrapper - and is a chat-slot path, not an equipment one. No
  CONFIRMED row; nothing in M3 needs it.
* **varcs 5886..5906.** Script **12915 sets all twenty-one itself**, from
  `gosub 12916`. They are client-computed; a server write would race the client's
  own value rather than inform it. This is the clearest retire of the four.

Not M3's to decide, left where they are: the bank varbits 45139-45156 /
45190-45192, the preset varbits 22179/22186/22187/26188, varcs 6709, the bank
varps 111/160/680/1037-1039/7751-7753/8970, the 24 components of 517, container
623 and the two interfaces 187/1448 (all bank/dialogue, M7); login varps
171/1056/427 (CANDIDATE drops - no reader and no writer in 20,477 scripts, and
their varbits 473-475 and 1004-1008 are equally unread).

Newly bound out of that inventory: **varp 463** (run toggle), which section 3
explicitly parks for M3, plus varp 462 and 679 and the four M3 varbits, none of
which appeared in the section-3 list because nothing on the M2b path emitted them
yet.

---

## 4. Loader changes and their fail-closed value

* `Var` gained `varp`, `startBit`, `endBit` and `bitWidth()`; the loader now
  requires a `varp` and a `"lo..hi"` `bits` range on every varbit and rejects the
  whole table when either disagrees with the cache's own decode.
* `Native947Bindings.Resolver` gained `varc(int)`. Both resolvers reject an
  undeclared varc (the permissive one has no archive to check against - client
  variables live only in the client's store - so it can do no better than the
  declared set). The pre-M3 `AllowListResolver` constructor pushed varc and
  varcstr ids into a **throwaway `HashSet`**, so a declared varc could never have
  been resolved even in principle; that is now one set per kind.

## 5. Known gaps / handover

* **`Native947ActionRouter.IdMapAdapter.varc` still returns -1 unconditionally**
  (that file belongs to another area and was not touched). It is therefore the
  binding authority for varcs today, not the table, and the effect is correct for
  M3 because M3 binds no varc. When M7 binds the input-box varcs (`input_mode` 5,
  `input_text` 2506), that method must be changed to `return resolver.varc(id);`
  - `Native947Bindings.Resolver.varc` now exists for exactly that - and its
  javadoc sentence "Varcs have no verified binding at all (the facade keeps them
  STRICT)" is already stale.
* No live client was run. Everything here is static cache evidence plus unit and
  smoke coverage.
* The skills panel choice (1466 vs 320) is still CANDIDATE and is the one live
  test M3 needs before the tab can be declared correct.
