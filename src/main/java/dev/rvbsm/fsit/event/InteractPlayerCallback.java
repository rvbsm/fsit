package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.config.FSitConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public abstract class InteractPlayerCallback {

	public static ActionResult interactPlayer(PlayerEntity player, World world, Hand hand, Entity entity, HitResult ignored) {
		if (!FSitConfig.sitPlayers.getValue()) return ActionResult.PASS;
		else if (world.isClient || player.isSpectator()) return ActionResult.PASS;
		else if (player.shouldCancelInteraction()) return ActionResult.PASS;
		else if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
		else if (!player.getStackInHand(hand).isEmpty()) return ActionResult.PASS;

		if (entity instanceof ServerPlayerEntity vehicle) if (!entity.isSpectator() && !entity.hasPassengers()) {
			vehicle.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
			player.startRiding(vehicle, true);
			vehicle.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(vehicle));
		}

		return ActionResult.SUCCESS;
	}
}
