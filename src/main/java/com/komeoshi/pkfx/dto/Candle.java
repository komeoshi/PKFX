package com.komeoshi.pkfx.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Candle {
    private String time;
    private Double openBid;
    private Double openAsk;
    private Double highBid;
    private Double highAsk;
    private Double lowBid;
    private Double lowAsk;
    private Double closeBid;
    private Double closeAsk;
    private Integer volume;
    private Boolean complete;

    public String toString(){
        StringBuilder s = new StringBuilder();

        s.append("time:" + time + "\n");
        s.append("openBid:" + openBid + "\n");
        s.append("openAsk:" + openAsk + "\n");
        s.append("highBid:" + highBid + "\n");
        s.append("highAsk:" + highAsk + "\n");
        s.append("lowBid:" + lowBid + "\n");
        s.append("lowAsk:" + lowAsk + "\n");
        s.append("closeBid:" + closeBid + "\n");
        s.append("closeAsk:" + closeAsk + "\n");
        s.append("volume:" + volume + "\n");
        s.append("complete:" + complete + "\n");

        return s.toString();
    }
}