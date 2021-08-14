# Crypto-Tracker

Crypto Tracker using [Binance API](https://binance-docs.github.io/apidocs/spot/en/#all-market-mini-tickers-stream) ([Java version](https://github.com/binance-exchange/binance-java-api)), [XChart](https://github.com/knowm/XChart) and [Apache Spark](https://spark.apache.org/docs/latest/streaming-programming-guide.html).

### Build

From command line

```
mvn clean verify
```

### Unpack

From command line

```
cd target/
tar -xvzf crypto-tracker-1.0.1-SNAPSHOT-dist.tar.gz 
```

### Run

Once unpacked

```
cd crypto-tracker
```

and

```
./run.sh "adausdt,bnbusdt"
```

Some of the Crypto pairs to try

```
ethusdt, ltcusdt,
adausdt, bnbusdt,
ethbusd, bnbbusd,
btcbusd
```

![alt text](https://raw.githubusercontent.com/rtybase/crypto-tracker/main/imgs/ui-view.png "UI screenshot")
