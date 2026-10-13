package com.nisra.client.ui.component;

import com.nisra.client.module.Module;
import com.nisra.client.module.settings.BooleanSetting;
import com.nisra.client.module.settings.ModeSetting;
import com.nisra.client.module.settings.NumberSetting;
import com.nisra.client.module.settings.Setting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class ModuleButton {
    private final Module module;
    private final Frame parent;
    private final int offsetY;
    private boolean extended;
    
    // Control para arrastrar sliders de NumberSetting
    private NumberSetting draggingSetting = null;

    public ModuleButton(Module module, Frame parent, int offsetY) {
        this.module = module;
        this.parent = parent;
        this.offsetY = offsetY;
        this.extended = false;
    }

    public void draw(DrawContext context, int x, int y, int mouseX, int mouseY) {
        MinecraftClientAccessor client = (MinecraftClientAccessor) net.minecraft.client.MinecraftClient.getInstance();
        TextRenderer tr = net.minecraft.client.MinecraftClient.getInstance().textRenderer;

        int width = parent.getWidth();
        int height = 18;

        // Color de fondo del botón del módulo (más claro si está activo)
        int bgColor = module.isEnabled() ? 0xFF2563EB : 0xFF18181B;
        if (isHovered(mouseX, mouseY, x, y, width, height)) {
            bgColor = module.isEnabled() ? 0xFF1D4ED8 : 0xFF27272A;
        }

        context.fill(x, y, x + width, y + height, bgColor);
        
        // Nombre del módulo y estado
        int textColor = module.isEnabled() ? 0xFFFFFFFF : 0xFFA1A1AA;
        context.drawTextWithShadow(tr, module.getName(), x + 6, y + 5, textColor);

        // Si está expandido, renderizar configuraciones
        if (extended && !module.getSettings().isEmpty()) {
            int currentY = y + height;
            for (Setting setting : module.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    BooleanSetting boolSet = (BooleanSetting) setting;
                    context.fill(x, currentY, x + width, currentY + 16, 0xFF121215);
                    int boolColor = boolSet.getValue() ? 0xFF3B82F6 : 0xFF71717A;
                    context.drawTextWithShadow(tr, boolSet.getName() + ": " + (boolSet.getValue() ? "ON" : "OFF"), x + 10, currentY + 4, boolColor);
                    currentY += 16;
                } 
                else if (setting instanceof NumberSetting) {
                    NumberSetting numSet = (NumberSetting) setting;
                    context.fill(x, currentY, x + width, currentY + 24, 0xFF121215);

                    // Lógica para arrastrar el slider en tiempo real con el mouse
                    if (draggingSetting == numSet) {
                        if (net.minecraft.client.MinecraftClient.getInstance().mouse.wasMousePressed(0)) {
                            double min = numSet.getMin();
                            double max = numSet.getMax();
                            double percent = MathHelper.clamp((double) (mouseX - (x + 10)) / (width - 20), 0.0, 1.0);
                            double newValue = min + (max - min) * percent;
                            
                            // Redondear a un decimal para limpieza
                            newValue = Math.round(newValue * 10.0) / 10.0;
                            numSet.setValue(newValue);
                        } else {
                            draggingSetting = null;
                        }
                    }

                    // Dibujar barra de fondo del slider
                    context.fill(x + 10, currentY + 14, x + width - 10, currentY + 18, 0xFF27272A);
                    
                    // Relleno proporcional del slider
                    double percent = (numSet.getValue() - numSet.getMin()) / (numSet.getMax() - numSet.getMin());
                    int fillWidth = (int) ((width - 20) * percent);
                    context.fill(x + 10, currentY + 14, x + 10 + fillWidth, currentY + 18, 0xFF3B82F6);

                    // Texto del número
                    String displayVal = numSet.getName() + ": " + numSet.getValue();
                    context.drawTextWithShadow(tr, displayVal, x + 10, currentY + 3, 0xFFD4D4D8);
                    currentY += 24;
                }
                else if (setting instanceof ModeSetting) {
                    ModeSetting modeSet = (ModeSetting) setting;
                    context.fill(x, currentY, x + width, currentY + 16, 0xFF121215);
                    context.drawTextWithShadow(tr, modeSet.getName() + ": " + modeSet.getMode(), x + 10, currentY + 4, 0xFFD4D4D8);
                    currentY += 16;
                }
            }
        }
    }

    public boolean mousePressed(int mouseX, int mouseY, int button, int x, int y) {
        int width = parent.getWidth();
        int height = 18;

        // Clic en el botón principal del módulo
        if (isHovered(mouseX, mouseY, x, y, width, height)) {
            if (button == 0) { // Click izquierdo: Toggle del módulo
                module.toggle();
                return true;
            } else if (button == 1) { // Click derecho: Expandir ajustes
                extended = !extended;
                return true;
            }
        }

        // Si está expandido, comprobar clics en las configuraciones internas
        if (extended && !module.getSettings().isEmpty()) {
            int currentY = y + height;
            for (Setting setting : module.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    if (isHovered(mouseX, mouseY, x, currentY, width, 16)) {
                        BooleanSetting boolSet = (BooleanSetting) setting;
                        boolSet.setValue(!boolSet.getValue());
                        return true;
                    }
                    currentY += 16;
                } 
                else if (setting instanceof NumberSetting) {
                    if (isHovered(mouseX, mouseY, x, currentY, width, 24)) {
                        draggingSetting = (NumberSetting) setting;
                        // Ajustar inmediatamente al hacer clic en la barra
                        double min = draggingSetting.getMin();
                        double max = draggingSetting.getMax();
                        double percent = MathHelper.clamp((double) (mouseX - (x + 10)) / (width - 20), 0.0, 1.0);
                        double newValue = Math.round((min + (max - min) * percent) * 10.0) / 10.0;
                        draggingSetting.setValue(newValue);
                        return true;
                    }
                    currentY += 24;
                }
                else if (setting instanceof ModeSetting) {
                    if (isHovered(mouseX, mouseY, x, currentY, width, 16)) {
                        ModeSetting modeSet = (ModeSetting) setting;
                        modeSet.cycle();
                        return true;
                    }
                    currentY += 16;
                }
            }
        }

        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        draggingSetting = null;
    }

    public int getTotalHeight() {
        int height = 18;
        if (extended) {
            for (Setting setting : module.getSettings()) {
                if (setting instanceof NumberSetting) {
                    height += 24;
                } else {
                    height += 16;
                }
            }
        }
        return height;
    }

    private boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
