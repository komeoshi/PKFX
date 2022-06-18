package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class PKFXMiniDataGCSimulatorBatch {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCSimulatorBatch.class);

    public static void main(String[] args) {
        PKFXMiniDataGCSimulatorBatch batch = new PKFXMiniDataGCSimulatorBatch();
        batch.run();
    }

    public void run() {

        double[] params = {
                0.051,
                0.052,
                0.053,
                0.054,
                0.055,
                0.056,
                0.057,
                0.058,
                0.059,
                0.060,
                0.061,
                0.062,
                0.063,
                0.064,
                0.065,
                0.066,
                0.067,
                0.068,
                0.069,
                0.070,
                0.071,
                0.072,
                0.073,
                0.074,
                0.075,
                0.076,
                0.077,
                0.078,
                0.079,
                0.080,
                0.081,
                0.082,
                0.083,
                0.084,
                0.085,
                0.086,
                0.087,
                0.088,
                0.089,
                0.090,
                0.091,
                0.092,
                0.093,

        };

        List<Candle> candles = null;
        Map<String, Candle> longCandles = null;
        List<Candle> fiveMinCandles = null;

        for (double param : params) {
            log.info("" + (param * 1000));

            PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
            sim1.setParam(param);
            sim1.setCandles(candles);
            sim1.setLongCandles(longCandles);
            sim1.setFiveMinCandles(fiveMinCandles);
            sim1.run();

            if (candles == null) {
                candles = sim1.getCandles();
                longCandles = sim1.getLongCandles();
                fiveMinCandles = sim1.getFiveMinCandles();
            }
        }
    }
}
