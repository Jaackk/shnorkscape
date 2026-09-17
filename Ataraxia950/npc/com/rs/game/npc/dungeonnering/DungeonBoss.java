package com.rs.game.npc.dungeonnering;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.RoomReference;
import com.rs.game.player.content.dungeoneering.journals.Chronicles;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.DungeonController;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.val;

import java.util.List;

@SuppressWarnings("serial")
public class DungeonBoss extends DungeonNPC {

    private RoomReference reference;

    public DungeonBoss(final int id, final WorldTile tile, final DungeonManager manager, final RoomReference reference) {
        this(id, tile, manager, reference, 1);
    }

    public DungeonBoss(final int id, final WorldTile tile, final DungeonManager manager, final RoomReference reference, final double multiplier) {
        super(id, tile, manager, multiplier);
        setReference(reference);
        setForceAgressive(true);
        setIntelligentRouteFinder(true);
        setLureDelay(0);
    }

    @Override
    public void sendDeath(final Entity source) {
        super.sendDeath(source);
        for (final Player player : getManager().getParty().getTeam()) {
            if (player.getHitpoints() <= 10) {
                Controller controller = player.getControlerManager().getControler();
                if (controller instanceof DungeonController) {
                    ((DungeonController) controller).setKilledBossWithLessThan10Hp();
                }
            }
        }
        getManager().openStairs(getReference());
    }

    public static final int getTier(final int id) {
        val definitions = ItemDefinitions.getItemDefinitions(id);
        val name = definitions.getName();
        val examine = ItemExaminesDataParser.getExamine(name);
        val index = examine.indexOf("(Tier ");
        if (index == -1) {
            return -1;
        }

        val builder = new StringBuilder(examine.substring(index + 6));
        builder.deleteCharAt(builder.length() - 1);
        int tier = -1;
        try {
            tier = Integer.parseInt(builder.toString());
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        }
        return tier;
    }

    @Override
    public void drop() {
        final NPCDrop[] drops = NPCDropsDataParser.getDrops(getRealId(this));
        if (drops == null) {
            return;
        }
        final List<Player> players = getManager().getParty().getTeam();
        if (players.size() == 0) {
            return;
        }

        /** Ratio value varies from -1 to 1.  */
        float ratio = -1;
        ratio += (((getManager().getParty().getComplexity() - 1) * 20F)) / 200F;
        ratio += (((getManager().getParty().getSize() - 1) * 50F)) / 200F;

        val map = new Object2IntOpenHashMap<NPCDrop>(drops.length);
        int size = 0;
        for (int i = drops.length - 1; i >= 0; i--) {
            val drop = drops[i];
            val tier = getTier(drop.getItemId()) / 5.5F;
            if (tier == -1) {
                map.put(drop, 10);
                size += 10;
                continue;
            }
            val v = 250 + (int) (90F * (tier * ratio));
            size += v;
            map.put(drop, v);
        }
        val randomEntry = Utils.random(size);
        int currentPointer = 0;
        NPCDrop drop = drops[Utils.random(drops.length)];
        for (int i = drops.length - 1; i >= 0; i--) {
            val d = drops[i];
            val length = map.getInt(d);
            if ((currentPointer += length) >= randomEntry) {
                drop = d;
                break;
            }
        }


        final Player luckyPlayer = players.get(Utils.random(players.size()));
        handleJournalDrops(luckyPlayer);
        final Item item = new Item(drop.getItemId(), drop.getMinAmount());
        luckyPlayer.getInventory().addItemDrop(item.getId(), item.getAmount());
        luckyPlayer.getPackets().sendGameMessage("You received: " + item.getAmount() + " " + item.getName() + ".");
        for (final Player p2 : players) {
            if (p2 == luckyPlayer) {
                continue;
            }
            p2.getPackets().sendGameMessage("" + luckyPlayer.getDisplayName() + " received: " + item.getAmount() + " " + item.getName() + ".");
        }
    }

