package dev.rvbsm.fsit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.FSitMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

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
		final ServerPlayerEntity player = src.getPlayer();
		if (player == null) return -1;

		if (FSitMod.isPosing(player.getUuid())) FSitMod.resetPose(player);
		else FSitMod.setSitting(player, player.getPos());

		return Command.SINGLE_SUCCESS;
	}
}
