package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterC$DxBand {
    double parameter;

    public ParameterC$DxBand(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.010; d < 0.50; d += 0.010) {
            parameters.add(d);
        }

        return parameters;
    }
}
