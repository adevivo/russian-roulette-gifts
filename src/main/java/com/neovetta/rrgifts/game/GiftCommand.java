package com.neovetta.rrgifts.game;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;

public final class GiftCommand {
	private GiftCommand() {}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("rrgifts")
				.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
				.then(Commands.literal("give")
						.executes(ctx -> give(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException()), 1))
						.then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
								.executes(ctx -> give(ctx.getSource(),
										List.of(ctx.getSource().getPlayerOrException()),
										IntegerArgumentType.getInteger(ctx, "count")))
								.then(Commands.argument("targets", EntityArgument.players())
										.executes(ctx -> give(ctx.getSource(), targets(ctx),
												IntegerArgumentType.getInteger(ctx, "count"))))))
				.then(Commands.literal("reload").executes(ctx -> {
					String msg = ConfigLoader.reload(ctx.getSource().getServer());
					ctx.getSource().sendSuccess(() -> Component.literal(msg), true);
					return 1;
				})));
	}

	private static Collection<ServerPlayer> targets(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		return EntityArgument.getPlayers(ctx, "targets");
	}

	private static int give(CommandSourceStack src, Collection<ServerPlayer> targets, int count) {
		for (ServerPlayer p : targets) {
			ItemStack stack = GiftItems.create(count);
			p.getInventory().add(stack);
			if (!stack.isEmpty()) {
				p.drop(stack, false);
			}
		}
		src.sendSuccess(() -> Component.literal("Gave " + count + " Mystery Gift(s) to " + targets.size() + " player(s)"), true);
		return targets.size();
	}
}
