package dev.rvbsm.fsit.command.fsit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.command.CommandArgument;
import dev.rvbsm.fsit.command.Commandish;
import dev.rvbsm.fsit.text.TextUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public class GetCommand implements Commandish<ServerCommandSource> {

	private static final String UNKNOWN_FIELD_MESSAGE = "Unknown config field key: %s";
	private static final String GET_MESSAGE = "%s ➡ %s";

	@Override
	public String name() {
		return "get";
	}

	@Override
	public List<CommandArgument> arguments() {
		return List.of(CommandArgument.CONFIG_KEY);
	}

	@Override
	public boolean requires(ServerCommandSource src) {
		return src.hasPermissionLevel(2);
	}

	@Override
	public int executes(CommandContext<ServerCommandSource> ctx) {
		final ServerCommandSource src = ctx.getSource();
		final String key = ctx.getArgument(CommandArgument.CONFIG_KEY.getName(), String.class);
		final Object cfgValue = FSitMod.getConfigManager().getByFlat(key);
		if (cfgValue == null) {
			src.sendError(Text.of(UNKNOWN_FIELD_MESSAGE.formatted(key)));
			return -1;
		}

		final String msg = TextUtils.colorizeChatEntries(GET_MESSAGE.formatted(key, cfgValue));
		src.sendMessage(TextUtils.convertToModern(msg));

		return Command.SINGLE_SUCCESS;
	}
}
