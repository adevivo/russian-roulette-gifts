package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CurseRuleTest {
	@Test
	void vanishingAlwaysVanishing() {
		assertEquals("minecraft:vanishing_curse", CurseRule.resolve(Outcome.Curse.VANISHING, true, new Random(1)));
		assertEquals("minecraft:vanishing_curse", CurseRule.resolve(Outcome.Curse.VANISHING, false, new Random(1)));
	}

	@Test
	void bindingOnWearable() {
		assertEquals("minecraft:binding_curse", CurseRule.resolve(Outcome.Curse.BINDING, true, new Random(1)));
	}

	@Test
	void bindingOnNonWearableThrows() {
		var ex = assertThrows(IllegalArgumentException.class,
				() -> CurseRule.resolve(Outcome.Curse.BINDING, false, new Random(1)));
		assertEquals("Curse of Binding needs a wearable item", ex.getMessage());
	}

	@Test
	void anyOnNonWearableIsVanishing() {
		Random r = new Random(5);
		for (int i = 0; i < 500; i++) {
			assertEquals("minecraft:vanishing_curse", CurseRule.resolve(Outcome.Curse.ANY, false, r));
		}
	}

	@Test
	void anyOnWearableYieldsBoth() {
		Random r = new Random(5);
		Set<String> seen = new HashSet<>();
		for (int i = 0; i < 500; i++) seen.add(CurseRule.resolve(Outcome.Curse.ANY, true, r));
		assertEquals(Set.of("minecraft:vanishing_curse", "minecraft:binding_curse"), seen);
	}
}
