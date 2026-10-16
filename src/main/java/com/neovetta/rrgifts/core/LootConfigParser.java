package com.neovetta.rrgifts.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Parses and strictly validates the loot config JSON. Registry existence is not checked here. */
public final class LootConfigParser {
	private static final Pattern ID = Pattern.compile("^[a-z0-9_.-]+:[a-z0-9_./-]+$");
	private static final List<String> TYPES =
			List.of("give_item", "explode", "spawn_mob", "effect", "drop_block", "cursed", "nested_gift");

	private LootConfigParser() {}

	public static LootTable parse(Reader reader) {
		JsonElement root;
		try {
			root = JsonParser.parseReader(reader);
		} catch (Exception e) {
			throw new LootConfigException("$: malformed JSON: " + e.getMessage(), e);
		}
		JsonObject obj = asObject(root, "$");
		checkKeys(obj, "$", Set.of("version", "tiers"));
		int version = intField(obj, "version", "$", 1, 1, false, 0);
		if (version != 1) {
			throw new LootConfigException("version: unsupported version " + version + " (expected 1)");
		}
		JsonArray tiersArr = asArray(require(obj, "tiers", "$"), "tiers");
		if (tiersArr.isEmpty()) {
			throw new LootConfigException("tiers: must contain at least one tier");
		}
		List<TierDef> tiers = new ArrayList<>();
		Set<String> ids = new HashSet<>();
		long total = 0;
		for (int i = 0; i < tiersArr.size(); i++) {
			String p = "tiers[" + i + "]";
			TierDef t = parseTier(tiersArr.get(i), p);
			if (!ids.add(t.id())) {
				throw new LootConfigException(p + ".id: duplicate tier id '" + t.id() + "'");
			}
			total += t.weight();
			tiers.add(t);
		}
		if (total <= 0) {
			throw new LootConfigException("tiers: total tier weight must be greater than 0 (got " + total + ")");
		}
		return new LootTable(tiers);
	}

	private static TierDef parseTier(JsonElement el, String p) {
		JsonObject o = asObject(el, p);
		checkKeys(o, p, Set.of("id", "weight", "message", "outcomes", "bonus"));
		String id = stringField(o, "id", p, null);
		if (id.isEmpty()) {
			throw new LootConfigException(p + ".id: must not be empty (got '')");
		}
		int weight = intField(o, "weight", p, 0, Integer.MAX_VALUE, false, 0);
		String message = o.has("message") ? stringField(o, "message", p, null) : id;
		JsonArray arr = asArray(require(o, "outcomes", p), p + ".outcomes");
		if (arr.isEmpty()) {
			throw new LootConfigException(p + ".outcomes: tier needs at least one outcome");
		}
		List<WeightedOutcome> outs = new ArrayList<>();
		long total = 0;
		for (int i = 0; i < arr.size(); i++) {
			String op = p + ".outcomes[" + i + "]";
			JsonObject oo = asObject(arr.get(i), op);
			Outcome outcome = parseOutcome(oo, op);
			int w = oo.has("weight") ? intField(oo, "weight", op, 0, Integer.MAX_VALUE, true, 1) : 1;
			total += w;
			outs.add(new WeightedOutcome(outcome, w));
		}
		if (weight > 0 && total <= 0) {
			throw new LootConfigException(p + ".outcomes: total outcome weight must be greater than 0 (got " + total + ")");
		}
		List<Outcome> bonus = new ArrayList<>();
		if (o.has("bonus")) {
			JsonArray barr = asArray(o.get("bonus"), p + ".bonus");
			for (int i = 0; i < barr.size(); i++) {
				String bp = p + ".bonus[" + i + "]";
				JsonObject bo = asObject(barr.get(i), bp);
				if (bo.has("weight")) {
					throw new LootConfigException(bp + ".weight: bonus outcomes always apply and take no weight");
				}
				bonus.add(parseOutcome(bo, bp));
			}
		}
		return new TierDef(id, weight, message, outs, bonus);
	}

