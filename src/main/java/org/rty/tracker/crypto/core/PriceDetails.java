package org.rty.tracker.crypto.core;

import java.io.Serializable;
import java.math.BigDecimal;

public class PriceDetails implements Serializable {
	private static final long serialVersionUID = 7525068209312344004L;

	public final BigDecimal price;
	public final BigDecimal volume;
	public final boolean isBuyerMaker;

	public PriceDetails(double price, double volume, boolean isBuyerMake) {
		this.volume = new BigDecimal(volume);
		this.price = new BigDecimal(price);
		this.isBuyerMaker = isBuyerMake;
	}

	public PriceDetails(String price, String volume) {
		this.volume = new BigDecimal(volume);
		this.price = new BigDecimal(price);
		this.isBuyerMaker = false;
	}

	@Override
	public String toString() {
		return String.format("[price=%f, volume=%f]", price, volume);
	}
}
