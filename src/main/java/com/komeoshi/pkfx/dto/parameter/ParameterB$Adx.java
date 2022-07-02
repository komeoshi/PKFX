package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterB$Adx implements Serializable {
    double parameter = 14;

    public ParameterB$Adx(double parameter) {
        this.parameter = parameter;
    }

    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (int i = 10; i <= 40; i += 1) {
            parameters.add((double) i);
        }

        return parameters;
    }
}
