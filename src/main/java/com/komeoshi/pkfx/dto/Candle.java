package com.komeoshi.pkfx.dto;

import com.komeoshi.pkfx.Position;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class Candle implements Serializable {
    private LocalDateTime time;
    private int volume;
    private boolean complete;

    private Mid mid;

    private int number = -1;
    private Position position = Position.NONE;
    private double shortMa;
    private double longMa;
    private double superLongMa;
    private double shortVma;
    private double longVma;
    private double macd;
    private double sig;
    private double rsi;
    private Candle pastCandle;

    public boolean isYousen() {
        // 始値よりも終値が高ければ陽線
        return this.getMid().getC() > this.getMid().getO();
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
        double threshold = (this.getMid().getO() * candleLengthMagnification);
        return this.getMid().getC() > threshold;
    }

    public String toString(){
        StringBuilder s = new StringBuilder();

        s.append("time:" + time + "\n");
        s.append("volume:" + volume + "\n");
        s.append("complete:" + complete + "\n");
        s.append("mid:" + mid + "\n");

        return s.toString();
    }
}