package com.neovetta.rrgifts.game;

import com.neovetta.rrgifts.RussianRouletteGifts;
import com.neovetta.rrgifts.core.CaptureRules;
import com.neovetta.rrgifts.core.CaptureSettings;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;

/** Right-click a recently hit mob with an Empty Gift Box (or a filled one) to box it up; right-click the air to count. */
public final class BoxCapture {
	private BoxCapture() {}

	public static void register() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || player.isSpectator()
					|| !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer sp)) {
				return InteractionResult.PASS;
			}
			CaptureSettings cfg = ConfigLoader.current().capture();
			if (!cfg.enabled()) return InteractionResult.PASS;
			ItemStack held = player.getMainHandItem();
			Optional<UUID> filledId = GiftItems.boxId(held);
			if (!filledId.isPresent() && !GiftItems.isEmptyBox(held)) return InteractionResult.PASS;
			capture(sp, serverLevel, entity, held, filledId.orElse(null), cfg);
			return InteractionResult.SUCCESS;
		});

		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !(player instanceof ServerPlayer sp)
					|| player.isSpectator()) {
				return InteractionResult.PASS;
			}
			ItemStack held = player.getMainHandItem();
			Optional<UUID> filledId = GiftItems.boxId(held);
			if (!filledId.isPresent() && !GiftItems.isEmptyBox(held)) return InteractionResult.PASS;
			int n = filledId.map(id -> BoxStorage.get(sp.level().getServer()).count(id)).orElse(0);
			sp.sendOverlayMessage(countText(n, ConfigLoader.current().capture().maxMobs()));
			// player_head is equippable but unswappable, so right-click never equips it; PASS is enough.
			return InteractionResult.PASS;
		});
	}

	private static Component countText(int n, int max) {
		return Component.literal("📦 " + n + "/" + max);
	}

	private static void capture(ServerPlayer player, ServerLevel level, Entity target, ItemStack held, UUID filledId,
			CaptureSettings cfg) {
		BoxStorage storage = BoxStorage.get(level.getServer());
		int count = filledId == null ? 0 : storage.count(filledId);
		boolean isMob = target instanceof Mob;
		boolean hit = false;
		if (target instanceof Mob mob) {
			// Vanilla remembers the last player attacker for 100 ticks (the memory time counts down to 0).
			Player lastHurtBy = mob.getLastHurtByPlayer();
			hit = lastHurtBy != null && lastHurtBy.getUUID().equals(player.getUUID())
					&& mob.getLastHurtByPlayerMemoryTime() > 0;
		}
		CaptureRules.Result result = CaptureRules.check(target instanceof Player,
				target instanceof EnderDragon || target instanceof EnderDragonPart, isMob,
				target.isPassenger() || target.isVehicle(), target instanceof Enemy, cfg.hostileCapture(), hit, count,
				cfg.maxMobs());
		if (result != CaptureRules.Result.OK) {
			player.sendOverlayMessage(Component.literal(switch (result) {
				case FULL -> "This box is full (" + count + "/" + cfg.maxMobs() + ")";
				case HOSTILE_DISABLED -> "Hostile mobs can't be boxed on this server";
				case NOT_HIT_RECENTLY -> "Hit it first!";
				default -> "That one won't fit in a box";
			}));
			return;
		}
		Mob mob = (Mob) target;
		CompoundTag tag;
		try {
			TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
			if (!mob.save(out)) {
				player.sendOverlayMessage(Component.literal("That one won't fit in a box"));
				return;
			}
			tag = out.buildResult();
		} catch (Exception e) {
			RussianRouletteGifts.LOGGER.error("Could not save {} for a gift box", mob.getType(), e);
			player.sendOverlayMessage(Component.literal("That one won't fit in a box"));
			return;
		}
		UUID id = filledId != null ? filledId : UUID.randomUUID();
		storage.add(id, tag);
		double x = mob.getX(), y = mob.getY() + mob.getBbHeight() / 2.0, z = mob.getZ();
		if (mob.isLeashed()) mob.dropLeash();
		mob.discard();

		if (filledId == null) {
			ItemStack filled = GiftItems.createFilledBox(id);
			if (held.getCount() == 1) {
				player.setItemInHand(InteractionHand.MAIN_HAND, filled);
			} else {
				held.shrink(1);
				player.getInventory().add(filled);
				if (!filled.isEmpty()) player.drop(filled, false);
			}
		}
		level.playSound(null, x, y, z, SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0f, 0.8f);
		level.sendParticles(ParticleTypes.POOF, x, y, z, 12, 0.3, 0.3, 0.3, 0.05);
		player.sendOverlayMessage(countText(count + 1, cfg.maxMobs()));
	}
}
