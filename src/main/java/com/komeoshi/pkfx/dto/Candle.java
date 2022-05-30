package com.komeoshi.pkfx.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Candle {
    private String time;
    private int volume;
    private boolean complete;

    private Mid mid;

    public String toString(){
        StringBuilder s = new StringBuilder();

        s.append("time:" + time + "\n");
        s.append("volume:" + volume + "\n");
        s.append("complete:" + complete + "\n");
        s.append("mid:" + mid + "\n");

        return s.toString();
    }
}