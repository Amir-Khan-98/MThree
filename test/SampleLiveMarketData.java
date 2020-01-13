import java.util.Random;

import LiveMarketData.LiveMarketData;
import OrderManager.Order;
import Ref.Instrument;

// TODO this should really be in its own thread
// I think just make the class extend thread or implement runnable and then put setPrice logic in the run method.
public class SampleLiveMarketData implements LiveMarketData
{

    //First Commit of the Branch
    // Second commit of the branch
    private static final Random RANDOM_NUM_GENERATOR = new Random();

    public void setPrice(Order o)
    {
        o.initialMarketPrice = 199 * RANDOM_NUM_GENERATOR.nextDouble();
    }
}
