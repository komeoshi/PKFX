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

                0.000210,
                0.000220,
                0.000230,
                0.000240,
                0.000250,
                0.000260,
                0.000270,
                0.000280,
                0.000290,
                0.000300,
                0.000310,
                0.000320,
                0.000330,
                0.000340,
                0.000350,
                0.000360,
                0.000370,
                0.000380,
                0.000390,
                0.000400,
                0.000410,
                0.000420,
                0.000430,
                0.000440,
                0.000450,
                0.000460,
                0.000470,
                0.000480,
                0.000490,
                0.000500,
                0.000510,
                0.000520,
                0.000530,
                0.000540,
                0.000550,
                0.000560,
                0.000570,
                0.000580,
                0.000590,
                0.000600,

        };

        List<Candle> candles = null;

        double maxDiff = -9990.0;
        for (double param : params) {

            PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
            sim1.setParam(param);
            sim1.setCandles(candles);
            sim1.setLogging(false);
            sim1.setShortCut(false);
            sim1.setResultLogging(true);
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
