package dev.rvbsm.fsit.command.fsit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.command.Commandish;
import dev.rvbsm.fsit.utils.TextUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ConfigCommand implements Commandish<ServerCommandSource> {


	@Override
	public String name() {
		return "config";
	}

	@Override
	public int executes(CommandContext<ServerCommandSource> ctx) {
		final ServerCommandSource src = ctx.getSource();

		final Text colorizedToml = TextUtils.getColorizedConfig();
		src.sendMessage(colorizedToml);

		return Command.SINGLE_SUCCESS;
	}
}
