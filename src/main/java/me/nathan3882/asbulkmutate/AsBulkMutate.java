package me.nathan3882.asbulkmutate;

import me.nathan3882.asbulkmutate.executors.AsbmCommandExecutor;

import org.bukkit.plugin.java.JavaPlugin;

public class AsBulkMutate extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        getCommand("asbm").setExecutor(new AsbmCommandExecutor(this));
    }


    @Override
    public void onDisable() {
        super.onDisable();
    }
}
