package Ref;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

public class Instrument implements Serializable{
	long id;
	String name;
	Ric ric;
	String isin;
	String sedol;
	String bbid;
	private double initialMarketPrice;
	public double getInitialMarketPrice() {
		return initialMarketPrice;
	}


	private static final Random RANDOM_NUM_GENERATOR = new Random();
	public Instrument(Ric ric){
		this.ric=ric;
		this.initialMarketPrice = 199 * RANDOM_NUM_GENERATOR.nextDouble();

	}
	public String toString(){
		return ric.ric;
	}


}
// shares
class EqInstrument extends Instrument{
	Date exDividend;

	public EqInstrument(Ric ric){
		super(ric);
	}
}
// future
class FutInstrument extends Instrument{
	Date expiry;
	Instrument underlier;

	public FutInstrument(Ric ric){
		super(ric);
	}
}
/*TODO
Index
bond
methods
*/