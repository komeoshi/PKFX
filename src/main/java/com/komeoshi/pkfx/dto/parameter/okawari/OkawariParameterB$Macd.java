package com.komeoshi.pkfx.dto.parameter.okawari;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OkawariParameterB$Macd {
    double parameter = 0.007;

    public OkawariParameterB$Macd(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.001; d < 0.01; d += 0.001) {
            parameters.add(d);
        }

        return parameters;
    }
}
