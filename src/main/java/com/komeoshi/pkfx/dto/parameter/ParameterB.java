package com.komeoshi.pkfx.dto.parameter;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterB {
    double parameter;

    public ParameterB(double parameter) {
        this.parameter = parameter;
    }

    public static List<Integer> createParameters() {
        List<Integer> parameters = new ArrayList<>();
        for (int i = 0; i <= 100; i += 5) {
            parameters.add(i);
        }

        return parameters;
    }
}
