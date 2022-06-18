package com.komeoshi.pkfx.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Adx implements Serializable {

    private double plusDi;
    private double minusDi;
    private double dx;
    private double Adx;
}
