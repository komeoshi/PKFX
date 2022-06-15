package com.komeoshi.pkfx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PKFXMiniDataGCSimulatorBatch {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCSimulatorBatch.class);

    public static void main(String[] args) {
        PKFXMiniDataGCSimulatorBatch batch = new PKFXMiniDataGCSimulatorBatch();
        batch.run();
    }

    public void run(){

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
                1.20,
                1.31,
                1.42,
                1.53,
                1.64,
                1.75,
                1.86,
                1.97,
                2.08,

        };
        for(double param : params) {
            log.info("" + param);

            PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
            sim1.setParam(param);
            sim1.run();
        }
    }
}
