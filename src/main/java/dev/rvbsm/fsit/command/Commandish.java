package dev.rvbsm.fsit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.List;

public interface Commandish<S extends CommandSource> {

	default void register(CommandDispatcher<S> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(this.builder());
	}

	default LiteralArgumentBuilder<S> builder() {
		final LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder.<S>literal(this.name()).requires(this::requires);
		this.children().stream().map(Commandish::builder).forEach(builder::then);

		final Iterator<Pair<String, ArgumentType<?>>> argsIterator = this.arguments().iterator();
		RequiredArgumentBuilder<S, ?> argsBuilder = null;
		while (argsIterator.hasNext()) {
			final var arg = argsIterator.next();
			final RequiredArgumentBuilder<S, ?> argBuilder = RequiredArgumentBuilder.argument(arg.getKey(), arg.getValue());
			if (!argsIterator.hasNext()) argBuilder.executes(this::executes);

			argsBuilder = argsBuilder != null ? argsBuilder.then(argBuilder) : argBuilder;
		}

		return argsBuilder != null ? builder.then(argsBuilder) : builder.executes(this::executes);
	}

	default boolean requires(S src) {
		return true;
	}

	default List<Commandish<S>> children() {
		return List.of();
	}

	default List<Pair<String, ArgumentType<?>>> arguments() {
		return List.of();
	}

	String name();

	int executes(CommandContext<S> ctx);
}
