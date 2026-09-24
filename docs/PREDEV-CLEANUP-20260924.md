# Final pre-console cleanup

AUTOMATED VERIFIED; physical Vulkan LIVE TEST PENDING. Running server and source cache were not changed.

The latest live binding capture is `session-20260924-050959-230-jaxa`, marker `action-bar-binding-test`. It contains checkbox365:19 row10754 and equipment changes, but no decoded dropdown selection/value notifications. The user confirms choices do not save. Previous isolated preference tests did not establish the physical UI path.

Exact950 settings type4 builds a type16 dropdown through CS2830/8020. CS5181 and CS5591 own its display/selection callbacks. This is distinct from the icon chooser CS3069/3082; its opcode008f is a component-field setter, not a count-input packet. That earlier hypothesis was discarded. A narrowly guarded CS5591 bridge now notifies the existing server row/value handler using the same IF_FIND/CC_OP contract as CS10450/10451. Only numeric-timer and binding dropdown rows notify; unrelated native callbacks and switch offsets remain intact. Hidden value actors remain on365:20. Server validation still limits HUD selection to Main and saved presets to1–4, retains per-player storage, and rejects unarmed/stale values. The changed callback must still be tested physically.

Other changes:

- Residual-soul duration publishes only at combat exit/re-entry. During combat varc7246 is zero, which CS10886 renders without a duration; the active icon/count remains. Exit publishes CS4252 once. Resource expiry, soul visuals and intentional retention across equipment swaps remain unchanged.
- Sunshine/Death's Swiftness cooldown redraws no longer rerun the side-effectful CS6570 field branch. Their original cooldown varcs remain available to CS6506; CS6992 does not clear them. This removes repeated field-clock resets, but whether it resolves the reported Sunshine sweep still requires Vulkan acceptance.
- Quick `melee/range/mage/necro` commands apply the same transactional library Best loadouts, with food filtered out. Existing inventory/equipment is banked after preflight; bank capacity, item state, requirements and rollback protections are reused.
- Cracker962 enables the native player target-family bit8 for its backpack slot. Inventory refresh restores ordinary masks when the cracker leaves that slot. Existing opcode102 transaction, distance/ownership and duplicate/full-inventory protections remain. Native Use → player is pending live acceptance; no invented Pull caption.
- Bar drags accept the mounted workspace owner as well as the child interface. The native trash target and absent-target sentinel clear a slot when unlocked. Native lock1430:270/bit1892 is honoured and no longer reset by every redraw. Other drop destinations do not erase slots. The lock is session-local; saved bar contents still persist.

Validation: 1,538 JUnit tests, zero failures/errors, two existing skips; 1,857 cumulative paired-cache acceptance checks; three bridge tests covering all55 supported rows, values0–18, unrelated controls, invalid values and preservation of the original script. The paired-cache acceptance exercises decoded selection packets, save/restore, actual weapon metadata, preset-switch option, two-player isolation and full-inventory quick gear. Existing bank-full/overflow/stale transaction tests pass. These are not physical client results.

Preserve all latest live passes. Missing Strength book entries, Ghost presentation and general render priority remain deferred. No broad combat rewrite. The native Developer Console/world editor remains the next implementation phase after this cleanup's live gate; it is not included or represented as complete in this candidate.

## One live checklist

1. Soul buff timer stays dormant during combat, then counts down after exit.
2. Sunshine retains its cooldown sweep/numeric timer while Revolution attacks.
3. Binding dropdown choices survive reopening/relog; weapon swaps select the saved bar. Check the bank-preset switch toggle too.
4. With a full inventory, each quick gear command banks old items and equips Best gear without food.
5. Use a Christmas cracker on Nooby; verify one consumption and rewards.
6. Unlock the bar and drag an ability off; lock it and confirm removal is blocked.
7. Quick bank/items → keybind, manual queue and Revolution regression.
