/* CDStocks.java - main file for the ChemicalDevelopment stocks plugin for minecraft
 *
 * @author: Cade Brown <brown.cade@gmail.com>
 */

package us.chemicaldevelopment.cdstocks;

/* Java standard library */
import java.util.List;
import java.util.logging.Logger;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

/* the standard Bukkit API */
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Material;

/* the Vault economy/permissions/chat */
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.chat.Chat;

public class CDStocks extends JavaPlugin {

    // The global economy, found via the server
    // this is used to request balances/perform transactions
    Economy econ = null;

    // The global chat, found via the server
    // This is used for sending chat messages, and getting more information on chat events
    Chat chat = null;

    // the `config.yml` file settings layer
    File config_file = null;
    FileConfiguration config = null;

    // decimal formatting for currency/rates
    DecimalFormat df = new DecimalFormat("#.00");

    // the plugin's prefix, read from the config file
    public static String prefix = "";


    /* CDStocks specific variables */

    // the `values.yml` file that holds current values of stocks
    File values_file = null;
    FileConfiguration values_yml = null;

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

    /* initialize/set up the plugin, returning success, true if it worked, false if it failed */
    private boolean setup() {
        // ensure 'Vault' is loaded on the server
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
        
        /* attempt to ask the server for a service provider for Economy, fail if none is found */
        RegisteredServiceProvider<Economy> rsp_econ = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp_econ == null) return false;
        econ = rsp_econ.getProvider();
        if (econ == null) return false;

        /* attempt to ask the server for a service provider for Chat, fail if none is found */
        RegisteredServiceProvider<Chat> rsp_chat = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp_chat == null) return false;
        chat = rsp_chat.getProvider();
        if (chat == null) return false;

        // everything worked, so return true
        return true;
    }

    /* internal method to run an update method on all the stocks */
    private void updateStocks() {
        for (Stock stock : stocks) {
            stock.runUpdate(0);
        }
    }

    /* internal method to save the stock data */
    private void saveStocks() {
        for (Stock stock : stocks) {
            values_yml.set(stock.name, stock.price);
        }
        try {
            values_yml.save(values_file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /* called when Bukkit is trying to enable our plugin */
    @Override
    public void onEnable() {

        /* 1: Initialize */

        // first, try and set up dependencies/etc
        if (!setup()) {
            this.getLogger().severe("Disabling plugin, because Vault was not found (either Economy, Permission, or Chat is not supported)");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // now, ensure that the `config.yml` and `values.yml` files are both created
        // if they don't exist, copy in our defaults from resources/
        saveResource("config.yml", false);
        saveResource("values.yml", false);

        // now, load in the files as FileConfiguration objects so they can be tweaked, saved, and read
        config_file = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(config_file);
        values_file = new File(getDataFolder(), "values.yml");
        values_yml = YamlConfiguration.loadConfiguration(values_file);

        /* 2: Read in values */

        prefix = config.getString("prefix", "&7[&a&lCD&r&eStocks&7] &l»&r&9 ");

        double updatePeriod = config.getDouble("updatePeriod");

        taxFlatBuy = config.getDouble("taxFlatBuy");
        taxRateBuy = config.getDouble("taxRateBuy");

        taxFlatSell = config.getDouble("taxFlatSell");
        taxRateSell = config.getDouble("taxRateSell");

        // populate the list of stocks
        stocks = new ArrayList<>();

        // iterate through all the stocks we find in the config
        for (String key : config.getConfigurationSection("stocks").getKeys(false)) {
            // default price
            double def_price = config.getDouble("stocks." + key + ".price.default");
            stocks.add(new Stock(
                key,
                Material.getMaterial(config.getString("stocks." + key + ".material")),
                values_yml.getDouble(key, def_price),
                def_price,
                config.getDouble("stocks." + key + ".price.min"),
                config.getDouble("stocks." + key + ".price.max"),
                config.getDouble("stocks." + key + ".volatility"),
                config.getDouble("stocks." + key + ".randomness")
            ));
        }

        /* 3: Handle the '/stock' command */
        StockCommand cmd0 = new StockCommand(this);

        getCommand("stock").setExecutor(cmd0);
        getCommand("stock").setTabCompleter(cmd0);

        /* 4: Create a scheduler to update & save the stock values out to file every `updatePeriod` seconds */
        
        BukkitScheduler scheduler = getServer().getScheduler();

        // schedule until plugin is disabled
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                updateStocks();
                saveStocks();
            }
        // in ticks, so 20 * seconds
        }, 0L, (long)(20.0 * updatePeriod));

    }

    /* called when the plugin is to be disabled */
    @Override
    public void onDisable() {
        // don't update, but save the stocks to the values.yml file
        saveStocks();
    }
}
