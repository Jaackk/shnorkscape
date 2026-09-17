# Combat pursuit, loot and XP in950

This milestone restores ordinary910 mechanics through the native950 boundary. It does not enable the legacy NPCCombat, boss/death callbacks or instance creation wholesale.

## Reused infrastructure

- Player attack action and NPC retaliation target are distinct, as PlayerCombat/NPCCombat were in910. Walking cancels the player action. An NPC already struck (including a miss) pursues and retaliates until the16-tile home/target leash, teleport, logout or death releases it. Merely clicking Attack then walking before a swing does not provoke it. Existing Entity.calcFollow and intelligent EntityStrategy/FixedTileStrategy own movement and collision. The original CombatDefinitions auto-retaliation setting is respected while the player is idle. Repeated path failure is bounded; an unreachable return can relocate only to a verified clear home.
- Native950MeleeCombat records actual capped damage in Entity's existing received-damage ledger. The existing highest-damage player selection owns loot. The ledger clears at respawn. XP occurs once per damaging player hit; death does not award XP a second time.
- NPC.drop and the native adapter share NPCDropTableRolls, extracted from the original method without changing its guaranteed drops, independent rolls, rare-rate multiplier, shuffle/three-random-drop cap or inclusive quantities. Existing data/npcs/drops.json is unchanged. Settings account/drop rates and DOUBLE_DROPS remain in use. Item identities and certificates are checked against the selected950 cache. Coins995 use the exact pre-existing reviewed950 currency pin because their menu wording changed; the global identity gate remains strict.
- World.addGroundItem, Region ground lists, FloorItem and WorldTasksManager still own loot and its60-second private/60-second public lifetime. ItemConstants.isTradeable remains the public transfer rule; untradeable loot stays owner-only. Native item expiry removes by identity so a stale timer cannot delete a new equal pile.
- Ground Take routes through the existing pathfinder to the exact tile, rechecks the published viewer state and authoritative pile, private/account/controller gates, then uses the original ItemsContainer insertion on a staged copy of the actual inventory. Full bags, overflow and stale clicks consume nothing. Coins go into the backpack, consistent with the currently supported native containers. Collected validated loot can be banked and saved; carrying an item does not unlock unported item-specific actions or wear support.
- CombatDefinitions.giveXp and shared Skills calculations/commit own combat XP, account rates, the source's custom combat multiplier, levels, healing and fractional saved XP. Pet, invention, quest, seasonal and other optional unported callbacks remain outside native awards. Current default melee selection splits Attack/Strength/Defence, plus Constitution; native save schema3 already stores XP/current levels. Combat-style selection persistence is a later extension.

## Native950 presentation

Ground-item opcodes51/109/70 and zone prefix96 are derived from the local950 client parsers. Native950GroundItemsView is a per-viewer projection of Region storage, not a second item database. It diffs add/count/remove and handles owner/public transition, late login, plane change and region rebuild. Entries are aggregated by tile/item ID and display at most65,535; the authoritative quantity remains intact. Old visible entries are removed before rebuilding because the client retains overlapping scene piles.

The950 client scene is fixed256 tiles (scene selector5), while Player.getMapSize currently retains a104-tile server interest window. Ground packet origins use the verified256-tile client scene `(loadedChunk-16)*8`; they must not use the legacy server interest radius. This milestone leaves the server interest scope unchanged.

The cache's own interface1213 onStatTransmit scripts produce floating XP from UPDATE_STAT. It opens after the restored skill burst establishes the baseline. The exact cache-default wrapper geometry and existing layout persistence scripts keep it visible after resize. The overlay is initialized to the cache's default position on login. It uses134 cache pins; no invented per-hit XP script is sent.

## Validation and manual check

See validation-combat-rewards-2026-09-10.json, ground-items-950.md, npc-drops-reuse-950.md and the XP evidence notes. Automated checks establish server state, actual-cache bindings and encrypted frame composition, not native client rendering.

After launch, sign in with x/x, enter World1 and use `;;nxt combat`. Attack the nearby goblin, then walk several tiles away within its home area: it should chase. Stop walking to test auto-retaliation, then finish the fight. Check skill XP and floating XP, visible dropped bones/other rolled loot, and Take from several tiles away. Confirm the item reaches the backpack and persists after relog. Resize during another fight to check XP placement. NPCs beyond the ported basic melee profiles, abilities, scripted boss phases and custom death rewards remain later milestones.

## Follow-up found during the live check

NPC Examine still enters the legacy optional PetPerkManager, which is not hydrated in native950 (NPCHandler.handleExamine:2451). The router catches and rejects this operation while the player remains connected. Track it in the next NPC-handler compatibility pass; it is not claimed fixed by the combat rewards milestone.
