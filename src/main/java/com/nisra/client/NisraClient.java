package com.nisra.client;

import com.nisra.client.module.ModuleManager;
import com.nisra.client.ui.ClickGUIScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class NisraClient implements ClientModInitializer {
    private static ModuleManager moduleManager;
    private static ClickGUIScreen clickGUI;
    private static KeyBinding toggleKeyBind;

    @Override
    public void onInitializeClient() {
        moduleManager = new ModuleManager();
        clickGUI = new ClickGUIScreen(moduleManager);

        toggleKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nisraclient.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.nisraclient.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKeyBind.wasPressed()) {
                client.setScreen(clickGUI);
            }

            for (var module : moduleManager.getModules()) {
                if (module.isEnabled()) {
                    module.onUpdate();
                }
            }
        });
    }

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }
}
