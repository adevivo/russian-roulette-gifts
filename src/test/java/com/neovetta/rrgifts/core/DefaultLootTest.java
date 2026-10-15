package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultLootTest {
	private final LootTable table = DefaultLoot.load();

	private List<Outcome.GiveItem> allGives() {
		return table.tiers().stream().flatMap(t -> t.outcomes().stream()).map(WeightedOutcome::outcome)
				.filter(o -> o instanceof Outcome.GiveItem).map(o -> (Outcome.GiveItem) o).toList();
	}

	private Optional<Outcome.GiveItem> named(String name) {
		return allGives().stream().filter(g -> name.equals(g.name())).findFirst();
	}

	@Test
	void tiersAndWeights() {
		assertEquals(List.of("disaster", "cursed", "junk", "good", "jackpot"), table.tiers().stream().map(TierDef::id).toList());
		assertEquals(List.of(15, 15, 30, 30, 10), table.tiers().stream().map(TierDef::weight).toList());
	}

	@Test
	void handyDandyItemsExact() {
		check("Swim Fins", "minecraft:leather_boots", Map.of("minecraft:depth_strider", 7, "minecraft:respiration", 3),
				List.of("Mighty duck feet"));
		check("Jump Boots", "minecraft:leather_boots",
				Map.of("minecraft:feather_falling", 4, "minecraft:protection", 4),
				List.of("Jump like a kangaroo"));
		check("Copper Pickaxe", "minecraft:golden_pickaxe", Map.of("minecraft:unbreaking", 10, "minecraft:efficiency", 7),
				List.of("Mighty copper pickaxe", "of the dwarves"));
		check("Ginsu Iron Sword", "minecraft:iron_sword", Map.of("minecraft:sharpness", 5),
				List.of("Forged by ancient", "minecraft dwarves and", "sharper than a", "butter knife"));
		check("Ouchy Diamond Sword", "minecraft:diamond_sword", Map.of("minecraft:sharpness", 10),
				List.of("Forged by ancient", "minecraft elves and", "sharper than the", "average pointy object"));
	}

	private void check(String name, String item, Map<String, Integer> ench, List<String> lore) {
		Outcome.GiveItem g = named(name).orElseThrow(() -> new AssertionError("missing " + name));
		assertEquals(item, g.item());
		assertEquals(ench, g.enchantments());
		assertEquals(lore, g.lore());
	}

	@Test
	void containsNestedGift() {
		assertTrue(table.tiers().stream().flatMap(t -> t.outcomes().stream())
				.anyMatch(o -> o.outcome() instanceof Outcome.NestedGift));
	}

	@Test
	void bindingOnlyOnArmor() {
		int cursedCount = 0;
		for (TierDef t : table.tiers()) {
			for (WeightedOutcome wo : t.outcomes()) {
				if (wo.outcome() instanceof Outcome.Cursed c) {
					cursedCount++;
					if (c.curse() == Outcome.Curse.BINDING) {
						String id = c.item().item();
						assertTrue(id.endsWith("_helmet") || id.endsWith("_chestplate") || id.endsWith("_leggings")
								|| id.endsWith("_boots"), "binding on non-armor: " + id);
					}
				}
			}
		}
		assertTrue(cursedCount > 0);
	}
}
