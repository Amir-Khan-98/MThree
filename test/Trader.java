import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import javax.net.ServerSocketFactory;

import OrderManager.Order;
import TradeScreen.TradeScreen;

public class Trader extends Thread implements TradeScreen
{
    private HashMap<Integer, Order> orders = new HashMap<Integer, Order>();
    private static Socket omConn;
    private int port;

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
                    is = new ObjectInputStream(s);  // TODO check if we need to create each time. this will block if no data, but maybe we can still try to create it once instead of repeatedly

                    // Reads in the message from the stream.
                    api method = (api) is.readObject();
                    System.out.println(Thread.currentThread().getName() + " calling: " + method);

                    // Depending on the stream input, it will perform one of the following methods.
                    switch (method)
                    {
                        case newOrder:
                            newOrder(is.readInt(), (Order) is.readObject());
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
                            is.readInt();
                            is.readObject();
                            // We need to create the fill(is.readInt(), (Order) is.readObject()) method to be used here.
                            break; // TODO
                    }
                }
                // If there is no bytes in the stream, it stops for 1 second before going to the start of the while loop.
                else
                {
                    // System.out.println("Trader Waiting for data to be available - sleep 1s");
                    Thread.sleep(1000);
                }
            }
        }
        catch (IOException | ClassNotFoundException | InterruptedException e)
        {
            //  TODO Auto-generated catch block
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

        // .flush() writes all the bytes in the os buffer to their destination, presumably clearing the buffer.
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
    public void price(int id, Order o) throws InterruptedException, IOException
    {
        // TODO should update the trade screen
        // TradeScreen.api....
        Thread.sleep(2134);
        sliceOrder(id, (int) orders.get(id).sizeRemaining() / 2);
    }
}
