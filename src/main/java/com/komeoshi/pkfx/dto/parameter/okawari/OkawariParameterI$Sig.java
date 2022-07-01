package com.komeoshi.pkfx.dto.parameter.okawari;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OkawariParameterI$Sig {
    double parameter = 0.004;

    public OkawariParameterI$Sig(double parameter){
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
