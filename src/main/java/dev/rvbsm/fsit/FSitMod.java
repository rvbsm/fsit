package dev.rvbsm.fsit;

import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.config.FSitConfigManager;
import dev.rvbsm.fsit.entity.SeatEntity;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FSitMod implements ModInitializer {

	private static final String MOD_ID = "fsit";
	private static final FSitConfigManager configManager = new FSitConfigManager();
	private static FSitMod instance;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final Map<UUID, ScheduledFuture<Boolean>> scheduledTasks = new LinkedHashMap<>();
	private final List<UUID> sneakedPlayers = new LinkedList<>();
	private final Set<List<Double>> existingSeats = new LinkedHashSet<>();
	private FSitConfig config;

	public static FSitMod getInstance() {
		return instance;
	}

	public static String getTranslationKey(String type, String id) {
		return type + "." + FSitMod.MOD_ID + "." + id;
	}

	public static String getModId() {
		return FSitMod.MOD_ID;
	}

	public static FSitConfigManager getConfigManager() {
		return FSitMod.configManager;
	}

	public FSitConfig getConfig() {
		return this.config;
	}

	public boolean isNeedSeat(@NotNull PlayerEntity player) {
		return Collections.frequency(this.sneakedPlayers, player.getUuid()) == 2 && player.getPitch(1f) >= this.config.minAngle.getValue();
	}

	public void addSneaked(@NotNull PlayerEntity player) {
		final UUID playerUid = player.getUuid();
		if (Collections.frequency(this.sneakedPlayers, playerUid) < 2 && player.getPitch(1f) >= this.config.minAngle.getValue()) {
			this.sneakedPlayers.add(playerUid);
			this.scheduledTasks.put(playerUid, this.scheduler.schedule(() -> this.removeSneaked(player), this.config.shiftDelay.getValue(), TimeUnit.MILLISECONDS));
		}
	}

	public boolean removeSneaked(@NotNull PlayerEntity player) {
		return this.sneakedPlayers.remove(player.getUuid());
	}

	public void clearSneaked(@NotNull PlayerEntity player) {
		while (this.removeSneaked(player)) {
			final UUID playerUid = player.getUuid();
			final ScheduledFuture<Boolean> task = this.scheduledTasks.get(playerUid);
			if (task == null) continue;
			task.cancel(true);

			this.scheduledTasks.remove(playerUid);
		}
	}

	public void spawnSeat(@NotNull PlayerEntity player, @NotNull World world, double x, double y, double z) {
		final SeatEntity seatEntity = new SeatEntity(world, x, y, z);
		this.addSeatAt(x, y, z);
		world.spawnEntity(seatEntity);
		player.startRiding(seatEntity, true);
		this.clearSneaked(player);
	}

	private void addSeatAt(double x, double y, double z) {
		this.existingSeats.add(List.of(x, y, z));
	}

	public void removeSeatAt(double x, double y, double z) {
		this.existingSeats.remove(List.of(x, y, z));
	}

	public boolean hasSeatAt(double x, double y, double z) {
		return this.existingSeats.contains(List.of(x, y, z));
	}

	@Override public void onInitialize() {
		instance = this;

		this.config = configManager.load();
	}
}
