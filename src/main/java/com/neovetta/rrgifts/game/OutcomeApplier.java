package com.neovetta.rrgifts.game;

import com.neovetta.rrgifts.RussianRouletteGifts;
import com.neovetta.rrgifts.core.CurseRule;
import com.neovetta.rrgifts.core.DropSearch;
import com.neovetta.rrgifts.core.EnchantDef;
import com.neovetta.rrgifts.core.EnchantPicker;
import com.neovetta.rrgifts.core.Outcome;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/** Applies a rolled Outcome in the world. Ids are validated at config load, but still looked up defensively. */
public final class OutcomeApplier {
	private OutcomeApplier() {}

	public static void apply(Outcome outcome, ServerLevel level, BlockPos pos, ServerPlayer opener, RandomGenerator rng) {
		Vec3 origin = Vec3.atCenterOf(pos);
		switch (outcome) {
			case Outcome.GiveItem g -> drop(level, origin, buildStack(g, level, rng));
			case Outcome.Cursed c -> applyCursed(c, level, origin, rng);
			case Outcome.Explode e -> {
				PrimedTnt tnt = new PrimedTnt(level, origin.x, pos.getY(), origin.z, opener);
				tnt.setFuse(e.fuseTicks());
				level.addFreshEntity(tnt);
			}
			case Outcome.SpawnMob m -> spawnMobs(m, level, pos, opener);
			case Outcome.Effect e -> {
				Holder<MobEffect> h = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(e.effect()))
						.orElseThrow(() -> new IllegalStateException("Unknown effect " + e.effect()));
				opener.addEffect(new MobEffectInstance(h, e.durationTicks(), e.amplifier()));
			}
			case Outcome.DropBlock d -> dropBlock(d, level, pos);
			case Outcome.NestedGift n -> drop(level, origin, GiftItems.create(n.count()));
		}
	}

	private static void drop(ServerLevel level, Vec3 origin, ItemStack stack) {
		ItemEntity entity = new ItemEntity(level, origin.x, origin.y, origin.z, stack, 0.0, 0.2, 0.0);
		entity.setDefaultPickUpDelay();
		level.addFreshEntity(entity);
	}

	static ItemStack buildStack(Outcome.GiveItem g, ServerLevel level, RandomGenerator rng) {
		Item item = BuiltInRegistries.ITEM.get(Identifier.parse(g.item()))
				.orElseThrow(() -> new IllegalStateException("Unknown item " + g.item())).value();
		int count = g.count() + (g.maxCount() > g.count() ? rng.nextInt(g.maxCount() - g.count() + 1) : 0);
		ItemStack stack = new ItemStack(item, 1);
		count = Math.min(count, Math.max(1, stack.getMaxStackSize()));
		stack.setCount(count);

		DataComponentType<ItemEnchantments> type =
				stack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
		Map<String, Integer> levels = new LinkedHashMap<>(g.enchantments());
		if (g.randomEnchants() != null) {
			List<EnchantDef> defs = McEnchantAdapter.applicableTo(stack, level.registryAccess(), levels.keySet());
			levels = EnchantPicker.pickRandom(defs, levels, g.randomEnchants(), rng);
		}
		if (!levels.isEmpty()) {
			var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			ItemEnchantments.Mutable m = new ItemEnchantments.Mutable(stack.getOrDefault(type, ItemEnchantments.EMPTY));
			for (Map.Entry<String, Integer> e : levels.entrySet()) {
				Optional<Holder.Reference<Enchantment>> h = registry.get(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(e.getKey())));
				if (h.isPresent()) {
					m.set(h.get(), Math.min(255, e.getValue()));
				} else {
					RussianRouletteGifts.LOGGER.warn("Unknown enchantment '{}' skipped", e.getKey());
				}
			}
			stack.set(type, m.toImmutable());
		}
		if (g.name() != null) {
			stack.set(DataComponents.CUSTOM_NAME, Component.literal(g.name()));
		}
		if (!g.lore().isEmpty()) {
			stack.set(DataComponents.LORE, new ItemLore(g.lore().stream().map(s -> (Component) Component.literal(s)).toList()));
		}
		return stack;
	}

	private static void applyCursed(Outcome.Cursed c, ServerLevel level, Vec3 origin, RandomGenerator rng) {
		ItemStack stack = buildStack(c.item(), level, rng);
		var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		boolean wearable = registry.get(Enchantments.BINDING_CURSE).map(h -> h.value().isSupportedItem(stack)).orElse(false);
		String curseId;
		try {
			curseId = CurseRule.resolve(c.curse(), wearable, rng);
		} catch (IllegalArgumentException ex) {
			RussianRouletteGifts.LOGGER.warn("Binding curse not applicable to {}; using vanishing", c.item().item());
			curseId = CurseRule.VANISHING;
		}
		Optional<Holder.Reference<Enchantment>> h = registry.get(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(curseId)));
		if (h.isPresent()) {
			DataComponentType<ItemEnchantments> type =
					stack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
			ItemEnchantments.Mutable m = new ItemEnchantments.Mutable(stack.getOrDefault(type, ItemEnchantments.EMPTY));
			m.set(h.get(), 1);
			stack.set(type, m.toImmutable());
		}
		drop(level, origin, stack);
	}

	private static void spawnMobs(Outcome.SpawnMob m, ServerLevel level, BlockPos pos, ServerPlayer opener) {
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(m.entity()))
				.orElseThrow(() -> new IllegalStateException("Unknown entity " + m.entity())).value();
		for (int i = 0; i < m.count(); i++) {
			Entity e = type.spawn(level, pos, EntitySpawnReason.EVENT);
			if (e == null) {
				RussianRouletteGifts.LOGGER.warn("Could not spawn {} at {}", m.entity(), pos);
				continue;
			}
			if (m.angry() && e instanceof Mob mob) {
				mob.setTarget(opener);
				if (mob instanceof NeutralMob neutral) {
					neutral.setPersistentAngerTarget(EntityReference.of(opener));
					neutral.startPersistentAngerTimer();
				}
			}
		}
	}

	private static void dropBlock(Outcome.DropBlock d, ServerLevel level, BlockPos origin) {
		Block block = BuiltInRegistries.BLOCK.get(Identifier.parse(d.block()))
				.orElseThrow(() -> new IllegalStateException("Unknown block " + d.block())).value();
		int h = DropSearch.highestClear(d.height(), off -> level.getBlockState(origin.above(off)).isAir());
		FallingBlockEntity.fall(level, origin.above(h), block.defaultBlockState());
	}
}
