package com.komeoshi.pkfx.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mid {
    private double o;
    private double h;
    private double l;
    private double c;

    public String toString(){
        StringBuilder s = new StringBuilder();

        s.append("o:" + o + "\n");
        s.append("h:" + h + "\n");
        s.append("l:" + l + "\n");
        s.append("c:" + c + "\n");


        return s.toString();
    }

}
