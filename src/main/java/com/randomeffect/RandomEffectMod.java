package com.randomeffect;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomEffectMod implements ModInitializer {

    public static final String MOD_ID = "randomeffect";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Random RANDOM = new Random();
    private static boolean initialized = false;

    @Override
    public void onInitialize() {
        if (initialized) return;
        initialized = true;

        LOGGER.info("RandomEffect Mod 초기화 완료!");
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(RandomEffectMod::onEntityDamage);
    }

    private static boolean onEntityDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayerEntity player)) return true;
        if (amount <= 0) return true;
        if (isBlocking(player)) return true;

        // 1% 확률 이스터에그
        if (RANDOM.nextInt(100) == 0) {
            triggerEasterEgg(player);
            return true;
        }

        // 현재 걸린 효과 제외하고 후보 목록 만들기
        List<RegistryEntry<StatusEffect>> candidates = new ArrayList<>();
        for (RegistryEntry<StatusEffect> entry : Registries.STATUS_EFFECT.getIndexedEntries()) {
            if (!player.hasStatusEffect(entry)) {
                candidates.add(entry);
            }
        }

        if (candidates.isEmpty()) return true;

        RegistryEntry<StatusEffect> randomEntry = candidates.get(RANDOM.nextInt(candidates.size()));

        int[] durations = {5 * 20, 10 * 20, 15 * 20};
        int durationTicks = durations[RANDOM.nextInt(3)];
        int amplifier = getWeightedAmplifier();

        player.addStatusEffect(new StatusEffectInstance(randomEntry, durationTicks, amplifier, false, true));

        return true;
    }

    private static void triggerEasterEgg(ServerPlayerEntity player) {
        for (RegistryEntry<StatusEffect> entry : Registries.STATUS_EFFECT.getIndexedEntries()) {
            player.addStatusEffect(new StatusEffectInstance(entry, 90 * 20, 0, false, true));
        }

        player.sendMessage(Text.literal("§c어쩌다 이 지경까지.."), true);
    }

    private static boolean isBlocking(PlayerEntity player) {
        return player.isBlocking() &&
               (player.getStackInHand(Hand.MAIN_HAND).isOf(Items.SHIELD) ||
                player.getStackInHand(Hand.OFF_HAND).isOf(Items.SHIELD));
    }

    private static int getWeightedAmplifier() {
        int roll = RANDOM.nextInt(100);
        if (roll < 80) return 0;
        if (roll < 95) return 1;
        return 2;
    }
}
