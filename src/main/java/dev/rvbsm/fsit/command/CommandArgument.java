package dev.rvbsm.fsit.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.rvbsm.fsit.FSitMod;
import net.minecraft.command.CommandSource;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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

	public <S extends CommandSource> RequiredArgumentBuilder<S, ?> argument() {
		return RequiredArgumentBuilder.argument(this.getName(), type);
	}

	public CompletableFuture<Suggestions> suggestMatching(CommandContext<? extends CommandSource> ignored, SuggestionsBuilder suggestionsBuilder) {
		return CommandSource.suggestMatching(this.suggestions, suggestionsBuilder);
	}

	public <S extends CommandSource> RequiredArgumentBuilder<S, ?> argumentSuggestion() {
		return this.<S>argument().suggests(this::suggestMatching);
	}
}
