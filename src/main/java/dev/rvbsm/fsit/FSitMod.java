package dev.rvbsm.fsit;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FSitMod implements ModInitializer {

	private static final String MOD_ID = "fsit";
	private static final int SHIFT_DELAY = 1000; // ms
	private static final float MIN_ANGLE = 66.6f; // to sit down
	private static FSitMod instance;
	private final Logger logger = LoggerFactory.getLogger(MOD_ID);
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final List<UUID> sneakedPlayers = new LinkedList<>();

	public static FSitMod getInstance() {
		return instance;
	}

	public Logger getLogger() {
		return this.logger;
	}

	public boolean isNeedSeat(@NotNull PlayerEntity player) {
		return sneakedPlayers.stream().filter(player.getUuid()::equals).count() >= 2 && player.getPitch() >= MIN_ANGLE;
	}

	public void addSneaked(@NotNull PlayerEntity player) {
		if (player.getPitch() >= MIN_ANGLE) {
			sneakedPlayers.add(player.getUuid());
			scheduler.schedule(() -> removeSneaked(player), SHIFT_DELAY, TimeUnit.MILLISECONDS);
		}
	}

	public boolean removeSneaked(@NotNull PlayerEntity player) {
		return sneakedPlayers.remove(player.getUuid());
	}

	public void clearSneaked(@NotNull PlayerEntity player) {
		while (this.removeSneaked(player));
	}

	@Override
	public void onInitialize() {
		instance = this;
	}
}
