/* CDStocks.java - main file for the ChemicalDevelopment stocks plugin for minecraft



*/

package us.chemicaldevelopment.cdstocks;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CDStocks extends JavaPlugin {

    // our YAML config
    FileConfiguration config = getConfig();

    // get the prefix string
    public String getPrefix() {
        return config.getString("prefix");
    }


    // when the plugin is enabled
    @Override
    public void onEnable() {
        // Don't log enabling, Spigot does that for you automatically!

        config.addDefault("prefix", "&7[&a&lCD&r&eStocks&7] &l»&r&9 ");

        config.options().copyDefaults(true);
        saveConfig();

        // add a 'stock' command handler
        StockCommand cmd0 = new StockCommand(this);
        getCommand("stock").setExecutor(cmd0);
        getCommand("stock").setTabCompleter(cmd0);


    }

    // when the plugin is disabled
    @Override
    public void onDisable() {
        // Don't log disabling, Spigot does that for you automatically!
    }

}
