package com.nisra.client.module.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultMode);
        if (this.index == -1) {
            this.index = 0;
        }
    }

    public String getMode() {
        return modes.get(index);
    }

    public void setMode(String mode) {
        int newIndex = modes.indexOf(mode);
        if (newIndex != -1) {
            this.index = newIndex;
        }
    }

    public void cycle() {
        if (index < modes.size() - 1) {
            index++;
        } else {
            index = 0;
        }
    }

    public List<String> getModes() {
        return modes;
    }
}
