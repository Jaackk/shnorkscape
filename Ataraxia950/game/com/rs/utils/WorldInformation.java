package com.rs.utils;

public class WorldInformation {

    /**
     * Contains world id.
     */
    private final int id;
    /**
     * Contains country flag ID.
     */
    private final int countryFlagID;
    /**
     * Contains country name.
     */
    private final String countryName;
    /**
     * World location.
     */
    private final int location;
    /**
     * Contains world settings flags.
     */
    private final int flags;
    /**
     * Contains world activity name.
     */
    private final String activity;
    /**
     * Contains world ip.
     */
    private final String ip;


    public WorldInformation(int id, int countryFlag, String countryName, int location, int flags, String activity, String ip) {
        this.id = id;
        this.countryFlagID = countryFlag;
        this.countryName = countryName;
        this.location = location;
        this.flags = flags;
        this.activity = activity;
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public int getCountryFlagID() {
        return countryFlagID;
    }

    public String getCountryName() {
        return countryName;
    }

    public int getLocation() {
        return location;
    }

    public int getFlags() {
        return flags;
    }

    public String getActivity() {
        return activity;
    }

    public String getIp() {
        // return Settings.VPS1_IP;
        return ip;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += id << 2;
        hash += countryFlagID << 5;
        hash += countryName.hashCode() << 9;
        hash += location << 14;
        hash += flags << 17;
        hash += activity.hashCode() << 20;
        hash += getIp().hashCode() << 26;
        return hash;
    }
}
