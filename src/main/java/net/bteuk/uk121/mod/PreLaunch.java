package net.bteuk.uk121.mod;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class PreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        new ConfigVariables();
    }
}
