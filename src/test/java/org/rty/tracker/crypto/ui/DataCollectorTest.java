package org.rty.tracker.crypto.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.rty.tracker.crypto.core.MarketInformation;
import org.rty.tracker.crypto.core.PriceDetails;
import org.rty.tracker.crypto.ui.DataCollector;

public class DataCollectorTest {
	private MarketInformation pi1, pi2;

	@Before
	public void setup() {
		pi1 = new MarketInformation("TEST", 1L, false, 1D, 2D);
		pi2 = new MarketInformation("TEST", 2L, false, 0D, 1D);
	}

	@Test
	public void testNoData() {
		assertTrue(DataCollector.computePriceDiffDetails(Arrays.asList()).isEmpty());
		assertTrue(DataCollector.computePriceDiffDetails(Arrays.asList(new PriceDetails(0D, 0D, false))).isEmpty());

		assertTrue(DataCollector.collectPriceDetails(Arrays.asList()).isEmpty());

		assertTrue(DataCollector.computePriceDetails(Arrays.asList()).isEmpty());
	}

	@Test
	public void testCollectPriceDetails() {
		List<PriceDetails> priceDetails = DataCollector.collectPriceDetails(Arrays.asList(pi2, pi1));

		assertEquals(priceDetails.size(), 2);
		assertEquals(priceDetails.get(0).price.doubleValue(), 1D, 0.00001D);
		assertEquals(priceDetails.get(0).volume.doubleValue(), 2D, 0.00001D);
		assertEquals(priceDetails.get(1).price.doubleValue(), 0D, 0.00001D);
		assertEquals(priceDetails.get(1).volume.doubleValue(), 1D, 0.00001D);
	}

	@Test
	public void testComputePriceDetails() {
		List<PriceDetails> priceDetails = DataCollector.collectPriceDetails(Arrays.asList(pi2, pi1));

		Optional<double[][]> result = DataCollector.computePriceDetails(priceDetails);

		assertEquals(result.get()[0].length, 2);
		assertEquals(result.get()[0][0], 0D, 0.00001D);
		assertEquals(result.get()[0][1], 1D, 0.00001D);

		assertEquals(result.get()[1].length, 2);
		assertEquals(result.get()[1][0], 1D, 0.00001D);
		assertEquals(result.get()[1][1], 0D, 0.00001D);

		assertEquals(result.get()[2].length, 0);
		assertEquals(result.get()[3].length, 0);
	}

	@Test
	public void testComputePriceDiffDetails() {
		List<PriceDetails> priceDetails = DataCollector.collectPriceDetails(Arrays.asList(pi2, pi1));

		Optional<double[][]> result = DataCollector.computePriceDiffDetails(priceDetails);

		assertEquals(result.get()[0].length, 1);
		assertEquals(result.get()[0][0], -1D, 0.00001D);

		assertEquals(result.get()[1].length, 1);
		assertEquals(result.get()[1][0], 1D, 0.00001D);

		assertEquals(result.get()[2].length, 2);
		assertEquals(result.get()[2][0], 1D, 0.00001D);
		assertEquals(result.get()[2][1], 0D, 0.00001D);
	}
}
