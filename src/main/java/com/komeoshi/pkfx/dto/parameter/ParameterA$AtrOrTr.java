package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterA$AtrOrTr {
    double parameter;

    public ParameterA$AtrOrTr(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.0100; d < 0.05; d += 0.0001) {
            parameters.add(d);
        }

        return parameters;
    }
}
