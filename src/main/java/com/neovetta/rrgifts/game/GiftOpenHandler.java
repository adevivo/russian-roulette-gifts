package com.neovetta.rrgifts.game;

import com.neovetta.rrgifts.RussianRouletteGifts;
import com.neovetta.rrgifts.core.GiftRoller;
import com.neovetta.rrgifts.core.SpreadOffsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class GiftOpenHandler {
	private GiftOpenHandler() {}

	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || player.isSpectator()
					|| !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer opener)) {
				return InteractionResult.PASS;
			}
			if (player.isShiftKeyDown() && !player.getMainHandItem().isEmpty()) {
				return InteractionResult.PASS;
			}
			BlockPos pos = hit.getBlockPos();
			BlockState state = serverLevel.getBlockState(pos);
			if (!state.is(Blocks.PLAYER_HEAD) && !state.is(Blocks.PLAYER_WALL_HEAD)) {
				return InteractionResult.PASS;
			}
			if (!(serverLevel.getBlockEntity(pos) instanceof SkullBlockEntity skull)
					|| !GiftItems.isGiftProfile(skull.getOwnerProfile())) {
				return InteractionResult.PASS;
			}
			// Respect adventure mode and spawn protection: opening removes the block.
			if (!opener.mayBuild() || !serverLevel.mayInteract(opener, pos)) {
				return InteractionResult.PASS;
			}
			open(serverLevel, pos, opener, skull.getOwnerProfile());
			return InteractionResult.SUCCESS;
		});
	}

	private static void open(ServerLevel level, BlockPos pos, ServerPlayer opener, ResolvableProfile profile) {
		level.removeBlock(pos, false);
		Optional<UUID> boxId = GiftItems.boxId(profile);
		if (boxId.isPresent()) {
			releaseBox(level, pos, opener, boxId.get());
			return;
		}
		var rng = ThreadLocalRandom.current();
		GiftRoller.RolledGift rolled = GiftRoller.roll(ConfigLoader.current(), rng);
		Vec3 origin = Vec3.atCenterOf(pos);
		try {
			OutcomeApplier.apply(rolled.outcome(), level, pos, opener, rng);
			for (var bonus : rolled.tier().bonus()) {
				OutcomeApplier.apply(bonus, level, pos, opener, rng);
			}
		} catch (Exception e) {
			RussianRouletteGifts.LOGGER.error("Failed to apply gift outcome {} (tier {}); returning the gift",
					rolled.outcome(), rolled.tier().id(), e);
			ItemEntity back = new ItemEntity(level, origin.x, origin.y, origin.z, GiftItems.create(1), 0.0, 0.2, 0.0);
			level.addFreshEntity(back);
			return;
		}
		// Outside the refund path: the outcome already happened, so a feedback failure must not hand back a gift.
		try {
			Feedback.send(opener, level, origin, rolled.tier(), rolled.outcome());
		} catch (Exception e) {
			RussianRouletteGifts.LOGGER.error("Gift feedback failed for tier {}", rolled.tier().id(), e);
		}
	}

	/** A filled box: free every stored mob instead of rolling loot. */
	private static void releaseBox(ServerLevel level, BlockPos pos, ServerPlayer opener, UUID boxId) {
		BoxStorage storage = BoxStorage.get(level.getServer());
		List<CompoundTag> mobs = storage.take(boxId);
		if (mobs.isEmpty()) {
			Feedback.title(opener, Component.literal("Empty...").withStyle(ChatFormatting.GRAY),
					Component.literal("They escaped!").withStyle(ChatFormatting.WHITE));
			level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0f, 1.0f);
			return;
		}
		Vec3 origin = Vec3.atCenterOf(pos);
		List<SpreadOffsets.Offset> offsets = SpreadOffsets.spiral(mobs.size(), 3.0);
		List<CompoundTag> failed = new ArrayList<>();
		int released = 0;
		for (int i = 0; i < mobs.size(); i++) {
			CompoundTag tag = mobs.get(i);
			try {
				Vec3 at = freeSpot(level, origin, offsets.get(i));
				ValueInput in = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag);
				Entity entity = EntityType.loadEntityRecursive(in, level, EntitySpawnReason.LOAD, EntityProcessor.NOP);
				if (entity == null) throw new IllegalStateException("saved entity could not be loaded");
				entity.snapTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360.0f, 0.0f);
				entity.setDeltaMovement(Vec3.ZERO);
				entity.resetFallDistance();
				if (entity instanceof Mob mob && entity instanceof Enemy) mob.setTarget(opener);
				if (!level.tryAddFreshEntityWithPassengers(entity)) throw new IllegalStateException("world refused the entity");
				released++;
			} catch (Exception e) {
				// Spawned ones stay spawned; only the ones that failed go back into the box.
				RussianRouletteGifts.LOGGER.error("Failed to release a boxed mob ({}); keeping it in the box", tag, e);
				failed.add(tag);
			}
		}
		if (!failed.isEmpty()) {
			failed.forEach(t -> storage.add(boxId, t));
			level.addFreshEntity(new ItemEntity(level, origin.x, origin.y, origin.z, GiftItems.createFilledBox(boxId), 0.0, 0.2, 0.0));
		}
		try {
			int n = released;
			Feedback.title(opener, Component.literal("SURPRISE!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
					Component.literal(n + (n == 1 ? " mob!" : " mobs!")).withStyle(ChatFormatting.WHITE));
			level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.BLOCKS, 1.0f, 1.0f);
			level.sendParticles(ParticleTypes.FIREWORK, origin.x, origin.y + 0.3, origin.z, 40, 0.8, 0.5, 0.8, 0.1);
			level.sendParticles(ParticleTypes.POOF, origin.x, origin.y + 0.3, origin.z, 30, 1.5, 0.3, 1.5, 0.05);
		} catch (Exception e) {
			RussianRouletteGifts.LOGGER.error("Box feedback failed", e);
		}
	}

	/** The offset spot if feet and head are free of collision, else the gift's own spot. */
	private static Vec3 freeSpot(ServerLevel level, Vec3 origin, SpreadOffsets.Offset off) {
		Vec3 spot = new Vec3(origin.x + off.dx(), origin.y - 0.5, origin.z + off.dz());
		BlockPos feet = BlockPos.containing(spot);
		boolean free = level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
				&& level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
		return free ? spot : new Vec3(origin.x, origin.y - 0.5, origin.z);
	}
}
