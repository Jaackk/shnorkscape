# Beginner Gnome Agility, revision950

Scope: all seven ordinary Gnome Stronghold obstacles, using actual950 scenery IDs/shapes/rotations and collision routes. Advanced branches and other courses remain separate ports.

Uses the established910 GnomeAgility stage attribute and level-dependent obstacle XP formulas, with existing ActionManager and Skills. Balance log/rope and crawl pipes use byte-identical910/950 BAS155/295. Stair sequence828 and BAS movement/stand sequences have identical frame references/durations. All16 object/BAS/sequence definitions are pinned in resources/native950/agility-assets-950.properties. Only these verified temporary render sets override the native appearance's equipment stance; finish/cancel restores normal appearance.

Each obstacle routes to its exact entrance. The tree footprint blocks2473,3422 on plane1: actual collision testing corrected its entrance to2473,3423. The encrypted-click acceptance traverses the real map between all obstacle endpoints, including planes0/1/2, native player frames, distance refusal, ordered lap stages, reverse-pipe bonus refusal, cancellation and run/lock restoration. Seven encrypted clicks and202 decoded frames passed. The fixture mirrors the live session's scene-ready transition after a frame.

Lap bonus90 requires the ordered forward course. Reverse traversal clears lap progress. Cancellation/death/controller replacement/force movement prevent subsequent rewards. XP remains in the normal character save; in-progress lap state is temporary. Old random events, optional boosts/contracts and completion achievements are outside this slice.

Use ;;nxt agility to move north of the first log without changing items or levels. Follow log, net, tree up, rope, tree down, net, pipe. Run com.rs.game.player.client.Native950AgilityAcceptance with the950 cache path to repeat verification. Live animation pixels remain a user check.
