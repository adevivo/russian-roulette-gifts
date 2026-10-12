# Handoff — Russian Roulette Gifts

Last updated: 2026-10-12. Session 1 (scaffold + spike) is built; the in-game spike is **not yet run**.

## Done

- Gradle/Loom scaffold copied from player-revive, renamed. MC 26.2, Loader 0.19.5, Fabric API 0.160.0+26.2, Java 25, JUnit 5.11.3. No client source set or client entrypoint.
- `RussianRouletteGifts` (empty `ModInitializer`), `fabric.mod.json` (main entrypoint only), `core/GiftConstants` (gift profile UUID `5b7e2c1a-9d34-4f6e-8a21-c0ffee0b0b01`, name `MysteryGift`).
- Recipe `data/russianroulettegifts/recipe/mystery_gift.json`: `PSP/DGD/PSP`, paper, string, `#c:dyes`, gold ingot, giving a `player_head` with `minecraft:profile`, custom name and lore.
- Tests: `SmokeTest`, `GiftRecipeTest` (the profile id matches the constant; ingredients are plain strings). `./gradlew build test` is green.
- Repo is public at https://github.com/adevivo/russian-roulette-gifts (`master`, one commit).

## Known gaps / risks

- **The `textures` value is a placeholder, not valid base64.** A real gift-box skin is needed (a base64 textures JSON pointing at a skin).
- **`minecraft:profile` JSON format is unverified in-game.** The id is an int array. The unit test only checks JSON shape; Minecraft's codec has not loaded it. Fallbacks to try if the log shows a parse error: id as a UUID string, or drop `properties` to isolate it.
- **Profile survival through place → break is unconfirmed.** The whole identification design depends on it.
- `#c:dyes` comes from Fabric API's convention tags. `#minecraft:dyes` is a vanilla alternative.

## Testing to do (`export JAVA_HOME="$(/usr/libexec/java_home -v 25)"; ./gradlew runClient`)

**Critical: check the startup log for a recipe parse error first. Use a survival world.**

1. Craft with `paper x4, string x2, dye x2, gold_ingot x1` in the `PSP/DGD/PSP` layout. Confirm the name "Mystery Gift" and the lore.
2. Repeat with 2–3 dye colors, including mixed colors in the two dye slots. All should work.
3. Place it. Run `/data get block <x> <y> <z>` and check `profile` (name and id).
4. Break it (pickaxe and by hand). Check the dropped item (F3+H or `/data get entity @s Inventory`) has id `[I; 1534995482, -1657516178, -1977499393, -301266175]` and name `MysteryGift`.
5. Optional: blow up a placed gift with TNT or a creeper and check the drop.
6. Note whether the placed head and the item render a texture.

Record the findings in the readme's spike section.

**If the profile does not survive:** fall back to a `SavedData` set of placed gift positions plus a small mixin.

## Next steps (from the approved plan, `~/.claude/plans/read-the-readme-and-fluffy-dongarra.md`)

2. **Core + tests** (pure Java, no MC imports): `WeightedRoller` (seedable), outcome model (`GiveItem`, `Explode`, `SpawnMob`, `Effect`, `Cursed`, `NestedGift`), tiers/loot table, Gson parser and validator with clear errors, `EnchantPicker`, bundled `loot.json`. Tests: a ~100k-roll run within ±1% of the weights, weight 0 never picked, bad configs rejected, enchant rules (incompatibles, level bounds, Binding only on wearables), bundled `loot.json` validates.
3. **Open handler + outcomes** (`game/`): `GiftItems`, `GiftOpenHandler` (`UseBlockCallback`, server side, main hand, check the `SkullBlockEntity` profile id), `OutcomeApplier`, HandyDandy items as Jackpot outcomes, sounds, particles and action-bar messages.
4. **Polish:** tune weights, `/rrgifts give|reload` (op level 2), `ConfigLoader` (invalid config logs an error and falls back to the default), CurseForge description ("server-side only").

Out of scope for v1: per-color skins, "From: <player>" signing, a 1.21.11 build, a Paper plugin.

## Notes

- Do not use the workspace-root `CLAUDE.md` Yarn→Mojmap tables (26.1.2 only). Use `javap` against `~/.gradle/caches/fabric-loom/26.2/minecraft-common.jar`.
- Player-revive's `CLAUDE.md` has confirmed 26.2 API notes (`ResolvableProfile`, `DataComponents.PROFILE`, `SavedDataStorage`).
- Do not deploy to or restart the family server; hand over the jar.
