package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.parameter.Parameter;
import com.komeoshi.pkfx.dto.parameter.ParameterA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PKFXMiniDataGCFinder {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCFinder.class);

    public static void main(String[] args) {
        PKFXMiniDataGCFinder batch = new PKFXMiniDataGCFinder();
        batch.run();
    }

    public void run() {
        List<Parameter> parameters = new ArrayList<>();

        List<Double> paramA$01$parameters = ParameterA.createParameters();
        List<Double> paramA$02$parameters = ParameterA.createParameters();
        List<Double> paramA$03$parameters = ParameterA.createParameters();
        List<Double> paramA$04$parameters = ParameterA.createParameters();
        List<Double> paramA$05$parameters = ParameterA.createParameters();

        for (Double paramA$01$parameter : paramA$01$parameters) {
            for (Double paramA$02$parameter : paramA$02$parameters) {
                for (Double paramA$03$parameter : paramA$03$parameters) {
                    for (Double paramA$04$parameter : paramA$04$parameters) {
                        for (Double paramA$05$parameter : paramA$05$parameters) {

                            ParameterA paramA$01 = new ParameterA(paramA$01$parameter);
                            ParameterA paramA$02 = new ParameterA(paramA$02$parameter);
                            ParameterA paramA$03 = new ParameterA(paramA$03$parameter);
                            ParameterA paramA$04 = new ParameterA(paramA$04$parameter);
                            ParameterA paramA$05 = new ParameterA(paramA$05$parameter);

                            Parameter parameter = new Parameter();
                            parameter.setParamA$01(paramA$01);
                            parameter.setParamA$02(paramA$02);
                            parameter.setParamA$03(paramA$03);
                            parameter.setParamA$04(paramA$04);
                            parameter.setParamA$05(paramA$05);

                            parameters.add(parameter);
                        }
                    }
                }
            }
        }

        Collections.shuffle(parameters);

        List<Candle> candles = null;
        double maxDiff = -9990.0;
        Parameter maxParameter = null;
        for (int ii = 0; ii < parameters.size(); ii++) {
            Parameter p = parameters.get(ii);

            PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
            sim1.setCandles(candles);
            sim1.setParamA$01(p.getParamA$01());
            sim1.setParamA$02(p.getParamA$02());
            sim1.setParamA$03(p.getParamA$03());
            sim1.setParamA$04(p.getParamA$04());
            sim1.setParamA$05(p.getParamA$05());
            sim1.run();

            if (maxDiff < sim1.getDiff()) {
                maxDiff = sim1.getDiff();
                maxParameter = p;
            }

            log.info((ii + 1) + "/" + parameters.size());
            log.info("currentParam   :" + p);
            log.info("maxParam       :" + Objects.requireNonNull(maxParameter));
            log.info("maxDiff        :" + maxDiff + "\r\n");

            if (candles == null) {
                candles = sim1.getCandles();
            }
        }
    }
}
