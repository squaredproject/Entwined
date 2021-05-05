package com.charlesgadeken.entwined;

import heronarts.lx.parameter.BoundedParameter;
import java.util.ArrayList;
import java.util.List;

public class BasicParameterProxy extends BoundedParameter {
    final List<BoundedParameter> parameters = new ArrayList<>();

    BasicParameterProxy(double value) {
        super("Proxy", value);
    }

    @Override
    protected double updateValue(double value) {
        for (BoundedParameter parameter : parameters) {
            parameter.setValue(value);
        }
        return value;
    }
}
