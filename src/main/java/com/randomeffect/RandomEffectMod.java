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

    // 중복 등록 방지 플래그
    private static boolean initialized = false;

    @Override
    public void onInitialize() {
        if (initialized) {
            LOGGER.warn("RandomEffect Mod 이미 초기화됨, 중복 등록 방지");
            return;
        }
        initialized = true;

        LOGGER.info("RandomEffect Mod 초기화 완료!");
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(RandomEffectMod::onEntityDamage);
    }

    private static boolean onEntityDamage(LivingEntity entity, DamageSource source, float amount) {
        // 플레이어만 적용
        if (!(entity instanceof PlayerEntity player)) {
            return true;
        }

        // 실제 데미지가 없으면 무시 (방패로 완전히 막힌 경우 amount == 0)
        if (amount <= 0) {
            return true;
        }

        // 방패로 막는 중인지 확인
        if (isBlocking(player)) {
            return true;
        }

        // 모든 상태효과 목록 수집
        List<RegistryEntry<StatusEffect>> effects = new ArrayList<>();
        for (RegistryEntry<StatusEffect> entry : Registries.STATUS_EFFECT.getIndexedEntries()) {
            effects.add(entry);
        }

        if (effects.isEmpty()) {
            return true;
        }

        // 랜덤 상태효과 선택
        RegistryEntry<StatusEffect> randomEntry = effects.get(RANDOM.nextInt(effects.size()));
        StatusEffect randomEffect = randomEntry.value();

        // 랜덤 지속시간: 5초 / 10초 / 15초 균등 1/3
        int[] durations = {5 * 20, 10 * 20, 15 * 20};
        int durationTicks = durations[RANDOM.nextInt(3)];

        // 강도 확률: 80% → 0 (레벨1), 15% → 1 (레벨2), 5% → 2 (레벨3)
        int amplifier = getWeightedAmplifier();

        StatusEffectInstance instance = new StatusEffectInstance(
                randomEntry,
                durationTicks,
                amplifier,
                false,
                true
        );

        player.addStatusEffect(instance);

        LOGGER.debug("플레이어 {}에게 효과 {} (레벨 {}, {}틱) 적용됨",
                player.getName().getString(),
                Registries.STATUS_EFFECT.getId(randomEffect),
                amplifier + 1,
                durationTicks
        );

        return true;
    }

    // 방패를 들고 막는 중인지 확인
    private static boolean isBlocking(PlayerEntity player) {
        return player.isBlocking() &&
               (player.getStackInHand(Hand.MAIN_HAND).isOf(Items.SHIELD) ||
                player.getStackInHand(Hand.OFF_HAND).isOf(Items.SHIELD));
    }

    // 강도 가중치 랜덤: 80% → 0, 15% → 1, 5% → 2
    private static int getWeightedAmplifier() {
        int roll = RANDOM.nextInt(100);
        if (roll < 80) return 0;
        if (roll < 95) return 1;
        return 2;
    }
}
