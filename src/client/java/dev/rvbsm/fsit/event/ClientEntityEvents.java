package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.FSitModClient;
import dev.rvbsm.fsit.packet.RidePacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ClientEntityEvents {

	private ClientEntityEvents() {}

	public static ActionResult useOnPlayer(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if (!player.isMainPlayer()) return ActionResult.PASS;
		else if (!entity.isPlayer() && ServerEntityEvents.preventsFromSitting(player, entity)) return ActionResult.PASS;
		else if (FSitModClient.isBlockedRider(entity.getUuid())) return ActionResult.PASS;

		if (FSitMod.getConfig().ride && player.distanceTo(entity) <= FSitMod.getConfig().rideRadius) {
			ClientPlayNetworking.send(new RidePacket(RidePacket.ActionType.REQUEST, entity.getUuid()));

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