	private static Outcome parseOutcome(JsonObject o, String p) {
		String type = stringField(o, "type", p, null);
		return switch (type) {
			case "give_item" -> {
				checkKeys(o, p, Set.of("type", "weight", "item", "count", "max_count", "enchantments",
						"random_enchants", "name", "lore"));
				yield parseGive(o, p);
			}
			case "explode" -> {
				checkKeys(o, p, Set.of("type", "weight", "fuse_ticks"));
				yield new Outcome.Explode(intField(o, "fuse_ticks", p, 0, 200, true, 40));
			}
			case "spawn_mob" -> {
				checkKeys(o, p, Set.of("type", "weight", "entity", "count", "angry"));
				yield new Outcome.SpawnMob(idField(o, "entity", p), intField(o, "count", p, 1, 64, true, 1),
						boolField(o, "angry", p, false));
			}
			case "effect" -> {
				checkKeys(o, p, Set.of("type", "weight", "effect", "duration_ticks", "amplifier"));
				yield new Outcome.Effect(idField(o, "effect", p), intField(o, "duration_ticks", p, 1, 72000, false, 0),
						intField(o, "amplifier", p, 0, 255, true, 0));
			}
			case "drop_block" -> {
				checkKeys(o, p, Set.of("type", "weight", "block", "height"));
				yield new Outcome.DropBlock(idField(o, "block", p), intField(o, "height", p, 1, 64, false, 0));
			}
			case "cursed" -> {
				checkKeys(o, p, Set.of("type", "weight", "curse", "item"));
				String c = stringField(o, "curse", p, null);
				Outcome.Curse curse;
				switch (c) {
					case "vanishing" -> curse = Outcome.Curse.VANISHING;
					case "binding" -> curse = Outcome.Curse.BINDING;
					case "any" -> curse = Outcome.Curse.ANY;
					default -> throw new LootConfigException(p + ".curse: unknown curse '" + c
							+ "' (expected one of vanishing, binding, any)");
				}
				JsonObject item = asObject(require(o, "item", p), p + ".item");
				checkKeys(item, p + ".item", Set.of("item", "count", "max_count", "enchantments",
						"random_enchants", "name", "lore"));
				yield new Outcome.Cursed(parseGive(item, p + ".item"), curse);
			}
			case "nested_gift" -> {
				checkKeys(o, p, Set.of("type", "weight", "count"));
				yield new Outcome.NestedGift(intField(o, "count", p, 1, 16, true, 1));
			}
			default -> throw new LootConfigException(p + ".type: unknown outcome type '" + type
					+ "' (expected one of " + String.join(", ", TYPES) + ")");
		};
	}

