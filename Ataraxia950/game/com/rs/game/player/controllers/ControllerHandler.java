package com.rs.game.player.controllers;

import java.util.HashMap;

import com.rs.game.activites.BrimhavenAgility;
import com.rs.game.activites.PuroPuro;
import com.rs.game.activites.clanwars.FfaZone;
import com.rs.game.activites.clanwars.RequestController;
import com.rs.game.activites.clanwars.WarControler;
import com.rs.game.activites.creations.StealingCreationGame;
import com.rs.game.activites.creations.StealingCreationLobby;
import com.rs.game.activites.dnd.eviltree.EvilTreeInstanceController;
import com.rs.game.activites.dnd.eviltree.GlobalEvilTreeController;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.activites.duel.DuelControler;
import com.rs.game.activites.dungeon_architect.DungeonArchitectController;
import com.rs.game.activites.gambling.GamblingAreaController;
import com.rs.game.activites.multiboss.MultibossController;
import com.rs.game.activites.quest.deathsbounty.SoulFightInstanceController;
import com.rs.game.activites.soulwars.AreaController;
import com.rs.game.activites.soulwars.GameController;
import com.rs.game.activites.soulwars.LobbyController;
import com.rs.game.activities.aod.AoDController;
import com.rs.game.activities.rots.RiseOfTheSixController;
import com.rs.game.activities.snowball.game.SnowballFightGameController;
import com.rs.game.activities.snowball.lobby.SnowballLobbyController;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerDungeon;
import com.rs.game.player.content.araxxor.AraxxorController;
import com.rs.game.player.content.death.DeathController;
import com.rs.game.player.content.fistofguthix.FistOfGuthix;
import com.rs.game.player.content.ports.PlayerPortsController;
import com.rs.game.player.content.xmas.XmasController;
import com.rs.game.player.controllers.bossInstance.BossInstanceController;
import com.rs.game.player.controllers.bossInstance.KalphiteKingInstanceController;
import com.rs.game.player.controllers.bossInstance.LegiosInstanceController;
import com.rs.game.player.controllers.bossInstance.PZInstanceController;
import com.rs.game.player.controllers.bossInstance.SpiderBossInstanceController;
import com.rs.game.player.controllers.bossInstance.TelosInstanceController;
import com.rs.game.player.controllers.bossInstance.TheMagisterInstanceController;
import com.rs.game.player.controllers.bossInstance.VoragoInstanceController;
import com.rs.game.player.controllers.bossInstance.gwd2.HeartOfGielinorController;
import com.rs.game.player.controllers.castlewars.CastleWarsPlaying;
import com.rs.game.player.controllers.castlewars.CastleWarsWaiting;
import com.rs.game.player.controllers.fightpits.FightPitsArena;
import com.rs.game.player.controllers.fightpits.FightPitsLobby;
import com.rs.game.player.controllers.SolakController;
import com.rs.game.player.controllers.pestcontrol.PestControlGame;
import com.rs.game.player.controllers.pestcontrol.PestControlLobby;
import com.rs.utils.Logger;

public class ControllerHandler {

    private static final HashMap<Object, Class<Controller>> handledControlers = new HashMap<Object, Class<Controller>>();

