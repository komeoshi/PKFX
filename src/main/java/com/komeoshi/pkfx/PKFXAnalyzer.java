package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import com.komeoshi.pkfx.enumerator.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
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

    public double getEma(List<Candle> candles, int term) {
        double[] results = new double[candles.size()];

        calculateEmasHelper(candles, term, candles.size() - 1, results);

        return results[candles.size()-1];
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

        double rs = aveGain / Math.abs(aveLoss);

        return 100 - 100 / (1 + rs);
    }

    public boolean checkActiveTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        DayOfWeek w = currentTime.getDayOfWeek();
        int h = currentTime.getHour();

        return w != DayOfWeek.MONDAY || h != 6;
    }

    public void setPosition(List<Candle> candles, boolean logging) {
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

            double shortEma = getEma(currentCandles, 9);
            double longEma = getEma(currentCandles, 26);

            double macd = Math.abs(longMa - shortMa);
            currentCandle.setMacd(macd);

            double sig = getSig(currentCandles, 50);
            currentCandle.setSig(sig);

            double shortSig = getSig(currentCandles, 9);
            currentCandle.setShortSig(shortSig);

            double rsi = getRsi(currentCandles);
            currentCandle.setRsi(rsi);

            ArrayList<Candle> candlesForSave = new ArrayList<>();
            for (int jj = ii - 60; jj < ii; jj++) {
                candlesForSave.add(candles.get(jj));
            }
            currentCandle.setCandles(candlesForSave);

            if (shortMa > longMa) {
                currentCandle.setPosition(Position.LONG);
            } else {
                currentCandle.setPosition(Position.SHORT);
            }

            if (superShortMa > superShortLongMa) {
                currentCandle.setSuperShortPosition(Position.LONG);
            } else {
                currentCandle.setSuperShortPosition(Position.SHORT);
            }

            if(shortEma > longEma){
                currentCandle.setEmaPosition(Position.LONG);
            } else {
                currentCandle.setEmaPosition(Position.SHORT);
            }

            double spread = Math.abs(currentCandle.getAsk().getC() - currentCandle.getBid().getC());
            currentCandle.setSpread(spread);
            double spreadMa = getSpreadMa(currentCandles, 9);
            currentCandle.setSpreadMa(spreadMa);

            if (logging && ii % 10000 == 0) {
                long endTime = System.currentTimeMillis();
                log.info(ii + "/" + candles.size() + " " + currentCandle.getTime() + " " + currentCandle.getPosition() +
                        " " + currentCandle.getSig() +
                        " " + (endTime - startTime));
            }
        }
    }
}
