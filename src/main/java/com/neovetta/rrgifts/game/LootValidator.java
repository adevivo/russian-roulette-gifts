package com.neovetta.rrgifts.game;

import com.neovetta.rrgifts.core.LootConfigException;
import com.neovetta.rrgifts.core.LootTable;
import com.neovetta.rrgifts.core.Outcome;
import com.neovetta.rrgifts.core.TierDef;
import com.neovetta.rrgifts.core.WeightedOutcome;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/** Checks every id in a parsed loot table against the live registries. Throws LootConfigException. */
public final class LootValidator {
	private LootValidator() {}

	public static void validate(LootTable table, HolderLookup.Provider access) {
		for (int i = 0; i < table.tiers().size(); i++) {
			TierDef t = table.tiers().get(i);
			for (int j = 0; j < t.outcomes().size(); j++) {
				WeightedOutcome wo = t.outcomes().get(j);
				validateOutcome(wo.outcome(), "tiers[" + i + "].outcomes[" + j + "]", access);
			}
			for (int j = 0; j < t.bonus().size(); j++) {
				validateOutcome(t.bonus().get(j), "tiers[" + i + "].bonus[" + j + "]", access);
			}
		}
	}

	private static void validateOutcome(Outcome o, String p, HolderLookup.Provider access) {
		switch (o) {
			case Outcome.GiveItem g -> validateGive(g, p, access);
			case Outcome.Cursed c -> validateGive(c.item(), p + ".item", access);
			case Outcome.SpawnMob m -> {
				var type = BuiltInRegistries.ENTITY_TYPE.get(id(m.entity(), p + ".entity"));
				if (type.isEmpty()) throw bad(p + ".entity", "entity type", m.entity());
				if (!type.get().value().canSummon()) {
					throw new LootConfigException(p + ".entity: '" + m.entity() + "' cannot be spawned");
				}
			}
			case Outcome.Effect e -> {
				if (BuiltInRegistries.MOB_EFFECT.get(id(e.effect(), p + ".effect")).isEmpty()) {
					throw bad(p + ".effect", "mob effect", e.effect());
				}
			}
			case Outcome.DropBlock d -> {
				if (BuiltInRegistries.BLOCK.get(id(d.block(), p + ".block")).isEmpty()) {
					throw bad(p + ".block", "block", d.block());
				}
			}
			case Outcome.Explode e -> { }
			case Outcome.NestedGift n -> { }
		}
	}

	private static void validateGive(Outcome.GiveItem g, String p, HolderLookup.Provider access) {
		var item = BuiltInRegistries.ITEM.get(id(g.item(), p + ".item"));
		if (item.isEmpty() || item.get().value() == Items.AIR) throw bad(p + ".item", "item", g.item());
		var registry = access.lookupOrThrow(Registries.ENCHANTMENT);
		for (Map.Entry<String, Integer> e : g.enchantments().entrySet()) {
			if (registry.get(net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT, id(e.getKey(), p + ".enchantments"))).isEmpty()) {
				throw bad(p + ".enchantments." + e.getKey(), "enchantment", e.getKey());
			}
		}
	}

	private static Identifier id(String s, String p) {
		Identifier id = Identifier.tryParse(s);
		if (id == null) throw new LootConfigException(p + ": invalid id '" + s + "'");
		return id;
	}

	private static LootConfigException bad(String path, String what, String id) {
		return new LootConfigException(path + ": unknown " + what + " '" + id + "'");
	}
}
