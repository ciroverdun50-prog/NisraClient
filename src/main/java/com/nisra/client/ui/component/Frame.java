package com.nisra.client.ui.component;

import com.nisra.client.module.Category;
import com.nisra.client.module.Module;
import com.nisra.client.module.ModuleManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class Frame {
    private final Category category;
    private int x, y, width, height;
    private boolean dragging;
    private int dragX, dragY;
    private boolean extended = true;
    private final List<ModuleButton> buttons = new ArrayList<>();

    public Frame(Category category, int x, int y, int width, int height, ModuleManager moduleManager) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        int offsetY = 0;
        if (moduleManager != null) {
            for (Module module : moduleManager.getModules()) {
                if (module.getCategory() == category) {
                    buttons.add(new ModuleButton(module, this, offsetY));
                    offsetY += 18;
                }
            }
        }
    }

    public void draw(DrawContext context, int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        // Renderizar cabecera de la categoría
        context.fill(x, y, x + width, y + height, 0xFF1E1E24);
        
        TextRenderer tr = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
        context.drawTextWithShadow(tr, category.getName(), x + 6, y + 5, 0xFFFFFFFF);

        if (extended) {
            int currentY = y + height;
            for (ModuleButton button : buttons) {
                button.draw(context, x, currentY, mouseX, mouseY);
                currentY += button.getTotalHeight();
            }
        }
    }

    public boolean mousePressed(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, x, y, width, height)) {
            if (button == 0) { // Arrastrar ventana
                dragging = true;
                dragX = (int) mouseX - x;
                dragY = (int) mouseY - y;
                return true;
            } else if (button == 1) { // Contraer / Expandir categoría
                extended = !extended;
                return true;
            }
        }

        if (extended) {
            int currentY = y + height;
            for (ModuleButton mb : buttons) {
                if (mb.mousePressed((int) mouseX, (int) mouseY, button, x, currentY)) {
                    return true;
                }
                currentY += mb.getTotalHeight();
            }
        }

        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        for (ModuleButton mb : buttons) {
            mb.mouseReleased((int) mouseX, (int) mouseY, button);
        }
    }

    public void mouseDragged(int mouseX, int mouseY) {
        // Controlado por la lógica de dragging en mousePressed
    }

    public void mouseScrolled(int mouseX, int mouseY, double amount) {
        // Opcional para scroll en un futuro
    }

    public List<ModuleButton> getButtons() {
        return buttons;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHeaderHeight() { return height; }

    private boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
