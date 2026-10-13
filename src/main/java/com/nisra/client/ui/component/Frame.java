package com.nisra.client.ui.component;

import com.nisra.client.module.Category;
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

    public Frame(Category category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void addButton(ModuleButton button) {
        buttons.add(button);
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        // Renderizar la cabecera de la categoría
        context.fill(x, y, x + width, y + height, 0xFF1E1E24);
        context.drawTextWithShadow(textRenderer, category.getName(), x + 6, y + 5, 0xFFFFFFFF);

        if (extended) {
            int currentY = y + height;
            for (ModuleButton button : buttons) {
                button.draw(context, x, currentY, mouseX, mouseY);
                // IMPORTANTE: Sumar la altura total para que los siguientes botones bajen correctamente
                currentY += button.getTotalHeight();
            }
        }
    }

    public boolean mousePressed(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, x, y, width, height)) {
            if (button == 0) { // Clic izquierdo para arrastrar ventana
                dragging = true;
                dragX = (int) mouseX - x;
                dragY = (int) mouseY - y;
                return true;
            } else if (button == 1) { // Clic derecho para contraer toda la categoría
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

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHeaderHeight() { return height; }

    private boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
