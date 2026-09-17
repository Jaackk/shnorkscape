package com.rs.utils.mysql;

import com.rs.Settings;
import com.rs.utils.Logger;

public class SQLThread extends Thread {

    public static Pool pool;
    public static volatile boolean ENABLED = true;

    @Override
    public void run() {
        try {

            Pool.preload();
            pool = new Pool();

            while (ENABLED) {
                QueryExecutor.process();
                sleep(250);
            }

        } catch (final Exception e) {
            if(Settings.TEST_SERVER_MODE) {
                Logger.getGlobal().warn("Failed to initialize SQL thread.");
            } else {
                Logger.getGlobal().catching(e);
            }
        }
    }

}
