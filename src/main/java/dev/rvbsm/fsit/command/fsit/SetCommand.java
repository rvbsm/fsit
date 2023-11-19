package dev.rvbsm.fsit.command.fsit;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.command.Commandish;
import dev.rvbsm.fsit.command.argument.ConfigFieldArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class SetCommand implements Commandish<ServerCommandSource> {

	@Override
	public String name() {
		return "set";
	}

	@Override
	public boolean requires(ServerCommandSource src) {
		return src.hasPermissionLevel(2);
	}

	@Override
	public int executes(CommandContext<ServerCommandSource> ctx) {
		final ServerCommandSource src = ctx.getSource();
		final String key = ctx.getArgument("key", String.class);
		final String value = ctx.getArgument("value", String.class);
		final Object cfgValue = FSitMod.getConfigManager().getByFlat(key);

		final Object newCfgValue;
		try {
			newCfgValue = new Gson().fromJson(value, cfgValue.getClass());
		} catch (JsonSyntaxException e) {
			src.sendError(Text.of(e.getMessage()));
			return -1;
		}

		FSitMod.getConfigManager().updateByFlat(key, newCfgValue);
		src.sendMessage(Text.of("Updated %s ➡ %s".formatted(key, newCfgValue)));

		return Command.SINGLE_SUCCESS;
	}

	@Override
	public List<Pair<String, ArgumentType<?>>> arguments() {
		return List.of(Pair.of("key", ConfigFieldArgumentType.field()), Pair.of("value", StringArgumentType.greedyString()));
	}
}
