package dev.rvbsm.fsit.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.rvbsm.fsit.FSitMod;
import lombok.Getter;
import net.minecraft.command.CommandSource;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum CommandArgument {
	CONFIG_KEY(StringArgumentType.word(), FSitMod.getConfigManager().getConfigKeys()),
	CONFIG_VALUE(StringArgumentType.greedyString());

	private final ArgumentType<?> type;
	private final Set<String> suggestions;

	CommandArgument(ArgumentType<?> type, Set<String> suggestions) {
		this.type = type;
		this.suggestions = suggestions;
	}

	CommandArgument(ArgumentType<?> type) {
		this(type, Set.of());
	}

	public String getName() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	public Set<String> getSuggestions(CommandContext<? extends CommandSource> ctx) {
		try {
			final String argument = ctx.getArgument(this.getName(), String.class);
			return this.suggestions.stream()
							.filter(suggestion -> suggestion.startsWith(argument))
							.collect(Collectors.toUnmodifiableSet());
		} catch (IllegalArgumentException ignored) {}

		return this.suggestions;
	}
}
