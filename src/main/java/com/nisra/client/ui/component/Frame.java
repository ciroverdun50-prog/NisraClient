package com.nisra.client.ui.component;

import com.nisra.client.module.Category;
import com.nisra.client.module.Module;
import com.nisra.client.module.ModuleManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Frame {
    private final Category category;
    private int x, y, width, maxHeight;
    private boolean dragging;
    private int dragX, dragY;
    private boolean extended = true;
    private float scrollOffset = 0;
    private final List<ModuleButton> buttons = new ArrayList<>();

    public Frame(Category category, int x, int y, int width, int maxHeight, ModuleManager moduleManager) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.maxHeight = maxHeight;

        int offsetY = 25;
        for (Module module : moduleManager.getModulesByCategory(category)) {
            buttons.add(new ModuleButton(module, this, offsetY));
            offsetY += 25;
        }
    }

    public void draw(DrawContext context, int mouseX, int mouseY) {
        // Cabecera del panel
        context.fill(x, y, x + width, y + 25, new Color(30, 30, 42).getRGB());
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawText(textRenderer, category.getName(), x + 12, y + 8, new Color(110, 80, 240).getRGB(), true);

        if (extended && !buttons.isEmpty()) {
            int totalContentHeight = buttons.size() * 25;
            int renderHeight = Math.min(totalContentHeight, maxHeight);

            // Fondo del área del contenido
            context.fill(x, y + 25, x + width, y + 25 + renderHeight, new Color(20, 20, 28, 180).getRGB());

            // Recorte de pantalla para scroll
            context.enableScissor(x, y + 25, x + width, y + 25 + renderHeight);
            
            int currentY = y + 25 + (int) scrollOffset;
            for (ModuleButton button : buttons) {
                button.draw(context, x, currentY, mouseX, mouseY);
                currentY += 25;
            }

            context.disableScissor();
        }
    }

    public void mousePressed(int mouseX, int mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 25) {
            if (button == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            } else if (button == 1) {
                extended = !extended;
            }
        }

        if (extended) {
            int totalContentHeight = buttons.size() * 25;
            int renderHeight = Math.min(totalContentHeight, maxHeight);
            if (mouseX >= x && mouseX <= x + width && mouseY >= y + 25 && mouseY <= y + 25 + renderHeight) {
                int currentY = y + 25 + (int) scrollOffset;
                for (ModuleButton mb : buttons) {
                    mb.mousePressed(mouseX, mouseY, button, x, currentY);
                    currentY += 25;
                }
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
        if (extended) {
            for (ModuleButton mb : buttons) {
                mb.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    public void mouseDragged(int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }
    }

    public void mouseScrolled(int mouseX, int mouseY, double amount) {
        if (extended && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 25 + maxHeight) {
            scrollOffset += (float) (amount * 12);
            int maxScroll = -Math.max(0, (buttons.size() * 25) - maxHeight);
            if (scrollOffset > 0) scrollOffset = 0;
            if (scrollOffset < maxScroll) scrollOffset = maxScroll;
        }
    }

    public List<ModuleButton> getButtons() {
        return buttons;
    }

    public int getWidth() {
        return width;
    }
}
