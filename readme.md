# Russian Roulette Gifts

A **server-side-only** Fabric mod for Minecraft **26.2**. Craft a Mystery Gift, place it, right-click it, and something random comes out. It might be terrible (primed TNT, a creeper) or amazing (an enchanted sword). Vanilla clients can join a server running it, and it is optional on clients.

> **Status: v0.1.0, scaffold and spike.** Only the gift recipe exists so far. Opening a gift and the loot tiers are not implemented yet.

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

The recipe's `textures` value is a **placeholder**. A real base64 gift-box skin is still needed.

## Planned design

- **Opening:** right-clicking a placed gift head removes the block, rolls a tier and applies the outcome.
- **Tiers** (weights are defaults, overridable in config):

  | Tier | Weight | Examples |
  |---|---|---|
  | Disaster | 15 | primed TNT, creeper, angry bees, bad effects, falling anvil |
  | Cursed | 15 | good enchanted item with Curse of Vanishing or Binding |
  | Junk | 30 | dirt, rotten flesh, a stick |
  | Good | 30 | iron/gold, food, gear with random valid enchantments |
  | Jackpot | 10 | diamonds, HandyDandy custom items, high-level gear, another gift |

- **Identification:** the profile UUID. Vanilla skull block entities should keep the profile through place and break. This is the open question the spike must confirm.
- **Code layout** (`com.neovetta.rrgifts`): `core/` is pure Java with no Minecraft imports (rolling, loot config, enchant picking, all unit-tested). `game/` is a thin Minecraft layer. This keeps a later 1.21.11 port cheap.

## Spike checklist (not yet verified in-game)

Run `./gradlew runClient`, then:

1. Craft the gift with several dye colors.
2. Place it, check `/data get block` shows the profile.
3. Break it and confirm the dropped head keeps the same profile id.
4. Note whether the head renders a texture.

If the profile does not survive place then break, the fallback is a `SavedData` set of placed gift positions plus a small mixin.

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
