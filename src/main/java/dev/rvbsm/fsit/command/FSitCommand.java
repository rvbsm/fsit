package dev.rvbsm.fsit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.command.fsit.ReloadCommand;
import net.minecraft.server.command.ServerCommandSource;

public class FSitCommand implements Commandish<ServerCommandSource> {

	@Override
	public LiteralArgumentBuilder<ServerCommandSource> builder() {
		return Commandish.super.builder()
						.then(new ReloadCommand().builder());
//						.then(new GetCommand().builder())
//						.then(new SetCommand().builder());
	}

	@Override
	public String name() {
		return "fsit";
	}

	@Override
	public boolean requires(ServerCommandSource src) {
		return src.hasPermissionLevel(2);
	}

	@Override
	public int command(CommandContext<ServerCommandSource> ctx) {
		// todo
		return Command.SINGLE_SUCCESS;
	}
}
