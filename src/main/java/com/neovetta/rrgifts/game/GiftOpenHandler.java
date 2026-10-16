package com.neovetta.rrgifts.game;

import com.neovetta.rrgifts.RussianRouletteGifts;
import com.neovetta.rrgifts.core.GiftRoller;
import java.util.concurrent.ThreadLocalRandom;
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
			open(serverLevel, pos, opener);
			return InteractionResult.SUCCESS;
		});
	}

	private static void open(ServerLevel level, BlockPos pos, ServerPlayer opener) {
		level.removeBlock(pos, false);
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
}
