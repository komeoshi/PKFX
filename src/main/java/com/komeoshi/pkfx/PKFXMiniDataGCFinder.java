package com.komeoshi.pkfx;

import com.google.common.collect.Lists;
import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.parameter.*;
import com.komeoshi.pkfx.enumerator.ParameterPosition;
import com.komeoshi.pkfx.simulatedata.PKFXSimulateDataReader;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
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
        this.pool = Executors.newFixedThreadPool(poolSize);

        log.info("reading candle data.");
        if (candles == null) {
            PKFXSimulateDataReader reader = new PKFXSimulateDataReader("data/mindata");
            candles = reader.read();
        }
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

    private ExecutorService pool;
    private long completeCount = 0;
    private double maxDiff = -999.0;
    private double winRate = -999.0;
    private double maxDiffTotal = 0;
    private Parameter maxDiffParameter = new Parameter();
    private long startTime = 0;
    private long size = 0L;
    private boolean isBatch = false;
    private ParameterPosition parameterPosition;
    private int executeMaxSize = 50000;
    private Parameter defaultParameter = null;
    private double maxDiffAllTheTime = -999.0;
    private int loopCount = -999;
    List<Candle> candles = null;

    Parameter parameter1 = null;
    Parameter parameter2 = null;
    Parameter parameter3 = null;
    Parameter parameter4 = null;
    Parameter parameter5 = null;
    Parameter parameter6 = null;
    Parameter parameter7 = null;
    Parameter parameter8 = null;
    Parameter parameter9 = null;
    Parameter parameter10 = null;

    private PKFXMiniDataGCSimulator createSimulator(List<Candle> candles, Parameter p) {
        PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
        sim1.setCandles(candles);

        if (parameterPosition == ParameterPosition.PARAMETER1)
            sim1.setParameter1(p);
        else
            sim1.setParameter1(parameter1);

        if (parameterPosition == ParameterPosition.PARAMETER2)
            sim1.setParameter2(p);
        else
            sim1.setParameter2(parameter2);

        if (parameterPosition == ParameterPosition.PARAMETER3)
            sim1.setParameter3(p);
        else
            sim1.setParameter3(parameter3);

        if (parameterPosition == ParameterPosition.PARAMETER4)
            sim1.setParameter4(p);
        else
            sim1.setParameter4(parameter4);

        if (parameterPosition == ParameterPosition.PARAMETER5)
            sim1.setParameter5(p);
        else
            sim1.setParameter5(parameter5);

        if (parameterPosition == ParameterPosition.PARAMETER6)
            sim1.setParameter6(p);
        else
            sim1.setParameter6(parameter6);

        if (parameterPosition == ParameterPosition.PARAMETER7)
            sim1.setParameter7(p);
        else
            sim1.setParameter7(parameter7);

        if (parameterPosition == ParameterPosition.PARAMETER8)
            sim1.setParameter8(p);
        else
            sim1.setParameter8(parameter8);

        if (parameterPosition == ParameterPosition.PARAMETER9)
            sim1.setParameter9(p);
        else
            sim1.setParameter9(parameter9);

        if (parameterPosition == ParameterPosition.PARAMETER10)
            sim1.setParameter10(p);
        else
            sim1.setParameter10(parameter10);

        return sim1;
    }

    private void createParametersAndExecute(ExecutorService pool, List<Candle> candles) {

        log.info("creating parameters.");
        List<Double> paramA$01$parameters = ParameterA$CurrentAtr.createParameters();
        List<Double> paramA$02$parameters = ParameterA$CurrentTr.createParameters();
        List<Double> paramA$03$parameters = ParameterA$Past2Tr.createParameters();
        List<Double> paramA$04$parameters = ParameterA$Past3Atr.createParameters();
        List<Double> paramA$05$parameters = ParameterA$Past4Atr.createParameters();

        List<Double> paramB$01$parameters = ParameterB$Adx.createParameters();
        List<Double> paramB$02$parameters = ParameterB$Past2Rsi.createParameters();
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
        log.info("cartesianProduct paramAs, done. " + paramAs.size());
        List<List<Double>> paramBs = Lists.cartesianProduct(paramB$01$parameters,
                paramB$02$parameters,
                paramB$03$parameters,
                paramB$04$parameters);
        log.info("cartesianProduct paramBs, done. " + paramBs.size());
        List<List<Double>> paramCs = Lists.cartesianProduct(paramC$01$parameters,
                paramC$02$parameters,
                paramC$03$parameters,
                paramC$04$parameters);
        log.info("cartesianProduct paramCs, done. " + paramCs.size());
        List<List<Double>> paramDs = Lists.cartesianProduct(paramD$01$parameters,
                paramD$02$parameters,
                paramD$03$parameters);
        log.info("cartesianProduct paramDs, done. " + paramDs.size());

        this.size = (long) paramAs.size() + paramBs.size() + paramCs.size() + paramDs.size();
        log.info("scheduled size = " + size);

        List<Parameter> parameters = new ArrayList<>();
        for (List<Double> tmpParamB : paramBs) {
            Parameter parameter = new Parameter();

            parameter.setParamA$01(new ParameterA$CurrentAtr(defaultParameter.getParamA$01().getParameter()));
            parameter.setParamA$02(new ParameterA$CurrentTr(defaultParameter.getParamA$02().getParameter()));
            parameter.setParamA$03(new ParameterA$Past2Tr(defaultParameter.getParamA$03().getParameter()));
            parameter.setParamA$04(new ParameterA$Past3Atr(defaultParameter.getParamA$04().getParameter()));
            parameter.setParamA$05(new ParameterA$Past4Atr(defaultParameter.getParamA$05().getParameter()));

            parameter.setParamB$01(new ParameterB$Adx(tmpParamB.get(0)));
            parameter.setParamB$02(new ParameterB$Past2Rsi(tmpParamB.get(1)));
            parameter.setParamB$03(new ParameterB$Rsi(tmpParamB.get(2)));
            parameter.setParamB$04(new ParameterB$Rsi(tmpParamB.get(3)));

            parameter.setParamC$01(new ParameterC$Bband(defaultParameter.getParamC$01().getParameter()));
            parameter.setParamC$02(new ParameterC$Bband(defaultParameter.getParamC$02().getParameter()));
            parameter.setParamC$03(new ParameterC$DxBand(defaultParameter.getParamC$03().getParameter()));
            parameter.setParamC$04(new ParameterC$DxBand(defaultParameter.getParamC$04().getParameter()));

            parameter.setParamD$01(new ParameterD$Macd1(defaultParameter.getParamD$01().getParameter()));
            parameter.setParamD$02(new ParameterD$Macd2(defaultParameter.getParamD$02().getParameter()));
            parameter.setParamD$03(new ParameterD$Sig(defaultParameter.getParamD$03().getParameter()));

            parameters.add(parameter);
        }

        for (List<Double> tmpParamC : paramCs) {
            Parameter parameter = new Parameter();

            parameter.setParamA$01(new ParameterA$CurrentAtr(defaultParameter.getParamA$01().getParameter()));
            parameter.setParamA$02(new ParameterA$CurrentTr(defaultParameter.getParamA$02().getParameter()));
            parameter.setParamA$03(new ParameterA$Past2Tr(defaultParameter.getParamA$03().getParameter()));
            parameter.setParamA$04(new ParameterA$Past3Atr(defaultParameter.getParamA$04().getParameter()));
            parameter.setParamA$05(new ParameterA$Past4Atr(defaultParameter.getParamA$05().getParameter()));

            parameter.setParamB$01(new ParameterB$Adx(defaultParameter.getParamB$01().getParameter()));
            parameter.setParamB$02(new ParameterB$Past2Rsi(defaultParameter.getParamB$02().getParameter()));
            parameter.setParamB$03(new ParameterB$Rsi(defaultParameter.getParamB$03().getParameter()));
            parameter.setParamB$04(new ParameterB$Rsi(defaultParameter.getParamB$04().getParameter()));

            parameter.setParamC$01(new ParameterC$Bband(tmpParamC.get(0)));
            parameter.setParamC$02(new ParameterC$Bband(tmpParamC.get(1)));
            parameter.setParamC$03(new ParameterC$DxBand(tmpParamC.get(2)));
            parameter.setParamC$04(new ParameterC$DxBand(tmpParamC.get(3)));

            parameter.setParamD$01(new ParameterD$Macd1(defaultParameter.getParamD$01().getParameter()));
            parameter.setParamD$02(new ParameterD$Macd2(defaultParameter.getParamD$02().getParameter()));
            parameter.setParamD$03(new ParameterD$Sig(defaultParameter.getParamD$03().getParameter()));

            parameters.add(parameter);
        }

        for (List<Double> tmpParamD : paramDs) {
            Parameter parameter = new Parameter();

            parameter.setParamA$01(new ParameterA$CurrentAtr(defaultParameter.getParamA$01().getParameter()));
            parameter.setParamA$02(new ParameterA$CurrentTr(defaultParameter.getParamA$02().getParameter()));
            parameter.setParamA$03(new ParameterA$Past2Tr(defaultParameter.getParamA$03().getParameter()));
            parameter.setParamA$04(new ParameterA$Past3Atr(defaultParameter.getParamA$04().getParameter()));
            parameter.setParamA$05(new ParameterA$Past4Atr(defaultParameter.getParamA$05().getParameter()));

            parameter.setParamB$01(new ParameterB$Adx(defaultParameter.getParamB$01().getParameter()));
            parameter.setParamB$02(new ParameterB$Past2Rsi(defaultParameter.getParamB$02().getParameter()));
            parameter.setParamB$03(new ParameterB$Rsi(defaultParameter.getParamB$03().getParameter()));
            parameter.setParamB$04(new ParameterB$Rsi(defaultParameter.getParamB$04().getParameter()));

            parameter.setParamC$01(new ParameterC$Bband(defaultParameter.getParamC$01().getParameter()));
            parameter.setParamC$02(new ParameterC$Bband(defaultParameter.getParamC$02().getParameter()));
            parameter.setParamC$03(new ParameterC$DxBand(defaultParameter.getParamC$03().getParameter()));
            parameter.setParamC$04(new ParameterC$DxBand(defaultParameter.getParamC$04().getParameter()));

            parameter.setParamD$01(new ParameterD$Macd1(tmpParamD.get(0)));
            parameter.setParamD$02(new ParameterD$Macd2(tmpParamD.get(1)));
            parameter.setParamD$03(new ParameterD$Sig(tmpParamD.get(2)));

            parameters.add(parameter);
        }

        for (List<Double> tmpParamA : paramAs) {
            Parameter parameter = new Parameter();

            parameter.setParamA$01(new ParameterA$CurrentAtr(tmpParamA.get(0)));
            parameter.setParamA$02(new ParameterA$CurrentTr(tmpParamA.get(1)));
            parameter.setParamA$03(new ParameterA$Past2Tr(tmpParamA.get(2)));
            parameter.setParamA$04(new ParameterA$Past3Atr(tmpParamA.get(3)));
            parameter.setParamA$05(new ParameterA$Past4Atr(tmpParamA.get(4)));

            parameter.setParamB$01(new ParameterB$Adx(defaultParameter.getParamB$01().getParameter()));
            parameter.setParamB$02(new ParameterB$Past2Rsi(defaultParameter.getParamB$02().getParameter()));
            parameter.setParamB$03(new ParameterB$Rsi(defaultParameter.getParamB$03().getParameter()));
            parameter.setParamB$04(new ParameterB$Rsi(defaultParameter.getParamB$04().getParameter()));

            parameter.setParamC$01(new ParameterC$Bband(defaultParameter.getParamC$01().getParameter()));
            parameter.setParamC$02(new ParameterC$Bband(defaultParameter.getParamC$02().getParameter()));
            parameter.setParamC$03(new ParameterC$DxBand(defaultParameter.getParamC$03().getParameter()));
            parameter.setParamC$04(new ParameterC$DxBand(defaultParameter.getParamC$04().getParameter()));

            parameter.setParamD$01(new ParameterD$Macd1(defaultParameter.getParamD$01().getParameter()));
            parameter.setParamD$02(new ParameterD$Macd2(defaultParameter.getParamD$02().getParameter()));
            parameter.setParamD$03(new ParameterD$Sig(defaultParameter.getParamD$03().getParameter()));

            parameters.add(parameter);
        }
        Collections.shuffle(parameters);

        this.size = parameters.size();
        log.info("parameters size = " + size);

        FinderExecutor exec1 = new FinderExecutor(defaultParameter, candles);
        pool.submit(exec1);

        long count = 0;
        for (Parameter parameter : parameters) {
            count++;
            FinderExecutor exec = new FinderExecutor(parameter, candles);

            try {
                pool.submit(exec);
                if (count % 100 == 0) {
                    log.info("submit count:" + count );
                    PKFXAnalyzer.showMemoryUsage();
                }
            } catch (RejectedExecutionException ignored) {

            }

            if (count > executeMaxSize) {
                break;
            }
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
            sim1.setLogging(false);
            sim1.setShortCut(true);
            sim1.run();

            double diff = sim1.getDiff();
            int total = sim1.getTotalCount();

            long endTime = System.currentTimeMillis();
            long time = (endTime - startTime);
            double winRate = ((double) sim1.getCountWin() / (double) sim1.getTotalCount());

            diff(diff, total, parameter, time, winRate);

        }

        private void diff(double diff,
                          int total,
                          Parameter parameter,
                          long time,
                          double winRate_) {

            synchronized (pool) {
                completeCount++;
                if (diff != 0.0 && diff >= maxDiff) {
                    maxDiff = diff;
                    maxDiffTotal = total;
                    maxDiffParameter = parameter;
                    winRate = winRate_;
                }

                long elapsedTime = System.currentTimeMillis() - startTime;
                long averageTime = elapsedTime / completeCount;
                long remainTime = (size - completeCount) * averageTime;
                long remainTimeH = remainTime / 1000 / 60 / 60;

                if(completeCount % 25 ==0){
                    log.info("complete count:" + completeCount + " maxDiff:" + maxDiff);
                }
                if (completeCount % 100 == 0) {
                    StringBuilder s = new StringBuilder();
                    s.append("\n");
                    s.append("maxParam             : " + maxDiffParameter + "\n");
                    s.append("parameterPosition    : " + parameterPosition + "\n");
                    s.append("maxDiff              : " + maxDiff + "\n");
                    s.append("maxDiff(count)       : " + maxDiffTotal + "\n");
                    s.append("maxDiff(win Rate)    : " + winRate * 100 + "\n");
                    s.append("completeCount        : " + completeCount + " / " + size + " " + ((double) completeCount / (double) size) * 100 + "%" + "\n");
                    s.append("this time.           : " + time + " ms." + "\n");
                    s.append("average time.        : " + averageTime + " ms." + "\n");
                    s.append("elapsed total time.  : " + elapsedTime + " ms." + "\n");
                    s.append("remain time(H).      : " + remainTimeH + " H." + "\n");
                    s.append("maxDiff allthetime   : " + maxDiffAllTheTime + "\n");
                    s.append("loop count           : " + loopCount + "\n");

                    log.info(s.toString());
                    PKFXAnalyzer.showMemoryUsage();
                }

                if (isBatch && completeCount > executeMaxSize) {
                    pool.shutdownNow();
                }
            }
        }
    }
}


