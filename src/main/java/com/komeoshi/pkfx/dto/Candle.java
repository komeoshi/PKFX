package com.komeoshi.pkfx.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Candle {
    private LocalDateTime time;
    private int volume;
    private boolean complete;

    private Mid mid;

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