package dev.rvbsm.fsit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.command.fsit.GetCommand;
import dev.rvbsm.fsit.command.fsit.ReloadCommand;
import dev.rvbsm.fsit.command.fsit.SetCommand;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class FSitCommand implements Commandish<ServerCommandSource> {

	@Override
	public String name() {
		return "fsit";
	}

	@Override
	public boolean requires(ServerCommandSource src) {
		return src.hasPermissionLevel(2);
	}

	@Override
	public int executes(CommandContext<ServerCommandSource> ctx) {
		// todo
		return Command.SINGLE_SUCCESS;
	}

	@Override
	public List<Commandish<ServerCommandSource>> children() {
		return List.of(new ReloadCommand(), new GetCommand(), new SetCommand());
	}
}
