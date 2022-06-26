package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Parameter {
    private ParameterA paramA$01;
    private ParameterA paramA$02;
    private ParameterA paramA$03;
    private ParameterA paramA$04;
    private ParameterA paramA$05;

    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("\n");
        s.append("paramA$01  :" + paramA$01.parameter + "\n");
        s.append("paramA$02  :" + paramA$02.parameter + "\n");
        s.append("paramA$03  :" + paramA$03.parameter + "\n");
        s.append("paramA$04  :" + paramA$04.parameter + "\n");
        s.append("paramA$05  :" + paramA$05.parameter + "\n");

        return s.toString();
    }
}
