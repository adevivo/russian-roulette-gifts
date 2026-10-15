package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import org.junit.jupiter.api.Test;

class LootConfigParserTest {
	private static LootTable parse(String json) {
		return LootConfigParser.parse(new StringReader(json));
	}

	/** Wraps one outcome JSON in a valid single-tier config. */
	private static String withOutcome(String outcome) {
		return "{\"version\":1,\"tiers\":[{\"id\":\"t\",\"weight\":1,\"outcomes\":[" + outcome + "]}]}";
	}

	private static Outcome one(String outcome) {
		return parse(withOutcome(outcome)).tiers().get(0).outcomes().get(0).outcome();
	}

	private static void rejects(String json, String... fragments) {
		LootConfigException ex = assertThrows(LootConfigException.class, () -> parse(json));
		for (String f : fragments) {
			assertTrue(ex.getMessage().contains(f), "message <" + ex.getMessage() + "> should contain <" + f + ">");
		}
	}

	private static void rejectsOutcome(String outcome, String... fragments) {
		rejects(withOutcome(outcome), fragments);
	}

	@Test
	void parsesFullExampleFromSpec() {
		String json = """
				{"version":1,"tiers":[
				 {"id":"disaster","weight":15,"message":"Disaster!","outcomes":[
				  {"type":"explode","weight":3,"fuse_ticks":40},
				  {"type":"spawn_mob","entity":"minecraft:bee","count":4,"angry":true},
				  {"type":"effect","effect":"minecraft:levitation","duration_ticks":100,"amplifier":1},
				  {"type":"drop_block","block":"minecraft:anvil","height":10}]},
				 {"id":"good","weight":30,"message":"Nice!","outcomes":[
				  {"type":"give_item","item":"minecraft:iron_ingot","count":4,"max_count":10},
				  {"type":"give_item","item":"minecraft:iron_sword","random_enchants":{"min":1,"max":2}}]},
				 {"id":"cursed","weight":15,"message":"Cursed...","outcomes":[
				  {"type":"cursed","curse":"any","item":{"item":"minecraft:diamond_helmet","random_enchants":{"min":2,"max":3}}}]},
				 {"id":"jackpot","weight":10,"message":"JACKPOT!","outcomes":[
				  {"type":"give_item","item":"minecraft:leather_boots","name":"Swim Fins","lore":["Mighty duck feet"],
				   "enchantments":{"minecraft:depth_strider":7,"minecraft:respiration":3}},
				  {"type":"nested_gift","count":2}]}]}
				""";
		LootTable t = parse(json);
		assertEquals(4, t.tiers().size());
		TierDef d = t.tiers().get(0);
		assertEquals("Disaster!", d.message());
		assertEquals(3, d.outcomes().get(0).weight());
		assertEquals(new Outcome.Explode(40), d.outcomes().get(0).outcome());
		assertEquals(new Outcome.SpawnMob("minecraft:bee", 4, true), d.outcomes().get(1).outcome());
		assertEquals(new Outcome.Effect("minecraft:levitation", 100, 1), d.outcomes().get(2).outcome());
		assertEquals(new Outcome.DropBlock("minecraft:anvil", 10), d.outcomes().get(3).outcome());
		assertEquals(1, d.outcomes().get(3).weight());
		var g = (Outcome.GiveItem) t.tiers().get(1).outcomes().get(0).outcome();
		assertEquals(4, g.count());
		assertEquals(10, g.maxCount());
		var g2 = (Outcome.GiveItem) t.tiers().get(1).outcomes().get(1).outcome();
		assertEquals(new Outcome.RandomEnchants(1, 2, 0), g2.randomEnchants());
		assertEquals(1, g2.count());
		assertEquals(1, g2.maxCount());
		assertNull(g2.name());
		var c = (Outcome.Cursed) t.tiers().get(2).outcomes().get(0).outcome();
		assertEquals(Outcome.Curse.ANY, c.curse());
		assertEquals("minecraft:diamond_helmet", c.item().item());
		assertEquals(new Outcome.RandomEnchants(2, 3, 0), c.item().randomEnchants());
		var s = (Outcome.GiveItem) t.tiers().get(3).outcomes().get(0).outcome();
		assertEquals("Swim Fins", s.name());
		assertEquals(java.util.List.of("Mighty duck feet"), s.lore());
		assertEquals(7, s.enchantments().get("minecraft:depth_strider"));
		assertNull(s.randomEnchants());
		assertEquals(new Outcome.NestedGift(2), t.tiers().get(3).outcomes().get(1).outcome());
	}

