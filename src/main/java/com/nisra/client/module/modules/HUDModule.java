package com.nisra.client.module.modules;

import com.nisra.client.module.Category;
import com.nisra.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class HUDModule extends Module {

    public HUDModule() {
        super("HUD", "Displays active modules on the screen.", Category.RENDER);
        setEnabled(true); // Activado por defecto
    }

    public static void render(DrawContext context, List<Module> activeModules) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.textRenderer == null) return;

        TextRenderer tr = client.textRenderer;
        int y = 4;

        // Ordenar opcionalmente por longitud de texto o nombre
        for (Module module : activeModules) {
            if (!module.isEnabled()) continue;

            String name = module.getName();
            int screenWidth = client.getWindow().getScaledWidth();
            int textWidth = tr.getWidth(name);

            // Renderizado en la esquina superior derecha con sombra
            context.drawTextWithShadow(tr, name, screenWidth - textWidth - 4, y, 0xFF3B82F6);
            y += 10;
        }
    }
}
