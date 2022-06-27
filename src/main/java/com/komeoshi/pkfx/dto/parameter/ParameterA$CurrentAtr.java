package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterA$CurrentAtr {
    double parameter = 0.0243;

    public ParameterA$CurrentAtr(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.0100; d < 0.0300; d += 0.005) {
            parameters.add(d);
        }

        return parameters;
    }
}