	@Test
	void defaults() {
		assertEquals(new Outcome.Explode(40), one("{\"type\":\"explode\"}"));
		assertEquals(new Outcome.SpawnMob("minecraft:cow", 1, false), one("{\"type\":\"spawn_mob\",\"entity\":\"minecraft:cow\"}"));
		assertEquals(new Outcome.Effect("minecraft:speed", 20, 0),
				one("{\"type\":\"effect\",\"effect\":\"minecraft:speed\",\"duration_ticks\":20}"));
		assertEquals(new Outcome.NestedGift(1), one("{\"type\":\"nested_gift\"}"));
		var r = (Outcome.GiveItem) one("{\"type\":\"give_item\",\"item\":\"minecraft:stick\",\"random_enchants\":{\"min\":0,\"max\":1}}");
		assertEquals(0, r.randomEnchants().overMax());
		assertTrue(r.lore().isEmpty());
		assertTrue(r.enchantments().isEmpty());
		var tier = parse("{\"version\":1,\"tiers\":[{\"id\":\"abc\",\"weight\":1,\"outcomes\":[{\"type\":\"explode\"}]}]}").tiers().get(0);
		assertEquals("abc", tier.message());
		var ov = (Outcome.GiveItem) one("{\"type\":\"give_item\",\"item\":\"minecraft:stick\",\"random_enchants\":{\"min\":1,\"max\":2,\"over_max\":3}}");
		assertEquals(3, ov.randomEnchants().overMax());
		var vc = (Outcome.Cursed) one("{\"type\":\"cursed\",\"curse\":\"binding\",\"item\":{\"item\":\"minecraft:iron_helmet\"}}");
		assertEquals(Outcome.Curse.BINDING, vc.curse());
	}

	@Test
	void rejectsMalformedJson() {
		rejects("{not json", "malformed");
		rejects("", "$");
		rejects("[]", "expected an object");
	}

	@Test
	void rejectsVersion() {
		rejects("{\"version\":2,\"tiers\":[]}", "version", "2");
		rejects("{\"tiers\":[]}", "version");
	}

