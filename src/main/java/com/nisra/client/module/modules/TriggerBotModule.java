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

public class TriggerBotModule extends Module {
    private final NumberSetting distance = new NumberSetting("Distance", 4.2, 1.0, 6.0, 0.1);
    private final BooleanSetting onlySword = new BooleanSetting("Only Sword", true);
    private final BooleanSetting cooldownCheck = new BooleanSetting("Cooldown", true);

    public TriggerBotModule() {
        super("TriggerBot", "Automatically attacks targets when your attack cooldown is ready for critical hits.", Category.COMBAT);
        addSetting(distance);
        addSetting(onlySword);
        addSetting(cooldownCheck);
    }

    @Override
    public void onUpdate() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Comprobación de espada en mano principal si está activo
        if (onlySword.getValue() && !(client.player.getMainHandStack().getItem() instanceof SwordItem)) {
            return;
        }

        // Obtener la entidad a la que apunta la cruceta exactamente
        Entity target = client.targetedEntity;
        if (target == null || !(target instanceof LivingEntity) || !target.isAlive()) return;

        // Validar distancia máxima
        if (client.player.distanceTo(target) > distance.getValue()) return;

        // Verificación de cooldown de espada (para asegurar el golpe crítico / punish crit)
        if (cooldownCheck.getValue()) {
            if (client.player.getAttackCooldownProgress(0.5f) < 1.0f) {
                return;
            }
        }

        // Ejecutar ataque de forma nativa e impecable
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
    }
}
