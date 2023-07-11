package dev.rvbsm.fsit;

import dev.rvbsm.fsit.command.FSitCommand;
import dev.rvbsm.fsit.command.PoseCommand;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.event.InteractBlockCallback;
import dev.rvbsm.fsit.event.InteractPlayerCallback;
import dev.rvbsm.fsit.event.PlayerConnectionCallbacks;
import dev.rvbsm.fsit.packet.ConfigSyncC2SPacket;
import dev.rvbsm.fsit.packet.RidePlayerPacket;
import dev.rvbsm.fsit.packet.SpawnSeatC2SPacket;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FSitMod implements ModInitializer, DedicatedServerModInitializer {

	protected static final ConfigData config = new ConfigData();

	@Contract("_, _ -> new")
	public static @NotNull String getTranslationKey(String type, String id) {
		return String.join(".", type, "fsit", id);
	}

	@Contract(value = "_, _, _ -> new", pure = true)
	public static @NotNull Text getTranslation(String type, String id, Object... args) {
		final String translationKey = FSitMod.getTranslationKey(type, id);
		return Text.translatable(translationKey, args);
	}

	public static ConfigData getConfig() {
		return FSitMod.config;
	}

	public static void loadConfig() {
		FSitConfig.load(FSitMod.config);
	}

	@Override
	public void onInitialize() {
		FSitMod.loadConfig();

		UseBlockCallback.EVENT.register(InteractBlockCallback::interactBlock);
		UseEntityCallback.EVENT.register(InteractPlayerCallback::interactPlayer);
		ServerPlayConnectionEvents.JOIN.register(PlayerConnectionCallbacks::onConnect);
		ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionCallbacks::onDisconnect);

		ServerPlayNetworking.registerGlobalReceiver(ConfigSyncC2SPacket.TYPE, ConfigSyncC2SPacket::receive);
		ServerPlayNetworking.registerGlobalReceiver(SpawnSeatC2SPacket.TYPE, SpawnSeatC2SPacket::receive);
		ServerPlayNetworking.registerGlobalReceiver(RidePlayerPacket.TYPE, RidePlayerPacket::receive);
	}

	@Override
	public void onInitializeServer() {
		CommandRegistrationCallback.EVENT.register(new FSitCommand()::register);
		CommandRegistrationCallback.EVENT.register(new PoseCommand("sit", PlayerPose.SIT)::register);
		CommandRegistrationCallback.EVENT.register(new PoseCommand("crawl", PlayerPose.CRAWL)::register);
	}
}
