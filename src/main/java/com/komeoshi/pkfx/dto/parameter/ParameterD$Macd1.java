package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterD$Macd1 {
    double parameter = 0.00005;

    public ParameterD$Macd1(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.00001; d < 0.00100; d += 0.00001) {
            parameters.add(d);
        }

        return parameters;
    }
}
