package com.komeoshi.pkfx.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Trade {
    private String id;
    private String instrument;
    private double price;
    private LocalDateTime openTime;
    private int initialUnits;
    private double initialMarginRequired;
    private String state;
    private int currentUnits;
    private double realizedPL;
    private double financing;
    private double dividendAdjustment;
    private double unrealizedPL;
    private double marginUsed;
}
