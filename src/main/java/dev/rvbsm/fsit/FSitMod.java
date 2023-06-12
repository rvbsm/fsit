package dev.rvbsm.fsit;

import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.entity.SeatEntity;
import dev.rvbsm.fsit.event.InteractBlockCallback;
import dev.rvbsm.fsit.event.InteractPlayerCallback;
import dev.rvbsm.fsit.event.PlayerConnectionCallbacks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class FSitMod implements ModInitializer {

	private static final String MOD_ID = "fsit";
	private static final Set<UUID> sneakedPlayers = new LinkedHashSet<>();

	@Contract(pure = true)
	public static @NotNull Text getTranslation(String type, String id) {
		final String translationKey = String.join(".", type, FSitMod.MOD_ID, id);
		return Text.translatable(translationKey);
	}

	public static String getModId() {
		return FSitMod.MOD_ID;
	}

	public static boolean isNeedSeat(@NotNull PlayerEntity player) {
		return sneakedPlayers.contains(player.getUuid()) && player.getPitch() >= FSitConfig.data.minAngle;
	}

	public static void addSneaked(@NotNull PlayerEntity player) {
		if (!FSitConfig.data.sneakSit) return;

		final UUID playerUid = player.getUuid();
		if (!FSitMod.sneakedPlayers.contains(playerUid) && player.getPitch() >= FSitConfig.data.minAngle) {
			final Executor delayedExecutor = CompletableFuture.delayedExecutor(FSitConfig.data.sneakDelay, TimeUnit.MILLISECONDS);

			FSitMod.sneakedPlayers.add(playerUid);
			CompletableFuture.supplyAsync(() -> FSitMod.sneakedPlayers.remove(playerUid), delayedExecutor);
		}
	}

	public static void spawnSeat(@NotNull PlayerEntity player, @NotNull World world, Vec3d pos) {
		final SeatEntity seatEntity = new SeatEntity(world, pos);

		world.spawnEntity(seatEntity);
		player.startRiding(seatEntity, true);
		FSitMod.sneakedPlayers.remove(player.getUuid());
	}

	@Override
	public void onInitialize() {
		FSitConfig.load();

		UseBlockCallback.EVENT.register(InteractBlockCallback::interactBlock);
		UseEntityCallback.EVENT.register(InteractPlayerCallback::interactPlayer);
		ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionCallbacks::onDisconnect);
	}
}
