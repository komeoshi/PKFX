package com.komeoshi.pkfx.dto.parameter.okawari;

import com.komeoshi.pkfx.dto.parameter.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OkawariParameter {
    public static OkawariParameter getOkawariParameter() {
        OkawariParameter parameter1 = new OkawariParameter();

        parameter1.setParamA(new OkawariParameterA$Macd(0.0875));
        parameter1.setParamB(new OkawariParameterB$Macd(0.007));
        parameter1.setParamC(new OkawariParameterC$Adx(55));
        parameter1.setParamD(new OkawariParameterD$Adx(25));
        parameter1.setParamE(new OkawariParameterE$Rsi(90));
        parameter1.setParamF(new OkawariParameterF$Rsi(20));
        parameter1.setParamG(new OkawariParameterG$Bband(0.380));
        parameter1.setParamH(new OkawariParameterH$Bband(0.097));
        parameter1.setParamI(new OkawariParameterI$Sig(0.004));
        parameter1.setParamJ(new OkawariParameterJ$Atr(0.016));

        return parameter1;
    }

    private OkawariParameterA$Macd paramA = new OkawariParameterA$Macd(9999);
    private OkawariParameterB$Macd paramB = new OkawariParameterB$Macd(9999);
    private OkawariParameterC$Adx paramC = new OkawariParameterC$Adx(9999);
    private OkawariParameterD$Adx paramD = new OkawariParameterD$Adx(9999);
    private OkawariParameterE$Rsi paramE = new OkawariParameterE$Rsi(9999);
    private OkawariParameterF$Rsi paramF = new OkawariParameterF$Rsi(9999);
    private OkawariParameterG$Bband paramG = new OkawariParameterG$Bband(9999);
    private OkawariParameterH$Bband paramH = new OkawariParameterH$Bband(9999);
    private OkawariParameterI$Sig paramI = new OkawariParameterI$Sig(9999);
    private OkawariParameterJ$Atr paramJ = new OkawariParameterJ$Atr(9999);

    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("\n");
        s.append("paramA  :" + paramA.parameter * 1000 + "( / 1000)\n");
        s.append("paramB  :" + paramB.parameter * 1000 + "( / 1000)\n");
        s.append("paramC  :" + paramC.parameter * 1000 + "( / 1000)\n");
        s.append("paramD  :" + paramD.parameter * 1000 + "( / 1000)\n");
        s.append("paramE  :" + paramE.parameter * 1000 + "( / 1000)\n");
        s.append("paramF  :" + paramF.parameter * 1000 + "( / 1000)\n");
        s.append("paramG  :" + paramG.parameter * 1000 + "( / 1000)\n");
        s.append("paramH  :" + paramH.parameter * 1000 + "( / 1000)\n");
        s.append("paramI  :" + paramI.parameter * 1000 + "( / 1000)\n");
        s.append("paramJ  :" + paramJ.parameter * 1000 + "( / 1000)\n");


        return s.toString();
    }

}
