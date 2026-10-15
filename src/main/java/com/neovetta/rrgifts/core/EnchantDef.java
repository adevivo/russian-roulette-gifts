package com.neovetta.rrgifts.core;

import java.util.Set;

/** Abstract enchantment descriptor; the game layer builds these from the registry. */
public record EnchantDef(String id, int maxLevel, boolean curse, Set<String> exclusiveWith) {
	public EnchantDef {
		exclusiveWith = Set.copyOf(exclusiveWith);
	}

	/** Exclusivity is symmetric: either side listing the other is a conflict. */
	public boolean conflictsWith(EnchantDef other) {
		return exclusiveWith.contains(other.id) || other.exclusiveWith.contains(id);
	}
}
