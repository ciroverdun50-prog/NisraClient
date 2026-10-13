package com.nisra.client.module;

import com.nisra.client.module.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int key;
    private final List<Setting> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
        this.key = 0;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onUpdate() {}

    public void toggle() {
        this.enabled = !this.enabled;
        playToggleSound();
        if (this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    private void playToggleSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // Tono agudo al encender (1.4f) y tono grave al apagar (0.8f)
            float pitch = this.enabled ? 1.4f : 0.8f;
            client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, pitch);
        }
    }

    public void addSetting(Setting setting) {
        this.settings.add(setting);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            playToggleSound();
            if (this.enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public List<Setting> getSettings() {
        return settings;
    }
}
