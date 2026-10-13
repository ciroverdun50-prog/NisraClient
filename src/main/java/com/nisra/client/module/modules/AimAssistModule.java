package com.nisra.client.module.modules;

import com.nisra.client.module.Category;
import com.nisra.client.module.Module;
import com.nisra.client.module.settings.BooleanSetting;
import com.nisra.client.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.MathHelper;

public class AimAssistModule extends Module {
    // Ajustes configurables para el ClickGUI
    private final NumberSetting speed = new NumberSetting("Speed", 2.5, 0.1, 10.0, 0.1);
    private final NumberSetting fov = new NumberSetting("FOV", 70.0, 10.0, 180.0, 5.0);
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

        // Verificación de espada en mano principal
        if (onlySword.getValue() && !(client.player.getMainHandStack().getItem() instanceof SwordItem)) {
            return;
        }

        Entity target = getBestTarget();
        if (target != null) {
            aimAtTargetSmooth(client, target);
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
        double angleDiff = MathHelper.wrapDegrees(yawToTarget - client.player.getYaw());

        return Math.abs(angleDiff) <= fovAngle / 2.0;
    }

    private void aimAtTargetSmooth(MinecraftClient client, Entity target) {
        // Cálculo de posición del objetivo (apuntando al pecho/cabeza suavemente)
        double diffX = target.getX() - client.player.getX();
        double diffY = (target.getY() + target.getEyeHeight(target.getPose()) * 0.85) - (client.player.getY() + client.player.getEyeHeight(client.player.getPose()));
        double diffZ = target.getZ() - client.player.getZ();
        double distXZ = Math.hypot(diffX, diffZ);

        float targetYaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(diffY, distXZ)));

        float currentYaw = client.player.getYaw();
        float currentPitch = client.player.getPitch();

        // Diferencia de ángulos limpia (wrapToDegrees evita giros bruscos de 360°)
        float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDelta = MathHelper.wrapDegrees(targetPitch - currentPitch);

        // Factor de suavizado (Smooth Interpolation)
        // A menor valor de speed, más imperceptible e imantado es el movimiento de la cámara
        float smoothFactor = (float) (speed.getValue() * 0.05);

        float newYaw = currentYaw + (yawDelta * smoothFactor);
        float newPitch = currentPitch + (pitchDelta * smoothFactor);

        client.player.setYaw(newYaw);
        client.player.setPitch(MathHelper.clamp(newPitch, -90.0f, 90.0f));
    }
}