    private void handleJournalDrops(final Player luckyPlayer) {
        if (Utils.random(3) == 0) {
            int tier = Utils.random(2) == 0 ? getManager().getParty().getFloor() / 2 : getManager().getParty().getFloor() / 2 - 1;
            if (tier < 0) {
                tier = 0;
            }
            if (tier > 29) {
                tier = 29;
            }
            final Chronicles chronicle = Chronicles.values()[tier];
            dropChronicle(luckyPlayer, chronicle);
        }
        final boolean bookDrop = Utils.random(3) == 0;
        if (bookDrop) {
            if (this instanceof ToKashBloodChiller) {
                if (getManager().getParty().getFloor() >= 9 && getManager().getParty().getFloor() <= 11) {
                    final Chronicles chronicle = Chronicles.KALPART1;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof LakkTheRiftSplitter) {
                if (getManager().getParty().getFloor() >= 18 && getManager().getParty().getFloor() <= 29) {
                    final Chronicles chronicle = Chronicles.KALPART2;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof BalLakThePummeler) {
                if (getManager().getParty().getFloor() >= 33 && getManager().getParty().getFloor() <= 35) {
                    final Chronicles chronicle = Chronicles.KALPART3;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof YkLagorThunderous) {
                if (getManager().getParty().getFloor() >= 45 && getManager().getParty().getFloor() <= 47) {
                    final Chronicles chronicle = Chronicles.KALPART4;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof KalGerWarmonger) {
                if (getManager().getParty().getFloor() >= 57 && getManager().getParty().getFloor() <= 60) {
                    final Chronicles chronicle = Chronicles.KALPART5;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (getName().equals("Plane-freezer Lakhrahnaz")) {
                if (getManager().getParty().getFloor() >= 6 && getManager().getParty().getFloor() <= 11) {
                    final Chronicles chronicle = Chronicles.STALKPART1;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof NightGazerKhighorahk) {
                if (getManager().getParty().getFloor() >= 26 && getManager().getParty().getFloor() <= 29) {
                    final Chronicles chronicle = Chronicles.STALKPART2;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof ShadowForgerIhlakhizan) {
                if (getManager().getParty().getFloor() >= 30 && getManager().getParty().getFloor() <= 35) {
                    final Chronicles chronicle = Chronicles.STALKPART3;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof FleshspoilerHaasghenahk) {
                if (getManager().getParty().getFloor() >= 42 && getManager().getParty().getFloor() <= 47) {
                    final Chronicles chronicle = Chronicles.STALKPART4;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof WorldGorgerShukarhazh) {
                if (getManager().getParty().getFloor() >= 54 && getManager().getParty().getFloor() <= 60) {
                    final Chronicles chronicle = Chronicles.STALKPART5;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof GluttonousBehemoth) {
                if (getManager().getParty().getFloor() >= 1 && getManager().getParty().getFloor() <= 11) {
                    final Chronicles chronicle = Chronicles.BEHPART1;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof BulwarkBeast) {
                if (getManager().getParty().getFloor() >= 12 && getManager().getParty().getFloor() <= 17 || getManager().getParty().getFloor() >= 30 && getManager().getParty().getFloor() <= 35) {
                    final Chronicles chronicle = Chronicles.BEHPART2;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof Stomp) {
                if (getManager().getParty().getFloor() >= 18 && getManager().getParty().getFloor() <= 29) {
                    final Chronicles chronicle = Chronicles.BEHPART3;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof RuneboundBehemoth) {
                if (getManager().getParty().getFloor() >= 36 && getManager().getParty().getFloor() <= 47) {
                    final Chronicles chronicle = Chronicles.BEHPART4;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof HopeDevourer) {
                if (getManager().getParty().getFloor() >= 51 && getManager().getParty().getFloor() <= 60) {
                    final Chronicles chronicle = Chronicles.BEHPART4;
                    dropChronicle(luckyPlayer, chronicle);
                }
            } else if (this instanceof AsteaFrostweb) {
                final Chronicles chronicle = Chronicles.ASTEA_FROSTWEB;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof IcyBones) {
                final Chronicles chronicle = Chronicles.TROLL_SCRAWLINGS;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof LuminscentIcefiend) {
                final Chronicles chronicle = Chronicles.ENVIRONMENTAL_EFFECTS;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof DivineSkinweaver) {
                final Chronicles chronicle = Chronicles.DIVINE_SKINWEAVERS;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof HobgoblinGeomancer) {
                final Chronicles chronicle = Chronicles.HOBGOBLIN_SCRAWLINGS;
                dropChronicle(luckyPlayer, chronicle);
            } else if (getName().equals("Unholy cursebearer")) {
                final Chronicles chronicle = Chronicles.THE_PRICE_OF_BETRAYAL;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof Rammernaut) {
                final Chronicles chronicle = Chronicles.EQUIPMENT_REQUISITION_RECEIPTS;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof LexicusRunewright) {
                final Chronicles chronicle = Chronicles.LEXICUS_RUNEWRIGHT;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof Sagittare) {
                final Chronicles chronicle = Chronicles.AMMUNITION_REQUISITION_ORDERS;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof Gravecreeper) {
                final Chronicles chronicle = Chronicles.TOMBSTONE_TRANSCRIPTION;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof NecroLord) {
                final Chronicles chronicle = Chronicles.ARCH_NECROLORD_REQUEST;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof Blink) {
                final Chronicles chronicle = Chronicles.BLINKS_SCRIBBLINGS;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof WarpedGulega) {
                final Chronicles chronicle = Chronicles.GULEGA_MISSIVE;
                dropChronicle(luckyPlayer, chronicle);
            } else if (this instanceof Dreadnaut) {
                final Chronicles chronicle = Chronicles.RESOURCE_REQUISITION_ORDERS;
                dropChronicle(luckyPlayer, chronicle);
            }
        }
    }

    /**
     * Checking object instance rather than ids because the bosses often transform to diff npcs,
     * cbf listing down all npcs.
     *
     * @return drop npc id.
     */
    public int getRealId(final DungeonBoss boss) {
        if (boss instanceof AsteaFrostweb) {
            return 9965;
        }
        if (boss instanceof GluttonousBehemoth) {
            return 9948;
        }
        if (boss instanceof LuminscentIcefiend) {
            return 9912;
        }
        if (boss instanceof HobgoblinGeomancer) {
            return 10059;
        }
        if (boss instanceof IcyBones) {
            return 10040;
        }
        if (boss instanceof ToKashBloodChiller) {
            return 10024;
        }
        if (boss instanceof DivineSkinweaver) {
            return 10058;
        }
        if (boss instanceof BulwarkBeast) {
            return 10073;
        }
        if (boss instanceof Rammernaut) {
            return 9767;
        }
        if (boss instanceof Stomp) {
            return 9782;
        }
        if (boss instanceof LakkTheRiftSplitter) {
            return 9898;
        }
        if (boss instanceof Sagittare) {
            return 9753;
        }
        if (boss instanceof NightGazerKhighorahk) {
            return 9725;
        }
        if (boss instanceof LexicusRunewright) {
            return 9842;
        }
        if (boss instanceof BalLakThePummeler) {
            return 10128;
        }
        if (boss instanceof ShadowForgerIhlakhizan) {
            return 10143;
        }
        if (boss instanceof SkeletalAdventurer) {
            return 11985;
        }
        if (boss instanceof RuneboundBehemoth) {
            return 11752;
        }
        if (boss instanceof Gravecreeper) {
            return 11708;
        }
        if (boss instanceof NecroLord) {
            return 11737;
        }
        if (boss instanceof FleshspoilerHaasghenahk) {
            return 11895;
        }
        if (boss instanceof YkLagorThunderous) {
            return 11872;
        }
        if (boss instanceof WarpedGulega) {
            return 11737;
        }
        if (boss instanceof Dreadnaut) {
            return 12848;
        }
        if (boss instanceof HopeDevourer) {
            return 12886;
        }
        if (boss instanceof WorldGorgerShukarhazh) {
            return 12478;
        }
        if (boss instanceof Blink) {
            return 12865;
        }
        if (boss instanceof KalGerWarmonger) {
            return 12752;
        }
        if (boss.getName().equals("Unholy cursebearer")) {
            return 10111;
        }
        if (boss.getName().equals("Plane-freezer Lakhrahnaz")) {
            return 9929;
        }
        return -1;
    }


    @Override
    public void sendDrop(final Player player, final NPCDrop drop, final boolean lootbeam) {

    }
	
	
	
	/*@Override
	public boolean isPoisonImmune() {
		return true;
	}*/

    public RoomReference getReference() {
        return reference;
    }

    public void setReference(final RoomReference reference) {
        this.reference = reference;
    }
}
