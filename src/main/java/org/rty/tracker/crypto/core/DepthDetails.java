package org.rty.tracker.crypto.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class DepthDetails implements Serializable {
	private static final long serialVersionUID = -4108285520849597416L;

	public final List<PriceDetails> bids;
	public final List<PriceDetails> asks;

	public DepthDetails(List<PriceDetails> bids, List<PriceDetails> asks) {
		this.asks = Collections.unmodifiableList(asks);
		this.bids = Collections.unmodifiableList(bids);
	}
}
