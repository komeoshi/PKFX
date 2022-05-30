package com.komeoshi.pkfx.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Trades {
    private List<Trade> trades;
    private String lastTransactionID;
}
