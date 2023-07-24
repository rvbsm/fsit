package dev.rvbsm.fsit;

import dev.rvbsm.fsit.command.FSitCommand;
import dev.rvbsm.fsit.command.PoseCommand;
import dev.rvbsm.fsit.entity.PlayerPose;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class FSitModServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		CommandRegistrationCallback.EVENT.register(new FSitCommand()::register);

		if (FSitMod.config.commands) {
			CommandRegistrationCallback.EVENT.register(new PoseCommand(FSitMod.config.commandsSit, PlayerPose.SIT)::register);
			CommandRegistrationCallback.EVENT.register(new PoseCommand(FSitMod.config.commandsCrawl, PlayerPose.CRAWL)::register);
		}
	}
}
