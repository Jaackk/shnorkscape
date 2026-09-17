# Revision 950 life-points caller and scale

Derived on 2026-09-10 from `950RevTest/cache` and the unmodified950WIN64 client
SHA-256 `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`.
All native addresses below belong to950, not947.

## Live defect

`logs/server.out.log:59-60` recorded a rejected `sendConfigByFile(1668,1000)` on login.
The950UI binding table already mapped current life points to varp13537, but
`Player.refreshHitPoints()` still sent the old varbit1668. The handoff's statement
that nothing consumed the binding was inaccurate: the original caller was still active.

## The unit did not change

950script16856 is seven instructions, including the unreachable fallback:

```
push_int 3
current_stat                         //9500x0715,9470x04d8
push_int 100
multiply                             //9500x06c2,9470x051e
return
push_int 0
return
```

These correspond instruction-for-instruction to947script16856. Thus its base maximum
remains Constitution current level times100. `Player.getMaxHitpoints()` stores the
level times10 plus equipment bonuses: sending engine hitpoints times10 remains correct.

950script8122 still passes current and maximum life points to script4902. Its current
value is `max(0,getvarp(13537))`; its maximum is `max(1,gosub2915())`.
The old version reads varbit1668 at the corresponding instruction.

950script2915 changed its organization and bonus calculation, not its base unit:

```
base = gosub16856()
bonus = gosub21118()
scaled = scale(base + bonus, 1000, gosub21120())
return gosub16860(max(1, scaled))
```

Script21118 accumulates equipment/buff bonuses. Script21120 initializes its multiplier
to1000, with conditional modifiers (including500). The denominator1000 cancels the
normal multiplier1000; it does not multiply the life-points unit by1000.

The crucial three-argument `scale` operation was verified in the native950binary:
registration at`0x14005ee46` loads opcode`0x03bd`; `0x14005ee4b` installs the function
at`0x1401ff350`. It pops three integers a,b,c, sign-extends them, performs64-bit
`a*c` at`0x1401ff388`, then signed division by b at`0x1401ff396`. It is not a guessed
binary multiplication or division. This also agrees with unchanged script16843,
whose corresponding947opcode`0x059d` applies a percentage using a divisor100.

Script16860 now selects between an int maximum and32000, depending on a client mode.
That display-limit logic is distinct from the packet field width. Current life points
are a full-width varp; the old32767varbit cap must not remain in the950caller.

## Implemented boundary

Only`ClientProfile.NATIVE_950` sends varp13537. It sends engine hitpoints times10 using
a long intermediate, bounded to0..Integer.MAX_VALUE. The existing910/947varbit1668
path and its cap remain unchanged. This does not claim to port every new950HP bonus
or combat mode; it fixes the current-life-points wire unit and target.

`Native950StatsTest` exercises the actual Player caller with a resolver that allows
varp13537 and refuses all varbits (matching the relevant production distinction),
checks20engineHP->200clientLP and990->9900, and ensures rejected old writes cannot
silently pass under identity resolution. It also checks40000LP without the old15-bit
cap, saturated large input, zero, and normal allow-list refusal counters. The lifecycle
test's absent-transport expectation now counts`sendConfig`, not`sendConfigByFile`.

## Cache witnesses

Hashes are over the logical decompressed file0 of index12:

| Script | SHA-256 |
|---|---|
|8122|4c57ad2abbbab019f07a51d09aefdcce1d759c8549e42571b1817dcd6d8e166f|
|16856|20b20153861e99a3933a922e4ebe5e178765080dc28301004ea682baa54e1840|
|2915|6ffb198991d34345b437589d23e44126416f02e36f8e9f6c3aa4641fd0e769f2|
|21118|fc11121fd02d84eddfe3c90d52f2663e7007b6433b0a7ba202cbc354e8c09b44|
|21120|0ae0d2c8afa1cf95933b4b3fd97e5e8c12879f589e3ebff2d0c7af8a80a2c7dd|
|20982|a07884d72800293433681f727884cd6962943a121c3daab7f1655a4e358b789f|
|16860|99a9da645190516bdae5c912df5da498ddca9141ceeda09df6eba5bbf9619caf|

Reproduce the decisive native operation with the project's Python runtime and
`tools/dis950.py range 0x14005edf0 190`, then
`tools/dis950.py func 0x1401ff350 --max 110`.
