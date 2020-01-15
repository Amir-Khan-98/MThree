package OrderClient;

import java.io.Serializable;

import Ref.Instrument;

public class NewOrderSingle implements Serializable
{
	public final int size;
	public final float price;
	public final Instrument instrument;

	public NewOrderSingle(int size, float price, Instrument instrument)
	{
		this.size=size;
		this.price=price;
		this.instrument=instrument;
	}
}