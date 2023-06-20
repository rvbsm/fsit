package dev.rvbsm.fsit;

import dev.rvbsm.fsit.command.CrawlCommand;
import dev.rvbsm.fsit.command.FSitCommand;
import dev.rvbsm.fsit.command.SitCommand;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.FSitConfig;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class FSitMod implements ModInitializer, DedicatedServerModInitializer {

	public static final ConfigData config = new ConfigData();
	private static final Set<UUID> moddedPlayers = new HashSet<>();
	private static final Set<UUID> sneakedPlayers = new HashSet<>();
	private static final Set<UUID> crawledPlayers = new HashSet<>();

	@Contract("!null, !null, _ -> !null")
	public static @NotNull Text getTranslation(String type, String id, Object... args) {
		final String translationKey = String.join(".", type, "fsit", id);
		return Text.translatable(translationKey, args);
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

	public static void addCrawling(UUID playerUid) {
		crawledPlayers.add(playerUid);
	}

	public static void removeCrawling(UUID playerUid) {
		crawledPlayers.remove(playerUid);
	}

	public static boolean isCrawling(UUID playerUid) {
		return crawledPlayers.contains(playerUid);
	}

	public static void spawnSeat(PlayerEntity player, World world, Vec3d pos) {
		final SeatEntity seatEntity = new SeatEntity(world, pos);

		world.spawnEntity(seatEntity);
		player.startRiding(seatEntity, true);
		FSitMod.sneakedPlayers.remove(player.getUuid());
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
				FSitMod.spawnSeat(player, player.getWorld(), packet.sitPos());
		});
		ServerPlayNetworking.registerGlobalReceiver(CrawlC2SPacket.TYPE, (packet, player, responseSender) -> {
			if (packet.crawling()) FSitMod.addCrawling(player.getUuid());
			else FSitMod.removeCrawling(player.getUuid());
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
