# Cook dialogue completion and instance admission (950)

Date: 2026-09-10. Changes belong only to 950RevTest.

The user confirmed the corrected forced movement and the Cook shortcut landing/greeting,
then completed the Cook's entire Yes conversation and reported a crash. The old dialogue
is the custom combat miniquest (`dialogue.impl.Cook`), not Cook's Assistant.

## Reproduced cause

At stage6 the legacy dialogue marks talkedtoCook; stage8 starts ImpossibleJadControler.
ImpossibleJad.loadCave allocates/copies a legacy dynamic map, queues an instance teleport,
and starts a wave. The 950 move phase failed with `Modern region 0 failed to load` /
`Missing map settings group 0`. The same encounter constructed AgrithNaNa3493 through
the legacy NPC constructor and World.addNPC. That registered a non-native boss outside
the native roster. Native teardown removed only its own roster; every subsequent
ensureBanker call refused the remaining legacy entity (`The modern world must not share
a legacy NPC loop`). These are server-observed causes; no native client crash dump was
used to diagnose a separate renderer fault.

Evidence before restart is preserved under
`implementation-backup/2026-09-10-cook-instance`, including both server logs, the engine
jar, launch records and the unchanged local x save. The save is valid schema3/rev950,
position3208,3215,0, eight occupied inventory slots, empty bank/equipment. The bad instance
position was not checkpointed. No account reset or save edit was needed. Controller and
Cook quest flags are not part of the current native save format.

## Correction

- Cook's 950 stage6 ends with an unavailable-adventure message before changing progress;
  stage8 also handles already-progressed/completed characters without starting combat.
  The message has a normal Continue and the conversation can be reopened. The original
  910/947 dialogue behavior is unchanged.
- ControlerManager refuses this unported controller for exact ClientProfile.NATIVE_950
  before replacing a current controller, constructing a registered class, starting an
  instance, or restoring its login callback. Direct controller objects and known keys
  are covered. It does not invoke a legacy cleanup callback during refusal.
- In native world mode, the spawning legacy NPC constructor and World.addNPC refuse
  before legacy cache/combat/region work or registry/dynamic-area mutation respectively.
  Native spawns continue through their existing verified admission. Legacy registration
  outside native world mode remains unchanged.

This is a safe boundary around unported combat/instances, not an implementation of the
miniquest. The next combat milestone still needs verified NPC state, attacks, damage,
death/respawn and drops before this particular encounter can be enabled.

## Validation

New focused regressions cover the full decoded Yes/No flows, already-progressed paths,
reopening, unchanged progress, controller replacement/login refusal, both original
profiles, the exact legacy boss constructor, registry integrity and banker recreation.
The real-cache world probe now also completes the actual Cook class using native
presentation and encrypted output, checks the safe message, preserved location/progress,
no legacy NPC insertion, and dialogue reopening. Current run and live acceptance results
are tracked in `validation-effects-2026-09-10.json`; probes do not establish rendering.
