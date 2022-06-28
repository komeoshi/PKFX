package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Parameter {
    private ParameterA$CurrentAtr paramA$01 = new ParameterA$CurrentAtr(24.299999999999997 / 1000);
    private ParameterA$CurrentTr paramA$02 = new ParameterA$CurrentTr(45.0 / 1000);
    private ParameterA$Past2Tr paramA$03 = new ParameterA$Past2Tr(59.0 / 1000);
    private ParameterA$Past3Atr paramA$04 = new ParameterA$Past3Atr(27.900000000000002 / 1000);
    private ParameterA$Past4Atr paramA$05 = new ParameterA$Past4Atr(31.099999999999998 / 1000);

    private ParameterB$Adx paramB$01 = new ParameterB$Adx(14000.0 / 1000);
    private ParameterB$Past2Rsi paramB$02 = new ParameterB$Past2Rsi(23000.0 / 1000);
    private ParameterB$Rsi paramB$03 = new ParameterB$Rsi(91000.0 / 1000);
    private ParameterB$Rsi paramB$04 = new ParameterB$Rsi(78000.0 / 1000);

    private ParameterC$Bband paramC$01 = new ParameterC$Bband(60.00000000000001 / 1000);
    private ParameterC$Bband paramC$02 = new ParameterC$Bband(260.00000000000006 / 1000);
    private ParameterC$DxBand paramC$03 = new ParameterC$DxBand(370.0000000000002 / 1000);
    private ParameterC$DxBand paramC$04 = new ParameterC$DxBand(320.00000000000017 / 1000);

    private ParameterD$Macd1 paramD$01 = new ParameterD$Macd1(0.05 / 1000);
    private ParameterD$Macd2 paramD$02 = new ParameterD$Macd2(5.0 / 1000);
    private ParameterD$Sig paramD$03 = new ParameterD$Sig(0.06999999999999999 / 1000);

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
