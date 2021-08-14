package org.rty.tracker.crypto.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.rty.tracker.crypto.core.DepthDetails;
import org.rty.tracker.crypto.core.MarketInformation;
import org.rty.tracker.crypto.core.PriceDetails;

public class DataCollector {
	private static final BigDecimal ROUNDING_ERROR = new BigDecimal("0.000001");
	private static final int BID = 0;
	private static final int ASK = 1;
	private static final Map<String, Map<Integer, Map<BigDecimal, BigDecimal>>> ORDER_BOOK = new ConcurrentHashMap<>();

	/**
	 * 
	 */
	public static Optional<double[][]> computeBookDepthDetails(String assetName, Iterable<MarketInformation> data) {
		final List<List<PriceDetails>> bidAskDetails = collectBidAksDetails(data);
		final Map<Integer, Map<BigDecimal, BigDecimal>> orderBook = ORDER_BOOK.computeIfAbsent(assetName, k -> new ConcurrentHashMap<>());

		final Map<BigDecimal, BigDecimal> copyBidMap = mergeAndReturnDepthOf(BID, bidAskDetails, orderBook);
		final Map<BigDecimal, BigDecimal> copyAskMap = mergeAndReturnDepthOf(ASK, bidAskDetails, orderBook);

		if (copyBidMap.isEmpty() && copyAskMap.isEmpty()) {
			return Optional.empty();
		}
		
		final double[][] result = new double[4][];
		result[0] = new double[copyBidMap.size()];
		result[1] = new double[copyBidMap.size()];
		result[2] = new double[copyAskMap.size()];
		result[3] = new double[copyAskMap.size()];

		int i = 0;
		for (Map.Entry<BigDecimal, BigDecimal> entry : copyBidMap.entrySet()) {
			result[0][i] = entry.getKey().doubleValue();
			result[1][i] = entry.getValue().doubleValue();
			i++;
		}

		i = 0;
		for (Map.Entry<BigDecimal, BigDecimal> entry : copyAskMap.entrySet()) {
			result[2][i] = entry.getKey().doubleValue();
			result[3][i] = entry.getValue().doubleValue();
			i++;
		}
		return Optional.of(result);
	}

	private static Map<BigDecimal, BigDecimal> mergeAndReturnDepthOf(int side, List<List<PriceDetails>> bidAskDetails,
			Map<Integer, Map<BigDecimal, BigDecimal>> orderBook) {
		final List<PriceDetails> sides = bidAskDetails.get(side);
		final Map<BigDecimal, BigDecimal> sideMap = orderBook.computeIfAbsent(side, k -> new ConcurrentHashMap<>());
		for (PriceDetails d : sides) {
			if (d.volume.compareTo(BigDecimal.ZERO) == 0) {
				sideMap.remove(d.price);
			} else {
				sideMap.put(d.price, d.volume);
			}
		}
		final Map<BigDecimal, BigDecimal> copySideMap = new TreeMap<>(sideMap);
		return copySideMap;
	}

	private static List<List<PriceDetails>> collectBidAksDetails(Iterable<MarketInformation> data) {
		final List<PriceDetails> bidResults = new ArrayList<>();
		final List<PriceDetails> askResults = new ArrayList<>();
		final Map<Long, DepthDetails> orderedResult = new TreeMap<>();
		data.forEach(d -> {
			if (d.getDepthDetails().isPresent()) {
				orderedResult.put(d.eventOrder, d.getDepthDetails().get());
			}
		});
		orderedResult.entrySet().forEach(e -> bidResults.addAll(e.getValue().bids));
		orderedResult.entrySet().forEach(e -> askResults.addAll(e.getValue().asks));
		return Arrays.asList(bidResults, askResults);
	}

	/**
	 * 
	 */
	public static List<PriceDetails> collectPriceDetails(Iterable<MarketInformation> data) {
		final List<PriceDetails> result = new ArrayList<>();
		final Map<Long, PriceDetails> orderedResult = new TreeMap<>();
		data.forEach(d -> {
			if (d.getPriceDetails().isPresent()) {
				orderedResult.put(d.eventOrder, d.getPriceDetails().get());
			}
		});
		orderedResult.entrySet().forEach(e -> result.add(e.getValue()));
		return result;
	}

	/**
	 * indexes 0 and 1 are for buyer maker is false.
	 * indexes 2 and 3 are for buyer maker is true.
	 */
	public static Optional<double[][]> computePriceDetails(List<PriceDetails> data) {
		if (data.isEmpty()) {
			return Optional.empty();
		}

		Map<Integer, Double> buyerNotMakerMap = compuetMapFor(false, data);
		Map<Integer, Double> buyerMakerMap = compuetMapFor(true, data);

		final double[][] result = new double[4][];

		result[0] = new double[buyerNotMakerMap.size()];
		result[1] = new double[buyerNotMakerMap.size()];
		int i = 0;
		for (Map.Entry<Integer, Double> entry : buyerNotMakerMap.entrySet()) {
			result[0][i] = entry.getKey();
			result[1][i] = entry.getValue();
			i++;
		}
		
		result[2] = new double[buyerMakerMap.size()];
		result[3] = new double[buyerMakerMap.size()];
		i = 0;
		for (Map.Entry<Integer, Double> entry : buyerMakerMap.entrySet()) {
			result[2][i] = entry.getKey();
			result[3][i] = entry.getValue();
			i++;
		}

		return Optional.of(result);
	}

	private static Map<Integer, Double> compuetMapFor(boolean isBuyer, List<PriceDetails> data) {
		Map<Integer, Double> result = new TreeMap<>();
		int i = 0;
		for (PriceDetails details : data) {
			if (details.isBuyerMaker == isBuyer) {
				result.put(i, details.price.doubleValue());
			}
			i++;
		}
		return result;
	}

	/**
	 * 
	 */
	public static Optional<double[][]> computePriceDiffDetails(List<PriceDetails> data) {
		final Map<Long, Long> histogramValues = new TreeMap<>();

		PriceDetails previousDetails = null;
		for (PriceDetails currentDetails : data) {
			if (previousDetails != null) {
				updateMap(histogramValues, currentDetails.price.subtract(previousDetails.price));
			}
			previousDetails = currentDetails;
		}

		if (histogramValues.isEmpty()) {
			return Optional.empty();
		}

		final double[][] result = initiateResult(histogramValues.size());

		transformHistogramValues(histogramValues, result);

		return Optional.of(result);
	}

	private static void transformHistogramValues(final Map<Long, Long> histogramValues, final double[][] result) {
		int i = 0;
		double sum = 0D;
		double posSum = 0D;
		for (Map.Entry<Long, Long> entry : histogramValues.entrySet()) {
			final double realValue = entry.getKey() * ROUNDING_ERROR.doubleValue();
			result[0][i] = realValue;
			result[1][i] = entry.getValue();
			i++;

			double stepValue = entry.getValue() * Math.abs(realValue);
			sum += stepValue;
			if (entry.getKey() > 0L) {
				posSum += stepValue;
			}
		}
		result[2][0] = sum;
		result[2][1] = posSum;
	}

	private static double[][] initiateResult(final int mapSize) {
		final double[][] result = new double[3][];

		result[0] = new double[mapSize];
		result[1] = new double[mapSize];

		result[2] = new double[2];
		return result;
	}

	private static void updateMap(Map<Long, Long> values, BigDecimal decimal) {
		long bucketKey = decimal.divide(ROUNDING_ERROR).setScale(0, RoundingMode.DOWN).longValue();
		values.compute(bucketKey, (k, v) -> (v == null) ? 1L : v + 1L);
	}
}
