package me.nathan3882.asbulkmutate.executors;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class AsbmCommandExecutor implements CommandExecutor {

    private static final String AUTOSELL_CONFIGURATION_PATH = "plugins/AutoSell/config.yml";

    private final Plugin plugin;

    public AsbmCommandExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Command for /asbm bulkadd <material> <start-price> <percentage-increase-per-configuration-section>
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if command handled else false.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("asbm")) {
            if (!sender.hasPermission("asbm.issue")) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have have permission to use this command.");
                return true;
            }
            if (args.length < 4) {
                sendUsage(sender);
            } else if (args.length == 4 && args[0].equalsIgnoreCase("bulkadd")) {
                String name = args[1].toUpperCase();

                Material material;
                try {
                    material = Material.valueOf(name);
                } catch (IllegalArgumentException e) {
                    logStacktrace(e);
                    sender.sendMessage(args[1] + " doesn't match a " + plugin.getServer().getVersion() + " material.");
                    return true;
                }

                double basePrice;
                int percentageIncreasePerConfigurationSection;
                try {
                    basePrice = Double.valueOf(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(args[2] + " is not a double value.");
                    return true;
                }
                try {
                    percentageIncreasePerConfigurationSection = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(args[3] + " is not an integer value.");

                    return true;
                }

                YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(AUTOSELL_CONFIGURATION_PATH));

                if (!config.isConfigurationSection("shops")) {
                    sender.sendMessage("Your AutoSell config doesn't have a shops section. Please fix!");
                    return true;
                }
                String materialName = material.name();

                int done = 0;
                for (String shopIdentifier : config.getConfigurationSection("shops").getKeys(false)) {
                    String shopListPath = "shops." + shopIdentifier + ".shop_list";

                    Collection<String> items = config.getStringList(shopListPath);
                    if (items.contains(materialName)) {
                        sender.sendMessage(String.format("Update for %s. It already contains a mapping for %s. Skipping!",
                                                         shopIdentifier,
                                                         materialName));
                        continue; // go to next shop
                    }
                    // Doesn't contain this item already. Add it at our new price
                    double newPrice = calculatePrice(basePrice, percentageIncreasePerConfigurationSection, done);

                    String entry = materialName + ";0," + newPrice;
                    items.add(entry);

                    config.set(shopListPath, items);

                    sender.sendMessage(String.format("New price for shop \"%s\"'s %s is %s.", shopIdentifier, materialName, newPrice));
                    done++;
                }
                saveAutosellConfiguration(config);

            } else {
                sendUsage(sender);
            }
            return true;
        }
        return false;
    }

    /**
     * From a base price, calculate a specified shop's price
     *
     * @param basePrice                                 the base price for the first shop
     * @param percentageIncreasePerConfigurationSection what to increase the base price by each new shop traversed.
     * @param progress                                  how many shops we've already traversed and calculated the price for/
     * @return a specified shop's price taking into account how many shops we've already traversed and calculated the price for.
     */
    private double calculatePrice(double basePrice, int percentageIncreasePerConfigurationSection, int progress) {
        double scalar = 1 + ((percentageIncreasePerConfigurationSection * progress) / 100d);
        return basePrice * scalar;
    }

    /**
     * Saves the AutoSell configuration file.
     */
    private void saveAutosellConfiguration(FileConfiguration configurationInMemory) {
        try {
            File file = new File(AUTOSELL_CONFIGURATION_PATH);
            configurationInMemory.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed saving to plugins/AutoSell/config.yml: ");
            logStacktrace(e);
        }
    }

    /**
     * Log an exception without using System.out or printStackTrace. It doesn't work & we must use native Bukkit logger.
     *
     * @param exception the exception to log.
     */
    private void logStacktrace(Throwable exception) {
        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
            Bukkit.getLogger().severe(stackTraceElement.toString());
        }
    }

    /**
     * Send usage of the command to a Command sender
     *
     * @param reciever CommandSender to get the command.
     */
    private void sendUsage(CommandSender reciever) {
        reciever.sendMessage("Usage: /asbm bulkadd <material> <start-price> <percentage-increase-per-configuration-section>");
    }
}
