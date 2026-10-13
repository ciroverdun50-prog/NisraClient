package com.nisra.client.module.modules;

import com.nisra.client.module.Category;
import com.nisra.client.module.Module;
import com.nisra.client.module.settings.BooleanSetting;
import com.nisra.client.module.settings.ModeSetting;
import com.nisra.client.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Random;

public class TriggerBotModule extends Module {

    // Configuración general del módulo
    private final BooleanSetting playersOnly = new BooleanSetting("Players Only", true);
    private final BooleanSetting checkRaycast = new BooleanSetting("Check Raycast", true);
    private final BooleanSetting cooldownCheck = new BooleanSetting("Cooldown Check", true);
    
    private final NumberSetting minCps = new NumberSetting("Min CPS", 8.0, 1.0, 20.0, 1.0);
    private final NumberSetting maxCps = new NumberSetting("Max CPS", 12.0, 1.0, 20.0, 1.0);
    private final NumberSetting reachDistance = new NumberSetting("Reach Range", 3.2, 2.0, 6.0, 0.1);
    private final NumberSetting humanizationDelay = new NumberSetting("Human Delay (ms)", 35.0, 0.0, 200.0, 5.0);

    private final ModeSetting targetMode = new ModeSetting("Target Mode", "Players", "All", "Players", "Mobs");

    // Variables internas para el control de tiempos y aleatorización
    private final Random random = new Random();
    private long lastAttackTime = 0L;
    private long targetAcquiredTime = 0L;
    private Entity previousTarget = null;
    private long currentAttackDelay = 0L;

    public TriggerBotModule() {
        super("TriggerBot", "Automated attack simulation with humanized timing and target validation.", Category.COMBAT);
        
        // Registro de los ajustes en la lista del módulo
        addSetting(playersOnly);
        addSetting(checkRaycast);
        addSetting(cooldownCheck);
        addSetting(minCps);
        addSetting(maxCps);
        addSetting(reachDistance);
        addSetting(humanizationDelay);
        addSetting(targetMode);
    }

    @Override
    public void onUpdate() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Validaciones básicas de entorno y estado del jugador
        if (client.player == null || client.world == null || client.interactionManager == null) {
            resetTargetState();
            return;
        }

        // Si el jugador está usando/comiendo un objeto o tiene pantallas abiertas, cancelar interacción
        if (client.player.isUsingItem() || client.currentScreen != null) {
            resetTargetState();
            return;
        }

        // Obtención y validación de la entidad en la mira del jugador
        Entity target = getValidCrosshairTarget(client);

        if (target == null) {
            resetTargetState();
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Si la entidad acaba de entrar en la mira, registrar tiempo inicial
        if (target != previousTarget) {
            previousTarget = target;
            targetAcquiredTime = currentTime;
            currentAttackDelay = calculateNextDelay();
            return;
        }

        // Humanization Delay: esperar el tiempo configurado tras apuntar antes del primer clic
        if (currentTime - targetAcquiredTime < (long) humanizationDelay.getValue()) {
            return;
        }

        // Cooldown Check: verificar si la recarga de arma de Minecraft se ha completado
        if (cooldownCheck.getValue()) {
            if (client.player.getAttackCooldownProgress(0.5f) < 1.0f) {
                return;
            }
        }

        // Control de tiempo para cumplir el CPS aleatorizado con distribución Gaussiana
        if (currentTime - lastAttackTime < currentAttackDelay) {
            return;
        }

        // Ejecutar ataque a través de la API oficial del juego
        executeAttack(client, target);

        // Actualizar sellos de tiempo y calcular el siguiente intervalo aleatorio
        lastAttackTime = currentTime;
        currentAttackDelay = calculateNextDelay();
    }

    /**
     * Valida la entidad apuntada mediante Raycast / Crosshair y filtros de categoría.
     */
    private Entity getValidCrosshairTarget(MinecraftClient client) {
        HitResult hitResult = client.crosshairTarget;

        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return null;
        }

        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
        Entity entity = entityHitResult.getEntity();

        if (!(entity instanceof LivingEntity) || !entity.isAlive()) {
            return null;
        }

        // Verificación de distancia máxima (Reach)
        if (client.player.distanceTo(entity) > reachDistance.getValue()) {
            return null;
        }

        // Verificación opcional de visibilidad por raycast directo
        if (checkRaycast.getValue() && !client.player.canSee(entity)) {
            return null;
        }

        // Filtrado por modo de objetivo
        String mode = targetMode.getMode();

        if (playersOnly.getValue() || mode.equalsIgnoreCase("Players")) {
            if (!(entity instanceof PlayerEntity)) {
                return null;
            }
        } else if (mode.equalsIgnoreCase("Mobs")) {
            if (!(entity instanceof HostileEntity || entity instanceof AnimalEntity)) {
                return null;
            }
        }

        return entity;
    }

    /**
     * Calcula el intervalo entre clics usando el rango de CPS con distribución Gaussiana.
     */
    private long calculateNextDelay() {
        double min = Math.min(minCps.getValue(), maxCps.getValue());
        double max = Math.max(minCps.getValue(), maxCps.getValue());

        // Conversión de CPS a milisegundos base
        double targetCps = min + (random.nextDouble() * (max - min));
        double baseDelayMs = 1000.0 / Math.max(targetCps, 1.0);

        // Modificación gaussiana para simular la variación orgánica humana
        double gaussianVariation = random.nextGaussian() * 12.0; 
        double finalDelay = baseDelayMs + gaussianVariation;

        return (long) Math.max(finalDelay, 30.0);
    }

    /**
     * Ejecuta la orden de ataque sobre la entidad objetivo y realiza el swing visual.
     */
    private void executeAttack(MinecraftClient client, Entity target) {
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
    }

    /**
     * Reinicia los contadores cuando no hay un objetivo válido en la mira.
     */
    private void resetTargetState() {
        previousTarget = null;
        targetAcquiredTime = 0L;
    }
}
