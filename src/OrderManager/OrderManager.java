package OrderManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import Database.Database;
import LiveMarketData.LiveMarketData;
import OrderClient.NewOrderSingle;
import OrderRouter.Router;
import Ref.Instrument;
import TradeScreen.TradeScreen;

public class OrderManager
{
    private static LiveMarketData liveMarketData;
    private HashMap<Integer, Order> orders = new HashMap<Integer, Order>(); // debugger will do this line as it gives state to the object
    private Socket[] orderRouters; // debugger will skip these lines as they dissapear at compile time into 'the object'/stack
    private Socket[] clients;
    private Socket trader;

    private Socket connect(InetSocketAddress location) throws InterruptedException
    {
        boolean connected = false;
        int tryCounter = 0;

        while (!connected && tryCounter < 600)
        {
            try {
                Socket s = new Socket(location.getHostName(), location.getPort());
                s.setKeepAlive(true);
                return s;
            } catch (IOException e) {
                Thread.sleep(1000);
                tryCounter++;
            }
        }

        System.out.println("Failed to connect to " + location.toString());
        return null;
    }

    public void createConnections(InetSocketAddress[] orderRouters, InetSocketAddress[] clients, InetSocketAddress trader, LiveMarketData liveMarketData) throws InterruptedException {

        this.liveMarketData = liveMarketData;
        this.trader = connect(trader);
        // for the router connections, copy the input array into our object field.
        // but rather than taking the address we create a socket+ephemeral port and connect it to the address
        this.orderRouters = new Socket[orderRouters.length];
        int i = 0; // need a counter for the the output array

        for (InetSocketAddress location : orderRouters)
        {
            this.orderRouters[i] = connect(location);
            i++;
        }

        // repeat for the client connections
        this.clients = new Socket[clients.length];
        i = 0;

        for (InetSocketAddress location : clients)
        {
            this.clients[i] = connect(location);
            i++;
        }
    }

    // @param args the command line arguments
    public OrderManager(InetSocketAddress[] orderRouters, InetSocketAddress[] clients, InetSocketAddress trader, LiveMarketData liveMarketData) throws IOException, ClassNotFoundException, InterruptedException
    {

        createConnections(orderRouters, clients,trader, liveMarketData);

        int clientId, routerId;
        Socket client, router;

        // main loop, wait for a message, then process it
        while (true) {

            Thread.sleep(2000); // Just chill for a bit, make the output a bit more manageable.

            //CLIENTS
            for (clientId = 0; clientId < this.clients.length; clientId++)
            { // check if we have data on any of the sockets

                client = this.clients[clientId];
                if (0 < client.getInputStream().available())
                { // if we have part of a message ready to read, assuming this doesn't fragment messages
                    // create an object inputstream, this is a pretty stupid way of doing it, why not create it once rather than every time around the loop
                    ObjectInputStream is = new ObjectInputStream(client.getInputStream());
                    String method = (String) is.readObject();
                    System.out.println(Thread.currentThread().getName() + " calling " + method);

                    switch (method)
                    { // determine the type of message and process it
                        // call the newOrder message with the clientId and the message (clientMessageId,NewOrderSingle)
                        case "newOrderSingle":
                            newOrder(clientId, is.readInt(), (NewOrderSingle) is.readObject());
                            break;
                        // create a default case which errors with "Unknown message type"+...
                        case "sendCancel":
                            // This currently has not implementation, but it should be called like this.

                            // --- This needs to be turned into an order object and a router, then into the method.

                            // THIS IS A TEST THIS MIGHT BREAK EVERYTHING
                            int orderId2 = is.readInt();

                            // where the fuck am i supposed to get this router from!?!?
                            sendCancel(orders.get(orderId2), this.orderRouters, clientId);
                            break;
                        default:
                            System.err.println("Unknown Message type!");
                            break;
                    }
                }
            }

            //ROUTERS
            for (routerId = 0; routerId < this.orderRouters.length; routerId++)
            { // check if we have data on any of the sockets
                router = this.orderRouters[routerId];
                if (0 < router.getInputStream().available())
                { // if we have part of a message ready to read, assuming this doesn't fragment messages
                    ObjectInputStream is = new ObjectInputStream(router.getInputStream()); // create an object inputstream, this is a pretty stupid way of doing it, why not create it once rather than every time around the loop
                    String method = (String) is.readObject();
                    System.out.println(Thread.currentThread().getName() + " calling " + method);

                    switch (method)
                    { // determine the type of message and process it
                        case "bestPrice":
                            int orderId = is.readInt();
                            int sliceId = is.readInt();
                            Order slice = orders.get(orderId).getSlices().get(sliceId);
                            slice.getBestPrices()[routerId] = is.readLong();
                            slice.setBestPriceCount(slice.getBestPriceCount() + 1);

                            if (slice.getBestPriceCount() == slice.getBestPrices().length)
                                reallyRouteOrder(sliceId, slice);
                            break;
                        case "newFill":
                            newFill(is.readInt(), is.readInt(), is.readInt(), is.readLong());
                            break;
                        case "orderCancelled":
                            //TODO remove the order from the OM
                            cancelOrder(is.readInt(), is.readInt());
                            break;
                        default:
                            System.err.println("(OrderManager)Method: "+method+" is not a valid method.");
                    }
                }
            }

            //Trader
            if (0 < this.trader.getInputStream().available())
            {
                ObjectInputStream is = new ObjectInputStream(this.trader.getInputStream());
                String method = (String) is.readObject();
                System.out.println(Thread.currentThread().getName() + " calling " + method);

                switch (method)
                {
                    case "acceptOrder":
                        acceptOrder(is.readInt());
                        break;
                    case "sliceOrder":
                        sliceOrder(is.readInt(), is.readInt());
                }
            }
        }
    }

