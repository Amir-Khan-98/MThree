package OrderManager;

public class OrderIDTracker {

    private volatile static OrderIDTracker orderIDTracker;
    private int counter = 0;

    private OrderIDTracker(){

    }

    public synchronized int getNewID(){
        return counter++;
    }

    public static OrderIDTracker getInstance(){
        if(orderIDTracker == null){
            orderIDTracker = new OrderIDTracker();
        }
        return orderIDTracker;
    }
}
