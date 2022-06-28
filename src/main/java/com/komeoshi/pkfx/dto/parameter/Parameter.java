package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Parameter {

    public static Parameter getParameterSim(){
        Parameter parameter1 = new Parameter();
        parameter1.setParamA$01(new ParameterA$CurrentAtr(0));
        parameter1.setParamA$02(new ParameterA$CurrentTr(0));
        parameter1.setParamA$03(new ParameterA$Past2Tr(0));
        parameter1.setParamA$04(new ParameterA$Past3Atr(0));
        parameter1.setParamA$05(new ParameterA$Past4Atr(0));

        parameter1.setParamB$01(new ParameterB$Adx(15000.0 / 1000));
        parameter1.setParamB$02(new ParameterB$Past2Rsi(23000.0 / 1000));
        parameter1.setParamB$03(new ParameterB$Rsi(86000.0 / 1000));
        parameter1.setParamB$04(new ParameterB$Rsi(78000.0 / 1000));

        parameter1.setParamC$01(new ParameterC$Bband(52.0 / 1000));
        parameter1.setParamC$02(new ParameterC$Bband(300.0 / 1000));
        parameter1.setParamC$03(new ParameterC$DxBand(350.0 / 1000));
        parameter1.setParamC$04(new ParameterC$DxBand(350.0 / 1000));

        parameter1.setParamD$01(new ParameterD$Macd1(0.05 / 1000));
        parameter1.setParamD$02(new ParameterD$Macd2(5.0 / 1000));
        parameter1.setParamD$03(new ParameterD$Sig(0.06999999999999999 / 1000));

        return parameter1;
    }
    public static Parameter getParameter1(){
        Parameter parameter1 = new Parameter();
        parameter1.setParamA$01(new ParameterA$CurrentAtr(45.0 / 1000));
        parameter1.setParamA$02(new ParameterA$CurrentTr(44.99999999999999 / 1000));
        parameter1.setParamA$03(new ParameterA$Past2Tr(45.0 / 1000));
        parameter1.setParamA$04(new ParameterA$Past3Atr(75.0 / 1000));
        parameter1.setParamA$05(new ParameterA$Past4Atr(80.0 / 1000));

        parameter1.setParamB$01(new ParameterB$Adx(15000.0 / 1000));
        parameter1.setParamB$02(new ParameterB$Past2Rsi(23000.0 / 1000));
        parameter1.setParamB$03(new ParameterB$Rsi(86000.0 / 1000));
        parameter1.setParamB$04(new ParameterB$Rsi(78000.0 / 1000));

        parameter1.setParamC$01(new ParameterC$Bband(52.0 / 1000));
        parameter1.setParamC$02(new ParameterC$Bband(300.0 / 1000));
        parameter1.setParamC$03(new ParameterC$DxBand(350.0 / 1000));
        parameter1.setParamC$04(new ParameterC$DxBand(350.0 / 1000));

        parameter1.setParamD$01(new ParameterD$Macd1(0.05 / 1000));
        parameter1.setParamD$02(new ParameterD$Macd2(5.0 / 1000));
        parameter1.setParamD$03(new ParameterD$Sig(0.06999999999999999 / 1000));

        return parameter1;
    }

    public static Parameter getParameter2(){
        Parameter parameter2 = new Parameter();
        parameter2.setParamA$01(new ParameterA$CurrentAtr(15.0 / 1000));
        parameter2.setParamA$02(new ParameterA$CurrentTr(15.0 / 1000));
        parameter2.setParamA$03(new ParameterA$Past2Tr(15.0 / 1000));
        parameter2.setParamA$04(new ParameterA$Past3Atr(15.0 / 1000));
        parameter2.setParamA$05(new ParameterA$Past4Atr(15.0 / 1000));

        parameter2.setParamB$01(new ParameterB$Adx(15000.0 / 1000));
        parameter2.setParamB$02(new ParameterB$Past2Rsi(21000.0 / 1000));
        parameter2.setParamB$03(new ParameterB$Rsi(86000.0 / 1000));
        parameter2.setParamB$04(new ParameterB$Rsi(78000.0 / 1000));

        parameter2.setParamC$01(new ParameterC$Bband(139.99999999999997 / 1000));
        parameter2.setParamC$02(new ParameterC$Bband(170.0 / 1000));
        parameter2.setParamC$03(new ParameterC$DxBand(280.0000000000001 / 1000));
        parameter2.setParamC$04(new ParameterC$DxBand(340.00000000000017 / 1000));

        parameter2.setParamD$01(new ParameterD$Macd1(0.05 / 1000));
        parameter2.setParamD$02(new ParameterD$Macd2(5.0 / 1000));
        parameter2.setParamD$03(new ParameterD$Sig(0.06999999999999999 / 1000));

        return parameter2;
    }

    public static Parameter getParameter3(){
        Parameter parameter3 = new Parameter();
        parameter3.setParamA$01(new ParameterA$CurrentAtr(0));
        parameter3.setParamA$02(new ParameterA$CurrentTr(0));
        parameter3.setParamA$03(new ParameterA$Past2Tr(0));
        parameter3.setParamA$04(new ParameterA$Past3Atr(0));
        parameter3.setParamA$05(new ParameterA$Past4Atr(0));

        parameter3.setParamB$01(new ParameterB$Adx(10000.0 / 1000));
        parameter3.setParamB$02(new ParameterB$Past2Rsi(37000.0 / 1000));
        parameter3.setParamB$03(new ParameterB$Rsi(86000.0 / 1000));
        parameter3.setParamB$04(new ParameterB$Rsi(88000.0 / 1000));

        parameter3.setParamC$01(new ParameterC$Bband(139.99999999999997 / 1000));
        parameter3.setParamC$02(new ParameterC$Bband(170.0 / 1000));
        parameter3.setParamC$03(new ParameterC$DxBand(170.00000000000003 / 1000));
        parameter3.setParamC$04(new ParameterC$DxBand(300.00000000000017 / 1000));

        parameter3.setParamD$01(new ParameterD$Macd1(0.05 / 1000));
        parameter3.setParamD$02(new ParameterD$Macd2(5.0 / 1000));
        parameter3.setParamD$03(new ParameterD$Sig(0.06999999999999999 / 1000));

        return parameter3;
    }

    private ParameterA$CurrentAtr paramA$01 = new ParameterA$CurrentAtr(99999);
    private ParameterA$CurrentTr paramA$02 = new ParameterA$CurrentTr(99999);
    private ParameterA$Past2Tr paramA$03 = new ParameterA$Past2Tr(99999);
    private ParameterA$Past3Atr paramA$04 = new ParameterA$Past3Atr(99999);
    private ParameterA$Past4Atr paramA$05= new ParameterA$Past4Atr(99999);

    private ParameterB$Adx paramB$01= new ParameterB$Adx(99999);
    private ParameterB$Past2Rsi paramB$02 = new ParameterB$Past2Rsi(99999);
    private ParameterB$Rsi paramB$03 = new ParameterB$Rsi(99999);
    private ParameterB$Rsi paramB$04 = new ParameterB$Rsi(99999);

    private ParameterC$Bband paramC$01 =new ParameterC$Bband(99999) ;
    private ParameterC$Bband paramC$02 = new ParameterC$Bband(99999);
    private ParameterC$DxBand paramC$03= new ParameterC$DxBand(99999);
    private ParameterC$DxBand paramC$04 = new ParameterC$DxBand(99999);

    private ParameterD$Macd1 paramD$01=new ParameterD$Macd1(99999) ;
    private ParameterD$Macd2 paramD$02= new ParameterD$Macd2(99999);
    private ParameterD$Sig paramD$03=new ParameterD$Sig(99999);

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