    private void newOrder(int clientId, int clientOrderId, NewOrderSingle nos) throws IOException {
        Order tempOrder = new Order(clientId, clientOrderId, nos.instrument, nos.size);
        orders.put(tempOrder.getOrderId(), tempOrder);
        // send a message to the client with 39=A;
        // OrdStatus is Fix 39, 'A' is 'Pending New'
        ObjectOutputStream os = new ObjectOutputStream(clients[clientId].getOutputStream());
        // newOrderSingle acknowledgement
        // ClientOrderId is 11=
        os.writeObject("11=" + clientOrderId + ";35=A;39=A;");
        os.flush();

//        System.out.println("\n" + orders + "\n"); // Might be worth making a method to print this out so that its a bit more readable, but not necessary

        sendOrderToTrader(tempOrder.getOrderId(), tempOrder, TradeScreen.api.newOrder);
        // send the new order to the trading screen
        // don't do anything else with the order, as we are simulating high touch orders and so need to wait for the trader to accept the order
    }

    private void sendOrderToTrader(int orderId, Order o, Object method) throws IOException
    {
        ObjectOutputStream ost = new ObjectOutputStream(trader.getOutputStream());
        ost.writeObject(method);
        ost.writeInt(orderId);
        ost.writeObject(o);
        ost.flush();
    }

    public void acceptOrder(int orderId) throws IOException
    {
        Order o = orders.get(orderId);
        if (o.getOrdStatus() != 'A')
        { // Pending New
            System.out.println("error accepting order that has already been accepted");
            return;
        }
        o.setOrdStatus('0'); // New
        ObjectOutputStream os = new ObjectOutputStream(clients[(int) o.getClientId()].getOutputStream());
        // newOrderSingle acknowledgement
        // ClOrdId is 11=
        os.writeObject("11=" + o.getOrderId() + ";35=A;39=0");
        os.flush();

        // This was originally the global id in this class. (line 28)
        price(orderId, o);
    }

    public void sliceOrder(int orderId, int sliceSize) throws IOException
    {
        System.out.println("SliceOrder called with orderId: " +orderId+", sliceSize: "+sliceSize);
        Order o = orders.get(orderId);
        // slice the order. We have to check this is a valid size.
        // Order has a list of slices, and a list of fills, each slice is a childorder and each fill is associated with either a child order or the original order
        if (sliceSize > o.sizeRemaining() - o.sliceSizes())
        {
            System.out.println("error sliceSize is bigger than remaining size to be filled on the order");
            return;
        }

        //TODO JP Is rewritring the following:
        int sliceId = o.newSlice(sliceSize);
        Order slice = o.getSlices().get(sliceId);
        internalCross(orderId, slice);
        long sizeRemaining = o.getSlices().get(sliceId).sizeRemaining();

        if (sizeRemaining > 0)
        {
            routeOrderToRouter(orderId, sliceId, sizeRemaining, slice);
        }
    }

