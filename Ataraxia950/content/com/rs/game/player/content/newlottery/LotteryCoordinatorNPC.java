package com.rs.game.player.content.newlottery;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class LotteryCoordinatorNPC extends NPC {

    public LotteryCoordinatorNPC() {
        super(Lottery.getSingleton().coordinatorId, Lottery.getSingleton().coordinatorTile, -1, false);
        setDirection(Utils.getAngle(1, 0));
        setRandomWalk(0);
    }

    @Override
    public void processNPC() {
        if (Lottery.getSingleton().starting) {
            int rand = ThreadLocalRandom.current().nextInt(0, 16);
            if (rand < 6) {
                switch (rand) {
                    case 0:
                    case 1:
                        if (ThreadLocalRandom.current().nextBoolean()) {
                            setNextForceTalk(new ForceTalk("Keep an eye out, I'm about to pick a lottery winner!"));
                        } else {
                            setNextForceTalk(new ForceTalk("I'm about to pick a lottery winner!"));
                        }
                        setNextAnimation(new Animation(862));
                        break;
                    case 2:
                        setNextAnimation(new Animation(2106));
                        break;
                    case 3:
                        setNextAnimation(new Animation(2107));
                        break;
                    case 4:
                        setNextAnimation(new Animation(2108));
                        break;
                    case 5:
                        setNextAnimation(new Animation(2109));
                        break;
                }
            }
        }
    }
}
