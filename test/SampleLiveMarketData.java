import java.util.Random;

import LiveMarketData.LiveMarketData;
import OrderManager.Order;

// TODO this should really be in its own thread (completed it mate)
// I think just make the class extend thread or implement runnable and then put setPrice logic in the run method.
public class SampleLiveMarketData extends Thread implements LiveMarketData
{

    private static final Random RANDOM_NUM_GENERATOR = new Random();
    private Order order;

    public SampleLiveMarketData(Order order)
    {
        System.out.println("SampleLiveMarketData created.");
        this.order = order;
    }

    private synchronized void setPrice()
    {
            double x = RANDOM_NUM_GENERATOR.nextDouble();//used to determine -+ from Initial price

            double updatePrice;
            // adds 0-1% to the initial price to make fill price
            updatePrice = this.order.getInstrument().getUnitPrice()+ 0.01*this.order.getInstrument().getUnitPrice()*RANDOM_NUM_GENERATOR.nextDouble(); // adds 0-1% to the initial price to make fill price
            updatePrice = Math.round(updatePrice*100.0)/100.0;
//            this.order.unitPrice = 199 * RANDOM_NUM_GENERATOR.nextDouble();
            this.order.getInstrument().setUnitPrice(updatePrice);
            //this.order.unitPrice = updatePrice; -- dont need this atm as we dont need to chsnge the order prices
    }

    @Override
    public void run()
    {
        // Old set price method!
        System.out.println("SampleLiveMarketData run method called.");
        if(this.order == null)
        {
            setPrice();
        }
        else
        {
            System.out.println("Cannot set price, no order provided! (null)");
        }

    }

    @Override
    public void setPrice(Order o)
    {
        this.order = o;
        setPrice();
    }

}
