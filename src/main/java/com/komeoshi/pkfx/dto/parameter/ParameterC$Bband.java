package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterC$Bband {
    double parameter = 0.052; // 0.300

    public ParameterC$Bband(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.040; d < 0.300; d += 0.010) {
            parameters.add(d);
        }

        return parameters;
    }
}
