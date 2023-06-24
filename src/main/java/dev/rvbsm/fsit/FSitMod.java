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
import dev.rvbsm.fsit.packet.CrawlC2SPacket;
import dev.rvbsm.fsit.packet.PongC2SPacket;
import dev.rvbsm.fsit.packet.RidePlayerPacket;
import dev.rvbsm.fsit.packet.SpawnSeatC2SPacket;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class FSitMod implements ModInitializer, DedicatedServerModInitializer {

	public static final ConfigData config = new ConfigData();
	private static final Set<UUID> moddedPlayers = new HashSet<>();
	private static final Map<UUID, PlayerPose> playersPose = new HashMap<>();

	@Contract(value = "!null, !null, _ -> new", pure = true)
	public static @NotNull Text getTranslation(String type, String id, Object... args) {
		final String translationKey = String.join(".", type, "fsit", id);
		return Text.translatable(translationKey, args);
	}

	public static void addModded(UUID playerId) {
		moddedPlayers.add(playerId);
	}

	public static void removeModded(UUID playerId) {
		moddedPlayers.remove(playerId);
	}

	public static boolean isModded(UUID playerId) {
		return moddedPlayers.contains(playerId);
	}

	public static PlayerPose getPose(UUID playerId) {
		return playersPose.getOrDefault(playerId, PlayerPose.NONE);
	}

	public static boolean isInPose(UUID playerId, PlayerPose pose) {
		return FSitMod.getPose(playerId) == pose;
	}

	public static void resetPose(UUID playerId) {
		playersPose.put(playerId, PlayerPose.NONE);
	}

	public static void setSneaked(PlayerEntity player) {
		if (!FSitMod.isInPose(player.getUuid(), PlayerPose.NONE)) return;
		else if (player.isSpectator() || !player.isOnGround()) return;
		playersPose.put(player.getUuid(), PlayerPose.SNEAK);

		final Executor delayedExecutor = CompletableFuture.delayedExecutor(FSitMod.config.sneakDelay, TimeUnit.MILLISECONDS);
		CompletableFuture.runAsync(() -> {
			if (FSitMod.isInPose(player.getUuid(), PlayerPose.SNEAK)) FSitMod.resetPose(player.getUuid());
		}, delayedExecutor);
	}

	public static void setSitting(PlayerEntity player, Vec3d pos) {
		if (player.isSpectator() || !player.isOnGround()) return;
		playersPose.put(player.getUuid(), PlayerPose.SIT);

		final World world = player.getWorld();
		final SeatEntity seat = new SeatEntity(world, pos);
		world.spawnEntity(seat);
		player.startRiding(seat, true);
	}

	public static void setCrawling(PlayerEntity player) {
		if (player.isSpectator() || !player.isOnGround() || player.hasVehicle()) return;
		playersPose.put(player.getUuid(), PlayerPose.CRAWL);

		if (!FSitMod.isModded(player.getUuid())) player.sendMessage(Text.of("Press Sneak key to stop crawling"), true);
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

		ServerPlayNetworking.registerGlobalReceiver(PongC2SPacket.TYPE, (packet, player, responseSender) -> FSitMod.addModded(player.getUuid()));
		ServerPlayNetworking.registerGlobalReceiver(SpawnSeatC2SPacket.TYPE, (packet, player, responseSender) -> {
			if (InteractBlockCallback.isInRadius(packet.playerPos(), packet.sitPos()))
				FSitMod.setSitting(player, packet.sitPos());
		});
		ServerPlayNetworking.registerGlobalReceiver(CrawlC2SPacket.TYPE, (packet, player, responseSender) -> {
			if (packet.crawling()) FSitMod.setCrawling(player);
			else FSitMod.resetPose(player.getUuid());
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
