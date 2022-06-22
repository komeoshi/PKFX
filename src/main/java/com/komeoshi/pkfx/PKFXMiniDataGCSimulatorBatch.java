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
                0.02,
                0.0201,
                0.0202,
                0.0203,
                0.0204,
                0.0205,
                0.0206,
                0.0207,
                0.0208,
                0.0209,
                0.0210,
                0.0211,
                0.0212,
                0.0213,
                0.0214,
                0.0215,
                0.0216,
                0.0217,
                0.0218,
                0.0219,
                0.0220,
                0.0221,
                0.0222,
                0.0223,
                0.0224,
                0.0225,
                0.0226,
                0.0227,
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
