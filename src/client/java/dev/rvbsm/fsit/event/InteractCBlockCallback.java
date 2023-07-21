package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.packet.SpawnSeatC2SPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class InteractCBlockCallback {

	private InteractCBlockCallback() {}

	public static ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		if (!player.isMainPlayer()) return ActionResult.PASS;
		else if (!FSitMod.getConfig().sittable) return ActionResult.PASS;

		final Item handItem = player.getStackInHand(hand).getItem();
		if (handItem instanceof BlockItem) return ActionResult.PASS;
		else if (handItem instanceof FluidModificationItem) return ActionResult.PASS;
		else if (!player.isOnGround() && player.shouldCancelInteraction()) return ActionResult.PASS;
		else if (player.getPos().distanceTo(hitResult.getPos()) > FSitMod.getConfig().sittableRadius) return ActionResult.PASS;

		if (InteractSBlockCallback.isSittable(world, hitResult, FSitMod.getConfig().sittableTags, FSitMod.getConfig().sittableBlocks)) {
			ClientPlayNetworking.send(new SpawnSeatC2SPacket(player.getPos(), hitResult.getPos()));

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
