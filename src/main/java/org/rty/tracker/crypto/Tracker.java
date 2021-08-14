package org.rty.tracker.crypto;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.rty.tracker.crypto.core.MarketInformation;
import org.rty.tracker.crypto.io.PriceInformationReceiver;
import org.rty.tracker.crypto.ui.Renderer;

import scala.Tuple2;

public class Tracker {
	private static final Duration WINDOW_SIZE = Durations.seconds(30);
	private static final Duration BATCH_DURATION = Durations.seconds(5);

	public static void main(String[] args) throws InterruptedException {
		if (args.length == 0) {
			System.out.print("Provide a comma-separated list of crypto pairs to track!");
			System.exit(1);
		}

		final List<String> assets = Arrays.asList(args[0].split(","));

		File folder = new File(System.getProperty("hadoop_path"));
		System.setProperty("hadoop.home.dir", folder.getAbsolutePath());
		System.setProperty("spark.cleaner.referenceTracking.cleanCheckpoints", "true");

		SparkConf conf = new SparkConf().setMaster("local[2]")
				.setAppName("TradeAnalyser");

		JavaStreamingContext jssc = new JavaStreamingContext(conf, BATCH_DURATION);
		JavaDStream<MarketInformation> stream = jssc.receiverStream(new PriceInformationReceiver(assets));

		JavaPairDStream<String, MarketInformation> pairs = stream
				.mapToPair(marketInformation -> new Tuple2<>(marketInformation.assetName, marketInformation));

		final Function2<Iterable<MarketInformation>, Iterable<MarketInformation>, Iterable<MarketInformation>> reduceFunction = (
				l1, l2) -> {
			List<MarketInformation> result = new ArrayList<>();
			l1.forEach(result::add);
			l2.forEach(result::add);
			return result;
		};
	
		pairs.groupByKey()
			.reduceByKey(reduceFunction)
			.window(WINDOW_SIZE, BATCH_DURATION)
			.reduceByKey(reduceFunction)
			.foreachRDD(rdd -> 
				rdd.foreachPartition(partitionOfRecords -> {
					while (partitionOfRecords.hasNext()) {
						Tuple2<String, Iterable<MarketInformation>> nextElement = partitionOfRecords.next();
						final String assetName = nextElement._1();
						final Iterable<MarketInformation> decimals = nextElement._2();
						Renderer.drawData(assetName, decimals);
					}
				})
			);

		jssc.start();
		jssc.awaitTermination();
		jssc.close();
	}
}
