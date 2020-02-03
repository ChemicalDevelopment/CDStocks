/* CDStocks.java - main file for the ChemicalDevelopment stocks plugin for minecraft

@author: Cade Brown <brown.cade@gmail.com>

*/

package us.chemicaldevelopment.cdstocks;

// standard java API
import java.util.List;
import java.util.logging.Logger;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

// bukkit API
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Material;

// vault/economy API
import net.milkbowl.vault.economy.Economy;

public class CDStocks extends JavaPlugin {

    // our YAML config
    FileConfiguration config = getConfig();

    // values.yaml file, for storing the current value
    FileConfiguration values_yaml;
    
    // our economy
    private Economy econ = null;

    // minecraft logger
    private static final Logger log = Logger.getLogger("Minecraft");

    // decimal formatting for currency/rates
    DecimalFormat df = new DecimalFormat("#.00"); 


    /* config constants */

    // the plugin's prefix
    String prefix = "";

    // flat tax amount for buy transactions
    double taxFlatBuy;
    // tax rate percentage for buy transactions
    double taxRateBuy;

    // flat tax amount for sell transactions
    double taxFlatSell;
    // tax rate percentage for sell transactions
    double taxRateSell;

    // a list of all the stocks
    List<Stock> stocks;

    // try and set up the vault economy, returning whether it was successful
    private boolean setupEconomy() {
        // attempt to read a literal `Vault` plugin
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;

        // else, check if the server has an economy provider
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;

        // try and get the provider
        econ = rsp.getProvider();
        return econ != null;
    }
    
    @Override
    public void onEnable() {
        /* called when Bukkit is trying to enable our plugin */

        // copy in our example config.yml if one doesn't exist yet
        saveResource("config.yml", false);
        saveResource("values.yml", false);

        /* attempt to set up our economy */
        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        /* now, read in constants */
        prefix = config.getString("prefix");

        double updatePeriod = config.getDouble("updatePeriod");

        taxFlatBuy = config.getDouble("taxFlatBuy");
        taxRateBuy = config.getDouble("taxRateBuy");

        taxFlatSell = config.getDouble("taxFlatSell");
        taxRateSell = config.getDouble("taxRateSell");

        /* read in current values */
        values_yaml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "values.yaml"));

        /* populate the list of stocks */
        stocks = new ArrayList<>();

        // iterate through all the stocks we find in the config
        for (String key : config.getConfigurationSection("stocks").getKeys(false)) {

            // default price
            double def_price = config.getDouble("stocks." + key + ".price.default");
            stocks.add(new Stock(
                key,
                Material.getMaterial(config.getString("stocks." + key + ".material")),
                values_yaml.getDouble(key, def_price),
                def_price,
                config.getDouble("stocks." + key + ".price.min"),
                config.getDouble("stocks." + key + ".price.max"),
                config.getDouble("stocks." + key + ".volatility"),
                config.getDouble("stocks." + key + ".randomness")
            ));
        }

        /* set up the '/stock' command */
        StockCommand cmd0 = new StockCommand(this);

        getCommand("stock").setExecutor(cmd0);
        getCommand("stock").setTabCompleter(cmd0);
        
        // run stock update, once 
        BukkitScheduler scheduler = getServer().getScheduler();

        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Stock stock : stocks) {
                    stock.runUpdate(0);
                    values_yaml.set(stock.name, stock.price);
                }
            }
        }, 0L, (long)(20.0 * updatePeriod));

    }

    // when the plugin is disabled
    @Override
    public void onDisable() {
        // Don't log disabling, Spigot does that for you automatically!

        // save our values
        for (Stock stock : stocks) {
            values_yaml.set(stock.name, stock.price);
        }

    }

}
