package com.nisra.client.ui.component;

import com.nisra.client.module.settings.BooleanSetting;
import com.nisra.client.module.settings.NumberSetting;
import com.nisra.client.module.settings.Setting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class SettingComponent {
    private final Setting setting;
    private int x, y, width, height;

    public SettingComponent(Setting setting, int x, int y, int width, int height) {
        this.setting = setting;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        context.fill(x, y, x + width, y + height, 0xAA1E1E24);

        if (setting instanceof BooleanSetting boolSetting) {
            String text = boolSetting.getName() + ": " + (boolSetting.getValue() ? "ON" : "OFF");
            int color = boolSetting.getValue() ? 0x00FF00 : 0xFF0000;
            context.drawTextWithShadow(textRenderer, text, x + 6, y + 4, color);
        } else if (setting instanceof NumberSetting numSetting) {
            String text = numSetting.getName() + ": " + String.format("%.1f", numSetting.getValue());
            context.drawTextWithShadow(textRenderer, text, x + 6, y + 4, 0xFFFFFF);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (setting instanceof BooleanSetting boolSetting && button == 0) {
                boolSetting.toggle();
                return true;
            } else if (setting instanceof NumberSetting numSetting) {
                if (button == 0) {
                    double val = Math.min(numSetting.getMaximum(), numSetting.getValue() + numSetting.getIncrement());
                    numSetting.setValue(val);
                    return true;
                } else if (button == 1) {
                    double val = Math.max(numSetting.getMinimum(), numSetting.getValue() - numSetting.getIncrement());
                    numSetting.setValue(val);
                    return true;
                }
            }
        }
        return false;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public int getHeight() {
        return height;
    }
}
