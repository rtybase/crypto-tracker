package org.rty.tracker.crypto.ui;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.DialChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.XYStyler.ButtonPosition;
import org.rty.tracker.crypto.core.MarketInformation;
import org.rty.tracker.crypto.core.PriceDetails;

public class Renderer {
	private static final Map<String, UiElements> ALL_CHARTS = new ConcurrentHashMap<>();

	public static void drawData(final String assetName, final Iterable<MarketInformation> data) {
		List<PriceDetails> priceDetails = DataCollector.collectPriceDetails(data);

		Optional<double[][]> allPriceDiffData = DataCollector.computePriceDiffDetails(priceDetails);
		if (allPriceDiffData.isPresent()) {
			final UiElements uiElements = getChartsFor(assetName);

			final double[][] dataPoints = allPriceDiffData.get();
			uiElements.diffChart.updateCategorySeries(assetName, dataPoints[0], dataPoints[1], null);

			if (dataPoints[2][0] > 0.0D) {
				uiElements.dialChart.getSeriesMap().get(assetName)
						.setValue((dataPoints[2][1] * 1.0D) / dataPoints[2][0]);
			}

			repaint(uiElements, 0);
			repaint(uiElements, 1);
		}

		Optional<double[][]> allPriceData = DataCollector.computePriceDetails(priceDetails);
		if (allPriceData.isPresent()) {
			final UiElements uiElements = getChartsFor(assetName);
			final double[][] dataPoints = allPriceData.get();
			// buyer maker is false
			uiElements.xyPriceChart.updateXYSeries("buyer taker", dataPoints[0], dataPoints[1], null);
			// buyer maker is true
			uiElements.xyPriceChart.updateXYSeries("seller taker", dataPoints[2], dataPoints[3], null);

			repaint(uiElements, 2);
		}

		Optional<double[][]> depthData = DataCollector.computeBookDepthDetails(assetName, data);
		if (depthData.isPresent()) {
			final UiElements uiElements = getChartsFor(assetName);
			final double[][] dataPoints = depthData.get();
			uiElements.depthChart.updateXYSeries("bids", dataPoints[0], dataPoints[1], null);
			uiElements.depthChart.updateXYSeries("asks", dataPoints[2], dataPoints[3], null);

			repaint(uiElements, 3);
		}
	}

	private static UiElements getChartsFor(final String assetName) {
		return ALL_CHARTS.computeIfAbsent(assetName, k -> buildUi(k));
	}

	private static void repaint(final UiElements uiElements, int index) {
		try {
			uiElements.sw.repaintChart(index);
		} catch (Exception ex) {
		}
	}

	private static UiElements buildUi(String assetName) {
		final CategoryChart diffChart = createCategoryChart("Histogram of Deltas", assetName);
		final DialChart dialChart = createDialChart(assetName);
		final XYChart xyPriceChart = createXYChart(XYSeriesRenderStyle.Scatter, LegendPosition.InsideNW, "Price",
				"buyer taker", "seller taker");
		final XYChart depthChart = createXYChart(XYSeriesRenderStyle.StepArea, LegendPosition.InsideNE,
				"Order Book Depth", "bids", "asks");

		final SwingWrapper<?> sw = new SwingWrapper<>(Arrays.asList(diffChart, dialChart, xyPriceChart, depthChart), 4, 1);
		sw.displayChartMatrix();
		return new UiElements(diffChart, dialChart, xyPriceChart, depthChart, sw);
	}

	private static CategoryChart createCategoryChart(String title, String... series) {
		final CategoryChart chart = new CategoryChart(800, 200);
		chart.setTitle("Histogram of Deltas");
		chart.getStyler().setXAxisTickMarkSpacingHint(100);
		chart.getStyler().setXAxisLabelRotation(90);
		chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
		for (String s : series) {
			chart.addSeries(s, new double[] { 0.0D }, new double[] { 0.0D });
		}
		return chart;
	}

	private static DialChart createDialChart(String assetName) {
		final DialChart chart = new DialChart(200, 200);
		chart.setTitle("Frequency of positive Deltas");
		chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
		chart.addSeries(assetName, 0.0D);
		return chart;
	}

	private static XYChart createXYChart(XYSeriesRenderStyle style, LegendPosition lPos, String title, String... series) {
		final XYChart chart = new XYChart(800, 200);
		chart.setTitle(title);
		chart.getStyler().setZoomEnabled(true);
		chart.getStyler().setZoomResetButtomPosition(ButtonPosition.InsideS);
		chart.getStyler().setZoomResetByDoubleClick(false);
		chart.getStyler().setZoomResetByButton(true);
		chart.getStyler().setZoomSelectionColor(new Color(0, 0 , 192, 128));
		chart.getStyler().setXAxisTickMarkSpacingHint(100);
		chart.getStyler().setXAxisLabelRotation(90);
		chart.getStyler().setLegendPosition(lPos);
		chart.getStyler().setDefaultSeriesRenderStyle(style);
		for (String s : series) {
			chart.addSeries(s, new double[] { 0.0D }, new double[] { 0.0D });
		}
		return chart;
	}

	private static class UiElements {
		private final CategoryChart diffChart;
		private final DialChart dialChart;
		private final XYChart xyPriceChart;
		private final XYChart depthChart;
		private final SwingWrapper<?> sw;

		private UiElements(CategoryChart diffChart, DialChart dialChart, XYChart xyPriceChart, XYChart depthChart, SwingWrapper<?> sw) {
			this.diffChart = diffChart;
			this.dialChart = dialChart;
			this.xyPriceChart = xyPriceChart;
			this.depthChart = depthChart;
			this.sw = sw;
		}
	}
}
