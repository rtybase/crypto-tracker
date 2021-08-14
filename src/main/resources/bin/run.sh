JAR_NAME=`find lib -name crypto-tracker*.jar`

CRYPTO_PAIRS=$1

echo "Running: java -Dhadoop_path=hadoop_home/ -jar $JAR_NAME $CRYPTO_PAIRS"
java -Dhadoop_path=hadoop_home/ -jar $JAR_NAME $CRYPTO_PAIRS