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
                0.0030,
                0.0031,
                0.0032,
                0.0033,
                0.0034,
                0.0035,
                0.0036,
                0.0037,
                0.0038,
                0.0039,
                0.0041,
                0.0042,
                0.0043,
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
