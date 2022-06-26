package com.komeoshi.pkfx;

import com.google.common.collect.Lists;
import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.parameter.*;
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
        List<Parameter> parameters = createParameters();
        List<Candle> candles = null;
        double maxDiff = -9990.0;
        int total = 0;
        Parameter maxParameter = null;
        for (int ii = 0; ii < parameters.size(); ii++) {

            long startTime = System.currentTimeMillis();

            Parameter p = parameters.get(ii);

            PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
            sim1.setCandles(candles);
            sim1.setParamA$01(p.getParamA$01());
            sim1.setParamA$02(p.getParamA$02());
            sim1.setParamA$03(p.getParamA$03());
            sim1.setParamA$04(p.getParamA$04());
            sim1.setParamA$05(p.getParamA$05());

            sim1.setParamB$01(p.getParamB$01());
            sim1.setParamB$02(p.getParamB$02());
            sim1.setParamB$03(p.getParamB$03());
            sim1.setParamB$04(p.getParamB$04());

            sim1.setParamC$01(p.getParamC$01());
            sim1.setParamC$02(p.getParamC$02());
            sim1.setParamC$03(p.getParamC$03());
            sim1.setParamC$04(p.getParamC$04());

            sim1.setParamD$01(p.getParamD$01());
            sim1.setParamD$02(p.getParamD$02());
            sim1.setParamD$03(p.getParamD$03());

            sim1.run();

            if (maxDiff < sim1.getDiff()) {
                maxDiff = sim1.getDiff();
                total = sim1.getTotalCount();
                maxParameter = p;
            }

            long endTime = System.currentTimeMillis();

            long time = (endTime - startTime);
            long remain = (parameters.size() - (ii + 1)) / time;
            long remainHour = remain / 1000 / 60 / 60;

            log.info((ii + 1) + "/" + parameters.size());
            log.info("currentParam   :" + p);
            log.info("maxParam       :" + Objects.requireNonNull(maxParameter));
            log.info("maxDiff        :" + maxDiff);
            log.info("maxDiff(count) :" + total);
            log.info("end - start    :" + time + "ms.");
            log.info("remain         :" + remain + "ms");
            log.info("remain         ;" + remainHour + "h");


            if (candles == null) {
                candles = sim1.getCandles();
            }
        }
    }

    private List<Parameter> createParameters() {
        List<Parameter> parameters = new ArrayList<>();

        List<Double> paramA$01$parameters = ParameterA.createParameters();
        List<Double> paramA$02$parameters = ParameterA.createParameters();
        List<Double> paramA$03$parameters = ParameterA.createParameters();
        List<Double> paramA$04$parameters = ParameterA.createParameters();
        List<Double> paramA$05$parameters = ParameterA.createParameters();

        List<Integer> paramB$01$parameters = ParameterB.createParameters();
        List<Integer> paramB$02$parameters = ParameterB.createParameters();
        List<Integer> paramB$03$parameters = ParameterB.createParameters();
        List<Integer> paramB$04$parameters = ParameterB.createParameters();

        List<Double> paramC$01$parameters = ParameterC.createParameters();
        List<Double> paramC$02$parameters = ParameterC.createParameters();
        List<Double> paramC$03$parameters = ParameterC.createParameters();
        List<Double> paramC$04$parameters = ParameterC.createParameters();

        List<Double> paramD$01$parameters = ParameterD.createParameters();
        List<Double> paramD$02$parameters = ParameterD.createParameters();
        List<Double> paramD$03$parameters = ParameterD.createParameters();


        int size = paramA$01$parameters.size() *
                paramA$02$parameters.size() *
                paramA$03$parameters.size() *
                paramA$04$parameters.size() *
                paramA$05$parameters.size()
                +
                paramB$01$parameters.size() *
                        paramB$02$parameters.size() *
                        paramB$03$parameters.size() *
                        paramB$04$parameters.size()
                +
                paramC$01$parameters.size() *
                        paramC$02$parameters.size() *
                        paramC$03$parameters.size() *
                        paramC$04$parameters.size()
                +
                paramD$01$parameters.size() *
                        paramD$02$parameters.size() *
                        paramD$03$parameters.size();

        log.info("scheduled size: " + size);

        for (Double paramA$01$parameter : paramA$01$parameters) {
            for (Double paramA$02$parameter : paramA$02$parameters) {
                for (Double paramA$03$parameter : paramA$03$parameters) {
                    for (Double paramA$04$parameter : paramA$04$parameters) {
                        for (Double paramA$05$parameter : paramA$05$parameters) {

                            Parameter parameter = new Parameter();
                            parameter.setParamA$01(new ParameterA(paramA$01$parameter));
                            parameter.setParamA$02(new ParameterA(paramA$02$parameter));
                            parameter.setParamA$03(new ParameterA(paramA$03$parameter));
                            parameter.setParamA$04(new ParameterA(paramA$04$parameter));
                            parameter.setParamA$05(new ParameterA(paramA$05$parameter));

                            parameters.add(parameter);
                        }
                    }
                }
            }
        }

        log.info("create parameterA done. parameters: " + parameters.size() + "/" + size);

        for (Integer paramB$01$parameter : paramB$01$parameters) {
            for (Integer paramB$02$parameter : paramB$02$parameters) {
                for (Integer paramB$03$parameter : paramB$03$parameters) {
                    for (Integer paramB$04$parameter : paramB$04$parameters) {
                        Parameter parameter = new Parameter();
                        parameter.setParamB$01(new ParameterB(paramB$01$parameter));
                        parameter.setParamB$02(new ParameterB(paramB$02$parameter));
                        parameter.setParamB$03(new ParameterB(paramB$03$parameter));
                        parameter.setParamB$04(new ParameterB(paramB$04$parameter));

                        parameters.add(parameter);
                    }
                }
            }
        }

        log.info("create parameterB done. parameters: " + parameters.size() + "/" + size);

        for (Double paramC$01$parameter : paramC$01$parameters) {
            for (Double paramC$02$parameter : paramC$02$parameters) {
                for (Double paramC$03$parameter : paramC$03$parameters) {
                    for (Double paramC$04$parameter : paramC$04$parameters) {
                        Parameter parameter = new Parameter();
                        parameter.setParamC$01(new ParameterC(paramC$01$parameter));
                        parameter.setParamC$02(new ParameterC(paramC$02$parameter));
                        parameter.setParamC$03(new ParameterC(paramC$03$parameter));
                        parameter.setParamC$04(new ParameterC(paramC$04$parameter));

                        parameters.add(parameter);
                    }
                }
            }
        }

        log.info("create parameterC done. parameters: " + parameters.size() + "/" + size);

        for (Double paramD$01$parameter : paramD$01$parameters) {
            for (Double paramD$02$parameter : paramD$02$parameters) {
                for (Double paramD$03$parameter : paramD$03$parameters) {
                    Parameter parameter = new Parameter();
                    parameter.setParamD$01(new ParameterD(paramD$01$parameter));
                    parameter.setParamD$02(new ParameterD(paramD$02$parameter));
                    parameter.setParamD$03(new ParameterD(paramD$03$parameter));

                    parameters.add(parameter);
                }
            }
        }

        log.info("create parameterD done. parameters: " + parameters.size() + "/" + size);

        Collections.shuffle(parameters);
        return parameters;
    }
}
