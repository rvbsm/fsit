package dev.rvbsm.fsit.event.client;

import dev.rvbsm.fsit.FSitClientMod;
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
public abstract class InteractBlockCallback {

	public static ActionResult interactBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		if (!player.isMainPlayer()) return ActionResult.PASS;
		else if (!FSitClientMod.config.sittable) return ActionResult.PASS;

		final Item handItem = player.getStackInHand(hand).getItem();
		if (handItem instanceof BlockItem) return ActionResult.PASS;
		else if (handItem instanceof FluidModificationItem) return ActionResult.PASS;
		else if (!player.isOnGround() && player.shouldCancelInteraction()) return ActionResult.PASS;

		if (dev.rvbsm.fsit.event.InteractBlockCallback.isSittable(world, hitResult, FSitClientMod.config.sittableTags, FSitClientMod.config.sittableBlocks)) {
			ClientPlayNetworking.send(new SpawnSeatC2SPacket(player.getPos(), hitResult.getPos()));

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
