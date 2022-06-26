package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Parameter {
    private ParameterA paramA$01 = new ParameterA(0.0243);
    private ParameterA paramA$02 = new ParameterA(0.0450);
    private ParameterA paramA$03 = new ParameterA(0.0590);
    private ParameterA paramA$04 = new ParameterA(0.0279);
    private ParameterA paramA$05 = new ParameterA(0.0311);

    private ParameterB paramB$01 = new ParameterB(14);
    private ParameterB paramB$02 = new ParameterB(23);
    private ParameterB paramB$03 = new ParameterB(91);
    private ParameterB paramB$04 = new ParameterB(78);

    private ParameterC paramC$01 = new ParameterC(0.052);
    private ParameterC paramC$02 = new ParameterC(0.300);
    private ParameterC paramC$03 = new ParameterC(0.350);
    private ParameterC paramC$04 = new ParameterC(0.350);

    private ParameterD paramD$01 = new ParameterD(0.00005);
    private ParameterD paramD$02 = new ParameterD(0.005);
    private ParameterD paramD$03 = new ParameterD(0.00007);

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
