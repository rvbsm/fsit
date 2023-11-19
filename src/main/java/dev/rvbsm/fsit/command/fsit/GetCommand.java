package dev.rvbsm.fsit.command.fsit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.command.Commandish;
import dev.rvbsm.fsit.command.argument.ConfigFieldArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class GetCommand implements Commandish<ServerCommandSource> {

	@Override
	public String name() {
		return "get";
	}

	@Override
	public List<Pair<String, ArgumentType<?>>> arguments() {
		return List.of(Pair.of("key", ConfigFieldArgumentType.field()));
	}

	@Override
	public boolean requires(ServerCommandSource src) {
		return src.hasPermissionLevel(2);
	}

	@Override
	public int executes(CommandContext<ServerCommandSource> ctx) {
		final ServerCommandSource src = ctx.getSource();
		final String key = ctx.getArgument("key", String.class);
		final Object cfgValue = FSitMod.getConfigManager().getByFlat(key);

		src.sendMessage(Text.of("%s ➡ %s".formatted(key, cfgValue)));

		return Command.SINGLE_SUCCESS;
	}
}
