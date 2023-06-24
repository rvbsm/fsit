package dev.rvbsm.fsit;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.config.PlayerBlockList;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.event.client.InteractBlockCallback;
import dev.rvbsm.fsit.event.client.InteractPlayerCallback;
import dev.rvbsm.fsit.packet.*;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class FSitClientMod implements ClientModInitializer, ModMenuApi {

	public static final PlayerBlockList blockedPlayers = new PlayerBlockList("fsit-blocklist");
	private static PlayerPose playerPose = PlayerPose.NONE;

	public static PlayerPose getPose() {
		return FSitClientMod.playerPose;
	}

	public static boolean isInPose(PlayerPose pose) {
		return FSitClientMod.getPose() == pose;
	}

	public static void resetPose() {
		if (FSitClientMod.isInPose(PlayerPose.CRAWL))
			ClientPlayNetworking.send(new CrawlC2SPacket(false));

		FSitClientMod.playerPose = PlayerPose.NONE;
	}

	public static void setSneaked() {
		final MinecraftClient client = MinecraftClient.getInstance();
		if (!FSitClientMod.isInPose(PlayerPose.NONE)) return;
		else if (client.player.isSpectator() || !client.player.isOnGround()) return;
		FSitClientMod.playerPose = PlayerPose.SNEAK;

		final Executor delayedExecutor = CompletableFuture.delayedExecutor(FSitMod.config.sneakDelay, TimeUnit.MILLISECONDS);
		CompletableFuture.runAsync(() -> {
			if (FSitClientMod.isInPose(PlayerPose.SNEAK)) FSitClientMod.resetPose();
		}, delayedExecutor);
	}

	public static void setSitting(Vec3d pos) {
		final MinecraftClient client = MinecraftClient.getInstance();
		FSitClientMod.playerPose = PlayerPose.SIT;
		ClientPlayNetworking.send(new SpawnSeatC2SPacket(client.player.getPos(), pos));
	}

	public static void setCrawling() {
		final MinecraftClient client = MinecraftClient.getInstance();
		if (client.player.isSpectator() || !client.player.isOnGround() || client.player.hasVehicle()) return;
		FSitClientMod.playerPose = PlayerPose.CRAWL;

		ClientPlayNetworking.send(new CrawlC2SPacket(true));
		client.player.sendMessage(FSitMod.getTranslation("message", "oncrawl", client.options.sneakKey.getBoundKeyLocalizedText()), true);
	}

	@Override
	public void onInitializeClient() {
		blockedPlayers.load();

		UseBlockCallback.EVENT.register(InteractBlockCallback::interactBlock);
		UseEntityCallback.EVENT.register(InteractPlayerCallback::interactPlayer);

		ClientPlayNetworking.registerGlobalReceiver(PingS2CPacket.TYPE, (packet, player, responseSender) -> responseSender.sendPacket(new PongC2SPacket()));
		ClientPlayNetworking.registerGlobalReceiver(RidePlayerPacket.TYPE, (packet, player, responseSender) -> {
			if (packet.type() == RidePlayerPacket.RideType.REQUEST)
				if (FSitMod.config.ridePlayers && !FSitClientMod.blockedPlayers.contains(packet.uuid()))
					responseSender.sendPacket(new RidePlayerPacket(RidePlayerPacket.RideType.ACCEPT, packet.uuid()));
		});
	}

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			final ConfigBuilder configBuilder = ConfigBuilder.create()
							.setParentScreen(screen)
							.setTitle(Text.literal("FSit"))
							.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/deepslate_bricks.png"))
							.setSavingRunnable(FSitConfig::save);
			final ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();

			final List<String> sittableTags = FSitMod.config.sittableTags.stream().map(Identifier::toString).toList();
			final List<String> sittableBlocks = FSitMod.config.sittableBlocks.stream().map(Identifier::toString).toList();

			final ConfigCategory main = configBuilder.getOrCreateCategory(Text.literal("main"));

			final SubCategoryBuilder sneakCategory = entryBuilder.startSubCategory(FSitMod.getTranslation("category", "sneak"));
			sneakCategory.add(entryBuilder.startBooleanToggle(ConfigData.Entries.SNEAK_SIT.keyText(), FSitMod.config.sneakSit)
							.setDefaultValue(ConfigData.Entries.SNEAK_SIT::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SNEAK_SIT::save)
							.setTooltip(ConfigData.Entries.SNEAK_SIT.commentText())
							.build());
			sneakCategory.add(entryBuilder.startIntSlider(ConfigData.Entries.MIN_ANGLE.keyText(), (int) FSitMod.config.minAngle, -90, 90)
							.setDefaultValue(ConfigData.Entries.MIN_ANGLE.defaultValue()::intValue)
							.setSaveConsumer((value) -> ConfigData.Entries.MIN_ANGLE.save(value.doubleValue()))
							.setTooltip(ConfigData.Entries.MIN_ANGLE.commentText())
							.build());
			sneakCategory.add(entryBuilder.startIntSlider(ConfigData.Entries.SNEAK_SIT.keyText(), FSitMod.config.sneakDelay, 100, 2000)
							.setDefaultValue(ConfigData.Entries.SNEAK_DELAY::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SNEAK_DELAY::save)
							.setTooltip(ConfigData.Entries.SNEAK_DELAY.commentText())
							.build());

			final SubCategoryBuilder sittableCategory = entryBuilder.startSubCategory(FSitMod.getTranslation("category", "sittable"));
			sittableCategory.add(entryBuilder.startBooleanToggle(ConfigData.Entries.SITTABLE_SIT.keyText(), FSitMod.config.sittableSit)
							.setDefaultValue(ConfigData.Entries.SITTABLE_SIT::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SITTABLE_SIT::save)
							.setTooltip(ConfigData.Entries.SITTABLE_SIT.commentText())
							.build());
			sittableCategory.add(entryBuilder.startStrList(ConfigData.Entries.SITTABLE_TAGS.keyText(), sittableTags)
							.setDefaultValue(ConfigData.Entries.SITTABLE_TAGS::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SITTABLE_TAGS::save)
							.setTooltip(ConfigData.Entries.SITTABLE_TAGS.commentText())
							.build());
			sittableCategory.add(entryBuilder.startStrList(ConfigData.Entries.SITTABLE_BLOCKS.keyText(), sittableBlocks)
							.setDefaultValue(ConfigData.Entries.SITTABLE_BLOCKS::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SITTABLE_BLOCKS::save)
							.setTooltip(ConfigData.Entries.SITTABLE_BLOCKS.commentText())
							.build());

			final SubCategoryBuilder miscCategory = entryBuilder.startSubCategory(FSitMod.getTranslation("category", "misc"));
			miscCategory.add(entryBuilder.startBooleanToggle(ConfigData.Entries.RIDE_PLAYERS.keyText(), FSitMod.config.ridePlayers)
							.setDefaultValue(ConfigData.Entries.RIDE_PLAYERS::defaultValue)
							.setSaveConsumer(ConfigData.Entries.RIDE_PLAYERS::save)
							.setTooltip(ConfigData.Entries.RIDE_PLAYERS.commentText())
							.build());

			main.addEntry(sneakCategory.setExpanded(true).build());
			main.addEntry(sittableCategory.setExpanded(true).build());
			main.addEntry(miscCategory.build());

			return configBuilder.build();
		};
	}
}
