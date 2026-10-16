package com.neovetta.rrgifts.game;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.neovetta.rrgifts.core.GiftConstants;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

/** Builds the Mystery Gift item; identical to the crafting recipe result. */
public final class GiftItems {
	private GiftItems() {}

	public static ItemStack create(int count) {
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD, count);
		GameProfile profile = new GameProfile(GiftConstants.GIFT_PROFILE_ID, GiftConstants.GIFT_PROFILE_NAME,
				new PropertyMap(ImmutableMultimap.of("textures",
						new Property("textures", GiftConstants.GIFT_TEXTURE_VALUE))));
		stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(GiftConstants.GIFT_NAME));
		stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(GiftConstants.GIFT_LORE))));
		return stack;
	}

	/**
	 * A placed skull keeps only its name and profile, so a broken or exploded gift drops without lore.
	 * Put the lore back on any dropped gift as it enters the world.
	 */
	public static void registerLoreRestore() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (!(entity instanceof ItemEntity item)) return;
			ItemStack stack = item.getItem();
			// Every item has a default empty lore component, so test for lines rather than has().
			if (!stack.is(Items.PLAYER_HEAD) || !stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines().isEmpty()
					|| !isGiftProfile(stack.get(DataComponents.PROFILE))) {
				return;
			}
			ItemStack fixed = stack.copy();
			fixed.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(GiftConstants.GIFT_LORE))));
			item.setItem(fixed);
		});
	}

	public static boolean isGiftProfile(ResolvableProfile profile) {
		return profile != null && GiftConstants.GIFT_PROFILE_ID.equals(profile.partialProfile().id());
	}
}
