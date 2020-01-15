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
        // In the case of setPrice being called twice simultaneously, the synchronized block forces only one setPrice() call at a time.
        // Otherwise could get a stale price (no longer accurate);
        synchronized (this.order)
        {
            double x = RANDOM_NUM_GENERATOR.nextDouble();//used to determine -+ from Initial price

            double updatePrice;
            if (x>0 && x<0.5){//50% chance of adding or subtracting form initial market price
                updatePrice = this.order.getInstrument().getUnitPrice()+ 0.01*this.order.getInstrument().getUnitPrice()*RANDOM_NUM_GENERATOR.nextDouble(); // adds 0-1% to the initial price to make fill price
                updatePrice = Math.round(updatePrice*100.0)/100.0;
            }
            else{
                updatePrice = this.order.getInstrument().getUnitPrice()+ 0.01*this.order.getInstrument().getUnitPrice()*RANDOM_NUM_GENERATOR.nextDouble(); // subs 0-1% to the initial price to make fill price
                updatePrice = Math.round(updatePrice*100.0)/100.0;
            }
//            this.order.unitPrice = 199 * RANDOM_NUM_GENERATOR.nextDouble();
            this.order.getInstrument().setUnitPrice(updatePrice);
            this.order.unitPrice = updatePrice; //-- dont need this atm as we dont need to chsnge the order prices
        }
    }
    private void updateInstrumentPrices(){// intention is to update all instruments repeatedly
        Instrument[] instruments = SampleRouter.getINSTRUMENTS();
        for (int i = 0; i<instruments.length;i++){
            double x = RANDOM_NUM_GENERATOR.nextDouble();//used to determine -+ from Initial price
            double updatePrice;
            if (x >= 0 && x <= 0.5){//50% chance of adding or subtracting form initial market price
                updatePrice = instruments[i].getUnitPrice() + 0.01 * instruments[i].getUnitPrice() * RANDOM_NUM_GENERATOR.nextDouble(); // adds 0-1% to the initial price to make fill price
                updatePrice = Math.round(updatePrice*100.0)/100.0;
            }
            else{
                updatePrice = instruments[i].getUnitPrice() - 0.01 * instruments[i].getUnitPrice() * RANDOM_NUM_GENERATOR.nextDouble(); // subs 0-1% to the initial price to make fill price
                updatePrice = Math.round(updatePrice*100.0)/100.0;
            }
            instruments[i].setUnitPrice(updatePrice);
        }
    }

    @Override
    public void run()
    {
        // Old set price method!
        System.out.println("SampleLiveMarketData run method called.");
        if(this.order == null)
        {
            //setPrice();
        }
        else
        {
            System.out.println("Cannot set price, no order provided! (null)");
        }
//        while (true){// TODO Want to update live market continuously ??????????
//            if (SampleRouter.getINSTRUMENTS()!=null){
//                updateInstrumentPrices();
//                System.out.println("Updating Instrument Prices");
//            }
//        }

    }

    @Override
    public void setPrice(Order o)
    {
        this.order = o;
        setPrice();
    }

}
