package com.komeoshi.pkfx.dto.parameter.okawari;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OkawariParameterJ$Atr {
    double parameter = 0.016;

    public OkawariParameterJ$Atr(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.001; d < 0.03; d += 0.001) {
            parameters.add(d);
        }

        return parameters;
    }
}
