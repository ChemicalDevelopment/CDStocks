/* StockCommand.java - handles the `/stock` command and sub-commands */

package us.chemicaldevelopment.cdstocks;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.bukkit.ChatColor;

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
                matches.add("buy");
                matches.add("sell");
                return matches;

            } else if (args.length == 1) {
                // they've typed in 1 argument (i.e. the subcommand)
                List<String> subcmds = new ArrayList<>();
                subcmds.add("help");
                subcmds.add("info");
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
                    matches.add("iron");
                    matches.add("gold");
                    return matches;
                }

            }

        }/*
        // not our tab completion
        return null;
        //create new array
        final List<String> subcommands = new ArrayList<>("sib");

        // copy matches of first argument from list (ex: if first arg is 'm' will return just 'minecraft')
        //sort the list
        Collections.sort(completions);
        return completions;*/
        return null;
    }

    // when a command is issued
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmd_name = cmd.getName().toLowerCase();
        if (cmd_name.equals("stock")) {
            // we are given a valid command

            if (args.length > 0) {
                // we have a sub command
                String sub_cmd = args[0].toLowerCase();
                if (sub_cmd.equals("info")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Yeah heres some info"));
                    return true;
                } else if (sub_cmd.equals("help")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + "Usage: &6/stock [help|list|buy|sell]"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/stock help"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/stock list"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/stock buy [stockName] [num]"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/stock sell [stockName] [num]"));

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
