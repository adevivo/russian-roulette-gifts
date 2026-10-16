package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OutcomeTextTest {
	private static Outcome.GiveItem give(String item, String name) {
		return new Outcome.GiveItem(item, 1, 1, Map.of(), null, name, List.of());
	}

	@Test
	void prettifiesIds() {
		assertEquals("Enchanted Golden Apple", OutcomeText.pretty("minecraft:enchanted_golden_apple"));
		assertEquals("Diamond", OutcomeText.pretty("minecraft:diamond"));
	}

	@Test
	void namedItemsUseTheirName() {
		assertEquals("Lump of Coal", OutcomeText.describe(give("minecraft:coal", "Lump of Coal")));
		assertEquals("Iron Sword", OutcomeText.describe(give("minecraft:iron_sword", null)));
		assertEquals("Swim Fins", OutcomeText.describe(new Outcome.Cursed(give("minecraft:leather_boots", "Swim Fins"), Outcome.Curse.ANY)));
	}

	@Test
	void describesNonItemOutcomes() {
		assertEquals("Angry Bee ×4", OutcomeText.describe(new Outcome.SpawnMob("minecraft:bee", 4, true)));
		assertEquals("Creeper ×1", OutcomeText.describe(new Outcome.SpawnMob("minecraft:creeper", 1, false)));
		assertEquals("Levitation", OutcomeText.describe(new Outcome.Effect("minecraft:levitation", 100, 1)));
		assertEquals("Another gift!", OutcomeText.describe(new Outcome.NestedGift(1)));
		assertEquals("2 more gifts!", OutcomeText.describe(new Outcome.NestedGift(2)));
	}

	@Test
	void everyDefaultOutcomeHasText() {
		for (TierDef t : DefaultLoot.load().tiers()) {
			for (WeightedOutcome wo : t.outcomes()) {
				assertFalse(OutcomeText.describe(wo.outcome()).isBlank(), wo.toString());
			}
		}
	}
}
