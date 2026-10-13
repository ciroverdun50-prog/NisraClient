package com.nisra.client.module.modules;

import com.nisra.client.module.Category;
import com.nisra.client.module.Module;
import com.nisra.client.module.settings.BooleanSetting;
import com.nisra.client.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class TriggerBotModule extends Module {
    private final NumberSetting distance = new NumberSetting("Distance", 4.2, 1.0, 6.0, 0.1);
    private final NumberSetting fov = new NumberSetting("FOV", 90.0, 10.0, 360.0, 5.0);
    private final BooleanSetting onlySword = new BooleanSetting("Only Sword", true);
    private final BooleanSetting cooldownCheck = new BooleanSetting("Cooldown", true);

    public TriggerBotModule() {
        super("TriggerBot", "Attacks targets automatically based on distance, FOV, and sword cooldown for crits.", Category.COMBAT);
        addSetting(distance);
        addSetting(fov);
        addSetting(onlySword);
        addSetting(cooldownCheck);
    }

    @Override
    public void onUpdate() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Comprobación de espada en mano principal
        if (onlySword.getValue() && !(client.player.getMainHandStack().getItem() instanceof SwordItem)) {
            return;
        }

        Entity target = client.targetedEntity;
        if (target == null || !(target instanceof LivingEntity) || !target.isAlive()) return;

        // Validar distancia máxima configurable
        if (client.player.distanceTo(target) > distance.getValue()) return;

        // Validar ángulo de visión (FOV) configurable
        if (!isInFov(target, fov.getValue())) return;

        // Verificación de cooldown de espada (Punish Crit)
        if (cooldownCheck.getValue()) {
            if (client.player.getAttackCooldownProgress(0.5f) < 1.0f) {
                return;
            }
        }

        // Ejecutar ataque
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isInFov(Entity entity, double fovAngle) {
        if (fovAngle >= 360.0) return true;
        MinecraftClient client = MinecraftClient.getInstance();
        double diffX = entity.getX() - client.player.getX();
        double diffZ = entity.getZ() - client.player.getZ();
        double yawToTarget = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
        double angleDiff = MathHelper.wrapDegrees(yawToTarget - client.player.getYaw());
        return Math.abs(angleDiff) <= fovAngle / 2.0;
    }
}
