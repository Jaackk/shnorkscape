package com.opennxt.security;
import com.opennxt.model.Build;
import com.opennxt.net.login.LoginHandoffStore;
import com.opennxt.net.login.LoginRSAHeader;
import java.net.InetSocketAddress;

/** No listening socket: proves two accounts sharing an address cannot inherit each other's handoff. */
public final class LanHandoffAcceptance {
    private static void check(boolean value){if(!value)throw new AssertionError();}
    public static void main(String[] args){
        LoginHandoffStore store=LoginHandoffStore.INSTANCE;
        InetSocketAddress host=new InetSocketAddress("192.168.1.2",9000),other=new InetSocketAddress("192.168.1.3",9001);
        int[] a={1,2,3,4},b={5,6,7,8},newKeys={9,10,11,12};
        store.rememberGuest(host,new Build(950,1),"guestalpha","alpha-secret",a,new byte[]{1});
        store.rememberGuest(host,new Build(950,1),"guestbeta","beta-secret",b,new byte[]{2});
        check(store.recall(host)==null);
        check(store.recall(other,new LoginRSAHeader.Reconnecting(newKeys,100,a))==null);
        check(store.recall(host,new LoginRSAHeader.Reconnecting(newKeys,100,new int[]{3,2,1,0}))==null);
        LoginHandoffStore.LobbySnapshot result=store.recall(host,new LoginRSAHeader.Reconnecting(newKeys,100,b));
        check(result!=null&&result.getUsername().equals("guestbeta")&&result.getRemaining()[0]==2);
        check(store.recall(host,new LoginRSAHeader.Reconnecting(newKeys,100,b))==null);
        result=store.recall(host,new LoginRSAHeader.Reconnecting(newKeys,100,a));
        check(result!=null&&result.getUsername().equals("guestalpha"));
        try{store.rememberGuest(host,new Build(950,1),"guestalpha","secret",new int[4],new byte[0]);throw new AssertionError();}
        catch(IllegalArgumentException expected){}
        System.out.println("PASS: remote host-only recall denied; account-key isolation, peer binding, unknown token denial and single-use handoff");
    }
}
