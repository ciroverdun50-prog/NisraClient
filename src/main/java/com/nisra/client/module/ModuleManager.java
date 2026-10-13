package com.nisra.client.module;

import com.nisra.client.module.modules.AimAssistModule;
import com.nisra.client.module.modules.HUDModule;
import com.nisra.client.module.modules.TriggerBotModule;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // Registro de módulos de NisraClient
        register(new AimAssistModule());
        register(new TriggerBotModule());
        register(new HUDModule());
    }

    private void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> categoryModules = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) {
                categoryModules.add(m);
            }
        }
        return categoryModules;
    }

    public void onKeyBoardInput() {
        // Reservado para escuchar combinaciones de teclas si es necesario
    }

    public void onUpdate() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onUpdate();
            }
        }
    }
}
