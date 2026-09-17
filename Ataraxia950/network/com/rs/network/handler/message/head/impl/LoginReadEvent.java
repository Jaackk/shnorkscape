package com.rs.network.handler.message.head.impl;

import com.rs.network.codec.ResultMessage;
import com.rs.network.handler.message.head.ReadMessageEvent;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 2, 2018.
 */
public class LoginReadEvent extends ReadMessageEvent {

    private final String username;
    private final String password;
    private final int[] seeds;
    private final String ipaddress;
    private final String mac;
    private final int displayMode;
    private final boolean usingNXT;
    private final ResultMessage result;

    public LoginReadEvent(ResultMessage result) {
        this(null, null, null, null, null, -1, false, result);
    }

    public LoginReadEvent(String username, String password, int[] seeds, String ipaddress, String mac, int displayMode, boolean usingNXT, ResultMessage result) {
        this.username = username;
        this.password = password;
        this.seeds = seeds;
        this.ipaddress = ipaddress;
        this.mac = mac;
        this.displayMode = displayMode;
        this.usingNXT = usingNXT;
        this.result = result;
    }

    public boolean respondQuickly() {
        return (username == null && password == null && seeds == null && ipaddress == null && mac == null && displayMode == -1 && result != null);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int[] getSeeds() {
        return seeds;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public String getMac() {
        return mac;
    }

    public int getDisplayMode() {
        return displayMode;
    }

    public boolean isUsingNxt() {
        return usingNXT;
    }

    public ResultMessage getResult() {
        return result;
    }

}
