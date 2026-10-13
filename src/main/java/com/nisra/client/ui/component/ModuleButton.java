package com.nisra.client.ui.component;

import com.nisra.client.module.Module;
import com.nisra.client.module.settings.Setting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton {
    private final Module module;
    private final Frame parent;
    private int offsetY;
    private boolean extended = false;
    private boolean listeningForKey = false;
    private final List<SettingComponent> settingComponents = new ArrayList<>();

    public ModuleButton(Module module, Frame parent, int offsetY) {
        this.module = module;
        this.parent = parent;
        this.offsetY = offsetY;

        int currentY = parent.getY() + parent.getHeaderHeight() + offsetY + 18;
        for (Setting setting : module.getSettings()) {
            settingComponents.add(new SettingComponent(setting, parent.getX() + 4, currentY, parent.getWidth() - 8, 16));
            currentY += 16;
        }
    }

    public void draw(DrawContext context, int parentX, int currentY, int mouseX, int mouseY) {
        int width = parent.getWidth();
        int height = 18;

        int color = module.isEnabled() ? 0xDD2A75D3 : 0xDD2D2D34;
        if (isHovered(parentX, currentY, mouseX, mouseY, width, height)) {
            color = module.isEnabled() ? 0xFF3B82F6 : 0xFF3F3F46;
        }

        context.fill(parentX, currentY, parentX + width, currentY + height, color);

        TextRenderer tr = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
        String displayText = listeningForKey ? module.getName() + " [Listening...]" : module.getName();
        context.drawTextWithShadow(tr, displayText, parentX + 6, currentY + 5, 0xFFFFFFFF);

        if (extended) {
            int settingY = currentY + height;
            for (SettingComponent comp : settingComponents) {
                comp.setPosition(parentX + 4, settingY);
                comp.render(context, tr, mouseX, mouseY);
                settingY += comp.getHeight();
            }
        }
    }

    public boolean mousePressed(int mouseX, int mouseY, int button, int parentX, int currentY) {
        int width = parent.getWidth();
        int height = 18;

        if (isHovered(parentX, currentY, mouseX, mouseY, width, height)) {
            if (button == 0) { // Clic Izquierdo -> Toggle
                module.toggle();
                return true;
            } else if (button == 1) { // Clic Derecho -> Expandir Settings
                this.extended = !this.extended;
                return true;
            } else if (button == 2) { // Clic Central (Scroll) -> Escuchar Tecla
                this.listeningForKey = !this.listeningForKey;
                return true;
            }
        }

        if (extended) {
            for (SettingComponent comp : settingComponents) {
                if (comp.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        // Reservado para interacciones de arrastre de sliders
    }

    public Module getModule() {
        return module;
    }

    public boolean isListeningForKey() {
        return listeningForKey;
    }

    public void setListeningForKey(boolean listeningForKey) {
        this.listeningForKey = listeningForKey;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getTotalHeight() {
        if (!extended) return 18;
        int total = 18;
        for (SettingComponent comp : settingComponents) {
            total += comp.getHeight();
        }
        return total;
    }

    private boolean isHovered(int x, int y, int mouseX, int mouseY, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
