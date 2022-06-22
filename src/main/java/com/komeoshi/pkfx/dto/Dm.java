package com.komeoshi.pkfx.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Dm implements Serializable {
    private double plusDm = -99;
    private double minusDm = -99;
}
