import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import LiveMarketData.LiveMarketData;
import OrderManager.OrderManager;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("TEST: this program tests ordermanager");

        // start sample clients
        MockClient c1 = new MockClient("Client 1", 2000);
        c1.start();
        (new MockClient("Client 2", 2001)).start();

        // start sample routers
        (new SampleRouter("Router LSE", 2010)).start();
        (new SampleRouter("Router BATE", 2011)).start();

        (new Trader("Trader James", 2020)).start();
        // start order manager

        //Creating the clients here
        InetSocketAddress[] clients = {new InetSocketAddress("localhost", 2000),
                new InetSocketAddress("localhost", 2001)};

        //Creating the routers here
        InetSocketAddress[] routers = {new InetSocketAddress("localhost", 2010),
                new InetSocketAddress("localhost", 2011)};

        //Create the trader here
        InetSocketAddress trader = new InetSocketAddress("localhost", 2020);
        LiveMarketData liveMarketData = new SampleLiveMarketData(null);
        (new MockOM(routers, clients, trader, liveMarketData)).start();
    }
}

class MockClient extends Thread
{
    final int port;
    private String name;

    MockClient(String name, int port)
    {
        this.port = port;
        this.setName(name);
        this.name = name;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run()
    {

        SampleClient client = null;
        try {
            client = new SampleClient(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

            try {
                if (port == 2000) {
                    // done "why does this take an arg?"
                    client.setUniqueClientID(0);

                    Thread t = new Thread(client);
                    t.setName(this.name + " MESSAGE HANDLER");
                    t.start();

                    Objects.requireNonNull(client).sendOrder();
                    int id = client.sendOrder();

                    System.out.println("WE ARE CANCELIUNG THE ORDER HERE!!!!");
                    client.sendCancel(id);



                } else {
                    client.setUniqueClientID(1);
                    Thread t = new Thread(client);
                    t.start();
                    t.setName(this.name + " MESSAGE HANDLER");
                    Objects.requireNonNull(client).sendOrder();
//                    client.messageHandler();
                }
            } catch (IOException e) {
                System.out.println("IOException Occured. Message: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while(true){
                try {
                    sleep(3000);

                    client.sendOrder();

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }

    }
}

class MockOM extends Thread
{
    final InetSocketAddress[] clients;
    final InetSocketAddress[] routers;
    final InetSocketAddress trader;
    final LiveMarketData liveMarketData;

    MockOM(InetSocketAddress[] routers, InetSocketAddress[] clients, InetSocketAddress trader, LiveMarketData liveMarketData)
    {
        this.clients = clients;
        this.routers = routers;
        this.trader = trader;
        this.liveMarketData = liveMarketData;
        this.setName("Order Manager");
    }

    @Override
    public void run()
    {
        try
        {
            // In order to debug constructors you can do F5 F7 F5
            new OrderManager(routers, clients, trader, liveMarketData);
        }
        catch (IOException | ClassNotFoundException | InterruptedException ex)
        {
            Logger.getLogger(MockOM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}