    public static final Controller getControler(Object key) {
        if (key instanceof Controller)
            return (Controller) key;
        Class<Controller> classC = handledControlers.get(key);
        if (classC == null)
            return null;
        try {
            return classC.newInstance();
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static final void init() {
        try {
            // Class<Controller> duelClass = (Class<Controller>)
            // Class.forName(DuelArenaController.class.getCanonicalName());
            // handledControlers.put("DuelArenaController", duelClass);
            Class<Controller> value1 = (Class<Controller>) Class.forName(Wilderness.class.getCanonicalName());
            handledControlers.put("Wilderness", value1);
            Class<Controller> value4 = (Class<Controller>) Class.forName(GodWars.class.getCanonicalName());
            handledControlers.put("GodWars", value4);
            Class<Controller> value5 = (Class<Controller>) Class.forName(ZGDController.class.getCanonicalName());
            handledControlers.put("ZGDController", value5);
            Class<Controller> value9 = (Class<Controller>) Class.forName(DuelArena.class.getCanonicalName());
            handledControlers.put("DuelArena", value9);
            Class<Controller> value10 = (Class<Controller>) Class.forName(DuelControler.class.getCanonicalName());
            handledControlers.put("DuelControler", value10);
            Class<Controller> value11 = (Class<Controller>) Class.forName(CorpBeastController.class.getCanonicalName());
            handledControlers.put("CorpBeastController", value11);
            Class<Controller> value14 = (Class<Controller>) Class.forName(DTController.class.getCanonicalName());
            handledControlers.put("DTControler", value14);
            Class<Controller> value15 = (Class<Controller>) Class.forName(JailController.class.getCanonicalName());
            handledControlers.put("JailController", value15);
            Class<Controller> value17 = (Class<Controller>) Class.forName(CastleWarsPlaying.class.getCanonicalName());
            handledControlers.put("CastleWarsPlaying", value17);
            Class<Controller> value18 = (Class<Controller>) Class.forName(CastleWarsWaiting.class.getCanonicalName());
            handledControlers.put("CastleWarsWaiting", value18);
            Class<Controller> value20 = (Class<Controller>) Class.forName(HeartOfGielinorController.class.getCanonicalName());
            handledControlers.put("HeartOfGielinorController", value20);
            handledControlers.put("clan_wars_request", (Class<Controller>) Class.forName(RequestController.class.getCanonicalName()));
            handledControlers.put("clan_war", (Class<Controller>) Class.forName(WarControler.class.getCanonicalName()));
            handledControlers.put("clan_wars_ffa", (Class<Controller>) Class.forName(FfaZone.class.getCanonicalName()));
            handledControlers.put("NomadsRequiem", (Class<Controller>) Class.forName(NomadsRequiem.class.getCanonicalName()));
            handledControlers.put("BorkController", (Class<Controller>) Class.forName(BorkController.class.getCanonicalName()));
            handledControlers.put("BrimhavenAgility", (Class<Controller>) Class.forName(BrimhavenAgility.class.getCanonicalName()));
            handledControlers.put("FightCavesControler", (Class<Controller>) Class.forName(FightCaves.class.getCanonicalName()));
            handledControlers.put("ImpossibleJadControler", (Class<Controller>) Class.forName(ImpossibleJad.class.getCanonicalName()));
            handledControlers.put("FightKilnControler", (Class<Controller>) Class.forName(FightKiln.class.getCanonicalName()));
            handledControlers.put("FightPitsLobby", (Class<Controller>) Class.forName(FightPitsLobby.class.getCanonicalName()));
            handledControlers.put("FightPitsArena", (Class<Controller>) Class.forName(FightPitsArena.class.getCanonicalName()));
            handledControlers.put("PestControlGame", (Class<Controller>) Class.forName(PestControlGame.class.getCanonicalName()));
            handledControlers.put("PestControlLobby", (Class<Controller>) Class.forName(PestControlLobby.class.getCanonicalName()));
            handledControlers.put("Barrows", (Class<Controller>) Class.forName(com.rs.game.player.content.barrows.Barrows.class.getCanonicalName()));
            handledControlers.put("AoDController", (Class<Controller>) Class.forName(AoDController.class.getCanonicalName()));
            handledControlers.put("Falconry", (Class<Controller>) Class.forName(Falconry.class.getCanonicalName()));
            handledControlers.put("QueenBlackDragonControler", (Class<Controller>) Class.forName(QueenBlackDragonController.class.getCanonicalName()));
            handledControlers.put(RunespanController.class.getSimpleName(), (Class<Controller>) Class.forName(RunespanController.class.getCanonicalName()));
            handledControlers.put("SorceressGarden", (Class<Controller>) Class.forName(SorceressGarden.class.getCanonicalName()));
            handledControlers.put("CrucibleControler", (Class<Controller>) Class.forName(CrucibleController.class.getCanonicalName()));
            Class<Controller> stealingCreationGame = (Class<Controller>) Class.forName(StealingCreationGame.class.getCanonicalName());
            handledControlers.put("StealingCreationsGame", stealingCreationGame);
            handledControlers.put("StealingCreationGame", stealingCreationGame);
            Class<Controller> stealingCreationLobby = (Class<Controller>) Class.forName(StealingCreationLobby.class.getCanonicalName());
            handledControlers.put("StealingCreationsLobby", stealingCreationLobby);
            handledControlers.put("StealingCreationLobby", stealingCreationLobby);
            handledControlers.put("BarrelchestControler", (Class<Controller>) Class.forName(BarrelchestController.class.getCanonicalName()));

            handledControlers.put("FOGController", (Class<Controller>) Class.forName(FOGController.class.getCanonicalName()));
            handledControlers.put("FistOfGuthix", (Class<Controller>) Class.forName(FistOfGuthix.class.getCanonicalName()));

            handledControlers.put("Dungeoneering", (Class<Controller>) Class.forName(Dungeoneering.class.getCanonicalName()));
             handledControlers.put("SolakController", (Class<Controller>) Class.forName(SolakController.class.getCanonicalName()));
            handledControlers.put("TrexController", (Class<Controller>) Class.forName(TrexController.class.getCanonicalName()));


            handledControlers.put("PuroPuro", (Class<Controller>) Class.forName(PuroPuro.class.getCanonicalName()));
            handledControlers.put("WarriorsGuild", (Class<Controller>) Class.forName(WarriorsGuild.class.getCanonicalName()));
            handledControlers.put("XmasController", (Class<Controller>) Class.forName(XmasController.class.getCanonicalName()));
            handledControlers.put("MultibossController", (Class<Controller>) Class.forName(MultibossController.class.getCanonicalName()));
            handledControlers.put("AreaController", (Class<Controller>) Class.forName(AreaController.class.getCanonicalName()));
            handledControlers.put("GameController", (Class<Controller>) Class.forName(GameController.class.getCanonicalName()));
            handledControlers.put("LobbyController", (Class<Controller>) Class.forName(LobbyController.class.getCanonicalName()));
            handledControlers.put("HouseController", (Class<Controller>) Class.forName(HouseController.class.getCanonicalName()));
            handledControlers.put("BossInstanceController", (Class<Controller>) Class.forName(BossInstanceController.class.getCanonicalName()));
            handledControlers.put("PlayerPortsController", (Class<Controller>) Class.forName(PlayerPortsController.class.getCanonicalName()));
            handledControlers.put("RiseOfTheSixController", (Class<Controller>) Class.forName(RiseOfTheSixController.class.getCanonicalName()));
            handledControlers.put("DungeonController", (Class<Controller>) Class.forName(DungeonController.class.getCanonicalName()));
            handledControlers.put("Kalaboss", (Class<Controller>) Class.forName(Kalaboss.class.getCanonicalName()));
            handledControlers.put("VoragoInstanceController",
                    (Class<Controller>) Class.forName(VoragoInstanceController.class.getCanonicalName()));
            handledControlers.put("KalphiteKingInstanceController",
                    (Class<Controller>) Class.forName(KalphiteKingInstanceController.class.getCanonicalName()));
            handledControlers.put("AraxxorController",
                    (Class<Controller>) Class.forName(AraxxorController.class.getCanonicalName()));
            handledControlers.put("ArtisansWorkShopControler",
                    (Class<Controller>) Class.forName(ArtisansWorkShopControler.class.getCanonicalName()));
            handledControlers.put("SophanemSlayerDungeon",
                    (Class<Controller>) Class.forName(SophanemSlayerDungeon.class.getCanonicalName()));
            handledControlers.put("EvilTreeInstanceController",
                    (Class<Controller>) Class.forName(EvilTreeInstanceController.class.getCanonicalName()));
            handledControlers.put("SoulFightInstanceController",
                    (Class<Controller>) Class.forName(SoulFightInstanceController.class.getCanonicalName()));
            handledControlers.put("GlobalEvilTreeController",
                    (Class<Controller>) Class.forName(GlobalEvilTreeController.class.getCanonicalName()));
            handledControlers.put("SnowballLobbyController",
                    (Class<Controller>) Class.forName(SnowballLobbyController.class.getCanonicalName()));
            handledControlers.put("SnowballFightGameController",
                    (Class<Controller>) Class.forName(SnowballFightGameController.class.getCanonicalName()));
            handledControlers.put("TelosInstanceController",
                    (Class<Controller>) Class.forName(TelosInstanceController.class.getCanonicalName()));
            handledControlers.put("DeathController",
                    (Class<Controller>) Class.forName(DeathController.class.getCanonicalName()));
            handledControlers.put("SpiderBossInstanceController", (Class<Controller>) Class.forName(SpiderBossInstanceController.class.getCanonicalName()));
            handledControlers.put("GamblingAreaController", (Class<Controller>) Class.forName(GamblingAreaController.class.getCanonicalName()));
            handledControlers.put("DungeonArchitectController", (Class<Controller>) Class.forName(DungeonArchitectController.class.getCanonicalName()));
            handledControlers.put("LegiosInstanceController", (Class<Controller>) Class.forName(LegiosInstanceController.class.getCanonicalName()));
            handledControlers.put("TheMagisterInstanceController", (Class<Controller>) Class.forName(TheMagisterInstanceController.class.getCanonicalName()));
            handledControlers.put("EliteDungeonController", (Class<Controller>) Class.forName(EliteDungeonController.class.getCanonicalName()));
            handledControlers.put("EliteDungeonsLobby", (Class<Controller>) Class.forName(EliteDungeonsLobby.class.getCanonicalName()));
            handledControlers.put("AraxxorHiveControler", (Class<Controller>) Class.forName(AraxxorHiveControler.class.getCanonicalName()));
            handledControlers.put("PZInstanceController", (Class<Controller>) Class.forName(PZInstanceController.class.getCanonicalName()));
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    public static final void reload() {
        handledControlers.clear();
        init();
    }
}
