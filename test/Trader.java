import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import javax.net.ServerSocketFactory;

import OrderManager.Order;
import TradeScreen.TradeScreen;

@SuppressWarnings("InfiniteLoopStatement")
public class Trader extends Thread implements TradeScreen
{
    private HashMap<Integer, Order> orders = new HashMap<Integer, Order>();
    private static Socket omConn;
    private final int port;

    Trader(String name, int port)
    {
        this.setName(name);
        this.port = port;
    }

    ObjectInputStream is;
    ObjectOutputStream os;

    public void run()
    {
        // OM will connect to us
        try
        {
            omConn = ServerSocketFactory.getDefault().createServerSocket(port).accept();

            // is=new ObjectInputStream( omConn.getInputStream());
            InputStream s = omConn.getInputStream(); // if i try to create an objectinputstream before we have data it will block
            while (true)
            {
                // s.available returns the estimated number of bytes to be read.
                if (0 < s.available())
                {
                    is = new ObjectInputStream(s);

                    // Reads in the message from the stream.
                    api method = (api) is.readObject();
                    System.out.println(Thread.currentThread().getName() + " calling: " + method);
                    // Depending on the stream input, it will perform one of the following methods.
                    //noinspection DuplicateBranchesInSwitch
                    switch (method)
                    {
                        case newOrder:
                            int orderid = is.readInt();
                            System.out.println("newOrder called with orderId: " + orderid);
                            Order tempOrder =  (Order) is.readObject();
                            newOrder(tempOrder.getOrderId(), tempOrder);
                            break;
                        case price:
                            price(is.readInt(), (Order) is.readObject());
                            break;
                        case cross:
                            is.readInt();
                            is.readObject();
                            // We need to create the cross(is.readInt(), (Order) is.readObject()) method to be used here.
                            break; // TODO
                        case fill:
                            System.out.println("We are ATTTTTTTTEMPTING TO FILL");
                            fill(is.readInt(), (Order) is.readObject());
                            break; // TODO
                    }
                }
                // If there is no bytes in the stream, it stops for 1 second before going to the start of the while loop.
                else
                {
                    System.out.println("Trader Waiting for data to be available - sleep 1s");
                    Thread.sleep(100);
                }
            }
        }
        catch (IOException | ClassNotFoundException | InterruptedException e)
        {
            if(e.getClass() == IOException.class)
                System.out.println("IOException occurred, message: "+e.getMessage());
            else if(e.getClass() == ClassNotFoundException.class)
                System.out.println("ClassNotFoundException occurred, message: "+e.getMessage());
            else if(e.getClass() == InterruptedException.class)
                System.out.println("InterruptedException occurred, message: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void fill(int orderId, Order o){
        //TODO this is a test to see if fill is working
        //We are filling the order fully
        //Later, we can implement portions of orders filled at once

        o.createFill(o.sizeRemaining(), o.getInstrument().getUnitPrice());

        try {
            os = new ObjectOutputStream(omConn.getOutputStream());
            os.writeObject("orderFilled");
            os.writeInt(orderId);
            os.writeObject(o);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    @Override
    public void newOrder(int id, Order order) throws IOException, InterruptedException
    {
        // TODO the order should go in a visual grid, but not needed for test purposes
        Thread.sleep(2134);
        // Put the order sent in the method with its ID into the map of orders.
        orders.put(id, order);
        acceptOrder(id);
    }

    @Override
    public void acceptOrder(int id) throws IOException
    {
        os = new ObjectOutputStream(omConn.getOutputStream());
        os.writeObject("acceptOrder");
        os.writeInt(id);

        os.flush();
    }

    @Override
    public void sliceOrder(int id, int sliceSize) throws IOException
    {
        os = new ObjectOutputStream(omConn.getOutputStream());
        os.writeObject("sliceOrder");
        os.writeInt(id);
        os.writeInt(sliceSize);

        // .flush() writes all the bytes in the os buffer to their destination, presumably clearing the buffer.
        os.flush();
    }

    @Override
    public void price(int orderId, Order o) throws InterruptedException, IOException {

        System.out.println("Price called in Trader ("+this.getName()+") with orderId: " + orderId + ", Order.toString(): " + o.toString());
        // TODO should update the trade screen
        // TradeScreen.api....

        System.out.println("\nThe whole orders map is here: " + orders+"\n");

        Thread.sleep(1000);
        if(orders.containsKey(o.getOrderId())) {
            // TODO this is to prevent order.get returning null, but that might not be the actual problem.
            sliceOrder(orderId, orders.get(o.getOrderId()).sizeRemaining() / 2);
        }
        else {
            System.out.println("Order with id: "+orderId+" doesnt exist in Trader: "+this.getName());
        }
    }
}
