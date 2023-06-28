package dev.rvbsm.fsit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.FSitMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

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
		final ServerPlayerEntity player = src.getPlayer();
		if (player == null) return -1;

		switch (FSitMod.getPose(player.getUuid())) {
			case NONE, SNEAK -> FSitMod.setCrawling(player);
			case SIT, CRAWL -> FSitMod.resetPose(player);
		}

		return Command.SINGLE_SUCCESS;
	}
}
