package dev.rvbsm.fsit;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.config.PlayerBlockList;
import dev.rvbsm.fsit.event.client.InteractBlockCallback;
import dev.rvbsm.fsit.event.client.InteractPlayerCallback;
import dev.rvbsm.fsit.packet.CrawlC2SPacket;
import dev.rvbsm.fsit.packet.PingS2CPacket;
import dev.rvbsm.fsit.packet.PongC2SPacket;
import dev.rvbsm.fsit.packet.RidePlayerPacket;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class FSitClientMod implements ClientModInitializer, ModMenuApi {

	public static final PlayerBlockList blockedPlayers = new PlayerBlockList("fsit-blocklist");
	private static boolean sneaked = false;
	private static boolean crawling = false;

	public static void setSneaked() {
		FSitClientMod.sneaked = true;

		final Executor delayedExecutor = CompletableFuture.delayedExecutor(FSitMod.config.sneakDelay, TimeUnit.MILLISECONDS);
		CompletableFuture.runAsync(() -> FSitClientMod.sneaked = false, delayedExecutor);
	}

	public static boolean isSneaked() {
		return FSitClientMod.sneaked;
	}

	public static boolean isCrawling() {
		return FSitClientMod.crawling;
	}

	public static void setCrawling(boolean crawling) {
		FSitClientMod.crawling = crawling;

		ClientPlayNetworking.send(new CrawlC2SPacket(crawling));
	}

	@Override
	public void onInitializeClient() {
		blockedPlayers.load();

		UseBlockCallback.EVENT.register(InteractBlockCallback::interactBlock);
		UseEntityCallback.EVENT.register(InteractPlayerCallback::interactPlayer);

		ClientPlayNetworking.registerGlobalReceiver(PingS2CPacket.TYPE, (packet, player, responseSender) -> responseSender.sendPacket(new PongC2SPacket()));
		ClientPlayNetworking.registerGlobalReceiver(RidePlayerPacket.TYPE, (packet, player, responseSender) -> {
			if (packet.type() == RidePlayerPacket.RideType.REQUEST)
				if (FSitMod.config.sitPlayers && !FSitClientMod.blockedPlayers.contains(packet.uuid()))
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
			miscCategory.add(entryBuilder.startBooleanToggle(ConfigData.Entries.SIT_PLAYERS.keyText(), FSitMod.config.sitPlayers)
							.setDefaultValue(ConfigData.Entries.SIT_PLAYERS::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SIT_PLAYERS::save)
							.setTooltip(ConfigData.Entries.SIT_PLAYERS.commentText())
							.build());

			main.addEntry(sneakCategory.setExpanded(true).build());
			main.addEntry(sittableCategory.setExpanded(true).build());
			main.addEntry(miscCategory.build());

			return configBuilder.build();
		};
	}
}
