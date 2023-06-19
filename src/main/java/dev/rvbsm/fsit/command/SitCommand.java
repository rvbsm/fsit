package dev.rvbsm.fsit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.SeatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SitCommand implements Commandish<ServerCommandSource> {

	@Override
	public String name() {
		return "sit";
	}

	@Override
	public boolean requires(ServerCommandSource src) {
		return src.isExecutedByPlayer();
	}

	@Override
	public int command(CommandContext<ServerCommandSource> ctx) {
		final ServerCommandSource src = ctx.getSource();
		final PlayerEntity player = src.getPlayer();
		if (player == null) return -1;

		final Entity vehicle = player.getVehicle();
		final World world = src.getWorld();
		final Vec3d playerPos = player.getPos();
		if (player.isOnGround() && vehicle == null && !player.isSpectator()) FSitMod.spawnSeat(player, world, playerPos);
		else if (vehicle instanceof SeatEntity) player.stopRiding();

		return Command.SINGLE_SUCCESS;
	}
}
