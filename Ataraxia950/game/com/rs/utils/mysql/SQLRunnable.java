package com.rs.utils.mysql;

import com.rs.Settings;

public abstract class SQLRunnable implements Runnable {

    public SQLRunnable() {
    }

    public abstract void execute(final DatabaseCredential auth);

    public void prepare() { if (Settings.SQL_ENABLED) Pool.submit(this); }

    @Override
    public void run() {
        prepare();
    }
}

