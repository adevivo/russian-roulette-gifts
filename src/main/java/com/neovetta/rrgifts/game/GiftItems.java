package com.neovetta.rrgifts.game;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.neovetta.rrgifts.core.GiftConstants;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
		return build(count, GiftConstants.GIFT_PROFILE_ID, GiftConstants.GIFT_PROFILE_NAME, GiftConstants.GIFT_TEXTURE_VALUE,
				GiftConstants.GIFT_NAME, GiftConstants.GIFT_LORE, null);
	}

	/** Empty Gift Box; must match recipe/empty_gift_box.json. */
	public static ItemStack createEmptyBox(int count) {
		return build(count, GiftConstants.EMPTY_BOX_PROFILE_ID, GiftConstants.EMPTY_BOX_PROFILE_NAME,
				GiftConstants.EMPTY_BOX_TEXTURE_VALUE, GiftConstants.EMPTY_BOX_NAME, GiftConstants.EMPTY_BOX_LORE, null);
	}

	/** A Mystery Gift lookalike that holds the mobs stored under boxId. */
	public static ItemStack createFilledBox(UUID boxId) {
		return build(1, GiftConstants.GIFT_PROFILE_ID, GiftConstants.GIFT_PROFILE_NAME, GiftConstants.GIFT_TEXTURE_VALUE,
				GiftConstants.GIFT_NAME, GiftConstants.GIFT_LORE, boxId);
	}

	private static ItemStack build(int count, UUID profileId, String profileName, String texture, String name, String lore,
			UUID boxId) {
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD, count);
		ImmutableMultimap.Builder<String, Property> props = ImmutableMultimap.builder();
		props.put("textures", new Property("textures", texture));
		if (boxId != null) {
			props.put(GiftConstants.BOX_ID_PROPERTY, new Property(GiftConstants.BOX_ID_PROPERTY, boxId.toString()));
		}
		GameProfile profile = new GameProfile(profileId, profileName, new PropertyMap(props.build()));
		stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
		stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(lore))));
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
			if (!stack.is(Items.PLAYER_HEAD) || !stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines().isEmpty()) {
				return;
			}
			ResolvableProfile profile = stack.get(DataComponents.PROFILE);
			String lore = isGiftProfile(profile) ? GiftConstants.GIFT_LORE
					: isEmptyBoxProfile(profile) ? GiftConstants.EMPTY_BOX_LORE : null;
			if (lore == null) return;
			ItemStack fixed = stack.copy();
			fixed.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(lore))));
			item.setItem(fixed);
		});
	}

	public static boolean isGiftProfile(ResolvableProfile profile) {
		return profile != null && GiftConstants.GIFT_PROFILE_ID.equals(profile.partialProfile().id());
	}

	public static boolean isEmptyBoxProfile(ResolvableProfile profile) {
		return profile != null && GiftConstants.EMPTY_BOX_PROFILE_ID.equals(profile.partialProfile().id());
	}

	public static boolean isEmptyBox(ItemStack stack) {
		return stack.is(Items.PLAYER_HEAD) && isEmptyBoxProfile(stack.get(DataComponents.PROFILE));
	}

	/** The storage id of a filled box, if this profile has one. */
	public static Optional<UUID> boxId(ResolvableProfile profile) {
		if (profile == null) return Optional.empty();
		for (Property p : profile.partialProfile().properties().get(GiftConstants.BOX_ID_PROPERTY)) {
			try {
				return Optional.of(UUID.fromString(p.value()));
			} catch (IllegalArgumentException ignored) {
				// malformed: treat as no id
			}
		}
		return Optional.empty();
	}

	public static Optional<UUID> boxId(ItemStack stack) {
		return stack.is(Items.PLAYER_HEAD) ? boxId(stack.get(DataComponents.PROFILE)) : Optional.empty();
	}
}
