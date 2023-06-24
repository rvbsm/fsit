package dev.rvbsm.fsit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.FSitMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;

public class CrawlCommand implements Commandish<ServerCommandSource> {

	@Override
	public String name() {
		return "crawl";
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

		final UUID playerId = player.getUuid();
		switch (FSitMod.getPose(playerId)) {
			case NONE, SNEAK -> FSitMod.setCrawling(player);
			case SIT, CRAWL -> FSitMod.resetPose(playerId);
		}

		return Command.SINGLE_SUCCESS;
	}
}
