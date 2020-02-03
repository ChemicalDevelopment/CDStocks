/* CDStocks.java - main file for the ChemicalDevelopment stocks plugin for minecraft



*/

package us.chemicaldevelopment.cdstocks;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Material;

import java.util.List;
import java.util.logging.Logger;
import java.text.DecimalFormat;
import java.util.ArrayList;

import net.milkbowl.vault.economy.Economy;


// the main class 
public class CDStocks extends JavaPlugin {

    // our YAML config
    FileConfiguration config = getConfig();

    // minecraft logger
    private static final Logger log = Logger.getLogger("Minecraft");

    // the economy
    static Economy econ = null;

    // a list of all the stocks
    List<Stock> stocks;

    // decimal formatting for currency
    DecimalFormat df = new DecimalFormat("#.00"); 

    // tax rate percentage for buy transactions
    double taxRateBuy;

    // flat tax amount for buy transactions
    double taxFlatBuy;

    // tax rate percentage for sell transactions
    double taxRateSell;

    // flat tax amount for sell transactions
    double taxFlatSell;


    // get the prefix string
    public String getPrefix() {
        return config.getString("prefix");
    }


    // set up the vault economy
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    
    // when the plugin is enabled
    @Override
    public void onEnable() {

        /* set up the configuration options */

        config.addDefault("prefix", "&7[&a&lCD&r&eStocks&7] &l»&r&9 ");
        config.addDefault("taxRateBuy", 0.058);
        config.addDefault("taxFlatBuy", 40);

        config.addDefault("taxRateSell", 0.025);
        config.addDefault("taxFlatSell", 20);

        /* in the config, the YAML should be like:
            STOCK_NAME:
                material: STOCK_MATERIAL
                price: 
                    current: N
                    default: N
                    min: N
                    max: N
                    volatility: N
                    randomness: N
        */

        config.addDefault("stocks.iron.material", Material.IRON_INGOT.toString());
        config.addDefault("stocks.iron.price.current", 100);
        config.addDefault("stocks.iron.price.default", 100);
        config.addDefault("stocks.iron.price.min", 25);
        config.addDefault("stocks.iron.price.max", 400);
        config.addDefault("stocks.iron.volatility", 2.1f);
        config.addDefault("stocks.iron.randomness", 1.5f);

        config.addDefault("stocks.gold.material", Material.GOLD_INGOT.toString());
        config.addDefault("stocks.gold.price.current", 500);
        config.addDefault("stocks.gold.price.default", 500);
        config.addDefault("stocks.gold.price.min", 10);
        config.addDefault("stocks.gold.price.max", 7500);
        config.addDefault("stocks.gold.volatility", 180.0f);
        config.addDefault("stocks.gold.randomness", 20.0f);

        config.addDefault("stocks.diamond.material", Material.DIAMOND.toString());
        config.addDefault("stocks.diamond.price.current", 3200);
        config.addDefault("stocks.diamond.price.default", 3200);
        config.addDefault("stocks.diamond.price.min", 1200);
        config.addDefault("stocks.diamond.price.max", 6000);
        config.addDefault("stocks.diamond.volatility", 30.0f);
        config.addDefault("stocks.diamond.randomness", 8.0f);



        // replace all empty values with defaults
        config.options().copyDefaults(true);
        saveConfig();

        /* fill in variables from config */
        taxRateBuy = config.getDouble("taxRateBuy");
        taxFlatBuy = config.getDouble("taxFlatBuy");
        taxRateSell = config.getDouble("taxRateSell");
        taxFlatSell = config.getDouble("taxFlatSell");


        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        stocks = new ArrayList<>();

        // iterate through all the stocks
        for (String key : config.getConfigurationSection("stocks").getKeys(false)) {
            stocks.add(new Stock(
                key,
                Material.getMaterial(config.getString("stocks." + key + ".material")),
                config.getDouble("stocks." + key + ".price.current"),
                config.getDouble("stocks." + key + ".price.default"),
                config.getDouble("stocks." + key + ".price.min"),
                config.getDouble("stocks." + key + ".price.max"),
                config.getDouble("stocks." + key + ".volatility"),
                config.getDouble("stocks." + key + ".randomness")
            ));
        }

        //stocks.add(new Stock("iron", Material.IRON_INGOT, 100));

        /* set up the '/stock' command */
        StockCommand cmd0 = new StockCommand(this);
        getCommand("stock").setExecutor(cmd0);
        getCommand("stock").setTabCompleter(cmd0);
        
        // run stock update
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Stock stock : stocks) {
                    stock.runUpdate(0);
                }
            }
        }, 0L, 20L);


    }

    // when the plugin is disabled
    @Override
    public void onDisable() {
        // Don't log disabling, Spigot does that for you automatically!
    }

}
