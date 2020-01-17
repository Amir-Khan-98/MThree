package OrderManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import Database.OrderController;
import Ref.Instrument;

//Testing Braches and merging

public class Order implements Serializable
{

	private int orderId;
	public long orderRouter;
	private long clientOrderID;
	private int size;
	private long[] bestPrices;
	private long bestPriceCount;

	private long clientId;

	private int clientUniqueID;
	private Instrument instrument;
	public double unitPrice;
	private ArrayList<Order> slices;

	public int getClientUniqueID() {
		return clientUniqueID;
	}

	public void setClientUniqueID(int clientUniqueID) {
		this.clientUniqueID = clientUniqueID;
	}

	@Override
	public String toString() {
		return "Order{" +
				"orderId=" + orderId +
				", orderRouter=" + orderRouter +
				", clientOrderID=" + clientOrderID +
				", size=" + size +
				", bestPrices=" + Arrays.toString(bestPrices) +
				", bestPriceCount=" + bestPriceCount +
				", clientId=" + clientId +
				", instrument=" + instrument +
				", unitPrice=" +instrument.getUnitPrice() +
				", slices=" + slices +
				", fills=" + fills +
				", OrdStatus=" + OrdStatus +
				'}';
	}


	public int getSize()
	{
		return size;
	}

	private ArrayList<Fill> fills;
	private char OrdStatus = 'A'; //TODO OrdStatus is Fix 39, 'A' is 'Pending New'

	public Order(long clientId, long clientOrderID, Instrument instrument, int size)
	{
		this.orderId = OrderIDTracker.getInstance().getNewID();
		this.clientOrderID = clientOrderID;
		this.size = size;
		this.clientId = clientId;
		this.instrument = instrument;
		this.unitPrice = instrument.getUnitPrice(); // set the orders initial market price to the price of instrument
		fills = new ArrayList<Fill>();
		slices = new ArrayList<Order>();

		System.out.println("Order Created!" + this.orderId);
	}

	public int sliceSizes()
	{
		int totalSizeOfSlices = 0;

		for(Order c : slices)
			totalSizeOfSlices += c.size;

		return totalSizeOfSlices;
	}

	public int newSlice(int sliceSize)
	{

		Order temoOderSlice = new Order(clientId, clientOrderID, instrument, sliceSize);
		OrderController.addOrderToTable(temoOderSlice);
		slices.add(temoOderSlice);

		return slices.size() - 1;
	}

	private int sizeFilled()
	{
		int filledSoFar = 0;

		for(Fill f : fills)
		{
			filledSoFar += f.size;
		}

		for(Order c : slices)
		{
			filledSoFar += c.sizeFilled();
		}

		return filledSoFar;
	}

	public int sizeRemaining()
	{
		return size - sizeFilled();
	}

	// Status state;
	private float price()
	{
		// TODO this is buggy as it doesn't take account of slices. Let them fix it
		float sum = 0;

		for(Fill fill : fills)
		{
			sum += fill.price;
		}
		return sum / fills.size();
	}

	public void createFill(long size, double price)
	{
		fills.add(new Fill(size, price));
		if(sizeRemaining() == 0)
		{
			OrdStatus = '2';
		}
		else
		{
			OrdStatus = '1';
		}
	}

	public void cross(Order matchingOrder)
	{
		// pair slices first and then parent
		for(Order slice : slices)
		{
			if(slice.sizeRemaining() == 0)
				continue;

			// TODO could optimise this to not start at the beginning every time
			for(Order matchingSlice : matchingOrder.slices)
			{
				long msze=matchingSlice.sizeRemaining();

				if(msze == 0)continue;
					long sze=slice.sizeRemaining();

				if(sze <= msze)
				{
					 slice.createFill(sze,matchingOrder.unitPrice);//Amir has changed unitPrice to matchingOrder.unitPrice
					 matchingSlice.createFill(sze, matchingOrder.unitPrice);
					 break;
				}
				// sze>msze

				slice.createFill(msze,matchingOrder.unitPrice);
				matchingSlice.createFill(msze, matchingOrder.unitPrice);
			}

			long sze = slice.sizeRemaining();
			long mParent = matchingOrder.sizeRemaining() - matchingOrder.sliceSizes();
			if(sze > 0 && mParent > 0)
			{
				if(sze >= mParent)
				{
					slice.createFill(sze, matchingOrder.unitPrice);
					matchingOrder.createFill(sze, matchingOrder.unitPrice);
				}
				else
				{
					slice.createFill(mParent, matchingOrder.unitPrice);
					matchingOrder.createFill(mParent, matchingOrder.unitPrice);
				}
			}

			// no point continuing if we didn't fill this slice, as we must already have fully filled the matchingOrder
			if(slice.sizeRemaining() > 0)
				break;
		}

		if(sizeRemaining() > 0)
		{
			for(Order matchingSlice : matchingOrder.slices)
			{
				long msze = matchingSlice.sizeRemaining();

				if(msze == 0)
					continue;

				long sze = sizeRemaining();

				if(sze <= msze)
				{
					 createFill(sze, matchingOrder.unitPrice);
					 matchingSlice.createFill(sze, matchingOrder.unitPrice);
					 break;
				}
				// sze>msze

				createFill(msze,matchingOrder.unitPrice);
				matchingSlice.createFill(msze, matchingOrder.unitPrice);
			}

			long sze = sizeRemaining();
			long mParent = matchingOrder.sizeRemaining() - matchingOrder.sliceSizes();

			if(sze > 0 && mParent > 0)
			{
				if(sze >= mParent)
				{
					createFill(sze, matchingOrder.unitPrice);
					matchingOrder.createFill(sze, matchingOrder.unitPrice);
				}
				else
				{
					createFill(mParent, matchingOrder.unitPrice);
					matchingOrder.createFill(mParent, matchingOrder.unitPrice);
				}
			}
		}
	}

	public void cancel()
	{
		// state=cancelled
	}

		/* GETTER AND SETTERS */

	public long[] getBestPrices()
	{
		return bestPrices;
	}

	public long getBestPriceCount()
	{
		return bestPriceCount;
	}

	public void setBestPrices(long[] bestPrices)
	{
		this.bestPrices = bestPrices;
	}

	public void setBestPriceCount(long bestPriceCount)
	{
		this.bestPriceCount = bestPriceCount;
	}

	public Instrument getInstrument()
	{
		return instrument;
	}

	public char getOrdStatus()
	{
		return OrdStatus;
	}

	public void setOrdStatus(char ordStatus)
	{
		OrdStatus = ordStatus;
	}

	public int getOrderId()
	{
		return orderId;
	}

	public long getClientId()
	{
		return clientId;
	}

	public ArrayList<Order> getSlices()
	{
		return slices;
	}

	public long getClientOrderID() {
		return clientOrderID;
	}

	public double getInitialMarketPrice() {
		return 0.0;
	}
}

class Basket
{
	Order[] orders;
}

// A Fill, is the action of completing an order for a security.
// TODO (Chris added) do we need to incorporate the id? For what security the Fill is representing?
class Fill implements Serializable
{
	long id;
	long size;
	double price;
	Fill(long size, double price){
		this.size = size;
		this.price = price;
	}
}
