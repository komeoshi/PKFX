package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PKFXFinderAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(PKFXFinderAnalyzer.class);

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

    public boolean isMaOk(final List<Candle> candles){
        double aveShort = getMa(candles, PKFXConst.MA_SHORT_PERIOD);
        double aveMid = getMa(candles, PKFXConst.MA_MID_PERIOD);
        double aveLong = getMa(candles, PKFXConst.MA_LONG_PERIOD);

        Candle lastCandle = candles.get(candles.size() - 1);
        boolean b1 = lastCandle.getMid().getL() < aveShort;
        boolean b2 = lastCandle.getMid().getH() > aveShort;

        boolean b11 = aveShort > aveMid;
        boolean b12 = aveMid > aveLong;

        return b1 && b2 && lastCandle.isYousen();
        // return b1 && b2 && lastCandle.isYousen() && b11 && b12;

    }
    public double getVma(final List<Candle> candles, final int term){
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - term + 1;

        double ave = 0;
        for(int i= firstBar ; i <= lastBar; i++){
            ave += candles.get(i).getVolume();
        }
        ave = (ave / term);
        return ave;

    }
    public double getMa(final List<Candle> candles, final int term){
        int lastBar = candles.size() - 1;
        int firstBar = lastBar - term + 1;

        double ave = 0;
        for(int i= firstBar ; i <= lastBar; i++){
            ave += candles.get(i).getMid().getC();
        }
        ave = (ave / term);
        return ave;
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
            double change = candles.get(bar).getMid().getC() - candles.get(bar - 1).getMid().getC();
            if (change >= 0) {
                aveGain += change;
            } else {
                aveLoss += change;
            }
        }

        double rs = aveGain / Math.abs(aveLoss);
        double rsi = 100 - 100 / (1 + rs);

        rsi = rsi - 50;

        // log.info("RSI: " + rsi);
        return rsi;
    }

    public void setPosition(List<Candle> candles, boolean logging) {
        for (int ii = 0; ii < candles.size(); ii++) {
            Candle currentCandle = candles.get(ii);
            currentCandle.setNumber(ii);
            if (ii < 75) {
                continue;
            }

            List<Candle> currentCandles = new ArrayList<>();

            for (int jj = 0; jj < ii; jj++) {
                currentCandles.add(candles.get(jj));
            }
            double shortMa = getMa(currentCandles, 9);
            double longMa = getMa(currentCandles, 26);
            double superLongMa = getMa(currentCandles, 50);

            double shortVma = getVma(currentCandles, 5);
            double longVma = getVma(currentCandles, 10);

            currentCandle.setShortMa(shortMa);
            currentCandle.setLongMa(longMa);
            currentCandle.setSuperLongMa(superLongMa);

            currentCandle.setShortVma(shortVma);
            currentCandle.setLongVma(longVma);

            if (shortMa > longMa) {
                currentCandle.setPosition(Position.LONG);
            } else {
                currentCandle.setPosition(Position.SHORT);
            }
            if (logging) {
                log.info(ii + "/" + candles.size() + " " + currentCandle.getTime() + " " + currentCandle.getPosition());
            }
        }
    }
}
