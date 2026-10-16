package com.neovetta.rrgifts.game;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.neovetta.rrgifts.core.GiftConstants;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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

	public static boolean isGiftProfile(ResolvableProfile profile) {
		return profile != null && GiftConstants.GIFT_PROFILE_ID.equals(profile.partialProfile().id());
	}
}
