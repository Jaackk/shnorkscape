# Current-cache equipment metadata (950)

`Native950EquipmentTypes` admits equipment from strict current-cache definitions and referenced assets. There is no item-ID allow-list, per-item SHA pin, name matching for equipment classes, or 910 identity requirement.

The shared `Native950CacheItems.definition` resolver follows current note/lender/bound/shard links with bounded recursion and cycle/missing-record rejection. Notes and shard fragments do not acquire equipment capability. Valid lender and bound forms inherit the base equipment's slot, models, hidden slots, parameters and first four menu labels, keeping their own Discard/Destroy action.

Equipment admission requires a named strict definition, a slot in the current 19-slot layout and an actual Wear, Wield or Equip label in cache operations1–5. Model references must exist in index47. A model ID of -1 is a valid absent contribution, including nonvisual slots and gender-specific clothing; the server does not invent a male/female substitute. The client renders the item using its own model, recolour, retexture, scale and offset data. Inventory sprite zoom is not treated as a world-model size.

Opcode13 supplies the primary equipment slot. Opcodes14 and27 supply secondary/hidden slots. The transaction's occupied-slot set includes all three; a main-hand item occupying slot5 is two-handed. The live appearance removes the corresponding body kits. A complete raw scan found no explicit wearable whose hidden slot equals its own primary slot.

Base skill requirements are the SIX typed STAT/level pairs749/750 through759/760. Client script929 reads precisely these pairs and displays the failure messages for Wear/Wield. PARAM records2/11/749,751,753,755,757,759 identify STAT with default-1; the level records use INT default0. The old getter read ten pairs and could misinterpret unrelated INT params761–768 (for example Black mask's761=40) as skills. This implementation does not use that getter or its item-name requirement overrides. Skill-cape requirements use the skill in277 and master-cape flag4244 for99/120, replacing the former special item19709 check.

Weapon movement sets use direct item params2954/2955, then the current struct referenced by item3000 or686, then644 compatibility, then current unarmed defaults. `Native950EquipmentAnimations` validates the selected BAS and declared sequences against the same cache. Only the two hand slots resolve weapon profiles.

The concrete user example, Masterwork staff58486, resolves to slot3, secondary slot5, models135664 and135981 for both genders, and Magic99. It has no644. Its current profile686=14937 supplies normal BAS2697 and combat BAS2689. These are audit witnesses, not item-specific admission rules.

The raw index19 sweep found15,298 records with explicit Wear/Wield/Equip options,805 lender templates,2,898 bound templates and90 shard templates. This raw count includes placeholder/no-slot records and precedes complete template and asset admission. A separate executable audit reports final resolved admission counts; do not confuse the raw count with playable equipment count.

Evidence files:
- `equipment-raw-metadata-950.log`: representative definitions and full raw metadata counts.
- `equipment-menu-metadata-950.log`: special-menu witnesses, slot edge cases and old ten-pair requirement traps.
- `equipment-requirement-scripts-950.log`: normalized actual950 script929/9965 instructions and parameter type bytes.
- `../tools/Native950EquipmentMetadataAudit.java`: exhaustive read-only current-cache equipment resolution and representative witnesses; no player, save or listening socket.