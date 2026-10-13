package com.nisra.client.module.settings;

public class NumberSetting extends Setting {
    private double value;
    private final double minimum;
    private final double maximum;
    private final double increment;

    public NumberSetting(String name, double defaultValue, double minimum, double maximum, double increment) {
        super(name);
        this.value = defaultValue;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        double precision = 1.0 / this.increment;
        double rounded = Math.round(Math.max(this.minimum, Math.min(this.maximum, value)) * precision) / precision;
        this.value = rounded;
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    // Métodos alias (getMin / getMax) para la barra desplazable interactiva de ModuleButton
    public double getMin() {
        return minimum;
    }

    public double getMax() {
        return maximum;
    }

    public double getIncrement() {
        return increment;
    }
}
