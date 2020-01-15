import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import javax.net.ServerSocketFactory;

import OrderRouter.Router;
import Ref.Instrument;
import Ref.Ric;

public class SampleRouter extends Thread implements Router
{
    private static final Random RANDOM_NUM_GENERATOR = new Random();
    private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
    private Socket omConn;
    private int port;

    public SampleRouter(String name, int port)
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
            while (true)
            {
                // .available returns the estimated number of bytes to be read.
                if (0 < omConn.getInputStream().available())
                {
                    is = new ObjectInputStream(omConn.getInputStream());

                    // Router. is just a placeholder for what method should be called.
                    Router.api methodName = (Router.api) is.readObject();
                    System.out.println("Order Router recieved method call for:" + methodName);
                    switch (methodName)
                    {
                        case routeOrder:
                            // Instead of readingInts as the arguments to routeOrder(), read them and store them so we can see what they are.
                            int[] readIntArray = new int[]{is.readInt(), is.readInt(), is.readInt()};
                            System.out.println("RouteOrder called, read ints: Route Order INTS: "+readIntArray[0]+readIntArray[1]+readIntArray[2]);
                            routeOrder(readIntArray[0], readIntArray[1], readIntArray[2], (Instrument) is.readObject());
                            break;
                        case priceAtSize:
                            priceAtSize(is.readInt(), is.readInt(), (Instrument) is.readObject(), is.readInt());
                            break;
                        case sendCancel:
                            // This currently has not implementation, but it should be called like this.
                            sendCancel(is.readInt(), is.readInt(), is.readInt(), (Instrument) is.readObject());
                            break;
                        default:
                            System.err.println("Method: "+methodName.toString()+" is not a valid method.");
                    }
                }
                else
                {
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

    @Override
    /**
     * This class is used to communicate with the OrderManager
     * */
    public void routeOrder(int orderId, int sliceId, int size, Instrument i) throws IOException, InterruptedException
    {
        // MockI.show(""+order);
        int fillSize = RANDOM_NUM_GENERATOR.nextInt(size);

        // TODO have this similar to the market price of the instrument
        // Instead of fillPrice being 199 * 0-1, make it similar to the actual price of the instrument passed into the method? (I assume)
        double x = RANDOM_NUM_GENERATOR.nextDouble();//used to determine -+ from Initial price

        double fillPrice;
        if (x > 0 && x < 0.5){//50% chance of adding or subtracting form initial market price
            fillPrice = i.getUnitPrice()+ 0.1*i.getUnitPrice()*RANDOM_NUM_GENERATOR.nextDouble(); // adds 0-10% to the initial price to make fill price
            fillPrice = Math.round(fillPrice*100.0)/100.0;
        }
        else{
            fillPrice = i.getUnitPrice()- 0.1*i.getUnitPrice()*RANDOM_NUM_GENERATOR.nextDouble();// subs 0-10% to the initial price to make fill price
            fillPrice = Math.round(fillPrice*100.0)/100.0;
        }

        Thread.sleep(42);

        //Write and flush
        os = new ObjectOutputStream(omConn.getOutputStream());
        os.writeObject("newFill");
        os.writeInt(orderId);
        os.writeInt(sliceId);
        os.writeInt(fillSize);
        os.writeDouble(fillPrice);

        // .flush() writes all the bytes in the os buffer to their destination, presumably clearing the buffer.
        os.flush();
    }

    @Override
    public void priceAtSize(int id, int sliceId, Instrument i, int size) throws IOException
    {
        os = new ObjectOutputStream(omConn.getOutputStream());
        os.writeObject("bestPrice");
        os.writeInt(id);
        os.writeInt(sliceId);
        os.writeDouble(199 * RANDOM_NUM_GENERATOR.nextDouble());

        // .flush() writes all the bytes in the os buffer to their destination, presumably clearing the buffer.
        os.flush();
    }

    @Override
    public void sendCancel(int id, int sliceId, int size, Instrument i)
    {
        // MockI.show(""+order);
        // send the cancel signal in the output stream of something 'C' in sample client
        try
        {
            /* THIS SHIT HASNT ACTUALLY BEEN IMPLEMENTED YET!*/
            os = new ObjectOutputStream(omConn.getOutputStream());
            os.writeObject("bestPrice");
            os.writeInt(id);
            os.writeInt(sliceId);
            os.writeInt(size);
            os.writeObject(i);
            // .flush() writes all the bytes in the os buffer to their destination, presumably clearing the buffer.
            os.flush();
        } catch (IOException e)
        {
            System.err.println("IOException occured in sendCancel, message: "+e.getMessage());
            e.printStackTrace();
        }


    }
}
