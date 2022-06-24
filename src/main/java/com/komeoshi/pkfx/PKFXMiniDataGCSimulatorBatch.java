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
                1.00,
                1.01,
                1.02,
                1.03,
                1.04,
                1.05,
                1.06,
                1.07,
                1.08,
                1.09,
                1.10,
                1.11,
                1.12,
                1.13,
                1.14,
                1.15,
                1.16,
                1.17,
                1.18,
                1.19,
                1.20,
        };

        List<Candle> candles = null;
        Map<String, Candle> longCandles = null;

        for (double param : params) {
            log.info("" + (param * 1000));

            PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
            sim1.setParam(param);
            sim1.setCandles(candles);
            sim1.setLongCandles(longCandles);
            sim1.run();

            if (candles == null) {
                candles = sim1.getCandles();
                longCandles = sim1.getLongCandles();
            }
        }
    }
}
