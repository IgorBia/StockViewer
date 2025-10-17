package com.stockviewer.stockapi.indicators;

public class TimeframeParser {
    public static long parseTimeframe(String tf) {
        if (tf.endsWith("m")) {
            return Long.parseLong(tf.replace("m", ""));
        } else if (tf.endsWith("h")) {
            return Long.parseLong(tf.replace("h", "")) * 60;
        } else if (tf.endsWith("d")) {
            return Long.parseLong(tf.replace("d", "")) * 60 * 24;
        }
        throw new IllegalArgumentException("Unsupported timeframe: " + tf);
    }
}
