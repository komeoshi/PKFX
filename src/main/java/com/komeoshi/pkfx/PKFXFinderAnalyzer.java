package com.komeoshi.pkfx;

import com.komeoshi.pkfx.dto.Candle;

public class PKFXFinderAnalyzer {

    private Candle candle;

    public PKFXFinderAnalyzer(Candle candle){
        this.candle = candle;
    }

    /**
     * シグナル点灯を判定
     * @param candleLengthMagnification 閾値
     * @return true: シグナル点灯
     */
    public boolean isSignal(double candleLengthMagnification){
        return candle.isYousen() &&
                candle.isLengthEnough(candleLengthMagnification);
    }
}
