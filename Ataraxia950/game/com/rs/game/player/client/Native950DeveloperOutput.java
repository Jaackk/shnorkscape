package com.rs.game.player.client;

import com.rs.game.player.Player;
import io.netty.channel.Channel;
import java.util.*;

/** Synchronous world-thread output scope; never captures another player or later combat messages. */
final class Native950DeveloperOutput {
    private static final ThreadLocal<Scope> CURRENT=new ThreadLocal<>();
    private static final class Scope {
        final Player player;final Channel channel;final List<String> lines=new ArrayList<>();
        Scope(Player p,Channel c){player=p;channel=c;}
    }
    static List<String> run(Player p,Channel c,Runnable action){
        Scope previous=CURRENT.get(),scope=new Scope(p,c);CURRENT.set(scope);
        try{action.run();return new ArrayList<>(scope.lines);}
        finally{if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
    }
    static boolean capture(Player p,String text){Scope s=CURRENT.get();return s!=null&&s.player==p&&append(s,text);}
    static boolean capture(Channel c,String text){Scope s=CURRENT.get();return s!=null&&s.channel==c&&append(s,text);}
    private static boolean append(Scope s,String text){if(s.lines.size()<100)s.lines.add(String.valueOf(text));return true;}
}
