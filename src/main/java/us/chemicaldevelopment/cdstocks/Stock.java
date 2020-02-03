/* Stock.java - a class that represents a single kind of stock */

package us.chemicaldevelopment.cdstocks;

import org.bukkit.Material;

public class Stock {

    /* meta-data about stock */

    // the name of the stock
    String name;

    // the material that the stock represents
    Material mat;

    /* parameters of the stock */

    // the current price of the stock, in $
    double price;

    // the default price of the item
    double defaultPrice;

    // the hard cap on the prices
    double minPrice, maxPrice;

    // the volatility of the stock
    double volatility;

    // factor from 0-1 of how much the stock's hype decays per second
    // decay 1 should be short term, decay 2 should be long term
    double decay1, decay2;

    // the randomness at each loop
    double randomness;

    /* state variables */

    // the 'hype' (i.e. net volume over time), and a modified version for longer times
    double curHype1, curHype2;

    // last update time
    double last_time;

    // record a purchase of a stock
    void recordBuy() {
        runUpdate(+1);
    }

    // record a single sale
    void recordSell() {
        runUpdate(-1);
    }

    // updates prices
    void runUpdate(int diff) {
        // get current time
        double this_time = (double)System.currentTimeMillis();

        // calculate difference in time
        double dt = (this_time - last_time) / 1000.0;
        if (dt < 0.5) dt = 0.5;

        // this should be ran every time, and since it is called on buy/sell, the additional
        // volume is going to be 1
        curHype1 = Math.pow(decay1, dt + 0.2) * curHype1 + diff;
        curHype2 = Math.pow(decay2, dt + 0.5) * curHype2 + diff;

        if (diff != 0) {
            // someone bought or sold something, so run a random simulation

            /*if (Math.random() < 0.05f) {
                curHype2 += 3;
            }*/
        }


        // amount to add via randomness
        double amt_random = 0.0;

        if (price > defaultPrice) {
            // randomness should tend downwards
            double scl = (price - defaultPrice) / (maxPrice - defaultPrice);
            // intensity of effect
            scl *= 0.7f;
            amt_random = randomness * ((2 - scl) * Math.random() - 1);
        } else {
            // randomness should tend upwards
            double scl = (defaultPrice - price) / (defaultPrice - minPrice);
            // intensity of effect
            scl *= 0.6f;
            amt_random = randomness * (1 - (2 - scl) * Math.random());
        }

        // amount to add to readjust prices
        // adjust by the current hype (which is signed)
        double amt_readj = volatility * (0.15 * Math.atan(curHype1 * 0.16) / (Math.PI / 2) + .32 * Math.atan(curHype2 * 0.023) / (Math.PI / 2));

        // update our price
        price += dt * (amt_random + amt_readj);
        //price += dt * amt_random;       
        // force price cap
        if (price < minPrice) price = minPrice;
        else if (price > maxPrice) price = maxPrice;

        // reset
        this.last_time = this_time;

    }

    Stock(String name, Material mat, double price, double defaultPrice, double minPrice, double maxPrice, double volatility, double randomness) {
        this.name = name;
        this.mat = mat;

        this.price = price;
        this.defaultPrice = defaultPrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.volatility = volatility;
        this.randomness = randomness;

        decay1 = 0.5;
        decay2 = 0.9;

        this.last_time = (double)System.currentTimeMillis();
        this.curHype1 = 0.0;
        this.curHype2 = 0.0;
    }

}

