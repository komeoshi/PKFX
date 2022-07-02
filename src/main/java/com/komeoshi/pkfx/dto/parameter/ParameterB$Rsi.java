package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterB$Rsi implements Serializable {
    double parameter = 91; //  78

    public ParameterB$Rsi(double parameter) {
        this.parameter = parameter;
    }

    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (int i = 75; i <= 95; i += 1) {
            parameters.add((double) i);
        }

        return parameters;
    }
}
