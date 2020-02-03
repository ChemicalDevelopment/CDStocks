
# CDStocks

A stocks plugin for minecraft where values of commodities can be bought and sold for the current price.

## Building

`CDStocks` depends on both `spigot` & `Vault`

First, copy a `spigot.jar` and a `Vault.jar` into the root directory (you can copy these from your server folder)

Then, run `mvn build`. It should build a file in the `target` directory, `CDStocks-$VERSION.jar`


## Internals

In the `CDStocks/plugin.yml` file, 




Each stock has an associated item ID (for example, an iron ingot)
and parameters for minimum, maximum, and starting price per item `min=25,max=400,start=100` for iron

They also have a randomness value (TODO)


