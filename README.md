
# CDStocks

A stocks plugin for minecraft where values of commodities can be bought and sold for the current price.

## Building

To build, simply copy in a `spigot.jar` file into the root of the project, then `mvn`. It should build into `target`

## Algo

The algorithm behind the price of an individual item is calculately thusly:

Each stock has an associated item ID (for example, an iron ingot)
and parameters for minimum, maximum, and starting price per item `min=25,max=400,start=100` for iron

They also have a randomness value (TODO)


