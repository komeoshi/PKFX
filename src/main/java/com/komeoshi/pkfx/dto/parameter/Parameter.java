package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Parameter {
    private ParameterA$AtrOrTr paramA$01 = new ParameterA$AtrOrTr(0.0243);
    private ParameterA$AtrOrTr paramA$02 = new ParameterA$AtrOrTr(0.0450);
    private ParameterA$AtrOrTr paramA$03 = new ParameterA$AtrOrTr(0.0590);
    private ParameterA$AtrOrTr paramA$04 = new ParameterA$AtrOrTr(0.0279);
    private ParameterA$AtrOrTr paramA$05 = new ParameterA$AtrOrTr(0.0311);

    private ParameterB$Adx paramB$01 = new ParameterB$Adx(14);
    private ParameterB$Rsi paramB$02 = new ParameterB$Rsi(23);
    private ParameterB$Rsi paramB$03 = new ParameterB$Rsi(91);
    private ParameterB$Rsi paramB$04 = new ParameterB$Rsi(78);

    private ParameterC$Bband paramC$01 = new ParameterC$Bband(0.052);
    private ParameterC$Bband paramC$02 = new ParameterC$Bband(0.300);
    private ParameterC$DxBand paramC$03 = new ParameterC$DxBand(0.350);
    private ParameterC$DxBand paramC$04 = new ParameterC$DxBand(0.350);

    private ParameterD$Macd1 paramD$01 = new ParameterD$Macd1(0.00005);
    private ParameterD$Macd2 paramD$02 = new ParameterD$Macd2(0.005);
    private ParameterD$Sig paramD$03 = new ParameterD$Sig(0.00007);

    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("\n");
        s.append("paramA$01  :" + paramA$01.parameter * 1000 + "( / 1000)\n");
        s.append("paramA$02  :" + paramA$02.parameter * 1000 + "( / 1000)\n");
        s.append("paramA$03  :" + paramA$03.parameter * 1000 + "( / 1000)\n");
        s.append("paramA$04  :" + paramA$04.parameter * 1000 + "( / 1000)\n");
        s.append("paramA$05  :" + paramA$05.parameter * 1000 + "( / 1000)\n");

        s.append("paramB$01  :" + paramB$01.parameter * 1000 + "( / 1000)\n");
        s.append("paramB$02  :" + paramB$02.parameter * 1000 + "( / 1000)\n");
        s.append("paramB$03  :" + paramB$03.parameter * 1000 + "( / 1000)\n");
        s.append("paramB$04  :" + paramB$04.parameter * 1000 + "( / 1000)\n");

        s.append("paramC$01  :" + paramC$01.parameter * 1000 + "( / 1000)\n");
        s.append("paramC$02  :" + paramC$02.parameter * 1000 + "( / 1000)\n");
        s.append("paramC$03  :" + paramC$03.parameter * 1000 + "( / 1000)\n");
        s.append("paramC$04  :" + paramC$04.parameter * 1000 + "( / 1000)\n");

        s.append("paramD$01  :" + paramD$01.parameter * 1000 + "( / 1000)\n");
        s.append("paramD$02  :" + paramD$02.parameter * 1000 + "( / 1000)\n");
        s.append("paramD$03  :" + paramD$03.parameter * 1000 + "( / 1000)\n");


        return s.toString();
    }
}
