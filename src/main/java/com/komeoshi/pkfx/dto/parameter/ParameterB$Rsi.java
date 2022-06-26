package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterB$Rsi {
    double parameter;

    public ParameterB$Rsi(double parameter) {
        this.parameter = parameter;
    }

    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (int i = 20; i <= 95; i += 3) {
            parameters.add((double) i);
        }

        return parameters;
    }
}
