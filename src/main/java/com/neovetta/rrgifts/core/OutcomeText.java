package com.neovetta.rrgifts.core;

import java.util.Locale;

/** Short player-facing description of an outcome, shown under the tier title. */
public final class OutcomeText {
	private OutcomeText() {}

	public static String describe(Outcome outcome) {
		return switch (outcome) {
			case Outcome.GiveItem g -> itemName(g);
			case Outcome.Cursed c -> itemName(c.item());
			case Outcome.Explode _ -> "Primed TNT! Run!";
			case Outcome.SpawnMob m -> (m.angry() ? "Angry " : "") + pretty(m.entity()) + " ×" + m.count();
			case Outcome.Effect e -> pretty(e.effect());
			case Outcome.DropBlock _ -> "Look up!";
			case Outcome.NestedGift n -> n.count() == 1 ? "Another gift!" : n.count() + " more gifts!";
		};
	}

	private static String itemName(Outcome.GiveItem g) {
		return g.name() != null ? g.name() : pretty(g.item());
	}

	/** "minecraft:enchanted_golden_apple" → "Enchanted Golden Apple". */
	public static String pretty(String id) {
		String path = id.substring(id.indexOf(':') + 1);
		path = path.substring(path.lastIndexOf('/') + 1);
		StringBuilder sb = new StringBuilder();
		for (String word : path.split("_")) {
			if (word.isEmpty()) continue;
			if (!sb.isEmpty()) sb.append(' ');
			sb.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
		}
		return sb.toString();
	}
}
