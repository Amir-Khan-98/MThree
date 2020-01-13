import java.util.Random;

import LiveMarketData.LiveMarketData;
import OrderManager.Order;
import Ref.Instrument;

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

    private void setPrice()
    {
        synchronized (this.order)
        {
            this.order.initialMarketPrice = 199 * RANDOM_NUM_GENERATOR.nextDouble();
        }
    }

    @Override
    public void run()
    {
        // Old set price method!
        System.out.println("SampleLiveMarketData run method called.");
        setPrice();
    }

    @Override
    public void setPrice(Order o)
    {
        this.order = o;
        synchronized (this.order)
        {
            setPrice();
        }
    }

}
