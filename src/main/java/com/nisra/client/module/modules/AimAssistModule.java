package com.nisra.client.module.modules;

import com.nisra.client.module.Category;
import com.nisra.client.module.Module;
import com.nisra.client.module.settings.BooleanSetting;
import com.nisra.client.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.SwordItem;

public class AimAssistModule extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", 3.0, 0.5, 10.0, 0.5);
    private final NumberSetting fov = new NumberSetting("FOV", 90.0, 15.0, 180.0, 5.0);
    private final NumberSetting distance = new NumberSetting("Distance", 4.2, 1.0, 6.0, 0.1);
    private final BooleanSetting onlySword = new BooleanSetting("Only Sword", true);

    public AimAssistModule() {
        super("AimAssist", "Smoothly assists your crosshair toward targets.", Category.COMBAT);
        addSetting(speed);
        addSetting(fov);
        addSetting(distance);
        addSetting(onlySword);
    }

    @Override
    public void onUpdate() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        if (onlySword.getValue() && !(client.player.getMainHandStack().getItem() instanceof SwordItem)) {
            return;
        }

        Entity target = getBestTarget();
        if (target != null) {
            aimAtTarget(client, target);
        }
    }

    private Entity getBestTarget() {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity bestEntity = null;
        double closestDistance = distance.getValue();

        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player || !(entity instanceof LivingEntity) || !entity.isAlive()) continue;
            
            double dist = client.player.distanceTo(entity);
            if (dist <= closestDistance && isInFov(entity, fov.getValue())) {
                closestDistance = dist;
                bestEntity = entity;
            }
        }
        return bestEntity;
    }

    private boolean isInFov(Entity entity, double fovAngle) {
        MinecraftClient client = MinecraftClient.getInstance();
        double diffX = entity.getX() - client.player.getX();
        double diffZ = entity.getZ() - client.player.getZ();
        double yawToTarget = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
        double angleDiff = Math.abs((client.player.getYaw() - yawToTarget) % 360.0);
        if (angleDiff > 180.0) angleDiff = 360.0 - angleDiff;
        return angleDiff <= fovAngle / 2.0;
    }

    private void aimAtTarget(MinecraftClient client, Entity target) {
        double diffX = target.getX() - client.player.getX();
        double diffZ = target.getZ() - client.player.getZ();
        double yawToTarget = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;

        float currentYaw = client.player.getYaw();
        float yawDiff = (float) (yawToTarget - currentYaw);
        
        while (yawDiff < -180.0f) yawDiff += 360.0f;
        while (yawDiff >= 180.0f) yawDiff -= 360.0f;

        float adjustment = (float) (yawDiff * (speed.getValue() / 10.0));
        client.player.setYaw(currentYaw + adjustment);
    }
}
