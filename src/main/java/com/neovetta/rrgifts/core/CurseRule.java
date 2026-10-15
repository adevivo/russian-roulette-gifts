package com.neovetta.rrgifts.core;

import java.util.random.RandomGenerator;

public final class CurseRule {
	public static final String VANISHING = "minecraft:vanishing_curse";
	public static final String BINDING = "minecraft:binding_curse";

	private CurseRule() {}

	public static String resolve(Outcome.Curse curse, boolean wearable, RandomGenerator rng) {
		return switch (curse) {
			case VANISHING -> VANISHING;
			case BINDING -> {
				if (!wearable) {
					throw new IllegalArgumentException("Curse of Binding needs a wearable item");
				}
				yield BINDING;
			}
			case ANY -> wearable && rng.nextBoolean() ? BINDING : VANISHING;
		};
	}
}
