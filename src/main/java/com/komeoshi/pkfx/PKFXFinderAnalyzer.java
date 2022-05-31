package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PKFXFinderAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(PKFXFinderAnalyzer.class);

    private final Candle candle;

    public PKFXFinderAnalyzer(Candle candle) {
        this.candle = candle;
    }

    /**
     * シグナル点灯を判定
     *
     * @param candleLengthMagnification 閾値
     * @return true: シグナル点灯
     */
    public boolean isSignal(double candleLengthMagnification) {
        return candle.isYousen() &&
                candle.isLengthEnough(candleLengthMagnification);
    }

    public double getRsi(final List<Candle> candles) {
        int periodLength = 14;
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - periodLength + 1;
        if (firstBar < 0) {
            String msg = "Quote history length " + candles.size() + " is insufficient to calculate the indicator.";
            log.info(msg);
            return -100;
        }

        double aveGain = 0, aveLoss = 0;
        for (int bar = firstBar + 1; bar <= lastBar; bar++) {
            double change = candles.get(bar).getMid().getC() - candles.get(bar - 1).getMid().getC();
            if (change >= 0) {
                aveGain += change;
            } else {
                aveLoss += change;
            }
        }

        double rs = aveGain / Math.abs(aveLoss);
        double rsi = 100 - 100 / (1 + rs);

        rsi = rsi - 50;

        // log.info("RSI: " + rsi);
        return rsi;
    }
}
