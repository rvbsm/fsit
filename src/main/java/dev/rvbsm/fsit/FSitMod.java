package dev.rvbsm.fsit;

import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.event.InteractSBlockCallback;
import dev.rvbsm.fsit.event.InteractSPlayerCallback;
import dev.rvbsm.fsit.event.PlayerConnectionCallbacks;
import dev.rvbsm.fsit.packet.ConfigSyncC2SPacket;
import dev.rvbsm.fsit.packet.RidePlayerPacket;
import dev.rvbsm.fsit.packet.SpawnSeatC2SPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FSitMod implements ModInitializer {

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

		UseBlockCallback.EVENT.register(InteractSBlockCallback::interact);
		UseEntityCallback.EVENT.register(InteractSPlayerCallback::interact);
		ServerPlayConnectionEvents.JOIN.register(PlayerConnectionCallbacks::onConnect);
		ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionCallbacks::onDisconnect);

		ServerPlayNetworking.registerGlobalReceiver(ConfigSyncC2SPacket.TYPE, ConfigSyncC2SPacket::receive);
		ServerPlayNetworking.registerGlobalReceiver(SpawnSeatC2SPacket.TYPE, SpawnSeatC2SPacket::receive);
		ServerPlayNetworking.registerGlobalReceiver(RidePlayerPacket.TYPE, RidePlayerPacket::receive);
	}
}
