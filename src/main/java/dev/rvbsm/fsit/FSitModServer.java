package dev.rvbsm.fsit;

import dev.rvbsm.fsit.command.FSitCommand;
import dev.rvbsm.fsit.command.PoseCommand;
import dev.rvbsm.fsit.entity.PlayerPose;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class FSitModServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		CommandRegistrationCallback.EVENT.register(new FSitCommand()::register);
		CommandRegistrationCallback.EVENT.register(new PoseCommand("sit", PlayerPose.SIT)::register);
		CommandRegistrationCallback.EVENT.register(new PoseCommand("crawl", PlayerPose.CRAWL)::register);
	}
}
