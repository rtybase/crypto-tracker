package org.rty.tracker.crypto.io;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;
import org.rty.tracker.crypto.core.MarketInformation;
import org.rty.tracker.crypto.core.PriceDetails;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.AggTradeEvent;
import com.binance.api.client.domain.event.DepthEvent;

import avro.shaded.com.google.common.base.Preconditions;

public class PriceInformationReceiver extends Receiver<MarketInformation> {

	private static final long serialVersionUID = 3965021241968286984L;
	private final Set<String> assets;

	private transient BinanceApiWebSocketClient client;

	public PriceInformationReceiver(Collection<String> assets) {
		super(StorageLevel.MEMORY_AND_DISK_2());

		Objects.requireNonNull(assets, "assets must not be null!!");
		Preconditions.checkArgument(assets.size() > 0, "assets must not me empty!");

		this.assets = assets.stream()
				.map(String::trim)
				.map(String::toLowerCase)
				.collect(Collectors.toSet()); 
	}

	@Override
	public void onStart() {
		new Thread(this::receive).start();
	}

	@Override
	public void onStop() {

	}

	private void receive() {
		try {
			client = BinanceApiClientFactory.newInstance().newWebSocketClient();
			assets.forEach(asset -> registerToFeed(asset));

			while (!isStopped()) {
				try {
					Thread.sleep(1000);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void registerToFeed(final String asset) {
		client.onAggTradeEvent(asset, tradeEvent -> storeEvent(asset, tradeEvent));
		client.onDepthEvent(asset, depth -> storeEvent(asset, depth));
	}

	private void storeEvent(final String asset, final DepthEvent depthEvent) {
		try {
			final MarketInformation marketInfo = new MarketInformation(
					depthEvent.getSymbol(),
					depthEvent.getFinalUpdateId(), //.getEventTime(),
					depthEvent.getBids().stream()
						.map(di -> new PriceDetails(di.getPrice(), di.getQty()))
						.collect(Collectors.toList()),
					depthEvent.getAsks().stream()
						.map(di -> new PriceDetails(di.getPrice(), di.getQty()))
						.collect(Collectors.toList()));
			store(marketInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void storeEvent(final String asset, final AggTradeEvent tradeEvent) {
		try {
			final MarketInformation marketInfo = new MarketInformation(
					tradeEvent.getSymbol(),
					tradeEvent.getTradeTime(),
					tradeEvent.isBuyerMaker(),
					Double.parseDouble(tradeEvent.getPrice()),
					Double.parseDouble(tradeEvent.getQuantity()));
			store(marketInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
