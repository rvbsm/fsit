package dev.rvbsm.fsit.command.fsit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.command.Commandish;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ReloadCommand implements Commandish<ServerCommandSource> {

	@Override
	public String name() {
		return "reload";
	}

	@Override
	public boolean requires(ServerCommandSource src) {
		return src.hasPermissionLevel(2);
	}

	@Override
	public int executes(CommandContext<ServerCommandSource> ctx) {
		FSitMod.loadConfig();
		ctx.getSource().sendMessage(Text.of("Config reloaded"));
		return Command.SINGLE_SUCCESS;
	}
}
