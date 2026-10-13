package com.nisra.client.ui;

import com.nisra.client.module.Category;
import com.nisra.client.module.ModuleManager;
import com.nisra.client.ui.component.Frame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGUIScreen extends Screen {
    private final List<Frame> frames = new ArrayList<>();

    public ClickGUIScreen(ModuleManager moduleManager) {
        super(Text.literal("ClickGUI"));
        int x = 30;
        for (Category category : Category.values()) {
            frames.add(new Frame(category, x, 30, 175, 250, moduleManager));
            x += 185;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Fondo semi-transparente moderno
        context.fillGradient(0, 0, this.width, this.height, new Color(18, 18, 24, 220).getRGB(), new Color(28, 28, 38, 220).getRGB());
        
        for (Frame frame : frames) {
            frame.draw(context, mouseX, mouseY);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) {
            frame.mousePressed((int) mouseX, (int) mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) {
            frame.mouseReleased((int) mouseX, (int) mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Frame frame : frames) {
            frame.mouseDragged((int) mouseX, (int) mouseY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        for (Frame frame : frames) {
            frame.mouseScrolled((int) mouseX, (int) mouseY, amount);
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
