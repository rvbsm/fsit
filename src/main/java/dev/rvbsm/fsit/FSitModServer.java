package dev.rvbsm.fsit;

import dev.rvbsm.fsit.command.FSitCommand;
import dev.rvbsm.fsit.command.PoseCommand;
import dev.rvbsm.fsit.command.argument.ConfigFieldArgumentType;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.PoseHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

public final class FSitModServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		ArgumentTypeRegistry.registerArgumentType(ConfigFieldArgumentType.ID, ConfigFieldArgumentType.class, ConstantArgumentSerializer.of(ConfigFieldArgumentType::field));
		CommandRegistrationCallback.EVENT.register(new FSitCommand()::register);

		final ConfigData.CommandsTable configCommands = FSitMod.getConfig().getCommandsServer();
		if (configCommands.isEnabled()) {
			CommandRegistrationCallback.EVENT.register(new PoseCommand(configCommands.getSit(), PoseHandler::fsit$setSitting)::register);
			CommandRegistrationCallback.EVENT.register(new PoseCommand(configCommands.getCrawl(), PoseHandler::fsit$setCrawling)::register);
		}
	}
}
