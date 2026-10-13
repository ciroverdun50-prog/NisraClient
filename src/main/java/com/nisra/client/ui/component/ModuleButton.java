package com.nisra.client.ui.component;

import com.nisra.client.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;

import java.awt.Color;

public class ModuleButton {
    private final Module module;
    private final Frame parent;
    private final int offsetY;

    public ModuleButton(Module module, Frame parent, int offsetY) {
        this.module = module;
        this.parent = parent;
        this.offsetY = offsetY;
    }

    public void draw(DrawContext context, int renderX, int renderY, int mouseX, int mouseY) {
        // Color base según estado y efecto hover al pasar el mouse por encima
        int bgColor = module.isEnabled() ? new Color(70, 50, 180).getRGB() : new Color(35, 35, 48).getRGB();
        
        if (mouseX >= renderX && mouseX <= renderX + parent.getWidth() && mouseY >= renderY && mouseY <= renderY + 25) {
            bgColor = module.isEnabled() ? new Color(85, 65, 200).getRGB() : new Color(45, 45, 60).getRGB();
        }

        context.fill(renderX, renderY, renderX + parent.getWidth(), renderY + 25, bgColor);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textColor = module.isEnabled() ? Color.WHITE.getRGB() : new Color(170, 170, 185).getRGB();
        context.drawText(textRenderer, module.getName(), renderX + 12, renderY + 8, textColor, true);
    }

    public void mousePressed(int mouseX, int mouseY, int button, int renderX, int renderY) {
        if (mouseX >= renderX && mouseX <= renderX + parent.getWidth() && mouseY >= renderY && mouseY <= renderY + 25) {
            if (button == 0) {
                module.toggle();
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {}
}
