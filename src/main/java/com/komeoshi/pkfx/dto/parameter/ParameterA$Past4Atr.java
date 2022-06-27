package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterA$Past4Atr {
    double parameter = 0.0311;

    public ParameterA$Past4Atr(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.0200; d < 0.0900; d += 0.005) {
            parameters.add(d);
        }

        return parameters;
    }
}
