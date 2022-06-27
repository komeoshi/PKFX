package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterB$Adx {
    double parameter = 14;

    public ParameterB$Adx(double parameter) {
        this.parameter = parameter;
    }

    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (int i = 10; i <= 20; i += 2) {
            parameters.add((double) i);
        }

        return parameters;
    }
}
