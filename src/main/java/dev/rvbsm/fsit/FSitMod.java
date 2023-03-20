package dev.rvbsm.fsit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.config.FSitConfigManager;
import dev.rvbsm.fsit.entity.SeatEntity;
import dev.rvbsm.fsit.event.InteractBlockCallback;
import dev.rvbsm.fsit.event.InteractPlayerCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FSitMod implements ModInitializer {

	private static final String MOD_ID = "fsit";
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static final Map<UUID, ScheduledFuture<Boolean>> scheduledTasks = new LinkedHashMap<>();
	private static final Set<UUID> sneakedPlayers = new LinkedHashSet<>();

	@Contract(pure = true)
	public static @NotNull String getTranslationKey(String type, String id) {
		return String.join(".", type, FSitMod.MOD_ID, id);
	}

	public static String getModId() {
		return FSitMod.MOD_ID;
	}

	public static boolean isNeedSeat(@NotNull PlayerEntity player) {
		return sneakedPlayers.contains(player.getUuid()) && player.getPitch(1f) >= FSitConfig.minAngle.getValue();
	}

	public static void addSneaked(@NotNull PlayerEntity player) {
		final UUID playerUid = player.getUuid();
		if (!FSitMod.sneakedPlayers.contains(playerUid) && player.getPitch(1f) >= FSitConfig.minAngle.getValue()) {
			FSitMod.sneakedPlayers.add(playerUid);
			FSitMod.scheduledTasks.put(playerUid, scheduler.schedule(() -> FSitMod.sneakedPlayers.remove(playerUid), FSitConfig.shiftDelay.getValue(), TimeUnit.MILLISECONDS));
		}
	}

	private static void clearSneaked(@NotNull PlayerEntity player) {
		final UUID playerUid = player.getUuid();
		final ScheduledFuture<Boolean> task = FSitMod.scheduledTasks.get(playerUid);
		if (task != null) task.cancel(true);

		FSitMod.sneakedPlayers.remove(playerUid);
		FSitMod.scheduledTasks.remove(playerUid);
	}

	public static void spawnSeat(@NotNull PlayerEntity player, @NotNull World world, Vec3d pos) {
		final SeatEntity seatEntity = new SeatEntity(world, pos);

		world.spawnEntity(seatEntity);
		player.startRiding(seatEntity, true);
		FSitMod.clearSneaked(player);
	}

	@Override
	public void onInitialize() {
		FSitConfigManager.load();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> this.registerCommand(dispatcher));

		UseBlockCallback.EVENT.register(InteractBlockCallback::interactBlock);
		UseEntityCallback.EVENT.register(InteractPlayerCallback::interactPlayer);
	}

	@SuppressWarnings("unchecked")
	private void registerCommand(@NotNull CommandDispatcher dispatcher) {
		dispatcher.register(LiteralArgumentBuilder.literal("sit")
						.executes(ctx -> this.sitCommand((ServerCommandSource) ctx.getSource())));
	}

	private int sitCommand(@NotNull ServerCommandSource source) {
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
}
