package dev.rvbsm.fsit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;

public interface Commandish<Source extends CommandSource> {

	default void register(CommandDispatcher<Source> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(this.builder());
	}

	default LiteralArgumentBuilder<Source> builder() {
		return LiteralArgumentBuilder.<Source>literal(this.name())
						.requires(this::requires)
						.executes(this::command);
	}

	default <T> RequiredArgumentBuilder<Source, T> requiredArgumentBuilder(String name, ArgumentType<T> type, Command<Source> command) {
		return RequiredArgumentBuilder.<Source, T>argument(name, type).executes(command);
	}


	String name();

	boolean requires(Source src);

	int command(CommandContext<Source> ctx);
}
