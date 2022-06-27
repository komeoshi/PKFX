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

                0,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,

        };

        List<Candle> candles = null;

        double maxDiff = -9990.0;
        for (double param : params) {

            PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
            sim1.setParam(param);
            sim1.setCandles(candles);
            sim1.run();

            if (maxDiff < sim1.getDiff()) {
                maxDiff = sim1.getDiff();
            }

            if (candles == null) {
                candles = sim1.getCandles();
            }
            log.info("param   :" + (param * 1000));
            log.info("maxDiff :" + maxDiff + "\r\n");
        }
    }
}
