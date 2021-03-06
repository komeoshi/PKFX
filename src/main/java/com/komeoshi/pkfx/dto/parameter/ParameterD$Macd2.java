package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterD$Macd2 implements Serializable {
    double parameter = 0.005;

    public ParameterD$Macd2(double parameter) {
        this.parameter = parameter;
    }

    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.001; d < 0.100; d += 0.001) {
            parameters.add(d);
        }

        return parameters;
    }
}
