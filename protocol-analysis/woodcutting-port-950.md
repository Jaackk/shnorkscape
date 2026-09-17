# Ordinary woodcutting on revision950

The native bridge runs the existing `Woodcutting` action through the existing `ActionManager`. It retains `TreeDefinitions` levels, XP and log IDs, `AxeDef` tool requirements and power, the original four-tick success-roll delay, the low/high chance curve, the original felling roll, and `World.spawnObjectTemporary` / `World.removeObjectTemporary` respawn scheduling. It uses the existing `Skills.addXp` native branch, so normal configured rates, levels, XP popups and persistence apply.

Native admission reads the actual950 object's semantic name and Chop down operation. Numeric IDs alone do not establish a tree: for example, old1308 now resolves to Target, while1306/1307 now resolve to Abyssal portal. Quest resources, custom donor trees, farming patches, ivy, crystal shards, transforms and unsupported shapes do not enter ordinary harvesting.

Supported ordinary categories are tree, evergreen, dead tree, oak, willow, maple, teak, mahogany, yew, magic and elder. All ten authored ordinary tools from bronze through crystal are supported in inventory or the weapon slot, subject to their existing Woodcutting requirements. The existing toolbelt remains governed by its own admission rules; this port does not seed it or grant high-tier tools.

The current world slot is checked before every action tick and before each reward, so a stump produced by another player ends the stale action. The native inventory adapter stages the original ItemsContainer add before committing a log; full inventory cannot generate XP without the log. Regular successful harvests add one log and one Skills XP award. Optional pets, bird nests, contracts, outfits, auras, Invention, adze auto-burning and quest callbacks remain with those systems' ports. Elder's original per-player five-minute activity / ten-minute replenishment timer remains unchanged.

## Asset evidence

`woodcutting-assets-950.json` records34 exact950 definition hashes, including the selected animations,19 tool/log identities and five stumps. The runtime resource `native950/woodcutting-assets-950.properties` pins those same bytes. Runtime item admission uses strict950 decode for non-noted/non-transformed, non-stackable log/tool semantics, Light or Wield, and actual950 equipped models.

Most old regular chopping animation IDs were repurposed:879,877,875,873,871,869,867 and2846 now select different frame bindings and item overrides. They are not emitted. The selected animation is the original authored `AxeDef.specialEmote`, resolved through the actual950 hatchet parameter8835. Cache struct11172 explicitly names this role Lumberjack Woodcutting and points to17091 for bronze. All ten selected roles retain the910 frame/duration and hand-item binding. Their duration is185 client cycles, so animation renewal waits seven server ticks independently of the original four-tick yield cadence. This currently gives the Lumberjack chopping style. The ordinary new950 animation variants are present under other item parameters but their distinct contexts were not guessed.

Actual950 stump definitions1341,57931,12733,31057 and37824 are Tree stump, have no operations or transformations, and retain the original chosen replacement roles. Teak's original negative stump ID removes the tree temporarily. The shared object projection is responsible for native rendering, collision updates, late viewers and restoring the original object.

## Validation

Seven cache-independent regression tests cover semantic admission, repurposed and quest object rejection, operation movement, original chance endpoints, tool improvement and bounded probabilities. The root combined real-cache acceptance exercises actual object input, ActionManager harvesting, XP and inventory commits, depletion, respawn and the subsequent firemaking flow. Cache/protocol validation does not assert that a user has visually approved the selected animation.
