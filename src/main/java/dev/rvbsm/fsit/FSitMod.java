package dev.rvbsm.fsit;

import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.ConfigManager;
import dev.rvbsm.fsit.network.ServerNetworkHandler;
import dev.rvbsm.fsit.network.packet.ConfigSyncC2SPacket;
import dev.rvbsm.fsit.network.packet.RestrictPlayerC2SPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FSitMod implements ModInitializer {

	public static final String MOD_ID = "fsit";
	private static final ConfigManager<ConfigData> CONFIG_MANAGER = new ConfigManager<>(MOD_ID, ConfigData.class);

	@Contract("_, _ -> new")
	public static @NotNull String getTranslationKey(String type, String id) {
		return String.join(".", type, MOD_ID, id);
	}

	@Contract(value = "_, _, _ -> new", pure = true)
	public static @NotNull Text getTranslation(String type, String id, Object... args) {
		final String translationKey = FSitMod.getTranslationKey(type, id);
		return Text.translatable(translationKey, args);
	}

	public static ConfigManager<ConfigData> getConfigManager() {
		return CONFIG_MANAGER;
	}

	public static ConfigData getConfig() {
		return CONFIG_MANAGER.getConfig();
	}

	public static void loadConfig() {
		CONFIG_MANAGER.loadConfig();
	}

	@Override
	public void onInitialize() {
		FSitMod.loadConfig();

		ServerPlayConnectionEvents.JOIN.register(ServerNetworkHandler.Connection::onJoin);

		UseBlockCallback.EVENT.register(ServerNetworkHandler::onUseBlock);
		UseEntityCallback.EVENT.register(ServerNetworkHandler::onUseEntity);

		ServerPlayNetworking.registerGlobalReceiver(ConfigSyncC2SPacket.TYPE, ServerNetworkHandler::onConfigReceive);
		ServerPlayNetworking.registerGlobalReceiver(RestrictPlayerC2SPacket.TYPE, ServerNetworkHandler::onRestrictReceive);
	}
}
