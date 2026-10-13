package com.nisra.client.module;

import com.nisra.client.module.modules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final List<Integer> pressedKeys = new ArrayList<>();

    public ModuleManager() {
        // Registramos nuestros módulos
        modules.add(new AimAssistModule());
        // Aquí iremos añadiendo Triggerbot, ESP, JumpReset, HUD después...
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    // Método que se ejecuta en cada tick para revisar si se presionó la tecla de algún módulo
    public void onKeyBoardInput() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null) return; // No activar si estás escribiendo en el chat o en un menú

        long window = client.getWindow().getHandle();

        for (Module module : modules) {
            int key = module.getKey();
            if (key <= 0) continue;

            boolean isPressed = InputUtil.isKeyPressed(window, key);
            if (isPressed && !pressedKeys.contains(key)) {
                module.toggle();
                pressedKeys.add(key);
            } else if (!isPressed && pressedKeys.contains(key)) {
                pressedKeys.remove(Integer.valueOf(key));
            }
        }
    }
}
