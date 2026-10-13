package com.nisra.client.module.modules;

import com.nisra.client.module.Category;
import com.nisra.client.module.Module;
import com.nisra.client.module.settings.BooleanSetting;
import com.nisra.client.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Random;

public class TriggerBotModule extends Module {

    // Configuraciones de filtrado y seguridad
    private final BooleanSetting playersOnly = new BooleanSetting("Players Only", true);
    private final BooleanSetting checkRaycast = new BooleanSetting("Check Raycast", true);
    private final BooleanSetting cooldownCheck = new BooleanSetting("Strict Cooldown", true);
    
    // Controles de humanización y bypass de heurísticas
    private final NumberSetting reachDistance = new NumberSetting("Reach Range", 3.0, 2.0, 4.0, 0.05);
    private final NumberSetting hitChance = new NumberSetting("Hit Accuracy %", 85.0, 50.0, 100.0, 1.0);
    private final NumberSetting minCps = new NumberSetting("Min CPS", 8.0, 1.0, 20.0, 1.0);
    private final NumberSetting maxCps = new NumberSetting("Max CPS", 13.0, 1.0, 20.0, 1.0);

    private final Random random = new Random();
    private int lastAttackTick = 0;
    private int nextAttackDelayTicks = 0;
    private Entity trackedTarget = null;

    public TriggerBotModule() {
        super("TriggerBot", "Advanced bypass TriggerBot with humanized error margins and tick-sync.", Category.COMBAT);
        
        addSetting(playersOnly);
        addSetting(checkRaycast);
        addSetting(cooldownCheck);
        addSetting(reachDistance);
        addSetting(hitChance);
        addSetting(minCps);
        addSetting(maxCps);
    }

    @Override
    public void onUpdate() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Validaciones de seguridad de entorno
        if (client.player == null || client.world == null || client.interactionManager == null) {
            resetState();
            return;
        }

        // Si el usuario está usando un objeto (bloquear con espada, comer, beber) o en menús, no actuar
        if (client.player.isUsingItem() || client.currentScreen != null) {
            resetState();
            return;
        }

        // Obtener la entidad bajo la mira del cliente
        Entity target = getValidTarget(client);
        if (target == null) {
            resetState();
            return;
        }

        // Si cambiamos de objetivo, reiniciamos el contador de ticks de espera
        if (target != trackedTarget) {
            trackedTarget = target;
            nextAttackDelayTicks = calculateRandomTickDelay();
            return;
        }

        // Control estricto: evitar múltiples ataques en el mismo tick de juego
        int currentTick = client.player.age;
        if (currentTick - lastAttackTick < nextAttackDelayTicks) {
            return;
        }

        // Bypass Heurístico: Simular error humano omitiendo aleatoriamente algunos aciertos válidos
        if (random.nextDouble() * 100.0 > hitChance.getValue()) {
            // Salta este ciclo de ataque para emular la imprecisión biológica
            nextAttackDelayTicks = calculateRandomTickDelay();
            return;
        }

        // Verificación de Cooldown Vanilla para golpes críticos limpios (Punish Crit)
        if (cooldownCheck.getValue()) {
            if (client.player.getAttackCooldownProgress(0.5f) < 0.95f) {
                return;
            }
        }

        // Ejecutar ataque nativo sin alteración de paquetes de red malformados
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);

        // Actualizar métricas de control
        lastAttackTick = currentTick;
        nextAttackDelayTicks = calculateRandomTickDelay();
    }

    /**
     * Valida el objetivo actual usando las reglas de raycast y restricciones de distancia del servidor.
     */
    private Entity getValidTarget(MinecraftClient client) {
        HitResult hitResult = client.crosshairTarget;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return null;
        }

        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
        Entity entity = entityHitResult.getEntity();

        if (!(entity instanceof LivingEntity) || !entity.isAlive()) {
            return null;
        }

        // Comprobación estricta de distancia en bloques
        if (client.player.distanceTo(entity) > reachDistance.getValue()) {
            return null;
        }

        // Verificación opcional de línea de visión (Raycast de bloques)
        if (checkRaycast.getValue() && !client.player.canSee(entity)) {
            return null;
        }

        // Filtro exclusivo de jugadores (Vital para evitar bloqueos contra mobs en servidores competitivos)
        if (playersOnly.getValue() && !(entity instanceof PlayerEntity)) {
            return null;
        }

        return entity;
    }

    /**
     * Calcula dinámicamente el retardo en ticks de juego basado en el rango de CPS configurado.
     */
    private int calculateRandomTickDelay() {
        double min = Math.min(minCps.getValue(), maxCps.getValue());
        double max = Math.max(minCps.getValue(), maxCps.getValue());
        
        // CPS efectivo con variación aleatoria uniforme
        double currentCps = min + (random.nextDouble() * (max - min));
        
        // 20 ticks por segundo / CPS = Ticks necesarios entre cada ataque
        double ticksNeeded = 20.0 / Math.max(currentCps, 1.0);
        
        // Añadir una micro-desviación gaussiana/aleatoria para evitar patrones lineales
        double jitter = (random.nextGaussian() * 0.4);
        
        int finalDelay = (int) Math.round(ticksNeeded + jitter);
        return Math.max(finalDelay, 1); // Nunca menor a 1 tick de diferencia
    }

    private void resetState() {
        trackedTarget = null;
        nextAttackDelayTicks = 0;
    }
}
