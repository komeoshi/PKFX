package com.komeoshi.pkfx.dto.parameter.okawari;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OkawariParameterD$Adx {
    double parameter = 25;

    public OkawariParameterD$Adx(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 5; d < 95; d += 5) {
            parameters.add(d);
        }

        return parameters;
    }
}
