package dev.rvbsm.fsit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.PoseHandler;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public record PoseCommand(String name, PlayerPose pose)
				implements Commandish<ServerCommandSource> {

	@Override
	public boolean requires(ServerCommandSource src) {
		return src.isExecutedByPlayer();
	}

	@Override
	public int executes(CommandContext<ServerCommandSource> ctx) {
		final ServerCommandSource src = ctx.getSource();
		final ServerPlayerEntity player = src.getPlayer();
		final PoseHandler poseHandler = (PoseHandler) player;
		if (player == null) return -1;

		if (poseHandler.isPosing()) poseHandler.resetPose();
		else switch (pose) {
			case SIT -> poseHandler.fsit$setSitting();
			case CRAWL -> poseHandler.fsit$setCrawling();
		}

		return Command.SINGLE_SUCCESS;
	}
}
