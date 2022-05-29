package com.komeoshi.pkfx.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Instrument {
    private String instrument;
    private String granularity;
    private List<Candle> candles;

    public String toString(){
        StringBuilder s = new StringBuilder();

        s.append("instrument:" + instrument + "\n");
        s.append("granularity:" + granularity + "\n");
        s.append("candles:" + candles + "\n");

        return s.toString();
    }
}
