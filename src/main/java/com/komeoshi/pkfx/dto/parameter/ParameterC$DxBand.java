package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterC$DxBand implements Serializable {
    double parameter = 0.350;

    public ParameterC$DxBand(double parameter){
        this.parameter = parameter;
    }
    public static List<Double> createParameters() {
        List<Double> parameters = new ArrayList<>();
        for (double d = 0.10; d < 0.40; d += 0.010) {
            parameters.add(d);
        }

        return parameters;
    }
}
