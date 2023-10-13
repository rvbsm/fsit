package dev.rvbsm.fsit.network;

import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.ConfigHandler;
import dev.rvbsm.fsit.entity.PoseHandler;
import dev.rvbsm.fsit.entity.RestrictHandler;
import dev.rvbsm.fsit.network.packet.ConfigSyncC2SPacket;
import dev.rvbsm.fsit.network.packet.RestrictPlayerC2SPacket;
import dev.rvbsm.fsit.network.packet.RestrictionListSyncS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public final class ServerNetworkHandler {

	private ServerNetworkHandler() {}

	public static void onJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		sender.sendPacket(new RestrictionListSyncS2CPacket(((RestrictHandler) handler.player).fsit$getRestrictionList()));
		((PoseHandler) handler.player).resetPose();
	}

	public static void onConfigReceive(ConfigSyncC2SPacket packet, ServerPlayerEntity player, PacketSender sender) {
		((ConfigHandler) player).fsit$setConfig(packet.config());
	}

	public static void onRestrictReceive(RestrictPlayerC2SPacket packet, ServerPlayerEntity player, PacketSender sender) {
		final PlayerEntity target = player.server.getPlayerManager().getPlayer(packet.targetUUID());
		switch (packet.type()) {
			case ALLOW -> ((RestrictHandler) player).fsit$allowPlayer(target.getUuid());
			case RESTRICT -> ((RestrictHandler) player).fsit$restrictPlayer(target.getUuid());
		}
	}

	private static boolean isSittable(World world, BlockHitResult hitResult, Set<Identifier> sittableTags, Set<Identifier> sittableBlocks) {
		if (hitResult.getSide() != Direction.UP) return false;

		final BlockPos blockPos = hitResult.getBlockPos();
		if (!world.isAir(blockPos.up())) return false;

		final BlockState blockState = world.getBlockState(blockPos);
		final Block block = blockState.getBlock();

		final Stream<Identifier> blockTags = blockState.streamTags().map(TagKey::id);
		final Identifier blockIdentifier = Registries.BLOCK.getId(block);
		if (blockTags.anyMatch(sittableTags::contains) || sittableBlocks.contains(blockIdentifier)) {
			final Collection<Property<?>> blockProperties = blockState.getProperties();
			if (blockProperties.contains(Properties.AXIS)) return blockState.get(Properties.AXIS) != Direction.Axis.Y;
			else if (blockProperties.contains(Properties.BLOCK_HALF))
				return blockState.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM;
			else if (blockProperties.contains(Properties.SLAB_TYPE))
				return blockState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM;
			else return true;
		}

		return false;
	}

	private static boolean preventsFromSitting(PlayerEntity player) {
		return player.isSpectator() || player.isSneaking() || !player.getMainHandStack().isEmpty();
	}

	public static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		if (world.isClient || preventsFromSitting(player)) return ActionResult.PASS;

		final ConfigHandler configHandler = (ConfigHandler) player;
		final ConfigData.SittableTable configSittable = configHandler.fsit$getConfig().getSittable();
		if (!configSittable.isEnabled()) return ActionResult.PASS;

		if (player.getPos().distanceTo(hitResult.getPos()) <= configSittable.getRadius())
			if (isSittable(world, hitResult, configSittable.getTags(), configSittable.getBlocks())) {
				((PoseHandler) player).fsit$setSitting(hitResult.getPos());
				return ActionResult.SUCCESS;
			}

		return ActionResult.PASS;
	}

	private static boolean preventsFromSitting(PlayerEntity player, PlayerEntity targetPlayer) {
		return preventsFromSitting(player) || preventsFromSitting(targetPlayer) || targetPlayer.hasPassengers();
	}

	public static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if (world.isClient || !entity.isPlayer() || preventsFromSitting(player, (PlayerEntity) entity))
			return ActionResult.PASS;

		if (((ConfigHandler) player).fsit$canStartRiding((PlayerEntity) entity)) {
			player.startRiding(entity);
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}
}
