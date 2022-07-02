package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterA$CurrentAtr implements Serializable {
    double parameter = 0.0243;

    public ParameterA$CurrentAtr(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0; d < 0.0001; d += 0.0001) {
            parameters.add(d);
        }

        return parameters;
    }
}