	@Test
	void rejectsUnknownKeys() {
		rejects("{\"version\":1,\"tiers\":[],\"extra\":1}", "extra");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"t\",\"weight\":1,\"wieght\":2,\"outcomes\":[{\"type\":\"explode\"}]}]}",
				"tiers[0].wieght", "wieght");
		rejectsOutcome("{\"type\":\"explode\",\"fuse\":4}", "tiers[0].outcomes[0].fuse", "fuse");
		rejectsOutcome("{\"type\":\"give_item\",\"item\":\"minecraft:stick\",\"random_enchants\":{\"min\":1,\"max\":1,\"mx\":2}}",
				"tiers[0].outcomes[0].random_enchants.mx");
		rejectsOutcome("{\"type\":\"cursed\",\"curse\":\"any\",\"item\":{\"item\":\"minecraft:stick\",\"type\":\"give_item\"}}",
				"tiers[0].outcomes[0].item.type");
		rejectsOutcome("{\"type\":\"cursed\",\"curse\":\"any\",\"item\":{\"item\":\"minecraft:stick\",\"weight\":2}}",
				"tiers[0].outcomes[0].item.weight");
	}

	@Test
	void rejectsUnknownOutcomeType() {
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"a\",\"weight\":1,\"outcomes\":[{\"type\":\"explode\"}]},"
				+ "{\"id\":\"b\",\"weight\":1,\"outcomes\":[{\"type\":\"explode\"}]},"
				+ "{\"id\":\"c\",\"weight\":1,\"outcomes\":[{\"type\":\"explod\"}]}]}",
				"tiers[2].outcomes[0].type: unknown outcome type 'explod' (expected one of give_item, explode, spawn_mob, effect, drop_block, cursed, nested_gift)");
		rejectsOutcome("{\"weight\":1}", "tiers[0].outcomes[0].type", "missing");
	}

	@Test
	void rejectsTierProblems() {
		rejects("{\"version\":1}", "tiers");
		rejects("{\"version\":1,\"tiers\":[]}", "tiers");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"\",\"weight\":1,\"outcomes\":[{\"type\":\"explode\"}]}]}", "tiers[0].id");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"x\",\"weight\":1,\"outcomes\":[{\"type\":\"explode\"}]},"
				+ "{\"id\":\"x\",\"weight\":1,\"outcomes\":[{\"type\":\"explode\"}]}]}", "tiers[1].id", "'x'");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"x\",\"outcomes\":[{\"type\":\"explode\"}]}]}", "tiers[0].weight");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"x\",\"weight\":-1,\"outcomes\":[{\"type\":\"explode\"}]}]}", "tiers[0].weight", "-1");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"x\",\"weight\":0,\"outcomes\":[{\"type\":\"explode\"}]}]}", "total tier weight");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"x\",\"weight\":1,\"outcomes\":[]}]}", "tiers[0].outcomes");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"x\",\"weight\":1,\"outcomes\":[{\"type\":\"explode\",\"weight\":0}]}]}",
				"tiers[0].outcomes", "total outcome weight");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"x\",\"weight\":1,\"outcomes\":[{\"type\":\"explode\",\"weight\":-3}]}]}",
				"tiers[0].outcomes[0].weight", "-3");
	}

	@Test
	void zeroWeightTierMayHaveZeroWeightOutcomes() {
		var t = parse("{\"version\":1,\"tiers\":[{\"id\":\"off\",\"weight\":0,\"outcomes\":[{\"type\":\"explode\",\"weight\":0}]},"
				+ "{\"id\":\"on\",\"weight\":1,\"outcomes\":[{\"type\":\"explode\"}]}]}");
		assertEquals(2, t.tiers().size());
	}

	@Test
	void rejectsBadIds() {
		rejectsOutcome("{\"type\":\"give_item\",\"item\":\"Stick\"}", "tiers[0].outcomes[0].item", "'Stick'");
		rejectsOutcome("{\"type\":\"give_item\",\"item\":\"stick\"}", "'stick'");
		rejectsOutcome("{\"type\":\"spawn_mob\",\"entity\":\"minecraft:Bee\"}", "outcomes[0].entity", "'minecraft:Bee'");
		rejectsOutcome("{\"type\":\"effect\",\"effect\":\"bad id\",\"duration_ticks\":5}", "outcomes[0].effect", "'bad id'");
		rejectsOutcome("{\"type\":\"drop_block\",\"block\":\"anvil\",\"height\":5}", "outcomes[0].block", "'anvil'");
		rejectsOutcome("{\"type\":\"give_item\",\"item\":\"minecraft:stick\",\"enchantments\":{\"Sharp\":1}}",
				"outcomes[0].enchantments.Sharp", "'Sharp'");
		rejectsOutcome("{\"type\":\"give_item\"}", "outcomes[0].item", "missing");
	}

	@Test
	void rejectsRanges() {
		String s = "\"type\":\"give_item\",\"item\":\"minecraft:stick\"";
		rejectsOutcome("{" + s + ",\"count\":0}", "outcomes[0].count", "0");
		rejectsOutcome("{" + s + ",\"count\":65}", "outcomes[0].count", "65");
		rejectsOutcome("{" + s + ",\"count\":5,\"max_count\":4}", "outcomes[0].max_count", "4");
		rejectsOutcome("{" + s + ",\"max_count\":65}", "outcomes[0].max_count", "65");
		rejectsOutcome("{" + s + ",\"enchantments\":{\"minecraft:sharpness\":0}}", "enchantments.minecraft:sharpness", "0");
		rejectsOutcome("{" + s + ",\"enchantments\":{\"minecraft:sharpness\":256}}", "enchantments.minecraft:sharpness", "256");
		rejectsOutcome("{" + s + ",\"random_enchants\":{\"min\":-1,\"max\":2}}", "random_enchants.min", "-1");
		rejectsOutcome("{" + s + ",\"random_enchants\":{\"min\":3,\"max\":2}}", "random_enchants.max", "2");
		rejectsOutcome("{" + s + ",\"random_enchants\":{\"min\":1,\"max\":11}}", "random_enchants.max", "11");
		rejectsOutcome("{" + s + ",\"random_enchants\":{\"min\":1,\"max\":2,\"over_max\":11}}", "random_enchants.over_max", "11");
		rejectsOutcome("{" + s + ",\"random_enchants\":{\"min\":1}}", "random_enchants.max", "missing");
		rejectsOutcome("{\"type\":\"nested_gift\",\"count\":0}", "outcomes[0].count", "0");
		rejectsOutcome("{\"type\":\"nested_gift\",\"count\":17}", "outcomes[0].count", "17");
		rejectsOutcome("{\"type\":\"explode\",\"fuse_ticks\":201}", "outcomes[0].fuse_ticks", "201");
		rejectsOutcome("{\"type\":\"explode\",\"fuse_ticks\":-1}", "outcomes[0].fuse_ticks", "-1");
		rejectsOutcome("{\"type\":\"effect\",\"effect\":\"minecraft:speed\",\"duration_ticks\":0}", "duration_ticks", "0");
		rejectsOutcome("{\"type\":\"effect\",\"effect\":\"minecraft:speed\",\"duration_ticks\":72001}", "duration_ticks", "72001");
		rejectsOutcome("{\"type\":\"effect\",\"effect\":\"minecraft:speed\",\"duration_ticks\":5,\"amplifier\":256}", "amplifier", "256");
		rejectsOutcome("{\"type\":\"effect\",\"effect\":\"minecraft:speed\"}", "duration_ticks", "missing");
		rejectsOutcome("{\"type\":\"drop_block\",\"block\":\"minecraft:anvil\",\"height\":0}", "height", "0");
		rejectsOutcome("{\"type\":\"drop_block\",\"block\":\"minecraft:anvil\",\"height\":65}", "height", "65");
		rejectsOutcome("{\"type\":\"spawn_mob\",\"entity\":\"minecraft:bee\",\"count\":65}", "count", "65");
	}

	@Test
	void rejectsBadCurse() {
		rejectsOutcome("{\"type\":\"cursed\",\"curse\":\"doom\",\"item\":{\"item\":\"minecraft:stick\"}}",
				"outcomes[0].curse", "'doom'");
		rejectsOutcome("{\"type\":\"cursed\",\"item\":{\"item\":\"minecraft:stick\"}}", "outcomes[0].curse");
		rejectsOutcome("{\"type\":\"cursed\",\"curse\":\"any\"}", "outcomes[0].item");
		rejectsOutcome("{\"type\":\"cursed\",\"curse\":\"any\",\"item\":{\"item\":\"nope\"}}", "outcomes[0].item.item", "'nope'");
	}

	@Test
	void rejectsWrongTypes() {
		String s = "\"type\":\"give_item\",\"item\":\"minecraft:stick\"";
		rejectsOutcome("{" + s + ",\"count\":\"4\"}", "outcomes[0].count", "expected an integer", "\"4\"");
		rejectsOutcome("{" + s + ",\"count\":1.5}", "outcomes[0].count", "1.5");
		rejectsOutcome("{" + s + ",\"count\":true}", "outcomes[0].count", "true");
		rejectsOutcome("{" + s + ",\"count\":null}", "outcomes[0].count", "null");
		rejectsOutcome("{" + s + ",\"count\":[1]}", "outcomes[0].count", "array");
		rejectsOutcome("{" + s + ",\"count\":99999999999}", "outcomes[0].count", "99999999999");
		rejectsOutcome("{\"type\":\"give_item\",\"item\":5}", "outcomes[0].item", "expected a string");
		rejectsOutcome("{" + s + ",\"name\":7}", "outcomes[0].name", "expected a string");
		rejectsOutcome("{" + s + ",\"lore\":\"x\"}", "outcomes[0].lore", "expected an array");
		rejectsOutcome("{" + s + ",\"lore\":[1]}", "outcomes[0].lore[0]", "1");
		rejectsOutcome("{" + s + ",\"enchantments\":[]}", "outcomes[0].enchantments", "expected an object");
		rejectsOutcome("{" + s + ",\"enchantments\":{\"minecraft:sharpness\":\"5\"}}", "enchantments.minecraft:sharpness");
		rejectsOutcome("{" + s + ",\"random_enchants\":3}", "outcomes[0].random_enchants", "expected an object");
		rejectsOutcome("{\"type\":\"spawn_mob\",\"entity\":\"minecraft:bee\",\"angry\":\"yes\"}", "outcomes[0].angry", "\"yes\"");
		rejectsOutcome("{\"type\":5}", "outcomes[0].type", "expected a string");
		rejectsOutcome("5", "tiers[0].outcomes[0]", "expected an object");
		rejects("{\"version\":\"1\",\"tiers\":[]}", "version", "\"1\"");
		rejects("{\"version\":1,\"tiers\":{}}", "tiers", "expected an array");
		rejects("{\"version\":1,\"tiers\":[3]}", "tiers[0]", "expected an object");
		rejects("{\"version\":1,\"tiers\":[{\"id\":1,\"weight\":1,\"outcomes\":[]}]}", "tiers[0].id", "expected a string");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"x\",\"weight\":\"1\",\"outcomes\":[]}]}", "tiers[0].weight");
		rejects("{\"version\":1,\"tiers\":[{\"id\":\"x\",\"weight\":1,\"outcomes\":{}}]}", "tiers[0].outcomes", "expected an array");
	}

	@Test
	void neverThrowsRawExceptions() {
		for (String bad : new String[] {"null", "42", "\"s\"", "{\"version\":null}", "{\"version\":1,\"tiers\":null}",
				"{\"version\":1,\"tiers\":[null]}", "{\"version\":1,\"tiers\":[{\"id\":null}]}"}) {
			assertThrows(LootConfigException.class, () -> parse(bad), bad);
		}
	}
}
