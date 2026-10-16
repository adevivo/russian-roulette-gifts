package com.neovetta.rrgifts.game;

import com.neovetta.rrgifts.core.TierDef;
import com.neovetta.rrgifts.core.TierFeedback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Action-bar message, sound and particles for a rolled tier. */
public final class Feedback {
	private Feedback() {}

	public static ChatFormatting[] styleFor(TierFeedback kind) {
		return switch (kind) {
			case DISASTER -> new ChatFormatting[] {ChatFormatting.RED};
			case CURSED -> new ChatFormatting[] {ChatFormatting.DARK_PURPLE};
			case JUNK -> new ChatFormatting[] {ChatFormatting.GRAY};
			case GOOD -> new ChatFormatting[] {ChatFormatting.GREEN};
			case JACKPOT -> new ChatFormatting[] {ChatFormatting.GOLD, ChatFormatting.BOLD};
			case UNKNOWN -> new ChatFormatting[] {ChatFormatting.WHITE};
		};
	}

	public static void send(ServerPlayer opener, ServerLevel level, Vec3 origin, TierDef tier) {
		TierFeedback kind = TierFeedback.of(tier.id());
		opener.sendOverlayMessage(Component.literal(tier.message()).withStyle(styleFor(kind)));

		SoundEvent sound;
		float pitch = 1.0f;
		ParticleOptions particle;
		int count;
		switch (kind) {
			case DISASTER -> { sound = SoundEvents.LIGHTNING_BOLT_THUNDER; particle = ParticleTypes.LARGE_SMOKE; count = 30; }
			case CURSED -> { sound = SoundEvents.SOUL_ESCAPE.value(); particle = ParticleTypes.SOUL; count = 25; }
			case JUNK -> { sound = SoundEvents.VILLAGER_NO; particle = ParticleTypes.SMOKE; count = 12; }
			case GOOD -> { sound = SoundEvents.EXPERIENCE_ORB_PICKUP; particle = ParticleTypes.HAPPY_VILLAGER; count = 15; }
			case JACKPOT -> { sound = SoundEvents.PLAYER_LEVELUP; particle = ParticleTypes.TOTEM_OF_UNDYING; count = 60; }
			default -> { sound = SoundEvents.EXPERIENCE_ORB_PICKUP; particle = ParticleTypes.ENCHANT; count = 15; pitch = 0.8f; }
		}
		level.playSound(null, origin.x, origin.y, origin.z, sound, SoundSource.BLOCKS, 1.0f, pitch);
		level.sendParticles(particle, origin.x, origin.y + 0.3, origin.z, count, 0.3, 0.3, 0.3, 0.05);
	}
}
