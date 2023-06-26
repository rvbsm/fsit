package dev.rvbsm.fsit;

import dev.rvbsm.fsit.command.CrawlCommand;
import dev.rvbsm.fsit.command.FSitCommand;
import dev.rvbsm.fsit.command.SitCommand;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.SeatEntity;
import dev.rvbsm.fsit.event.InteractBlockCallback;
import dev.rvbsm.fsit.event.InteractPlayerCallback;
import dev.rvbsm.fsit.event.PlayerConnectionCallbacks;
import dev.rvbsm.fsit.packet.ConfigSyncC2SPacket;
import dev.rvbsm.fsit.packet.PoseSyncS2CPacket;
import dev.rvbsm.fsit.packet.RidePlayerPacket;
import dev.rvbsm.fsit.packet.SpawnSeatC2SPacket;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class FSitMod implements ModInitializer, DedicatedServerModInitializer {

	public static final ConfigData config = new ConfigData();
	private static final Map<UUID, ConfigData> moddedPlayers = new HashMap<>();
	private static final Map<UUID, PlayerPose> playersPose = new HashMap<>();

	@Contract(value = "!null, !null, _ -> new", pure = true)
	public static @NotNull Text getTranslation(String type, String id, Object... args) {
		final String translationKey = String.join(".", type, "fsit", id);
		return Text.translatable(translationKey, args);
	}

	public static void setModded(UUID playerId, ConfigData config) {
		moddedPlayers.put(playerId, config);
	}

	public static void removeModded(UUID playerId) {
		moddedPlayers.remove(playerId);
	}

	public static ConfigData getConfig(UUID playerId) {
		return moddedPlayers.getOrDefault(playerId, config);
	}

	public static boolean isModded(UUID playerId) {
		return moddedPlayers.containsKey(playerId);
	}

	private static void setPose(PlayerEntity player, PlayerPose pose) {
		FSitMod.playersPose.put(player.getUuid(), pose);
		ServerPlayNetworking.send((ServerPlayerEntity) player, new PoseSyncS2CPacket(pose));

		if (pose != PlayerPose.NONE && pose != PlayerPose.SNEAK && !FSitMod.isModded(player.getUuid()))
			player.sendMessage(Text.of("Press Sneak key to get up"), true);
	}

	public static void resetPose(PlayerEntity player) {
		FSitMod.setPose(player, PlayerPose.NONE);
	}

	public static PlayerPose getPose(UUID playerId) {
		return playersPose.getOrDefault(playerId, PlayerPose.NONE);
	}

	public static boolean isInPose(UUID playerId, PlayerPose pose) {
		return FSitMod.getPose(playerId) == pose;
	}

	public static boolean isPosing(UUID playerId) {
		return !FSitMod.isInPose(playerId, PlayerPose.NONE) && !FSitMod.isInPose(playerId, PlayerPose.SNEAK);
	}

	public static void setSneaked(PlayerEntity player) {
		if (!FSitMod.isInPose(player.getUuid(), PlayerPose.NONE)) return;
		else if (player.isSpectator() || !player.isOnGround()) return;
		FSitMod.setPose(player, PlayerPose.SNEAK);

		final ConfigData config = FSitMod.getConfig(player.getUuid());
		final Executor delayedExecutor = CompletableFuture.delayedExecutor(config.sneakDelay, TimeUnit.MILLISECONDS);
		CompletableFuture.runAsync(() -> {
			if (FSitMod.isInPose(player.getUuid(), PlayerPose.SNEAK)) FSitMod.resetPose(player);
		}, delayedExecutor);
	}

	public static void setSitting(PlayerEntity player, Vec3d pos) {
		if (player.isSpectator() || !player.isOnGround() || player.hasVehicle()) return;

		final BlockPos blockPos = player.getBlockPos();
		final BlockState blockBelowState = player.getWorld().getBlockState(player.getPos().y % 1 == 0 ? blockPos.down() : blockPos);
		if (blockBelowState.isAir()) return;
		FSitMod.setPose(player, PlayerPose.SIT);

		final World world = player.getWorld();
		final SeatEntity seat = new SeatEntity(world, pos);
		world.spawnEntity(seat);
		player.startRiding(seat, true);
	}

	public static void setCrawling(PlayerEntity player) {
		if (player.isSpectator() || !player.isOnGround() || player.hasVehicle()) return;
		FSitMod.setPose(player, PlayerPose.CRAWL);
	}

	public static void loadConfig() {
		FSitConfig.load(FSitMod.config);
	}

	@Override
	public void onInitialize() {
		FSitMod.loadConfig();

		UseBlockCallback.EVENT.register(InteractBlockCallback::interactBlock);
		UseEntityCallback.EVENT.register(InteractPlayerCallback::interactPlayer);
		ServerPlayConnectionEvents.JOIN.register(PlayerConnectionCallbacks::onConnect);
		ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionCallbacks::onDisconnect);

		ServerPlayNetworking.registerGlobalReceiver(ConfigSyncC2SPacket.TYPE, (packet, player, responseSender) -> FSitMod.setModded(player.getUuid(), packet.config()));
		ServerPlayNetworking.registerGlobalReceiver(SpawnSeatC2SPacket.TYPE, (packet, player, responseSender) -> {
			if (InteractBlockCallback.isInRadius(packet.playerPos(), packet.sitPos()))
				FSitMod.setSitting(player, packet.sitPos());
		});
		ServerPlayNetworking.registerGlobalReceiver(RidePlayerPacket.TYPE, (packet, player, responseSender) -> {
			final PlayerEntity target = player.getWorld().getPlayerByUuid(packet.uuid());
			if (target == null) return;

			switch (packet.type()) {
				case REQUEST -> {
					if (FSitMod.isModded(packet.uuid()))
						ServerPlayNetworking.send((ServerPlayerEntity) target, new RidePlayerPacket(packet.type(), player.getUuid()));
				}
				case ACCEPT -> {
					if (player.distanceTo(target) <= 3) target.startRiding(player);
				}
				case REFUSE -> {
					if (player.hasPassenger(target)) target.stopRiding();
					else if (target.hasPassenger(player)) player.stopRiding();
				}
			}
		});
	}

	@Override
	public void onInitializeServer() {
		CommandRegistrationCallback.EVENT.register(new FSitCommand()::register);
		CommandRegistrationCallback.EVENT.register(new SitCommand()::register);
		CommandRegistrationCallback.EVENT.register(new CrawlCommand()::register);
	}
}
