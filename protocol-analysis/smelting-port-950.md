# Ordinary furnace smelting — 12 September 2026

Native950Smelting reuses the original910 SmeltingBar enum for product selection, levels and XP; the existing ActionManager and Skills handle repeated work and XP. The shared native recipe action uses the original inventory through its atomic exchange boundary. The original Smelting level-based delay is preserved. The current furnace must remain present, reachable and on the same plane for the entire action.

The paired950 cache supplies the ingredients. Each finished bar item links recipe structs with params2675..2677; struct2656/2666 supplies backpack item/quantity and7763 repeats its identity. All used item, struct and sequence bytes are pinned in smelting-assets-950.properties. This matters because the copied910 recipe table has obsolete material combinations: old banite21779 is unnamed, adamant/rune use luminite, and elder rune includes a rune bar. See smelting-ingredients-950.json for every raw structure.

Supported ordinary bars and actual950 materials:
- Bronze: copper ore + tin ore.
- Iron: two iron ore.
- Steel: iron ore + coal.
- Silver: silver ore.
- Mithril: mithril ore + coal.
- Adamant: adamantite ore + luminite.
- Gold: gold ore.
- Rune: runite ore + luminite.
- Orikalkum: orichalcite ore + drakolith.
- Necronium: necrite ore + phasmatite.
- Bane: two banite ore.
- Elder rune: rune bar + light animica + dark animica.

Native950SmeltingAcceptance passed with the actual Lumbridge furnace113261 at3226,3256: collision approach, absent ingredients, level refusal, cancellation before first result, repeating bronze production, all twelve exact recipe exchanges, XP frames and furnace-replacement cancellation. Recorded89 originalengine ticks and237 parsed encrypted950 frames. This standalone fixture invokes the furnace helper after real collision arrival; the full root integration owns the native Smelt click and recipe/quantity dialogue. It uses no account save or listener. Client rendering remains a live check.

This is backpack smelting. Metal bank, forge equipment production, portable furnace, bars beyond the original ordinary twelve, cannonballs and quest/invention bonuses remain separate milestones.
