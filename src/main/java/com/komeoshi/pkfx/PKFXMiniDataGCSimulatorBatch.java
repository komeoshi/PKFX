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

    public void run(){

        double[] params = {
                0.1,
                0.2,
                0.3,
                0.4,
                0.5,
                0.6,
                0.7,
                0.8,
                0.9,
                1.0,
        };

        List<Candle> candles =null;
        Map<String, Candle> longCandles =null;
        List<Candle> fiveMinCandles =null;

        for(double param : params) {
            log.info("" + param);

            PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
            sim1.setParam(param);
            sim1.setCandles(candles);
            sim1.setLongCandles(longCandles);
            sim1.setFiveMinCandles(fiveMinCandles);
            sim1.run();

            if(candles == null){
                candles = sim1.getCandles();
                longCandles = sim1.getLongCandles();
                fiveMinCandles = sim1.getFiveMinCandles();
            }
        }
    }
}
