package OrderClient;

import java.io.IOException;

import OrderManager.Order;

public interface Client
{
	//  Outgoing messages
	int sendOrder()throws IOException;
	void sendCancel(int id);
	
	//  Incoming messages
	void partialFill(int orderId);
	void fullyFilled(int orderId);
	void cancelled(int orderId);
	
	void messageHandler();
}