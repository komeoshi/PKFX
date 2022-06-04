package com.komeoshi.pkfx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Account {
    private String guaranteedStopLossOrderMode;
    private boolean hedgingEnabled;
    private String id;
    private LocalDateTime createdTime;
    private String currency;
    private Long createdByUserID;
    private String alias;
    private double marginRate;
    private String lastTransactionID;
    private double balance;
    private int openTradeCount;
    private int openPositionCount;
    private int pendingOrderCount;
    private double pl;
    private double resettablePL;
    private double resettablePLTime;
    private double financing;
    private double commission;
    private double dividendAdjustment;
    private double guaranteedExecutionFees;
    private double unrealizedPL;
    @JsonProperty("NAV")
    private double nav;
    private double marginUsed;
    private double marginAvailable;
    private double positionValue;
    private double marginCloseoutUnrealizedPL;
    private double marginCloseoutNAV;
    private double marginCloseoutMarginUsed;
    private double marginCloseoutPositionValue;
    private double marginCloseoutPercent;
    private double withdrawalLimit;



}
