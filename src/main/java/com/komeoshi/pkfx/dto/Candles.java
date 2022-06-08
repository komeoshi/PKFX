package com.komeoshi.pkfx.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class Candles implements Serializable {
    List<Candle> candles;
}
