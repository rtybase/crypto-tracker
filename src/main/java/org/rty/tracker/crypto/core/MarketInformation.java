package org.rty.tracker.crypto.core;

import java.io.Serializable;
import java.util.List;

import org.apache.spark.api.java.Optional;

public class MarketInformation implements Serializable {
	private static final long serialVersionUID = -1318765421200944040L;

	public final String assetName;
	public final long eventOrder;

	private final PriceDetails priceDetails;
	private final DepthDetails depthDetails;

	public MarketInformation(String assetName, long tradeTime, boolean isBuyerMaker, double price, double volume) {
		this.assetName = assetName;
		this.eventOrder = tradeTime;
		this.priceDetails = new PriceDetails(price, volume, isBuyerMaker);
		this.depthDetails = null;
	}

	public MarketInformation(String assetName, long eventOrder, List<PriceDetails> bids, List<PriceDetails> asks) {
		this.assetName = assetName;
		this.eventOrder = eventOrder;
		this.priceDetails = null;
		this.depthDetails = new DepthDetails(bids, asks);
	}

	public Optional<PriceDetails> getPriceDetails() {
		return Optional.ofNullable(priceDetails);
	}

	public Optional<DepthDetails> getDepthDetails() {
		return Optional.ofNullable(depthDetails);
	}

	@Override
	public String toString() {
		return String.format("assetName=%s latest update on %d", assetName, eventOrder);
	}
}
