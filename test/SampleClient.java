import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import OrderClient.Client;
import OrderClient.NewOrderSingle;
import OrderManager.Order;
import Ref.Instrument;
import Ref.Ric;

public class SampleClient extends Mock implements Client
{
    private static final Random RANDOM_NUM_GENERATOR = new Random();
    private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
    private static final HashMap OUT_QUEUE = new HashMap(); // queue for outgoing orders
    private volatile AtomicInteger messageId = new AtomicInteger( 0); // message id number
    private Socket omConn; // connection to order manager

    public SampleClient(int port) throws IOException
    {
        // OM will connect to us
        omConn = new ServerSocket(port).accept();
        System.out.println("OM connected to client port " + port);
    }

    @Override
    public synchronized int sendOrder() throws IOException
    {
        int size = RANDOM_NUM_GENERATOR.nextInt(5000);
        int instid = RANDOM_NUM_GENERATOR.nextInt(3);
        Instrument instrument = INSTRUMENTS[RANDOM_NUM_GENERATOR.nextInt(INSTRUMENTS.length)];
        NewOrderSingle nos = new NewOrderSingle(size, instid, instrument);

        show("sendOrder: messageId=" + messageId + " size=" + size + " instrument=" + INSTRUMENTS[instid].toString());
        OUT_QUEUE.put(messageId, nos);

        if (omConn.isConnected())
        {
            ObjectOutputStream os = new ObjectOutputStream(omConn.getOutputStream());
            os.writeObject("newOrderSingle");
            //os.writeObject("35=D;");
            os.writeInt(messageId.get());
            os.writeObject(nos);
            os.flush();
        }
        return messageId.incrementAndGet();
    }

    @Override
    public void sendCancel(int idToCancel)
    {
        show("sendCancel: id=" + idToCancel);

        if (omConn.isConnected())
        {
            try
            {

                ObjectOutputStream os = new ObjectOutputStream(omConn.getOutputStream());
                os.writeObject("sendCancel");
                //os.writeObject("35=D;");
                os.writeInt(idToCancel);
                os.flush();
            } catch (IOException e)
            {
                System.out.println("Cant send message");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void partialFill(int orderId)
    {
        show("" + OUT_QUEUE.get(orderId));
        OUT_QUEUE.remove(orderId);
    }

    @Override
    public void fullyFilled(int orderId) {

        //TODO fectch Orders here
        show("" + OUT_QUEUE.get(orderId));
        OUT_QUEUE.remove(orderId);
    }

    @Override
    public void cancelled(int orderId)
    {
        System.out.println("Cancelled order: " + orderId );
        show("" + OUT_QUEUE.get(orderId));
        OUT_QUEUE.remove(orderId);
    }

    enum methods
    {
        newOrderSingleAcknowledgement, dontKnow
    };

    @Override
    public void messageHandler()
    {
        ObjectInputStream is;
        try
        {
            while (true)
            {
                // is.wait(); // this throws an exception!!
                while (0 < omConn.getInputStream().available())
                {
                    is = new ObjectInputStream(omConn.getInputStream());
                    String fix = (String) is.readObject();
                    System.out.println(Thread.currentThread().getName() + " received fix message: " + fix);
                    String[] fixTags = fix.split(";");
                    int orderId = -1;
                    char MsgType;
                    int OrdStatus;
                    methods whatToDo = methods.dontKnow;
                    // String[][] fixTagsValues=new String[fixTags.length][2];

                    for (int i = 0; i < fixTags.length; i++)
                    {
                        String[] tag_value = fixTags[i].split("=");

                        System.out.println(tag_value.toString());


                        switch (tag_value[0])
                        {
                            case "11":
                                orderId = Integer.parseInt(tag_value[1]);
                                break;
                            case "35":
                                MsgType = tag_value[1].charAt(0);
                                if (MsgType == 'A') whatToDo = methods.newOrderSingleAcknowledgement;
                                break;
                            case "39":
                                OrdStatus = tag_value[1].charAt(0);

                                System.out.println(OrdStatus);

                                if (OrdStatus == 'C') cancelled(orderId);
                                else if (OrdStatus == 'P') partialFill(orderId);
                                else if (OrdStatus == 'F') fullyFilled(orderId);
                                else if (OrdStatus == '0') sendOrder();
                                break;
                        }
                    }
                    switch (whatToDo)
                    {
                        case newOrderSingleAcknowledgement:
                            newOrderSingleAcknowledgement(orderId);
                    }
					
//					message=connection.getMessage();
//					char type;
//					switch(type){
//
//					}
                    show("");
                }


            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            if(e.getClass() == IOException.class)
                System.out.println("IOException occurred, message: "+e.getMessage());
            else if(e.getClass() == ClassNotFoundException.class)
                System.out.println("ClassNotFoundException occurred, message: "+e.getMessage());
            e.printStackTrace();
        }
    }

    void newOrderSingleAcknowledgement(int OrderId)
    {
        System.out.println(Thread.currentThread().getName() + " called newOrderSingleAcknowledgement");
        // do nothing, as not recording so much state in the NOS class at present
    }
/*listen for connections
once order manager has connected, then send and cancel orders randomly
listen for messages from order manager and print them to stdout.*/
}