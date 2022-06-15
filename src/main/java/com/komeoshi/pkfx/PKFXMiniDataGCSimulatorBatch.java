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
                0.000280,
                0.000050,
                0.000100,
                0.000150,
                0.000200,
                0.000250,
        };
        for(double param : params) {
            log.info("" + param);

            PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
            sim1.setParam(param);
            sim1.run();
        }
    }
}
