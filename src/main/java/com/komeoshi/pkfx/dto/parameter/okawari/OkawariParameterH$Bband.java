package com.komeoshi.pkfx.dto.parameter.okawari;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OkawariParameterH$Bband {
    double parameter = 0.097;

    public OkawariParameterH$Bband(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.01; d < 0.9; d += 0.01) {
            parameters.add(d);
        }

        return parameters;
    }
}
