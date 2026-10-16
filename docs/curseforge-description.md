# Russian Roulette Gifts: CurseForge project description

> Paste-ready copy for the CurseForge project page. The **Summary** goes in the short "Summary"
> field. Everything below the second divider is the long description (CurseForge accepts Markdown).
> Written for 0.2.1 on 2026-10-15.

---

## Summary (short field)

CurseForge caps this field at 256 characters and shows it as plain text. The line below is 179 characters.

Craft a Mystery Gift, place it, right-click it. It might be diamonds. It might be a creeper. Server-side only: vanilla players can join and open gifts without installing anything.

---

# Russian Roulette Gifts

**Every gift is a gamble.** Craft a Mystery Gift, place it down, and right-click to open it. Inside could be a stack of diamonds, a legendary sword, or a creeper with your name on it. You never know until you open it.

A subscriber suggested a craftable gift system. We added the twist: the mod picks what's inside, not the player.

## The gift

```
P S P     P = paper
D G D     S = string
P S P     D = any dye     G = gold ingot
```

The recipe shows up in your recipe book as soon as you pick up paper or gold. The gold ingot is the stake: gifts cost something, so you can't farm free diamonds out of them.

A placed gift looks like a wrapped present. Break it and you get it back, still unopened.

## What's inside

| Tier | Odds | What you might get |
|---|---|---|
| 💥 Disaster | 20% | Primed TNT, a creeper, a swarm of angry bees, an anvil from the sky, levitation, blindness |
| 💀 Cursed | 20% | Beautiful enchanted gear with Curse of Vanishing or Curse of Binding hidden in it |
| 🧦 Junk | 10% | A gag gift, like a Lump of Coal ("You've been naughty"), Premium Dirt or an IOU, plus a few gold nuggets back |
| ✅ Good | 35% | Iron, gold, food, and tools, weapons and armour with random enchantments |
| 💎 Jackpot | 15% | Diamonds, an enchanted golden apple, over-enchanted gear, more gifts, or a classic item from our family server |

The classic items are Swim Fins, Jump Boots, the Copper Pickaxe, the Ginsu Iron Sword and the Ouchy Diamond Sword (Sharpness X).

Opening a gift shows a big title with the result, along with sounds and particles for each tier.

## Server-side only

You don't need to install anything on the client. Put the mod on a Fabric server, and players with a completely **vanilla** client can craft, place and open gifts. It also works in single-player.

There are no new blocks or items. The gift is a vanilla player head with its own look, so worlds stay clean if you ever remove the mod. Gifts that were already crafted just become decorative heads.

## Commands (op)

- `/rrgifts give [count] [players]`: hand out gifts, for events or testing.
- `/rrgifts reload`: reload the loot config without restarting.

## Configure the loot

Everything is in `config/russian-roulette-gifts.json`: tier odds, items, counts, enchantments, mobs, effects and messages.

- **Mistakes don't break your server.** A typo or an item that doesn't exist is reported in the log with its exact location, and the mod falls back to the default loot.
- **Updates are safe.** If you never edited the config, it is upgraded automatically when the mod updates. If you did edit it, your changes are kept, and the log tells you when there's new default loot to compare against.

Gifts can't be opened in adventure mode or inside spawn protection.

## Requirements

- Minecraft **26.2**
- Fabric Loader **0.19.5** or newer
- **Fabric API** (required)
- Java 25

## Source and license

MIT licensed. The source, issues and downloads are on [GitHub](https://github.com/adevivo/russian-roulette-gifts).
