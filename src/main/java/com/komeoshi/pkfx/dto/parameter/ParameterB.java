package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterB {
    double parameter;

    public ParameterB(double parameter) {
        this.parameter = parameter;
    }

    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (int i = 1; i <= 100; i += 5) {
            parameters.add((double) i);
        }

        return parameters;
    }
}
