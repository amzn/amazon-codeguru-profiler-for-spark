package software.amazon.profiler.utils;

import org.apache.spark.SparkConf;

public class SparkConfUtils {
    public static String getValueFromEnvOrSparkConf(SparkConf conf, boolean isDriver, String propertyName) {
        String result = System.getenv(propertyName);
        if (result != null) {
            return result;
        }
        String prefix = isDriver ? "spark.yarn.appMasterEnv" : "spark.executorEnv";
        return conf.get(String.format("%s.%s", prefix, propertyName), null);
    }
}
