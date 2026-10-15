# Handoff — Russian Roulette Gifts

Last updated: 2026-10-15. Session 1 (scaffold and spike) is done. The in-game spike **passed**: the gift profile survives place and break. Results are in the readme's "Spike results" section. `v0.1.0+mc26.2` is published as a GitHub pre-release.

## Done

- Gradle/Loom scaffold copied from player-revive, renamed. MC 26.2, Loader 0.19.5, Fabric API 0.160.0+26.2, Java 25, JUnit 5.11.3. No client source set or client entrypoint.
- `RussianRouletteGifts` (empty `ModInitializer`), `fabric.mod.json` (main entrypoint only), `core/GiftConstants` (gift profile UUID `5b7e2c1a-9d34-4f6e-8a21-c0ffee0b0b01`, name `MysteryGift`).
- Recipe `data/russianroulettegifts/recipe/mystery_gift.json`: `PSP/DGD/PSP`, paper, string, `#c:dyes`, gold ingot, giving a `player_head` with `minecraft:profile`, custom name and lore.
- Tests: `SmokeTest`, `GiftRecipeTest` (the profile id matches the constant; ingredients are plain strings). `./gradlew build test` is green.
- Repo is public at https://github.com/adevivo/russian-roulette-gifts (`master`, one commit).

## Known gaps / risks

- **The `textures` value is a placeholder, not valid base64.** A real gift-box skin is needed (a base64 textures JSON pointing at a skin).
- `minecraft:profile` JSON format (int-array id) **loads correctly in-game**, confirmed 2026-10-15.
- ~~Profile survival through place → break~~: **confirmed 2026-10-15** (break by hand). The pickaxe and TNT drops were not checked with `/data`.
- **Lore is lost on place → break** (the skull block entity keeps only `custom_name` and `profile`). This is cosmetic. If it matters, restore the lore on drop in `game/` when the profile id matches. Do not override the vanilla `player_head` loot table, because that changes every head.
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

## Session 2 (core): done 2026-10-15

`core/` holds:
- the `Outcome` sealed model
- `WeightedRoller` and `GiftRoller` (picks a tier, then an outcome)
- `LootConfigParser`, which rejects unknown keys and reports the JSON path of each error
- `EnchantPicker` and `CurseRule`
- `DefaultLoot`, which loads `resources/russianroulettegifts/default_loot.json`

There are 42 tests. They include a check that no file in core imports Minecraft.

**The game layer (Session 3) has to:**
- Validate every id in `default_loot.json` and in the config against the 26.2 registries. Core only checks the `ns:path` format.
- Build an `EnchantDef` list for each item from the enchantment registry.
- Map the `RolledGift` outcomes to world actions.

Jump Boots lost HandyDandy's `handydandy:jumping` enchant, because that enchant only exists when HandyDandy is installed.

**Gift skin:** `art/gift-skin.png` is a 64×64 skin with the gift box on the head layer and the bow on the hat layer. It has to be uploaded to Mojang's texture host, for example through mineskin.org. Then replace the placeholder `textures` value in `recipe/mystery_gift.json` with the returned base64 value, and add the `signature` if one is returned.

## Next steps (from the approved plan, `~/.claude/plans/read-the-readme-and-fluffy-dongarra.md`)

2. ~~**Core + tests**~~ done. (pure Java, no MC imports): `WeightedRoller` (seedable), outcome model (`GiveItem`, `Explode`, `SpawnMob`, `Effect`, `Cursed`, `NestedGift`), tiers/loot table, Gson parser and validator with clear errors, `EnchantPicker`, bundled `loot.json`. Tests: a ~100k-roll run within ±1% of the weights, weight 0 never picked, bad configs rejected, enchant rules (incompatibles, level bounds, Binding only on wearables), bundled `loot.json` validates.
3. **Open handler + outcomes** (`game/`): `GiftItems`, `GiftOpenHandler` (`UseBlockCallback`, server side, main hand, check the `SkullBlockEntity` profile id), `OutcomeApplier`, HandyDandy items as Jackpot outcomes, sounds, particles and action-bar messages.
4. **Polish:** tune weights, `/rrgifts give|reload` (op level 2), `ConfigLoader` (invalid config logs an error and falls back to the default), CurseForge description ("server-side only").

Out of scope for v1: per-color skins, "From: <player>" signing, a 1.21.11 build, a Paper plugin.

## Notes

- Do not use the workspace-root `CLAUDE.md` Yarn→Mojmap tables (26.1.2 only). Use `javap` against `~/.gradle/caches/fabric-loom/26.2/minecraft-common.jar`.
- Player-revive's `CLAUDE.md` has confirmed 26.2 API notes (`ResolvableProfile`, `DataComponents.PROFILE`, `SavedDataStorage`).
- Do not deploy to or restart the family server; hand over the jar.
