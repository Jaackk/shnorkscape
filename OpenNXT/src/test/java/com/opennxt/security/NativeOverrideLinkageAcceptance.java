package com.opennxt.security;

import com.opennxt.OpenNXT;

/** Run with production overrides FIRST, followed by the freshly built full JAR. */
public final class NativeOverrideLinkageAcceptance {
    public static void main(String[] args) throws Exception {
        if (!OpenNXT.INSTANCE.offlineModeEnabled$OpenNXT("1"))
            throw new AssertionError("Offline admission contract changed");
        Object result=OpenNXT.class.getDeclaredMethod("offlineModeEnabled$OpenNXT$default",
                OpenNXT.class,String.class,int.class,Object.class)
                .invoke(null,OpenNXT.INSTANCE,"true",0,null);
        if (!Boolean.TRUE.equals(result)) throw new AssertionError("Default bridge failed");
        System.out.println("PASS: production overrides preserve full-JAR Kotlin module ABI and offline default bridge");
    }
}
