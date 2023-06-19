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
		if (player == null || !player.isOnGround() || player.hasVehicle() || player.isSpectator()) return -1;

		final UUID playerUid = player.getUuid();
		if (FSitMod.isCrawled(playerUid)) FSitMod.removeCrawled(playerUid);
		else FSitMod.addCrawled(player);

		return Command.SINGLE_SUCCESS;
	}
}
