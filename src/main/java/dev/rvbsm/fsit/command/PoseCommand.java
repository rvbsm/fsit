package dev.rvbsm.fsit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public record PoseCommand(String name, PlayerPose pose)
				implements Commandish<ServerCommandSource> {

	@Override
	public boolean requires(ServerCommandSource src) {
		return src.isExecutedByPlayer();
	}

	@Override
	public int command(CommandContext<ServerCommandSource> ctx) {
		final ServerCommandSource src = ctx.getSource();
		final ServerPlayerEntity player = src.getPlayer();
		final PlayerPoseAccessor poseAccessor = (PlayerPoseAccessor) player;
		if (player == null) return -1;

		if (poseAccessor.isPlayerPosing()) poseAccessor.resetPlayerPose();
		else switch (pose) {
			case SIT -> poseAccessor.setPlayerSitting();
			case CRAWL -> poseAccessor.setPlayerCrawling();
		}

		return Command.SINGLE_SUCCESS;
	}
}
