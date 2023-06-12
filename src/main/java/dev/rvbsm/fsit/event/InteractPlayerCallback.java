package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.config.FSitConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public abstract class InteractPlayerCallback {

	public static ActionResult interactPlayer(PlayerEntity player, World world, Hand hand, Entity entity, HitResult ignored) {
		if (!FSitConfig.data.sitPlayers) return ActionResult.PASS;
		else if (world.isClient) return ActionResult.PASS;
		else if (player.isSpectator() || entity.isSpectator()) return ActionResult.PASS;
		else if (player.isSneaking() || entity.isSneaking()) return ActionResult.PASS;
		else if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
		else if (!player.getStackInHand(hand).isEmpty()) return ActionResult.PASS;

		if (entity instanceof PlayerEntity && !entity.hasPassengers()) {
			player.startRiding(entity, true);

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
