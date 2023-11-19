package dev.rvbsm.fsit.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.rvbsm.fsit.FSitMod;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ConfigFieldArgumentType implements ArgumentType<String> {

	public static final Identifier ID = Identifier.of(FSitMod.MOD_ID, "config_field");
	private static final DynamicCommandExceptionType UNKNOWN_FIELD_EXCEPTION = new DynamicCommandExceptionType(str -> Text.of("Unknown config field key: " + str.toString()));

	public static ConfigFieldArgumentType field() {
		return new ConfigFieldArgumentType();
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		final String key = reader.readUnquotedString();
		if (!FSitMod.getConfigManager().getConfigKeys().contains(key)) throw UNKNOWN_FIELD_EXCEPTION.create(key);

		return key;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestMatching(FSitMod.getConfigManager().getConfigKeys(), builder);
	}
}
