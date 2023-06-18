package dev.rvbsm.fsit.event.client;

import dev.rvbsm.fsit.FSitClientMod;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.packet.RidePlayerS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class InteractPlayerCallback {

	public static ActionResult interactPlayer(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if (!FSitMod.config.sitPlayers) return ActionResult.PASS;
		else if (!MinecraftClient.getInstance().player.equals(player)) return ActionResult.PASS;
		else if (FSitClientMod.blockedPlayers.contains(entity.getUuid())) return ActionResult.PASS;
		else if (player.isSpectator() || entity.isSpectator()) return ActionResult.PASS;
		else if (!player.getStackInHand(hand).isEmpty()) return ActionResult.PASS;

		if (entity.isPlayer() && !entity.hasPassengers()) {
			RidePlayerS2CPacket.sendRequest((PlayerEntity) entity);

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
