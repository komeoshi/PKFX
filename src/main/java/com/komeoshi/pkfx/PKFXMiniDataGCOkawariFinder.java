package com.komeoshi.pkfx;

import com.google.common.collect.Lists;
import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.parameter.*;
import com.komeoshi.pkfx.dto.parameter.okawari.*;
import com.komeoshi.pkfx.simulatedata.PKFXSimulateDataReader;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class PKFXMiniDataGCOkawariFinder {
    private static final Logger log = LoggerFactory.getLogger(PKFXMiniDataGCOkawariFinder.class);

    public static void main(String[] args) {
        PKFXMiniDataGCOkawariFinder batch = new PKFXMiniDataGCOkawariFinder();
        batch.execute();
    }

    public void execute() {

        log.info("creating executors.");
        int poolSize = Runtime.getRuntime().availableProcessors();
        log.info("available processors = " + poolSize);
        this.pool = Executors.newFixedThreadPool(poolSize);

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

    private ExecutorService pool;
    private long completeCount = 0;
    private double maxDiff = -999.0;
    private double maxDiffTotal = 0;
    private OkawariParameter maxDiffParameter = new OkawariParameter();
    private long startTime = 0;
    private long size = 0L;
    private boolean isBatch = false;
    private int executeMaxSize = 50000;
    private OkawariParameter defaultParameter = OkawariParameter.getOkawariParameter();
    private double maxDiffAllTheTime = -999.0;


    private PKFXMiniDataGCSimulator createSimulator(List<Candle> candles, OkawariParameter p) {
        PKFXMiniDataGCSimulator sim1 = new PKFXMiniDataGCSimulator();
        sim1.setCandles(candles);
        sim1.setOkawariParameter(p);

        return sim1;
    }

    private void createParametersAndExecute(ExecutorService pool, List<Candle> candles) {

        log.info("creating parameters.");
        List<Double> paramA$parameters = OkawariParameterA$Macd.createParameters();
        List<Double> paramB$parameters = OkawariParameterB$Macd.createParameters();
        List<Double> paramC$parameters = OkawariParameterC$Adx.createParameters();
        List<Double> paramD$parameters = OkawariParameterD$Adx.createParameters();
        List<Double> paramE$parameters = OkawariParameterE$Rsi.createParameters();
        List<Double> paramF$parameters = OkawariParameterF$Rsi.createParameters();
        List<Double> paramG$parameters = OkawariParameterG$Bband.createParameters();
        List<Double> paramH$parameters = OkawariParameterH$Bband.createParameters();
        List<Double> paramI$parameters = OkawariParameterI$Sig.createParameters();
        List<Double> paramJ$parameters = OkawariParameterJ$Atr.createParameters();
        log.info("creating parameters, done.");

        Collections.shuffle(paramA$parameters);
        Collections.shuffle(paramB$parameters);
        Collections.shuffle(paramC$parameters);
        Collections.shuffle(paramD$parameters);
        Collections.shuffle(paramE$parameters);

        Collections.shuffle(paramF$parameters);
        Collections.shuffle(paramG$parameters);
        Collections.shuffle(paramH$parameters);
        Collections.shuffle(paramI$parameters);

        Collections.shuffle(paramJ$parameters);

        log.info("shuffle parameters, done.");

        List<List<Double>> paramMacds = Lists.cartesianProduct(
                paramA$parameters,
                paramB$parameters
        );
        log.info("cartesianProduct paramMacds, done. " + paramMacds.size());
        List<List<Double>> paramAdxs = Lists.cartesianProduct(
                paramC$parameters,
                paramD$parameters
        );
        log.info("cartesianProduct paramAdxs, done. " + paramAdxs.size());
        List<List<Double>> paramRsis = Lists.cartesianProduct(
                paramE$parameters,
                paramF$parameters
        );
        log.info("cartesianProduct paramRsis, done. " + paramRsis.size());
        List<List<Double>> paramBbands = Lists.cartesianProduct(
                paramG$parameters,
                paramH$parameters
        );
        log.info("cartesianProduct paramBbands, done. " + paramBbands.size());
        List<List<Double>> paramAtrs = Lists.cartesianProduct(
                paramI$parameters,
                paramJ$parameters
        );
        log.info("cartesianProduct paramAtrs, done. " + paramAtrs.size());


        this.size = (long) paramMacds.size() + paramAdxs.size() + paramRsis.size() + paramBbands.size() + paramAtrs.size();
        log.info("scheduled size = " + size);

        List<OkawariParameter> parameters = new ArrayList<>();
        for (List<Double> paramMacd : paramMacds) {
            OkawariParameter parameter = new OkawariParameter();

            parameter.setParamA(new OkawariParameterA$Macd(paramMacd.get(0)));
            parameter.setParamB(new OkawariParameterB$Macd(paramMacd.get(1)));
            parameter.setParamC(new OkawariParameterC$Adx(defaultParameter.getParamC().getParameter()));
            parameter.setParamD(new OkawariParameterD$Adx(defaultParameter.getParamD().getParameter()));
            parameter.setParamE(new OkawariParameterE$Rsi(defaultParameter.getParamE().getParameter()));
            parameter.setParamF(new OkawariParameterF$Rsi(defaultParameter.getParamF().getParameter()));
            parameter.setParamG(new OkawariParameterG$Bband(defaultParameter.getParamG().getParameter()));
            parameter.setParamH(new OkawariParameterH$Bband(defaultParameter.getParamH().getParameter()));
            parameter.setParamI(new OkawariParameterI$Sig(defaultParameter.getParamI().getParameter()));
            parameter.setParamJ(new OkawariParameterJ$Atr(defaultParameter.getParamJ().getParameter()));

            parameters.add(parameter);
        }
        for (List<Double> paramAdx : paramAdxs) {
            OkawariParameter parameter = new OkawariParameter();

            parameter.setParamA(new OkawariParameterA$Macd(defaultParameter.getParamA().getParameter()));
            parameter.setParamB(new OkawariParameterB$Macd(defaultParameter.getParamB().getParameter()));
            parameter.setParamC(new OkawariParameterC$Adx(paramAdx.get(0)));
            parameter.setParamD(new OkawariParameterD$Adx(paramAdx.get(0)));
            parameter.setParamE(new OkawariParameterE$Rsi(defaultParameter.getParamE().getParameter()));
            parameter.setParamF(new OkawariParameterF$Rsi(defaultParameter.getParamF().getParameter()));
            parameter.setParamG(new OkawariParameterG$Bband(defaultParameter.getParamG().getParameter()));
            parameter.setParamH(new OkawariParameterH$Bband(defaultParameter.getParamH().getParameter()));
            parameter.setParamI(new OkawariParameterI$Sig(defaultParameter.getParamI().getParameter()));
            parameter.setParamJ(new OkawariParameterJ$Atr(defaultParameter.getParamJ().getParameter()));

            parameters.add(parameter);
        }
        for (List<Double> paramRsi : paramRsis) {
            OkawariParameter parameter = new OkawariParameter();

            parameter.setParamA(new OkawariParameterA$Macd(defaultParameter.getParamA().getParameter()));
            parameter.setParamB(new OkawariParameterB$Macd(defaultParameter.getParamB().getParameter()));
            parameter.setParamC(new OkawariParameterC$Adx(defaultParameter.getParamC().getParameter()));
            parameter.setParamD(new OkawariParameterD$Adx(defaultParameter.getParamD().getParameter()));
            parameter.setParamE(new OkawariParameterE$Rsi(paramRsi.get(0)));
            parameter.setParamF(new OkawariParameterF$Rsi(paramRsi.get(1)));
            parameter.setParamG(new OkawariParameterG$Bband(defaultParameter.getParamG().getParameter()));
            parameter.setParamH(new OkawariParameterH$Bband(defaultParameter.getParamH().getParameter()));
            parameter.setParamI(new OkawariParameterI$Sig(defaultParameter.getParamI().getParameter()));
            parameter.setParamJ(new OkawariParameterJ$Atr(defaultParameter.getParamJ().getParameter()));

            parameters.add(parameter);
        }
        for (List<Double> paramBband : paramBbands) {
            OkawariParameter parameter = new OkawariParameter();

            parameter.setParamA(new OkawariParameterA$Macd(defaultParameter.getParamA().getParameter()));
            parameter.setParamB(new OkawariParameterB$Macd(defaultParameter.getParamB().getParameter()));
            parameter.setParamC(new OkawariParameterC$Adx(defaultParameter.getParamC().getParameter()));
            parameter.setParamD(new OkawariParameterD$Adx(defaultParameter.getParamD().getParameter()));
            parameter.setParamE(new OkawariParameterE$Rsi(defaultParameter.getParamE().getParameter()));
            parameter.setParamF(new OkawariParameterF$Rsi(defaultParameter.getParamF().getParameter()));
            parameter.setParamG(new OkawariParameterG$Bband(paramBband.get(0)));
            parameter.setParamH(new OkawariParameterH$Bband(paramBband.get(1)));
            parameter.setParamI(new OkawariParameterI$Sig(defaultParameter.getParamI().getParameter()));
            parameter.setParamJ(new OkawariParameterJ$Atr(defaultParameter.getParamJ().getParameter()));

            parameters.add(parameter);
        }
        for (List<Double> paramAtr : paramAtrs) {
            OkawariParameter parameter = new OkawariParameter();

            parameter.setParamA(new OkawariParameterA$Macd(defaultParameter.getParamA().getParameter()));
            parameter.setParamB(new OkawariParameterB$Macd(defaultParameter.getParamB().getParameter()));
            parameter.setParamC(new OkawariParameterC$Adx(defaultParameter.getParamC().getParameter()));
            parameter.setParamD(new OkawariParameterD$Adx(defaultParameter.getParamD().getParameter()));
            parameter.setParamE(new OkawariParameterE$Rsi(defaultParameter.getParamE().getParameter()));
            parameter.setParamF(new OkawariParameterF$Rsi(defaultParameter.getParamF().getParameter()));
            parameter.setParamG(new OkawariParameterG$Bband(defaultParameter.getParamG().getParameter()));
            parameter.setParamH(new OkawariParameterH$Bband(defaultParameter.getParamH().getParameter()));
            parameter.setParamI(new OkawariParameterI$Sig(paramAtr.get(0)));
            parameter.setParamJ(new OkawariParameterJ$Atr(paramAtr.get(1)));

            parameters.add(parameter);
        }

        Collections.shuffle(parameters);

        this.size = parameters.size();
        log.info("parameters size = " + size);

        FinderExecutor exec1 = new FinderExecutor(defaultParameter, candles);
        pool.submit(exec1);

        long count = 0;
        for (OkawariParameter parameter : parameters) {
            count++;
            FinderExecutor exec = new FinderExecutor(parameter, candles);

            try {
                pool.submit(exec);
                if (count % 1000 == 0) {
                    log.info("submit count:" + count + " complete count:" + completeCount);
                    sleep(5);
                }
            } catch (RejectedExecutionException ignored) {

            }
        }
    }

    public static void showMemoryUsage() {

        long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;

        long used = total - free;
        long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;

        if (log.isInfoEnabled()) {
            log.info("\n" + "memory usage {}(MB) / {}(MB) / {}(MB)", numberFormat(used), numberFormat(total),
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

        private final OkawariParameter parameter;
        private final List<Candle> candles;

        public FinderExecutor(OkawariParameter parameter,
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

            diff(diff, total, parameter, time);

        }

        private synchronized void diff(double diff,
                                       int total,
                                       OkawariParameter parameter,
                                       long time) {
            completeCount++;
            if (diff != 0.0 && diff >= maxDiff) {
                maxDiff = diff;
                maxDiffTotal = total;
                maxDiffParameter = parameter;
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            long averageTime = elapsedTime / completeCount;
            long remainTime = (size - completeCount) * averageTime;
            long remainTimeM = remainTime / 1000 / 60;
            long remainTimeH = remainTime / 1000 / 60 / 60;
            long remainTimeD = remainTimeH / 24;
            long remainTimeY = remainTimeD / 365;

            if (completeCount % 100 == 0) {
                StringBuilder s = new StringBuilder();
                s.append("\n");
                s.append("maxParam             : " + maxDiffParameter + "\n");
                s.append("maxDiff              : " + maxDiff + "\n");
                s.append("maxDiff(count)       : " + maxDiffTotal + "\n");
                s.append("completeCount        : " + completeCount + " / " + size + " " + ((double) completeCount / (double) size) * 100 + "%" + "\n");
                s.append("this time.           : " + time + " ms." + "\n");
                s.append("average time.        : " + averageTime + " ms." + "\n");
                s.append("elapsed total time.  : " + elapsedTime + " ms." + "\n");
                s.append("remain time(ms).     : " + remainTime + " ms." + "\n");
                s.append("remain time(M).      : " + remainTimeM + " M." + "\n");
                s.append("remain time(H).      : " + remainTimeH + " H." + "\n");
                s.append("remain time(Y).      : " + remainTimeY + " Y." + "\n");
                s.append("maxDiff allthetime   : " + maxDiffAllTheTime + "\n");

                log.info(s.toString());
                showMemoryUsage();
            }

            if (isBatch && completeCount > executeMaxSize) {
                pool.shutdownNow();
            }

        }
    }
}


