/* StockCommand.java - handles the `/stock` command and sub-commands */

package us.chemicaldevelopment.cdstocks;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;


public class StockCommand implements CommandExecutor, TabCompleter {
    CDStocks plugin;

    public StockCommand(CDStocks plugin) {
        this.plugin = plugin;
    }

    // when a tab completion is requested
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().toLowerCase().equals("stock")) {
            if (args.length == 0) {
                List<String> matches = new ArrayList<>();
                matches.add("help");
                matches.add("info");
                matches.add("list");
                matches.add("buy");
                matches.add("sell");
                return matches;

            } else if (args.length == 1) {
                // they've typed in 1 argument (i.e. the subcommand)
                List<String> subcmds = new ArrayList<>();
                subcmds.add("help");
                subcmds.add("info");
                subcmds.add("list");
                subcmds.add("buy");
                subcmds.add("sell");

                final List<String> completions = new ArrayList<>();

                // else, search for partial matches
                StringUtil.copyPartialMatches(args[0], subcmds, completions);
                Collections.sort(completions);

                return completions;

            } else if (args.length == 2) {
                // they have partially started typing the stock type, if it is a valid sub command

                String sub_cmd = args[0].toLowerCase();

                
                if (sub_cmd.equals("buy") || sub_cmd.equals("sell")) {
                    // give suggestions of stocks
                    List<String> matches = new ArrayList<>();
                    for (Stock stock : plugin.stocks) {
                        matches.add(stock.name);
                    }
                    return matches;
                }

            }

        }
        // doesn't match our arguments, so don't return a tab completion
        return null;
    }

    // when a command is issued
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmd_name = cmd.getName().toLowerCase();
        if (cmd_name.equals("stock")) {
            // we are given a valid command to liston to
            if (args.length > 0) {
                // we have a sub command
                String sub_cmd = args[0].toLowerCase();
                if (sub_cmd.equals("info")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + 
                    " &9Buy tax: &l&c$" + plugin.df.format(plugin.taxFlatBuy) + "&7+&c" + plugin.df.format(plugin.taxRateBuy * 100) + "%" + " &9Sell tax: &l&c$" + plugin.df.format(plugin.taxFlatSell) + "&7+&c" + plugin.df.format(plugin.taxRateSell * 100) + "%"));

                    return true;
                } else if (sub_cmd.equals("list")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "List of current stocks:"));
                    // go through all the stocks
                    for (Stock stock : plugin.stocks) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        "  &6" + stock.name + ": &a$" + plugin.df.format(stock.price) + " &9(min: &a$" + plugin.df.format(stock.minPrice) + "&9, max: &a$" + plugin.df.format(stock.maxPrice) + "&9, item: &6" +  stock.mat.toString() + "&9)"));
                    }

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    "&7(buy tax: &l&c$" + plugin.df.format(plugin.taxFlatBuy) + "&7+&c" + (plugin.df.format(plugin.taxRateBuy * 100)) + "%)"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    "&7(sell tax: &l&c$" + plugin.df.format(plugin.taxFlatSell) + "&7+&c" + (plugin.df.format(plugin.taxRateSell * 100)) + "%)"));

                    return true;

                } else if (sub_cmd.equals("buy")) {

                    /* buying stocks */

                    if (args.length < 2) {
                        // print usage message
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Usage: &6/stock buy [stock] [num=1]"));
                        // list of stocks
                        String ls = "&9Stocks: ";

                        int i;
                        for (i = 0; i < plugin.stocks.size(); ++i) {
                            if (i != 0) ls += ", ";
                            ls += "&6" + plugin.stocks.get(i).name + "&9(&a$" + plugin.df.format(plugin.stocks.get(i).price) + "&9)";
                        }

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ls));
                        return true;
                    } else {
                        // get the stock name/
                        String stockname = args[1].toLowerCase();
                        int idx = -1;
                        for (int i = 0; i < plugin.stocks.size(); ++i) {
                            if (stockname.equalsIgnoreCase(plugin.stocks.get(i).name)) {
                                idx = i;
                                break;
                            }
                        }

                        if (idx < 0) {
                            // not found
                            // print usage message
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Unknown stock: &6" + stockname));
                            // list of stocks
                            String ls = "&9Stocks: ";

                            int i;
                            for (i = 0; i < plugin.stocks.size(); ++i) {
                                if (i != 0) ls += ", ";
                                ls += "&6" + plugin.stocks.get(i).name + "&9(&a$" + plugin.df.format(plugin.stocks.get(i).price) + "&9)";
                            }

                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ls));

                        } else {
                            // found, attempt to buy

                            // quantity
                            int quant = 1;

                            if (args.length >= 3) quant = Integer.parseInt(args[2]);

                            if (quant < 1) quant = 1;

                            if (sender instanceof Player) {
                                Player player = (Player)sender;

                                Stock stock = plugin.stocks.get(idx);

                                double price = 0, bal = 0;


                                double sum_price = 0.0;

                                int i;
                                // buy them iteratively
                                for (i = 0; i < quant; ++i) {

                                    // calculate price
                                    price = stock.price;
                                    
                                    // calculate taxes
                                    price = price * (1 + plugin.taxRateBuy);

                                    // add just 1 instance of tax
                                    if (i == 0) price += plugin.taxFlatBuy;

                                    bal = CDStocks.econ.getBalance(player);

                                    if (bal < price) {
                                        // not enough money
                                        break;
    
                                    } else {
                                        // complete purchase

                                        // add item to inventory
                                        player.getInventory().addItem(new ItemStack(stock.mat, 1));

                                        // change balance
                                        CDStocks.econ.withdrawPlayer((OfflinePlayer)player, price);

                                        // record it with the stock
                                        stock.recordBuy();
                                        
                                        // keep track of it
                                        sum_price += price;

                                    }
                                }
                                
                                if (i < quant) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Not enough money! (need &a$" + plugin.df.format(price) + "&9). You ended up buying &a" + (i) + " &6" + stock.name + "&9 for &a$" + plugin.df.format(sum_price)));
                                } else {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Bought &6" + quant + " " + stock.name + "&9 for &a$" + plugin.df.format(sum_price) + "&9"));
                                }

                            } else {

                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "You can only buy stocks as a player!"));
                            }

                        }


                        return true;
                    }
                } else if (sub_cmd.equals("sell")) {

                    /* sellin stocks */

                    if (args.length < 2) {
                        // print usage message
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Usage: &6/stock sell [stock] [num=1]"));
                        // list of stocks
                        String ls = "&9Stocks: ";

                        int i;
                        for (i = 0; i < plugin.stocks.size(); ++i) {
                            if (i != 0) ls += ", ";
                            ls += "&6" + plugin.stocks.get(i).name + "&9(&a$" + plugin.df.format(plugin.stocks.get(i).price) + "&9)";
                        }

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ls));
                        return true;
                    } else {
                        // get the stock name/
                        String stockname = args[1].toLowerCase();
                        int idx = -1;
                        for (int i = 0; i < plugin.stocks.size(); ++i) {
                            if (stockname.equalsIgnoreCase(plugin.stocks.get(i).name)) {
                                idx = i;
                                break;
                            }
                        }

                        if (idx < 0) {
                            // not found
                            // print usage message
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Unknown stock: &6" + stockname));
                            // list of stocks
                            String ls = "&9Stocks: ";

                            int i;
                            for (i = 0; i < plugin.stocks.size(); ++i) {
                                if (i != 0) ls += ", ";
                                ls += "&6" + plugin.stocks.get(i).name + "&9(&a$" + plugin.df.format(plugin.stocks.get(i).price) + "&9)";
                            }

                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ls));

                        } else {
                            // found, attempt to sell

                            // quantity
                            int quant = 1;

                            if (args.length >= 3) quant = Integer.parseInt(args[2]);

                            if (quant < 1) quant = 1;
                            //if (quant > 64) quant = 64;

                            if (sender instanceof Player) {
                                Player player = (Player)sender;

                                Stock stock = plugin.stocks.get(idx);

                                // count up number
                                int quant_have = 0;
 
                                for (ItemStack stack : player.getInventory().getContents()) {
                                    if (stack != null && stack.getType() == stock.mat) {
                                        quant_have += stack.getAmount();
                                    }
                                }

                                if (quant_have < quant) {
                                    // not enough items

                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Not enough stock! (need &a" + quant + " &6" + stock.mat.toString() + "&9, but just have &c" + quant_have + "&9)"));

                                } else {
                                    
                                    //player.getInventory().addItem(new ItemStack(stock.mat, quant));
                                    // remove this many
                                    int num_left = quant;
                                    for(int i = 0; i < player.getInventory().getSize() && num_left > 0; i++){
                                        ItemStack itm = player.getInventory().getItem(i);
                                        if (itm != null && itm.getType().equals(stock.mat)) {
                                            System.out.println("Nee dto sell " + num_left);
                                            if (itm.getAmount() > num_left) {
                                                // we don't need to take it all away
                                                itm.setAmount(itm.getAmount() - num_left);
                                                player.getInventory().setItem(i, itm);
                                                num_left = 0;
                                                player.updateInventory();
                                                break;

                                            } else {
                                                // take it all away
                                                player.getInventory().setItem(i, null);
                                                num_left -= itm.getAmount();
                                                player.updateInventory();

                                            }
                                        }
                                    }
                                    
                                    double sum_price = 0.0;
                                    for (int i = 0; i < quant; ++i) {

                                        // actually do it
                                        stock.recordSell();

                                        // add it to how much money 
                                        sum_price += stock.price;

                                    }

                                    
                                    // apply taxes
                                    sum_price = (1 - plugin.taxRateSell) * sum_price - plugin.taxFlatSell;

                                    // ensure a positive amount
                                    if (sum_price < 0) sum_price = 0;


                                    CDStocks.econ.depositPlayer((OfflinePlayer)player, sum_price);

                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Sold &6" + quant + " " + stock.name + "&9 for &a$" + plugin.df.format(sum_price) + "&9"));

                                }


                            } else {

                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "You can only buy stocks as a player!"));
                            }

                        }


                        return true;
                    }

                } else if (sub_cmd.equals("help")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Usage: &6/stock [help|list|buy|sell]"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/stock help"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/stock list"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/stock buy [stockName] [num=1]"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/stock sell [stockName] [num=1]"));

                    return true;
                }

            }

            // if it wasn't handled yet, print a usage message
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Usage: /stock [help|list|buy|sell]"));

            // tell them we handled it
            return true;
        } else {
            // we did not handle the command
            return false;
        }
    }


}
