package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterD$Sig implements Serializable {
    double parameter = 0.00007;

    public ParameterD$Sig(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.00001; d < 0.00100; d += 0.00001) {
            parameters.add(d);
        }

        return parameters;
    }
}
