package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.packet.RidePlayerPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public abstract class InteractPlayerCallback {

	public static ActionResult interactPlayer(PlayerEntity player, World world, Hand hand, Entity entity, HitResult hitResult) {
		if (world.isClient) return ActionResult.PASS;
		else if (FSitMod.isModded(player.getUuid())) return ActionResult.PASS;
		else if (player.isSpectator() || entity.isSpectator()) return ActionResult.PASS;
		else if (!player.getStackInHand(hand).isEmpty()) return ActionResult.PASS;

		if (entity.isPlayer() && FSitMod.isModded(entity.getUuid())) {
			ServerPlayNetworking.send((ServerPlayerEntity) entity, new RidePlayerPacket(RidePlayerPacket.RideType.REQUEST, player.getUuid()));

			return ActionResult.SUCCESS;
		}

		if (!FSitMod.config.sitPlayers && entity.isPlayer() && !entity.hasPassengers()) {
			player.startRiding(entity, true);

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
