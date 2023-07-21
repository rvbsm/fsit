package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.PlayerConfigAccessor;
import dev.rvbsm.fsit.packet.RidePacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class InteractSPlayerCallback {

	private InteractSPlayerCallback() {}

	public static ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if (world.isClient) return ActionResult.PASS;
		else if (InteractSPlayerCallback.cantInteract(player, entity)) return ActionResult.PASS;

		final PlayerConfigAccessor playerConfigAccessor = (PlayerConfigAccessor) player;
		final PlayerConfigAccessor targetConfigAccessor = (PlayerConfigAccessor) entity;
		if (playerConfigAccessor.fsit$isModded()) return ActionResult.PASS;

		final ConfigData playerConfig = playerConfigAccessor.fsit$getConfig();
		final ConfigData targetConfig = targetConfigAccessor.fsit$getConfig();
		if (targetConfigAccessor.fsit$isModded()) {
			ServerPlayNetworking.send((ServerPlayerEntity) entity, new RidePacket(RidePacket.ActionType.REQUEST, player.getUuid()));

			return ActionResult.SUCCESS;
		} else if (targetConfig.ride && player.distanceTo(entity) <= playerConfig.rideRadius) {
			player.startRiding(entity, true);

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	static boolean cantInteract(PlayerEntity player, Entity entity) {
		return player.isSpectator() || entity.isSpectator() || !entity.isPlayer() || entity.hasPassengers() || !player.getMainHandStack().isEmpty();
	}
}
