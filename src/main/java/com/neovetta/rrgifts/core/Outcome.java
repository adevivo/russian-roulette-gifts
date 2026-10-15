package com.neovetta.rrgifts.core;

import java.util.List;
import java.util.Map;

/** What happens when a gift is opened. Pure data; the game layer applies it. */
public sealed interface Outcome {

	/** Give an item; count is uniform in [count, maxCount]. */
	record GiveItem(String item, int count, int maxCount, Map<String, Integer> enchantments,
			RandomEnchants randomEnchants, String name, List<String> lore) implements Outcome {
		public GiveItem {
			enchantments = Map.copyOf(enchantments);
			lore = List.copyOf(lore);
		}
	}

	/** Pick min..max random enchants; overMax extra levels above each enchant's max. */
	record RandomEnchants(int min, int max, int overMax) {}

	/** Primed TNT. */
	record Explode(int fuseTicks) implements Outcome {}

	record SpawnMob(String entity, int count, boolean angry) implements Outcome {}

	record Effect(String effect, int durationTicks, int amplifier) implements Outcome {}

	/** A block that falls from {@code height} blocks above the player. */
	record DropBlock(String block, int height) implements Outcome {}

	enum Curse { VANISHING, BINDING, ANY }

	record Cursed(GiveItem item, Curse curse) implements Outcome {}

	record NestedGift(int count) implements Outcome {}
}
