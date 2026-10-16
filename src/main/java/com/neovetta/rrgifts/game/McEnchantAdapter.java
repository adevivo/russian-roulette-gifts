package com.neovetta.rrgifts.game;

import com.neovetta.rrgifts.core.EnchantDef;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/** Converts live registry enchantments into the pure-Java EnchantDef model. */
public final class McEnchantAdapter {
	private McEnchantAdapter() {}

	public static String idOf(Holder<Enchantment> h) {
		return h.unwrapKey().orElseThrow().identifier().toString();
	}

	public static List<EnchantDef> applicableTo(ItemStack stack, HolderLookup.Provider access, Set<String> existing) {
		List<Holder.Reference<Enchantment>> all =
				access.lookupOrThrow(Registries.ENCHANTMENT).listElements().toList();
		boolean book = stack.is(Items.ENCHANTED_BOOK);
		List<Holder.Reference<Enchantment>> chosen = new ArrayList<>();
		for (Holder.Reference<Enchantment> h : all) {
			if (book || h.value().isSupportedItem(stack) || existing.contains(idOf(h))) {
				chosen.add(h);
			}
		}
		List<EnchantDef> defs = new ArrayList<>();
		for (Holder.Reference<Enchantment> h : chosen) {
			Set<String> excl = new HashSet<>();
			for (Holder.Reference<Enchantment> o : all) {
				if (o != h && !Enchantment.areCompatible(h, o)) {
					excl.add(idOf(o));
				}
			}
			defs.add(new EnchantDef(idOf(h), h.value().getMaxLevel(), h.is(EnchantmentTags.CURSE), excl));
		}
		return defs;
	}
}
