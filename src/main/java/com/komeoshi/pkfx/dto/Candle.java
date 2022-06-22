package com.komeoshi.pkfx.dto;

import com.komeoshi.pkfx.enumerator.AdxPosition;
import com.komeoshi.pkfx.enumerator.Position;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Candle implements Serializable {
    private LocalDateTime time;
    private int volume;
    private boolean complete;

    private Mid bid;
    private Mid ask;
    private double spread;
    private double spreadMa;

    private ArrayList<Candle> candles;
    private int number = -1;
    private Position position = Position.NONE;
    private Position emaPosition = Position.NONE;
    private Position shortEmaPosition = position.NONE;
    private Position sigPosition = Position.NONE;
    private double superShortMa;
    private double superShortLongMa;
    private double shortMa;
    private double longMa;
    private double superLongMa;
    private double shortVma;
    private double longVma;
    private double superShortEma;
    private double shortEma;
    private double longEma;
    private double tr;
    private double atr;
    private Dm dm;
    private Adx adx;
    private AdxPosition adxPosition = AdxPosition.NONE;

    private double macd;
    private double sig;
    private double shortSig;
    private double rsi;
    private Candle pastCandle;

    public boolean isYousen() {
        // 始値よりも終値が高ければ陽線
        return this.getAsk().getC() > this.getAsk().getO();
    }

    public boolean isInsen() {
        return !isYousen();
    }

    /**
     * ローソクの長さ閾値を達成するか.
     *
     * @param candleLengthMagnification 閾値
     * @return true:ローソクの長さ閾値を達成する
     */
    public boolean isLengthEnough(double candleLengthMagnification) {
        // ローソクの長さ閾値
        double threshold = (this.getAsk().getO() * candleLengthMagnification);
        return this.getAsk().getC() > threshold;
    }

    public String toString(){
        StringBuilder s = new StringBuilder();

        s.append("time:" + time + "\n");
        s.append("volume:" + volume + "\n");
        s.append("complete:" + complete + "\n");
        s.append("ask:" + ask + "\n");
        s.append("bid:" + bid + "\n");

        return s.toString();
    }
}