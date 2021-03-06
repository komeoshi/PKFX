package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Adx;
import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.dto.Dm;
import com.komeoshi.pkfx.dto.Mid;
import com.komeoshi.pkfx.enumerator.AdxPosition;
import com.komeoshi.pkfx.enumerator.BBPosition;
import com.komeoshi.pkfx.enumerator.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DatabaseMetaData;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class PKFXAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(PKFXAnalyzer.class);

    /**
     * シグナル点灯を判定
     *
     * @param candleLengthMagnification 閾値
     * @return true: シグナル点灯
     */
    public boolean isSignal(Candle candle, double candleLengthMagnification) {
        return candle.isYousen() &&
                candle.isLengthEnough(candleLengthMagnification);
    }

    public boolean isMaOk(final List<Candle> candles) {
        double aveShort = getMa(candles, PKFXConst.MA_SHORT_PERIOD);
        double aveMid = getMa(candles, PKFXConst.MA_MID_PERIOD);
        double aveLong = getMa(candles, PKFXConst.MA_LONG_PERIOD);

        Candle lastCandle = candles.get(candles.size() - 1);
        boolean b1 = lastCandle.getAsk().getL() < aveShort;
        boolean b2 = lastCandle.getAsk().getH() > aveShort;

        boolean b11 = aveShort > aveMid;
        boolean b12 = aveMid > aveLong;

        return b1 && b2 && lastCandle.isYousen();
        // return b1 && b2 && lastCandle.isYousen() && b11 && b12;

    }

    public double getVma(final List<Candle> candles, final int term) {
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - term + 1;

        double ave = 0;
        for (int i = firstBar; i <= lastBar; i++) {
            ave += candles.get(i).getVolume();
        }
        ave = (ave / term);
        return ave;
    }

    public double getSig(final List<Candle> candles, final int term) {
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - term + 1;

        double ave = 0;
        for (int i = firstBar; i <= lastBar; i++) {
            ave += candles.get(i).getMacd();
        }
        ave = (ave / term);
        return ave;
    }

    public double getSpreadMa(final List<Candle> candles, final int term) {
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - term + 1;

        double ave = 0;
        for (int i = firstBar; i <= lastBar; i++) {
            ave += candles.get(i).getSpread();
        }
        ave = (ave / term);
        return ave;
    }

    public double getMa(final List<Candle> candles, final int term) {
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - term + 1;

        double ave = 0;
        for (int i = firstBar; i <= lastBar; i++) {
            ave += candles.get(i).getAsk().getC();
        }
        ave = (ave / term);
        return ave;
    }

    public double getAtr(List<Candle> candles, int term) {
        double[] results = new double[candles.size()];

        calculateAtrHelper(candles, term, candles.size() - 1, results);

        return results[candles.size() - 1];
    }

    public static double calculateAtrHelper(List<Candle> candles, double term, int i, double[] results) {

        if (i == 0) {
            results[0] = candles.get(0).getTr();
            return results[0];
        } else {
            double close = candles.get(i).getTr();
            double factor = (2.0 / (term + 1));
            double ema = close * factor + (1 - factor) * calculateAtrHelper(candles, term, i - 1, results);
            results[i] = ema;
            return ema;
        }

    }

    public double getEma(List<Candle> candles, int term) {
        double[] results = new double[candles.size()];

        calculateEmasHelper(candles, term, candles.size() - 1, results);

        return results[candles.size() - 1];
    }

    public static double calculateEmasHelper(List<Candle> candles, double term, int i, double[] results) {

        if (i == 0) {
            results[0] = candles.get(0).getAsk().getC();
            return results[0];
        } else {
            double close = candles.get(i).getAsk().getC();
            double factor = (2.0 / (term + 1));
            double ema = close * factor + (1 - factor) * calculateEmasHelper(candles, term, i - 1, results);
            results[i] = ema;
            return ema;
        }

    }

    public double getRsi(final List<Candle> candles) {
        int periodLength = 14;
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - periodLength + 1;
        if (firstBar < 0) {
            String msg = "Quote history length " + candles.size() + " is insufficient to calculate the indicator.";
            log.info(msg);
            return -100;
        }

        double aveGain = 0, aveLoss = 0;
        for (int bar = firstBar + 1; bar <= lastBar; bar++) {
            double change = candles.get(bar).getAsk().getC() - candles.get(bar - 1).getAsk().getC();
            if (change >= 0) {
                aveGain += change;
            } else {
                aveLoss += change;
            }
        }

        double change = aveGain + Math.abs(aveLoss);

        return (change == 0) ? 50 : (100 * aveGain / change);

    }

    public boolean checkActiveTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        DayOfWeek w = currentTime.getDayOfWeek();
        int h = currentTime.getHour();

        return w != DayOfWeek.MONDAY || h != 6;
    }

    private double getTrueRange(List<Candle> candles) {
        Candle currentCandle = candles.get(candles.size() - 1);
        Candle lastCandle = candles.get(candles.size() - 2);

        double a1 = currentCandle.getAsk().getH() - currentCandle.getAsk().getL();
        double a2 = currentCandle.getAsk().getH() - currentCandle.getAsk().getC();
        double a3 = lastCandle.getAsk().getC() - currentCandle.getAsk().getL();
        return Math.max(Math.max(a1, a2), a3);
    }

    private Adx getDx(List<Candle> candles) {
        int term = 14;

        double totalPlusDm = 0.0;
        double totalMinusDm = 0.0;
        double totalTrueRange = 0.0;
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - term + 1;

        for (int bar = firstBar + 1; bar <= lastBar; bar++) {
            Candle c = candles.get(bar);

            if (c.getDm() == null) {
                continue;
            }
            totalPlusDm += c.getDm().getPlusDm();
            totalMinusDm += c.getDm().getMinusDm();
            totalTrueRange += c.getTr();

        }

        double plusDi = (totalPlusDm / totalTrueRange) * 100;
        double minusDi = (totalMinusDm / totalTrueRange) * 100;
        double dx_ = ((Math.abs(plusDi - minusDi)) / (plusDi + minusDi)) * 100;

        Adx dx = new Adx();
        dx.setPlusDi(plusDi);
        dx.setMinusDi(minusDi);
        dx.setDx(dx_);

        return dx;
    }

    public double getAdx(final List<Candle> candles, final int term) {
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - term + 1;

        double ave = 0;
        int count = 0;
        for (int i = firstBar; i <= lastBar; i++) {
            if (candles.get(i).getAdx() == null) {
                continue;
            }
            count++;
            ave += candles.get(i).getAdx().getDx();
        }
        ave = (ave / count);
        return ave;
    }

    private Dm getDm(List<Candle> candles) {
        Dm dm = new Dm();

        Candle currentCandle = candles.get(candles.size() - 1);
        Candle lastCandle = candles.get(candles.size() - 2);

        double plusDM = currentCandle.getAsk().getH() - lastCandle.getAsk().getH();
        double minusDM = lastCandle.getAsk().getL() - currentCandle.getAsk().getL();

        if (plusDM < 0)
            plusDM = 0;
        if (minusDM < 0)
            minusDM = 0;
        if (plusDM > minusDM)
            minusDM = 0;
        if (minusDM > plusDM)
            plusDM = 0;

        dm.setPlusDm(plusDM);
        dm.setMinusDm(minusDM);

        return dm;
    }

    public static void main(String[] args) {
        List<Candle> datas = new ArrayList<>();
        {
            Candle c1 = new Candle();
            Mid m1 = new Mid();
            m1.setC(5);
            c1.setAsk(m1);
            datas.add(c1);
        }
        {
            Candle c1 = new Candle();
            Mid m1 = new Mid();
            m1.setC(10);
            c1.setAsk(m1);
            datas.add(c1);
        }
        {
            Candle c1 = new Candle();
            Mid m1 = new Mid();
            m1.setC(2);
            c1.setAsk(m1);
            datas.add(c1);
        }
        {
            Candle c1 = new Candle();
            Mid m1 = new Mid();
            m1.setC(8);
            c1.setAsk(m1);
            datas.add(c1);
        }
        {
            Candle c1 = new Candle();
            Mid m1 = new Mid();
            m1.setC(5);
            c1.setAsk(m1);
            datas.add(c1);
        }

        PKFXAnalyzer a = new PKFXAnalyzer();
        double std = a.getStandard(datas, datas.size());

        log.info("std:" + std);
    }

    public double getStandard(List<Candle> candles, int term) {
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - term + 1;

        double ave = 0;
        int count = 0;
        for (int i = firstBar; i <= lastBar; i++) {
            count++;
            ave += candles.get(i).getAsk().getC();
        }
        ave = (ave / count);


        double sum2 = 0;
        for (int i = firstBar; i <= lastBar; i++) {
            sum2 = sum2 +
                    (candles.get(i).getAsk().getC() - ave) * (candles.get(i).getAsk().getC() - ave);
        }
        return Math.sqrt(sum2 / 5);
    }

    public void setPosition(List<Candle> candles, boolean logging) {
        setPosition(candles, logging, 80.0);
    }

    public void setPosition(List<Candle> candles, boolean logging, double param) {
        long startTimeAll = System.currentTimeMillis();
        for (int ii = 0; ii < candles.size(); ii++) {

            final int MAXSIZE = 200;

            long startTime = System.currentTimeMillis();

            Candle currentCandle = candles.get(ii);
            currentCandle.setNumber(ii);
            if (ii < MAXSIZE) {
                continue;
            }

            ArrayList<Candle> currentCandles = new ArrayList<>();
            for (int jj = ii - MAXSIZE; jj < ii; jj++) {
                currentCandles.add(candles.get(jj));
            }
            currentCandle.setPastCandle(candles.get(ii - 9));

            double superShortMa = getMa(currentCandles, 5);
            double superShortLongMa = getMa(currentCandles, 13);
            double shortMa = getMa(currentCandles, PKFXConst.MA_SHORT_PERIOD);
            double longMa = getMa(currentCandles, PKFXConst.MA_MID_PERIOD);
            double superLongMa = getMa(currentCandles, PKFXConst.MA_LONG_PERIOD);

            double shortVma = getVma(currentCandles, PKFXConst.VMA_SHORT_PERIOD);
            double longVma = getVma(currentCandles, PKFXConst.VMA_LONG_PERIOD);

            currentCandle.setSuperShortMa(superShortMa);
            currentCandle.setSuperShortLongMa(superShortLongMa);
            currentCandle.setShortMa(shortMa);
            currentCandle.setLongMa(longMa);
            currentCandle.setSuperLongMa(superLongMa);

            currentCandle.setShortVma(shortVma);
            currentCandle.setLongVma(longVma);

            double superShortEma = getEma(currentCandles, 1);
            double shortEma = getEma(currentCandles, 9);
            double longEma = getEma(currentCandles, 26);

            currentCandle.setSuperShortEma(superShortEma);
            currentCandle.setShortEma(shortEma);
            currentCandle.setLongEma(longEma);

            double macd = shortEma - longEma;
            currentCandle.setMacd(macd);

            double sig = getSig(currentCandles, 9);
            currentCandle.setSig(sig);

            double shortSig = getSig(currentCandles, 1);
            currentCandle.setShortSig(shortSig);
            if (macd > sig) {
                currentCandle.setMacdPosition(Position.LONG);
            } else {
                currentCandle.setMacdPosition(Position.SHORT);
            }

            double rsi = getRsi(currentCandles);
            currentCandle.setRsi(rsi);

            double tr = getTrueRange(currentCandles);
            currentCandle.setTr(tr);

            double atr = getAtr(currentCandles, 9);
            currentCandle.setAtr(atr);
            double longAtr = getAtr(currentCandles, 26);
            currentCandle.setLongAtr(longAtr);

            if (atr > longAtr) {
                currentCandle.setAtrPosition(Position.LONG);
            } else {
                currentCandle.setAtrPosition(Position.SHORT);
            }

            ArrayList<Candle> candlesForSave = new ArrayList<>();
            for (int jj = ii - 25; jj < ii; jj++) {
                candlesForSave.add(candles.get(jj));
            }
            currentCandle.setCandles(candlesForSave);

            if (shortMa > longMa) {
                currentCandle.setPosition(Position.LONG);
            } else {
                currentCandle.setPosition(Position.SHORT);
            }

            if (shortEma > longEma) {
                currentCandle.setEmaPosition(Position.LONG);
            } else {
                currentCandle.setEmaPosition(Position.SHORT);
            }

            if (superShortEma > longEma) {
                currentCandle.setShortEmaPosition(Position.LONG);
            } else {
                currentCandle.setShortEmaPosition(Position.SHORT);
            }

            if (shortSig > sig) {
                currentCandle.setSigPosition(Position.LONG);
            } else {
                currentCandle.setSigPosition(Position.SHORT);
            }

            double spread = Math.abs(currentCandle.getAsk().getC() - currentCandle.getBid().getC());
            currentCandle.setSpread(spread);
            double spreadMa = getSpreadMa(currentCandles, 9);
            currentCandle.setSpreadMa(spreadMa);

            Dm dm = getDm(currentCandles);
            currentCandle.setDm(dm);

            Adx dx = getDx(currentCandles);
            currentCandle.setAdx(dx);

            double adx = getAdx(currentCandles, 14);
            dx.setAdx(adx);
            currentCandle.setAdx(dx);

            if (dx.getPlusDi() > dx.getMinusDi()) {
                currentCandle.setAdxPosition(AdxPosition.OVER);
            } else {
                currentCandle.setAdxPosition(AdxPosition.UNDER);
            }

            double bollingerBand = getMa(currentCandles, 20);
            double bollingerBandHigh = bollingerBand + (getStandard(currentCandles, 20));
            double bollingerBandLow = bollingerBand - (getStandard(currentCandles, 20));

            currentCandle.setBollingerBand(bollingerBand);
            currentCandle.setBollingerBandHigh(bollingerBandHigh);
            currentCandle.setBollingerBandLow(bollingerBandLow);

            boolean isHigh = currentCandle.isYousen() &&
                    currentCandle.getBollingerBandHigh() < currentCandle.getAsk().getC();
            boolean isLow = currentCandle.isInsen() &&
                    currentCandle.getBollingerBandLow() > currentCandle.getAsk().getC();

            if (isHigh) {
                currentCandle.setBbPosition(BBPosition.OVER);
            } else if (isLow) {
                currentCandle.setBbPosition(BBPosition.UNDER);
            } else {
                currentCandle.setBbPosition(BBPosition.NONE);
            }

            if (logging && ii % 1_000_000 == 0) {
                long endTime = System.currentTimeMillis();

                long elapsedTime = endTime - startTimeAll;
                double aveTime = (double) elapsedTime / (double) ii;
                log.info(ii + "/" + candles.size() + " " + ((double) ii / (double) candles.size()) * 100 + "% " +
                        currentCandle.getTime() +
                        " " + (endTime - startTime) + " " + aveTime + " ");
                showMemoryUsage();
            }
        }
    }

    public static void showMemoryUsage() {

        long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;

        long used = total - free;
        long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;

        if (log.isInfoEnabled()) {
            log.info("memory usage {}(MB) / {}(MB) / {}(MB)", numberFormat(used), numberFormat(total),
                    numberFormat(max));
        }
    }

    public static String numberFormat(long l) {
        NumberFormat nfNum = NumberFormat.getNumberInstance();
        return nfNum.format(l);
    }

    public static void sleep(int sec) {
        try {
            Thread.sleep(sec * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static long daysCount(List<Candle> candles) {
        LocalDateTime from = candles.get(0).getTime();
        LocalDateTime to = candles.get(candles.size() - 1).getTime();
        return ChronoUnit.DAYS.between(from, to);
    }

}
