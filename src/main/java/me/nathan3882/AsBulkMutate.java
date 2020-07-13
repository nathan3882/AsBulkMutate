package me.nathan3882;

import org.bukkit.plugin.java.JavaPlugin;

public class AsBulkMutate extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        getCommand("asbm").setExecutor(this);
    }


    @Override
    public void onDisable() {
        super.onDisable();
    }
}
