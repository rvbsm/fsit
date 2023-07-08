package dev.rvbsm.fsit;

import dev.rvbsm.fsit.command.FSitCommand;
import dev.rvbsm.fsit.command.PoseCommand;
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

	protected static final ConfigData config = new ConfigData();
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

	private static void setPose(ServerPlayerEntity player, PlayerPose pose) {
		FSitMod.playersPose.put(player.getUuid(), pose);
		if (FSitMod.isModded(player.getUuid())) ServerPlayNetworking.send(player, new PoseSyncS2CPacket(pose));

		if (FSitMod.isPosing(player.getUuid()) && !FSitMod.isModded(player.getUuid()))
			player.sendMessage(Text.of("Press Sneak key to get up"), true);
	}

	public static PlayerPose getPose(UUID playerId) {
		return playersPose.getOrDefault(playerId, PlayerPose.NONE);
	}

	public static void resetPose(ServerPlayerEntity player) {
		FSitMod.setPose(player, PlayerPose.NONE);
	}

	public static boolean isInPose(UUID playerId, PlayerPose pose) {
		return FSitMod.getPose(playerId) == pose;
	}

	public static boolean isPosing(UUID playerId) {
		return !FSitMod.isInPose(playerId, PlayerPose.NONE) && !FSitMod.isInPose(playerId, PlayerPose.SNEAK);
	}

	public static void setSneaked(ServerPlayerEntity player) {
		if (!FSitMod.isInPose(player.getUuid(), PlayerPose.NONE)) return;
		else if (player.isSpectator() || !player.isOnGround()) return;
		FSitMod.setPose(player, PlayerPose.SNEAK);

		final ConfigData config = FSitMod.getConfig(player.getUuid());
		final Executor delayedExecutor = CompletableFuture.delayedExecutor(config.sneakDelay, TimeUnit.MILLISECONDS);
		CompletableFuture.runAsync(() -> {
			if (FSitMod.isInPose(player.getUuid(), PlayerPose.SNEAK)) FSitMod.resetPose(player);
		}, delayedExecutor);
	}

	public static void setSitting(ServerPlayerEntity player) {
		FSitMod.setSitting(player, player.getPos());
	}

	public static void setSitting(ServerPlayerEntity player, Vec3d pos) {
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

	public static void setCrawling(ServerPlayerEntity player) {
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

		ServerPlayNetworking.registerGlobalReceiver(ConfigSyncC2SPacket.TYPE, ConfigSyncC2SPacket::receive);
		ServerPlayNetworking.registerGlobalReceiver(SpawnSeatC2SPacket.TYPE, SpawnSeatC2SPacket::receive);
		ServerPlayNetworking.registerGlobalReceiver(RidePlayerPacket.TYPE, RidePlayerPacket::receive);
	}

	@Override
	public void onInitializeServer() {
		CommandRegistrationCallback.EVENT.register(new FSitCommand()::register);
		CommandRegistrationCallback.EVENT.register(new PoseCommand("sit", FSitMod::setSitting)::register);
		CommandRegistrationCallback.EVENT.register(new PoseCommand("crawl", FSitMod::setCrawling)::register);
	}
}
