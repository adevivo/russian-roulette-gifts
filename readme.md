# Russian Roulette Gifts

A **server-side-only** Fabric mod for Minecraft **26.2**. Craft a Mystery Gift, place it, right-click it, and something random comes out. It might be terrible (primed TNT, a creeper) or amazing (an enchanted sword). Vanilla clients can join a server running it, and it is optional on clients.

> **Status: v0.2.0, playable.** Craft a gift, place it and right-click it. Tested in single-player. The config file and `/rrgifts give|reload` work; a vanilla-client check on a dedicated server is still to do.

## Download

Built jars are on the [Releases page](https://github.com/adevivo/russian-roulette-gifts/releases). To install by hand on a Minecraft **26.2** Fabric server:

1. [Fabric Loader](https://fabricmc.net/use/) 0.19.5+ for 26.2
2. [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2 (required, not bundled)
3. Put `russianroulettegifts-<version>+mc26.2.jar` in the server's `mods/` folder

Clients do not need it.

## Origin

A subscriber suggested a craftable gift system. The "Russian roulette" twist is that the mod picks the contents at random rather than the player choosing them.

## The gift

One shaped recipe produces a `minecraft:player_head` with a fixed profile (UUID and name `MysteryGift`) plus a texture. The profile is the marker that identifies a gift.

```
P S P     P = paper
D G D     S = string
P S P     D = any dye (#c:dyes)   G = gold ingot (the stake)
```

The gold ingot is deliberate. Without a cost, gifts with diamond jackpots would be a free diamond farm.

The skin is an existing gift-box texture on Mojang's host (`textures.minecraft.net/texture/35787ce1…4e83`): a red box with a green ribbon. The recipe stores it as an unsigned base64 `textures` property.

## Design

- **Opening:** right-clicking a placed gift head removes the block, rolls a tier and applies the outcome. Opening is blocked in adventure mode and inside spawn protection.
- **Tiers** (weights are defaults, overridable in config):

  | Tier | Weight | Examples |
  |---|---|---|
  | Disaster | 20 | primed TNT, creeper, angry bees, bad effects, falling anvil |
  | Cursed | 20 | good enchanted item with Curse of Vanishing or Binding |
  | Junk | 10 | gag gifts ("Lump of Coal", "Premium Dirt", "IOU"...), each with 2–4 gold nuggets back |
  | Good | 35 | iron/gold, food, gear with random valid enchantments |
  | Jackpot | 15 | diamonds, HandyDandy custom items, high-level gear, another gift |

- **Identification:** the profile UUID. Vanilla skull block entities should keep the profile through place and break. This is the open question the spike must confirm.
- **Code layout** (`com.neovetta.rrgifts`): `core/` is pure Java with no Minecraft imports (rolling, loot config, enchant picking, all unit-tested). `game/` is a thin Minecraft layer. This keeps a later 1.21.11 port cheap.

## Spike results (2026-10-15, CurseForge client, MC 26.2, Fabric API 0.161.0)

| Check | Result |
|---|---|
| Recipe loads | ✅ No parse error in the log. The recipe unlocks and crafts in survival, including with mixed dyes (red + blue). |
| Recipe book | ✅ (after a fix) At first the recipe was missing from the book until the first craft. The advancement `advancement/recipes/misc/mystery_gift.json` now unlocks it when the player holds gold ingots or paper. Confirmed in a new world: `has_paper` fired at the first paper pickup. |
| Right-click opens it | ⏳ Not implemented yet (Session 3). Right-click does nothing; breaking it or blowing it up just drops the head. |
| Crafted item | ✅ Named "Mystery Gift", with the lore "Open at your own risk...". |
| Placed block keeps profile | ✅ `/data get block` shows `profile: {name: "MysteryGift", id: [I; 1534995482, -1657516178, -1977499393, -301266175]}` and `custom_name`. |
| Broken by hand: drop keeps profile | ✅ The dropped `player_head` has the same `minecraft:profile` id and name, plus `custom_name`. **The identification design holds.** |
| Drop keeps lore | ❌ **Lore is lost on place.** The skull block entity stores only `custom_name` and `profile` (`components: {}`), so a re-picked gift has no lore. This is cosmetic, because identification uses the profile. |
| Pickaxe / TNT drops | ⏳ Tested, but the drops were not checked with `/data`. |
| Texture | As expected, the placeholder logs `Could not decode textures payload: Illegal base64 character 5f` (harmless) and renders a default skin, based on the UUID. A real skin is still needed. |

The `SavedData` and mixin fallback is not needed.

## Build and test

Loom 1.17 needs a Java 21+ JVM. Set Java 25:

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 25)"
./gradlew build test
# jar: build/libs/russianroulettegifts-0.1.0+mc26.2.jar
```

| Component | Version |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.5 |
| Fabric API | 0.160.0+26.2 |
| Loom | 1.17-SNAPSHOT |
| Java | 25 |

Mod ID `russianroulettegifts`. Mojmap names only (no Yarn). Check APIs with `javap` against the 26.2 jar.

## Commands and config

- `/rrgifts give [count] [targets]`: give Mystery Gifts (op level 2).
- `/rrgifts reload`: re-read the loot config.
- `config/russian-roulette-gifts.json` is written from the bundled default on first start. Every id is checked against the game registries. On any error the mod logs the JSON path and uses the default loot instead.
- `config/russian-roulette-gifts.default.json` is the mod's reference copy of the current default. It is rewritten on each start. When the mod is updated:
  - an **unedited** config is replaced with the new default
  - an **edited** config is kept, and the log says when it is based on an older default, so you can compare the two files

## Release numbering and releasing

Versions are `<mod version>+mc<minecraft version>`, for example `0.1.0+mc26.2`. This is the same scheme as ai-companion and Fabric API. `build.gradle` sets `version = "${mod_version}+mc${minecraft_version}"`, and `processResources` expands it into `fabric.mod.json`.

Jars go out as **GitHub Release assets**. They are not committed to the repo. If you want a local staging directory, use `dist/` (gitignored). The tag is the version with a leading `v`, for example `v0.1.0+mc26.2`.

```bash
# 1. build, and confirm the jar name ends in +mc26.2
export JAVA_HOME="$(/usr/libexec/java_home -v 25)"
./gradlew clean build test && ls build/libs/

# 2. tag the exact commit that produced it, and push both
git tag -a "v0.1.0+mc26.2" -m "0.1.0 for Minecraft 26.2"
git push origin master && git push origin "v0.1.0+mc26.2"

# 3. create the release and attach the jar (drop --prerelease once it is tested in-game)
gh release create "v0.1.0+mc26.2" \
   "build/libs/russianroulettegifts-0.1.0+mc26.2.jar" \
   --title "0.1.0 — Minecraft 26.2" --prerelease \
   --notes "..."
```

Upload only the main jar, not `-sources.jar`. CurseForge is a parallel channel. Upload the same jar there, and mark Fabric API as a required dependency.

## Out of scope for v1

Per-color gift skins, "From: <player>" signing, a 1.21.11 build, a Paper plugin.

## License

MIT
