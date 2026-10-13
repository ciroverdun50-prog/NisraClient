package com.nisra.client.module.modules;

import com.nisra.client.module.Category;
import com.nisra.client.module.Module;
import com.nisra.client.module.settings.BooleanSetting;
import com.nisra.client.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

import java.util.Random;

public class AutoJumpResetModule extends Module {

    // Configuraciones de comportamiento y filtros de activación
    private final BooleanSetting onlyW = new BooleanSetting("Only W Key", true);
    private final BooleanSetting groundOnly = new BooleanSetting("Ground Only", true);
    private final NumberSetting delayMs = new NumberSetting("Reaction Delay (ms)", 35.0, 0.0, 150.0, 5.0);
    private final NumberSetting hitChance = new NumberSetting("Success Rate %", 90.0, 50.0, 100.0, 1.0);

    // Variables internas para el cálculo de tiempos y control de saltos
    private final Random random = new Random();
    private int lastHurtTime = 0;
    private long damageTakenTimestamp = 0L;
    private long scheduledJumpTime = 0L;
    private boolean pendingJump = false;

    public AutoJumpResetModule() {
        super("AutoJumpReset", "Automatically performs a jump reset when taking damage to reduce knockback.", Category.COMBAT);

        // Registro de configuraciones en la ClickGUI
        addSetting(onlyW);
        addSetting(groundOnly);
        addSetting(delayMs);
        addSetting(hitChance);
    }

    @Override
    public void onUpdate() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) {
            resetState();
            return;
        }

        int currentHurtTime = client.player.hurtTime;

        // Detección exacta del instante de daño (en Minecraft vanilla, hurtTime comienza en 10 al recibir un impacto)
        if (currentHurtTime == 10 && lastHurtTime != 10) {
            damageTakenTimestamp = System.currentTimeMillis();

            // Validar si el usuario debe mantener presionada la tecla W para activar el reseteo
            if (onlyW.getValue() && !client.options.forwardKey.isPressed()) {
                lastHurtTime = currentHurtTime;
                return;
            }

            // Validar si solo debe ejecutarse estando sobre el suelo
            if (groundOnly.getValue() && !client.player.isOnGround()) {
                lastHurtTime = currentHurtTime;
                return;
            }

            // Bypass anti-cheat: simular imprecisión omitiendo aleatoriamente un porcentaje de los saltos
            if (random.nextDouble() * 100.0 <= hitChance.getValue()) {
                // Añadir variación gaussiana / jitter al tiempo de reacción humano
                long jitter = (long) (random.nextGaussian() * 8.0);
                long finalDelay = (long) Math.max(delayMs.getValue() + jitter, 0.0);
                
                scheduledJumpTime = damageTakenTimestamp + finalDelay;
                pendingJump = true;
            }
        }

        lastHurtTime = currentHurtTime;

        // Procesar el salto programado una vez transcurrido el tiempo de reacción humanizado
        if (pendingJump) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= scheduledJumpTime) {
                // Simular la pulsación de la tecla de salto de forma nativa
                client.options.jumpKey.setPressed(true);
                pendingJump = false;
            } else {
                // Mantener la tecla liberada hasta que llegue el milisegundo exacto calculado
                client.options.jumpKey.setPressed(false);
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        resetState();
    }

    private void resetState() {
        lastHurtTime = 0;
        damageTakenTimestamp = 0L;
        scheduledJumpTime = 0L;
        pendingJump = false;
    }
}