    private void internalCross(int orderId, Order o) throws IOException
    {
        for (Map.Entry<Integer, Order> entry : orders.entrySet())
        {
            if (entry.getKey().intValue() == orderId)
                continue;

            Order matchingOrder = entry.getValue();
            if (!(matchingOrder.getInstrument().equals(o.getInstrument()) && matchingOrder.unitPrice == o.unitPrice))
                continue;

            // TODO add support here and in Order for limit orders
            long sizeBefore = o.sizeRemaining();
            o.cross(matchingOrder);

            if (sizeBefore != o.sizeRemaining())
            {
                sendOrderToTrader(orderId, o, TradeScreen.api.cross);
            }
        }
    }

    private void cancelOrder(int orderId, int clientID)
    {
        try
        {
            ObjectOutputStream os = new ObjectOutputStream(this.clients[clientID].getOutputStream());

            os.writeObject("11=" + orderId + ";35=0;39=C;");
            os.flush();

        } catch (IOException e)
        {
            System.out.println("IOException occurred in cancelOrder: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void newFill(int orderId, int sliceId, int size, double price) throws IOException
    {
//        System.out.println("The incoming orderId: " + orderId);
//        System.out.println("The Orders Array is!" +orders);
        if(orders.containsKey(orderId))
        {
            Order o = orders.get(orderId);
            o.getSlices().get(sliceId).createFill(size, price);

            if (o.sizeRemaining() == 0)
            {
                Database.write(o);
            }

            sendOrderToTrader(orderId, o, TradeScreen.api.fill);
        }
        // If the order we are looking for is not there look for the order inside all the orders.
        else
        {
            // For each order in orders
            for (Order order: orders.values())
            {
                // for each order in each order.
                for(Order innerOrder : order.getSlices())
                {
                    // If the inner order (the slice) is what we are looking for.
                    if(innerOrder.getOrderId() == orderId)
                    {
//                        System.out.println("Found the order inside another order!" + innerOrder.toString()+ " Sliceid: "+sliceId);
                        order.getSlices().get(sliceId).createFill(size, price);

                        if (innerOrder.sizeRemaining() == 0)
                        {
                            Database.write(innerOrder);
                        }

                        sendOrderToTrader(orderId, innerOrder, TradeScreen.api.fill);
                    }
                }
            }
        }
    }

    private void routeOrderToRouter(int orderId, int sliceId, long size, Order order) throws IOException
    {
        for (Socket r : orderRouters)
        {
            //TODO JP Is rewritring the following:
            ObjectOutputStream os = new ObjectOutputStream(r.getOutputStream());
            os.writeObject(Router.api.priceAtSize);
            os.writeInt(orderId);
            os.writeInt(sliceId);
            os.writeObject(order.getInstrument());
            os.writeLong(order.sizeRemaining());
            os.flush();
        }

        // need to wait for these prices to come back before routing
        order.setBestPrices(new long[orderRouters.length]);
        order.setBestPriceCount(0L); //  L = long to compiler
    }

    /**
     * Use in best price.
     * Why I have no idea
     * */
    private void reallyRouteOrder(int sliceId, Order o) throws IOException
    {
        // TODO this assumes we are buying rather than selling
        int minIndex = 0;
        long[] bestPrices = o.getBestPrices();
        double min = bestPrices[0];

        for (int i = 1; i < bestPrices.length; i++)
        {
            if (min > bestPrices[i])
            {
                minIndex = i;
                min = bestPrices[i];
            }
        }

        //TODO JP Is rewritring the following:
        ObjectOutputStream os = new ObjectOutputStream(orderRouters[minIndex].getOutputStream());
        os.writeObject(Router.api.routeOrder);
        os.writeInt(o.getOrderId());
        os.writeInt(sliceId);
        os.writeInt(o.sizeRemaining());
        os.writeObject(o.getInstrument());
        os.flush();
    }

    private void sendCancel(Order order, Socket[] orderRouter, int client)
    {
        try
        {
            for (Socket router : orderRouter)
            {
                ObjectOutputStream os = new ObjectOutputStream(router.getOutputStream()); // create an object outputstream, this is a pretty stupid way of doing it, why not create it once rather than every time around the loop
                 os.writeObject(Router.api.sendCancel);
                 os.writeObject(order);
                 os.writeInt(client);
                 os.flush();
            }
        } catch (IOException e)
        {
            System.out.println("IOException occurred in sendCancel: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void price(int orderId, Order o) throws IOException
    {
        liveMarketData.setPrice(o);
        sendOrderToTrader(orderId, o, TradeScreen.api.price);
    }
}