package dev.rvbsm.fsit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.entity.SeatEntity;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class FSitServerMod implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		CommandRegistrationCallback.EVENT.register(this::registerSitCommand);
		CommandRegistrationCallback.EVENT.register(this::registerReloadCommand);
	}

	private void registerSitCommand(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("sit").executes(this::sitCommand));
	}

	private void registerReloadCommand(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal(FSitMod.getModId())
						.requires(source -> source.hasPermissionLevel(2))
						.then(LiteralArgumentBuilder.<ServerCommandSource>literal("reload")
										.executes(this::reloadCommand)));
	}

	private int sitCommand(@NotNull CommandContext<ServerCommandSource> ctx) {
		final ServerCommandSource source = ctx.getSource();
		final PlayerEntity player;
		try {
			player = source.getPlayerOrThrow();
		} catch (CommandSyntaxException e) {
			source.sendError(Text.literal("Operation not permitted from console"));
			return -1;
		}

		final Entity vehicle = player.getVehicle();
		final World world = source.getWorld();
		final Vec3d playerPos = player.getPos();
		if (player.isOnGround() && vehicle == null && !player.isSpectator()) FSitMod.spawnSeat(player, world, playerPos);
		else if (vehicle instanceof SeatEntity) player.stopRiding();

		return Command.SINGLE_SUCCESS;
	}

	private int reloadCommand(@NotNull CommandContext<ServerCommandSource> ctx) {
		FSitConfig.load();
		ctx.getSource().sendMessage(Text.literal("Config reloaded"));
		System.out.println(FSitConfig.data.sitPlayers);
		return Command.SINGLE_SUCCESS;
	}
}