	private static Outcome.GiveItem parseGive(JsonObject o, String p) {
		String item = idField(o, "item", p);
		int count = intField(o, "count", p, 1, 64, true, 1);
		int max = count;
		if (o.has("max_count")) {
			max = intField(o, "max_count", p, 1, 64, false, 0);
			if (max < count) {
				throw new LootConfigException(p + ".max_count: must be >= count (" + count + "), got " + max);
			}
		}
		Map<String, Integer> ench = new LinkedHashMap<>();
		if (o.has("enchantments")) {
			String ep = p + ".enchantments";
			JsonObject eo = asObject(o.get("enchantments"), ep);
			for (String k : eo.keySet()) {
				if (!ID.matcher(k).matches()) {
					throw new LootConfigException(ep + "." + k + ": invalid id '" + k + "'");
				}
				ench.put(k, asInt(eo.get(k), ep + "." + k, 1, 255));
			}
		}
		Outcome.RandomEnchants re = null;
		if (o.has("random_enchants")) {
			String rp = p + ".random_enchants";
			JsonObject ro = asObject(o.get("random_enchants"), rp);
			checkKeys(ro, rp, Set.of("min", "max", "over_max"));
			int min = intField(ro, "min", rp, 0, 10, false, 0);
			int mx = intField(ro, "max", rp, 0, 10, false, 0);
			if (mx < min) {
				throw new LootConfigException(rp + ".max: must be >= min (" + min + "), got " + mx);
			}
			re = new Outcome.RandomEnchants(min, mx, intField(ro, "over_max", rp, 0, 10, true, 0));
		}
		String name = o.has("name") ? stringField(o, "name", p, null) : null;
		List<String> lore = new ArrayList<>();
		if (o.has("lore")) {
			JsonArray la = asArray(o.get("lore"), p + ".lore");
			for (int i = 0; i < la.size(); i++) {
				JsonElement e = la.get(i);
				if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
					throw new LootConfigException(p + ".lore[" + i + "]: expected a string, got " + describe(e));
				}
				lore.add(e.getAsString());
			}
		}
		return new Outcome.GiveItem(item, count, max, ench, re, name, lore);
	}

	// ---- helpers ----

	private static String describe(JsonElement e) {
		if (e == null || e.isJsonNull()) return "null";
		if (e.isJsonArray()) return "an array";
		if (e.isJsonObject()) return "an object";
		return e.toString();
	}

	private static JsonObject asObject(JsonElement e, String p) {
		if (e == null || !e.isJsonObject()) {
			throw new LootConfigException(p + ": expected an object, got " + describe(e));
		}
		return e.getAsJsonObject();
	}

	private static JsonArray asArray(JsonElement e, String p) {
		if (e == null || !e.isJsonArray()) {
			throw new LootConfigException(p + ": expected an array, got " + describe(e));
		}
		return e.getAsJsonArray();
	}

	private static JsonElement require(JsonObject o, String key, String p) {
		JsonElement e = o.get(key);
		if (e == null) {
			String path = p.equals("$") ? key : p + "." + key;
			throw new LootConfigException(path + ": missing required field");
		}
		return e;
	}

	private static void checkKeys(JsonObject o, String p, Set<String> allowed) {
		for (String k : o.keySet()) {
			if (!allowed.contains(k)) {
				String path = p.equals("$") ? k : p + "." + k;
				throw new LootConfigException(path + ": unknown key '" + k + "' (allowed: "
						+ String.join(", ", new java.util.TreeSet<>(allowed)) + ")");
			}
		}
	}

	private static int asInt(JsonElement e, String path, int min, int max) {
		if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) {
			throw new LootConfigException(path + ": expected an integer, got " + describe(e));
		}
		JsonPrimitive prim = e.getAsJsonPrimitive();
		BigDecimal bd;
		try {
			bd = new BigDecimal(prim.getAsString());
		} catch (NumberFormatException ex) {
			throw new LootConfigException(path + ": expected an integer, got " + prim);
		}
		if (bd.stripTrailingZeros().scale() > 0) {
			throw new LootConfigException(path + ": expected an integer, got " + prim);
		}
		if (bd.compareTo(BigDecimal.valueOf(min)) < 0 || bd.compareTo(BigDecimal.valueOf(max)) > 0) {
			throw new LootConfigException(path + ": value " + bd.toPlainString() + " out of range " + min + ".." + max);
		}
		return bd.intValueExact();
	}

	private static int intField(JsonObject o, String key, String p, int min, int max, boolean optional, int def) {
		if (!o.has(key)) {
			if (optional) return def;
			require(o, key, p);
		}
		return asInt(o.get(key), (p.equals("$") ? "" : p + ".") + key, min, max);
	}

	private static String stringField(JsonObject o, String key, String p, String def) {
		JsonElement e = def == null ? require(o, key, p) : o.get(key);
		String path = p + "." + key;
		if (e == null) return def;
		if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
			throw new LootConfigException(path + ": expected a string, got " + describe(e));
		}
		return e.getAsString();
	}

	private static boolean boolField(JsonObject o, String key, String p, boolean def) {
		if (!o.has(key)) return def;
		JsonElement e = o.get(key);
		if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isBoolean()) {
			throw new LootConfigException(p + "." + key + ": expected true or false, got " + describe(e));
		}
		return e.getAsBoolean();
	}

	private static String idField(JsonObject o, String key, String p) {
		String v = stringField(o, key, p, null);
		if (!ID.matcher(v).matches()) {
			throw new LootConfigException(p + "." + key + ": invalid id '" + v
					+ "' (expected namespace:path, e.g. minecraft:stone)");
		}
		return v;
	}
}
