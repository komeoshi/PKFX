package com.komeoshi.pkfx;

import com.google.common.collect.Lists;
import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.parameter.*;
import com.komeoshi.pkfx.simulatedata.PKFXSimulateDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PKFXMiniDataGCFinder {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCFinder.class);

    public static void main(String[] args) {
        PKFXMiniDataGCFinder batch = new PKFXMiniDataGCFinder();
        batch.execute();
    }

    public void execute() {

        log.info("creating executors.");
        int poolSize = Runtime.getRuntime().availableProcessors();
        log.info("available processors = " + poolSize);
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);

        log.info("reading candle data.");
        PKFXSimulateDataReader reader = new PKFXSimulateDataReader("minData.dat");
        List<Candle> candles = reader.read().getCandles();
        log.info("read done.");

        log.info("creating parameters, execute. ");
        startTime = System.currentTimeMillis();
        createParametersAndExecute(pool, candles);

        log.info("submit done.");

        try {
            pool.shutdown();
            pool.awaitTermination(12, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            log.error("", ex);
            Thread.currentThread().interrupt();

            System.exit(1);
        }

        log.info("await done. ");

    }

    private long completeCount = 0;
    private double maxDiff = -999.0;
    private double maxDiffTotal = 0;
    private Parameter maxDiffParameter;
    private long startTime = 0;

    private PKFXMiniDataGCSimulator createSimulator(List<Candle> candles, Parameter p) {
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
        return sim1;
    }

    private long size = 0L;

    private void createParametersAndExecute(ExecutorService pool, List<Candle> candles) {

        log.info("creating parameters.");
        List<Double> paramA$01$parameters = ParameterA$AtrOrTr.createParameters();
        List<Double> paramA$02$parameters = ParameterA$AtrOrTr.createParameters();
        List<Double> paramA$03$parameters = ParameterA$AtrOrTr.createParameters();
        List<Double> paramA$04$parameters = ParameterA$AtrOrTr.createParameters();
        List<Double> paramA$05$parameters = ParameterA$AtrOrTr.createParameters();

        List<Double> paramB$01$parameters = ParameterB$Adx.createParameters();
        List<Double> paramB$02$parameters = ParameterB$Rsi.createParameters();
        List<Double> paramB$03$parameters = ParameterB$Rsi.createParameters();
        List<Double> paramB$04$parameters = ParameterB$Rsi.createParameters();

        List<Double> paramC$01$parameters = ParameterC$Bband.createParameters();
        List<Double> paramC$02$parameters = ParameterC$Bband.createParameters();
        List<Double> paramC$03$parameters = ParameterC$DxBand.createParameters();
        List<Double> paramC$04$parameters = ParameterC$DxBand.createParameters();

        List<Double> paramD$01$parameters = ParameterD$Macd1.createParameters();
        List<Double> paramD$02$parameters = ParameterD$Macd2.createParameters();
        List<Double> paramD$03$parameters = ParameterD$Sig.createParameters();
        log.info("creating parameters, done.");

        Collections.shuffle(paramA$01$parameters);
        Collections.shuffle(paramA$02$parameters);
        Collections.shuffle(paramA$03$parameters);
        Collections.shuffle(paramA$04$parameters);
        Collections.shuffle(paramA$05$parameters);

        Collections.shuffle(paramB$01$parameters);
        Collections.shuffle(paramB$02$parameters);
        Collections.shuffle(paramB$03$parameters);
        Collections.shuffle(paramB$04$parameters);

        Collections.shuffle(paramC$01$parameters);
        Collections.shuffle(paramC$02$parameters);
        Collections.shuffle(paramC$03$parameters);
        Collections.shuffle(paramC$04$parameters);

        Collections.shuffle(paramD$01$parameters);
        Collections.shuffle(paramD$02$parameters);
        Collections.shuffle(paramD$03$parameters);

        log.info("shuffle parameters, done.");

        List<List<Double>> paramAs = Lists.cartesianProduct(paramA$01$parameters,
                paramA$02$parameters,
                paramA$03$parameters,
                paramA$04$parameters,
                paramA$05$parameters);
        log.info("cartesianProduct paramAs, done.");
        List<List<Double>> paramBs = Lists.cartesianProduct(paramB$01$parameters,
                paramB$02$parameters,
                paramB$03$parameters,
                paramB$04$parameters);
        log.info("cartesianProduct paramBs, done.");
        List<List<Double>> paramCs = Lists.cartesianProduct(paramC$01$parameters,
                paramC$02$parameters,
                paramC$03$parameters,
                paramC$04$parameters);
        log.info("cartesianProduct paramCs, done.");
        List<List<Double>> paramDs = Lists.cartesianProduct(paramD$01$parameters,
                paramD$02$parameters,
                paramD$03$parameters);
        log.info("cartesianProduct paramDs, done.");

        this.size = (long) paramAs.size() * paramBs.size() * paramCs.size() * paramDs.size();

        long count = 0;
        for (List<Double> tmpParamB : paramBs) {
            for (List<Double> tmpParamC : paramCs) {
                for (List<Double> tmpParamD : paramDs) {
                    for (List<Double> tmpParamA : paramAs) {
                        count++;
                        Parameter parameter = createParameter(tmpParamA, tmpParamB, tmpParamC, tmpParamD);
                        FinderExecutor exec = new FinderExecutor(parameter, candles);

                        pool.submit(exec);
                        if (count % 10000 == 0) {
                            sleep(5);
                        }
                    }
                }
            }
        }
    }

    private Parameter createParameter(List<Double> tmpParamA, List<Double> tmpParamB, List<Double> tmpParamC, List<Double> tmpParamD) {
        Parameter parameter = new Parameter();
        parameter.setParamA$01(new ParameterA$AtrOrTr(tmpParamA.get(0)));
        parameter.setParamA$02(new ParameterA$AtrOrTr(tmpParamA.get(1)));
        parameter.setParamA$03(new ParameterA$AtrOrTr(tmpParamA.get(2)));
        parameter.setParamA$04(new ParameterA$AtrOrTr(tmpParamA.get(3)));
        parameter.setParamA$05(new ParameterA$AtrOrTr(tmpParamA.get(4)));

        parameter.setParamB$01(new ParameterB$Adx(tmpParamB.get(0)));
        parameter.setParamB$02(new ParameterB$Rsi(tmpParamB.get(1)));
        parameter.setParamB$03(new ParameterB$Rsi(tmpParamB.get(2)));
        parameter.setParamB$04(new ParameterB$Rsi(tmpParamB.get(3)));

        parameter.setParamC$01(new ParameterC$Bband(tmpParamC.get(0)));
        parameter.setParamC$02(new ParameterC$Bband(tmpParamC.get(1)));
        parameter.setParamC$03(new ParameterC$DxBand(tmpParamC.get(2)));
        parameter.setParamC$04(new ParameterC$DxBand(tmpParamC.get(3)));

        parameter.setParamD$01(new ParameterD$Macd1(tmpParamD.get(0)));
        parameter.setParamD$02(new ParameterD$Macd2(tmpParamD.get(1)));
        parameter.setParamD$03(new ParameterD$Sig(tmpParamD.get(2)));
        return parameter;
    }

    public static void showMemoryUsage() {

        long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;

        long used = total - free;
        long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;

        if (log.isInfoEnabled()) {
            log.info( "\n" + "memory usage {}(MB) / {}(MB) / {}(MB)", numberFormat(used), numberFormat(total),
                    numberFormat(max));
        }
    }

    public static String numberFormat(long l) {
        NumberFormat nfNum = NumberFormat.getNumberInstance();

        return nfNum.format(l);
    }

    private void sleep(int sec) {
        try {
            Thread.sleep(sec * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    class FinderExecutor implements Runnable {

        private final Parameter parameter;
        private final List<Candle> candles;

        public FinderExecutor(Parameter parameter,
                              List<Candle> candles) {
            this.parameter = parameter;
            this.candles = candles;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();

            PKFXMiniDataGCSimulator sim1 = createSimulator(candles, parameter);
            sim1.setResultLogging(false);
            sim1.run();

            double diff = sim1.getDiff();
            int total = sim1.getTotalCount();

            long endTime = System.currentTimeMillis();
            long time = (endTime - startTime);

            diff(diff, total, parameter, time);

        }

        private synchronized void diff(double diff,
                                       int total,
                                       Parameter parameter,
                                       long time) {
            completeCount++;
            if (diff >= maxDiff) {
                maxDiff = diff;
                maxDiffTotal = total;
                maxDiffParameter = parameter;
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            long averageTime = elapsedTime / completeCount;

            StringBuilder s = new StringBuilder();
            s.append("\n");
            s.append("maxParam             : " + Objects.requireNonNull(maxDiffParameter) + "\n");
            s.append("maxDiff              : " + maxDiff + "\n");
            s.append("maxDiff(count)       : " + maxDiffTotal + "\n");
            s.append("completeCount        : " + completeCount + " / " + size + " " + (completeCount / size) + "%" + "\n");
            s.append("this time.           : " + time + " ms." + "\n");
            s.append("average time.        : " + averageTime + " ms." + "\n");
            s.append("elapsed total time.  : " + elapsedTime + " ms." + "\n");
            s.append("remain time(ms).     : ?" + " ms." + "\n");
            s.append("remain time(H).      : ?" + " H." + "\n");

            if (completeCount % 100 == 0) {
                log.info(s.toString());
                showMemoryUsage();
            }

        }
    }
}


