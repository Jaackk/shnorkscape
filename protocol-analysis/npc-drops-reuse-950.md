# Ordinary NPC drops: reuse of the 910 implementation

## Source and shared structure

The source is `C:/Users/developer/Desktop/Ataraxia-PS/npc/com/rs/game/npc/NPC.java`,
`drop()` (ordinary-table loop, approximately lines1274-1322) and `sendDrop()`
(quantity, approximately line2369). The copied950 implementation had the same
algorithm. `NPCDropTableRolls` extracts that algorithm; both legacy `NPC.drop()`
and `Native950NpcDrops` call it. Legacy `NPC.sendDrop()` also calls its quantity
helper. This is not the unrelated weighted `NPCDrop.selectRandomNPCDrop()`
algorithm, which the ordinary NPC death path never used.

The existing `NPCDropsDataParser` loads the same1093 authored NPC tables through
`DataPaths`. The file was not changed: source and staged `data/npcs/drops.json`
both have SHA256 `91257d31e0a1fe78951739958aafb8951c5732da5201648fff627ddc04c46c7f`.
The source includes trailing commas accepted by its existing Gson parser.

## Preserved table rules

- Every eligible rate100 row is guaranteed and does not count toward the random cap.
- Other rows roll independently; rates below30 use `Settings.getDropQuantityRate`.
- Successful random rows are shuffled and at most3 are emitted.
- The exact existing `Utils.getRandomDouble(maximum)` remains: it samples
  `[0, maximum+1)` and excludes exactly0 and100. Native ordinary kills use
  maximum100; legacy contract content can still supply95. No drop rebalance is
  hidden in the port.
- Quantities remain inclusive authored min..max via `Utils.getRandom(extra)`.
  Native awards respect the existing global `DOUBLE_DROPS` setting and refuse
  malformed/overflow quantities. A zero result produces no floor item.
- Root integration supplies the same damage-credit winner as
  `Entity.getMostDamageReceivedSourcePlayer()`, the NPC centre tile, and the
  existing ordinary drop lifetime of60 private seconds followed by60 public
  seconds. These differ from some tertiary drops'180-second timers.

The authored chicken41 table, for example, guarantees bones526, raw chicken2138,
and25-50 feathers314, plus its original egg1944 chance. Goblins use their existing
full equipment/rune/coin tables and guaranteed bones; there is no new starter
loot list.

## Current-cache adaptation

`Native950NpcDrops.roll(NPC, Player)` returns the selected `Item` list for the
world owner's placement path. Its public `metadata(int)` returns validated
`id`, `name`, `stackable`, `noted`, and `baseId`, or null when unsupported. The
metadata cache is keyed to the selected Store identity.

Each admitted item must pass the existing910-to950 SAME/RESTYLED identity gate,
strictly decode from index19, and fit the verified inventory item range1..65534.
Stacking comes from the950 definition. Authored noted item IDs stay unchanged;
a note must have a safe current base item, a reciprocal certificate ID, and a
real decodable template. Borrowed, bound and shard template families are refused.
No blanket renamed-item exception or `id+1` note conversion is introduced.

Unported donation noting, pet/death callbacks, clue eligibility, charm bonuses,
collection logs, quests, contracts, instances, lootshare and tertiary rules are not activated
by this adapter. Their legacy infrastructure remains available for their own
milestones. The ordinary table uses the existing game-mode/pet rarity accessor;
native players currently have no active legacy pet perks.

## Focused regression coverage

`NPCDropTableRollsTest` covers guaranteed rows versus the3-row cap, the strict
rarity threshold, endpoint behavior, eligibility filters, contract range,
inclusive quantity bounds and absent tables. `Native950NpcDropsTest` covers
native selection, invalid metadata/quantities, doubling overflow, zero results,
current stacking, reciprocal notes, missing templates and malformed/transformed
items. Fifteen tests were added; the integrating build records execution results.

## Floor lifecycle integration

`Native950Loot.createDeathDrops(NPC, Player)` passes validated selections to the
existing `World.addGroundItem` at the same NPC centre coordinate used by910.
`FloorItem`, `Region.getGroundItemsSafe()` and `WorldTasksManager` retain ownership
and the60-second private/public phases. The native marker follows a pile after
its owner logs out; native membership/removal checks use object identity so an
old pickup/publication/expiry task cannot mutate an equal replacement pile.
Legacy items retain their existing equality behavior. World-level donor note
conversion is bypassed for already validated native selections.

`Native950LootLifecycleTest` adds5 actual World-task lifecycle tests, including
both60-second boundaries and stale tasks after equal replacement piles. These
are in addition to the15 drop selection/metadata tests above.

## Public transfer policy

Native loot creation calls the original `ItemConstants.isTradeable(Item)` after
cache/bootstrap initialization and stamps its result on each FloorItem before
the world publishes the frame. Public appearance and pickup share this stored
rule; an untradeable item remains owner-only after its private timer expires.
The existing rule's GIM manager constructors are inert (loading and background
work require separate explicit start/load calls); SkillingPets initialization
only reads the already selected cache index size. No GIM process, database or
pet/death callback is started. This keeps the complete existing tradeability
rules rather than replacing their many exceptions with a new list.

`Native950GroundPickupTest` now adds17 transaction/gate tests, including the
owner-only visibility/pickup rule for untradeable public loot.

## Existing currency binding

The general identity table refuses coins995 because910's Convert inventory menu
became950's Add to pouch. Native loot reuses the already established exact
`Native950CacheContent.itemCatalog` coin SHA256
`628c2fd1154e10ed7eb2cda75f31d9736d12636269323e6adab292bbffac45ff`
for this one currency ID; the general validity table remains unchanged. A raw
fixture verifies that only the exact995 file is accepted by this exception.

An isolated actual-cache diagnosis confirmed Coins/Logs/Shrimps/Bones/notedBones
are tradable and Gold charm/Clue scroll are not. Calling the existing
ItemConstants rule left WorldTasksManager at0 tasks before and after. Evidence:
`logs/loot-metadata-diagnosis-950.log`.
