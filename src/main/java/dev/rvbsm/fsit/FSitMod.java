package dev.rvbsm.fsit;

import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.entity.SeatEntity;
import dev.rvbsm.fsit.event.InteractBlockCallback;
import dev.rvbsm.fsit.event.InteractPlayerCallback;
import dev.rvbsm.fsit.event.PlayerConnectionCallbacks;
import dev.rvbsm.fsit.packet.PongC2SPacket;
import dev.rvbsm.fsit.packet.RidePlayerC2SPacket;
import dev.rvbsm.fsit.packet.SpawnSeatC2SPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class FSitMod implements ModInitializer {

	private static final String MOD_ID = "fsit";
	private static final Set<UUID> moddedPlayers = new HashSet<>();
	private static final Set<UUID> sneakedPlayers = new HashSet<>();
	public static ConfigData config;

	@Contract("!null, !null -> _")
	public static @NotNull Text getTranslation(String type, String id) {
		final String translationKey = String.join(".", type, FSitMod.MOD_ID, id);
		return Text.translatable(translationKey);
	}

	public static String getModId() {
		return FSitMod.MOD_ID;
	}

	public static void addModded(UUID playerUid) {
		moddedPlayers.add(playerUid);
	}

	public static void removeModded(UUID playerUid) {
		moddedPlayers.remove(playerUid);
	}

	public static boolean isModded(UUID playerUid) {
		return moddedPlayers.contains(playerUid);
	}

	public static void addSneaked(UUID playerUid) {
		if (!FSitMod.sneakedPlayers.contains(playerUid)) {
			final Executor delayedExecutor = CompletableFuture.delayedExecutor(FSitMod.config.sneakDelay, TimeUnit.MILLISECONDS);

			FSitMod.sneakedPlayers.add(playerUid);
			CompletableFuture.runAsync(() -> FSitMod.sneakedPlayers.remove(playerUid), delayedExecutor);
		}
	}

	public static boolean isSneaked(UUID playerUid) {
		return sneakedPlayers.contains(playerUid);
	}

	public static void spawnSeat(PlayerEntity player, World world, Vec3d pos) {
		final SeatEntity seatEntity = new SeatEntity(world, pos);

		world.spawnEntity(seatEntity);
		player.startRiding(seatEntity, true);
		FSitMod.sneakedPlayers.remove(player.getUuid());
	}

	public static void loadConfig() {
		FSitMod.config = FSitConfig.load(ConfigData::new, ConfigData::defaultConfig);
	}

	@Override
	public void onInitialize() {
		FSitMod.loadConfig();

		UseBlockCallback.EVENT.register(InteractBlockCallback::interactBlock);
		UseEntityCallback.EVENT.register(InteractPlayerCallback::interactPlayer);
		ServerPlayConnectionEvents.JOIN.register(PlayerConnectionCallbacks::onConnect);
		ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionCallbacks::onDisconnect);

		ServerPlayNetworking.registerGlobalReceiver(PongC2SPacket.PONG_PACKET, PongC2SPacket::receive);
		ServerPlayNetworking.registerGlobalReceiver(SpawnSeatC2SPacket.SPAWN_SEAT_PACKET, SpawnSeatC2SPacket::receive);
		ServerPlayNetworking.registerGlobalReceiver(RidePlayerC2SPacket.RIDE_PLAYER_PACKET, RidePlayerC2SPacket::receiveRequest);
		ServerPlayNetworking.registerGlobalReceiver(RidePlayerC2SPacket.RIDE_ACCEPT_PACKET, RidePlayerC2SPacket::receiveAccept);
	}
}
