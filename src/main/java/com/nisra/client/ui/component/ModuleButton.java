package com.nisra.client.gui;

import com.nisra.client.module.Module;
import com.nisra.client.module.settings.Setting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton {
    private final Module module;
    private int x, y, width, height;
    private boolean extended = false;
    private final List<SettingComponent> settingComponents = new ArrayList<>();

    public ModuleButton(Module module, int x, int y, int width, int height) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        int currentY = y + height;
        for (Setting setting : module.getSettings()) {
            settingComponents.add(new SettingComponent(setting, x + 4, currentY, width - 8, 16));
            currentY += 16;
        }
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        // Color según estado (Activado / Desactivado)
        int color = module.isEnabled() ? 0xDD2A75D3 : 0xDD2D2D34;
        if (isHovered(mouseX, mouseY)) {
            color = module.isEnabled() ? 0xFF3B82F6 : 0xFF3F3F46;
        }

        // Render botón del módulo
        context.fill(x, y, x + width, y + height, color);
        context.drawTextWithShadow(textRenderer, module.getName(), x + 6, y + 5, 0xFFFFFFFF);

        // Submenú de configuraciones si está desplegado
        if (extended) {
            int currentY = y + height;
            for (SettingComponent comp : settingComponents) {
                comp.setPosition(x + 4, currentY);
                comp.render(context, textRenderer, mouseX, mouseY);
                currentY += comp.getHeight();
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) { // Clic izquierdo -> Toggle Módulo
                module.toggle();
                return true;
            } else if (button == 1) { // Clic derecho -> Expandir/Contraer opciones
                this.extended = !this.extended;
                return true;
            }
        }

        // Delegar clic a las configuraciones si está desplegado
        if (extended) {
            for (SettingComponent comp : settingComponents) {
                if (comp.mouseClicked(mouseX, mouseY, button)) {
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

    public int getTotalHeight() {
        if (!extended) return height;
        int total = height;
        for (SettingComponent comp : settingComponents) {
            total += comp.getHeight();
        }
        return total;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
