package dev.rvbsm.fsit.client

import com.mojang.serialization.Codec
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.client.command.RestrictCommand
import dev.rvbsm.fsit.client.config.KeyBindingMode
import dev.rvbsm.fsit.client.config.RestrictionList
import dev.rvbsm.fsit.client.event.ClientTickListener
import dev.rvbsm.fsit.client.network.ClientConnectionListener
import dev.rvbsm.fsit.client.network.FSitClientNetworking
import dev.rvbsm.fsit.network.packet.ConfigUpdateC2SPacket
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.minecraft.client.option.SimpleOption
import org.slf4j.LoggerFactory

object FSitModClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger(FSitModClient::class.java)

    @JvmStatic
    val sitKeyMode = SimpleOption(
        "key.fsit.sit",
        SimpleOption.emptyTooltip(),
        SimpleOption.enumValueText(),
        SimpleOption.PotentialValuesBasedCallbacks(
            enumValues<KeyBindingMode>().asList(), Codec.INT.xmap(KeyBindingMode::byId, KeyBindingMode::getId)
        ),
        KeyBindingMode.Hybrid,
    ) {}

    @JvmStatic
    val crawlKeyMode = SimpleOption(
        "key.fsit.crawl",
        SimpleOption.emptyTooltip(),
        SimpleOption.enumValueText(),
        SimpleOption.PotentialValuesBasedCallbacks(
            enumValues<KeyBindingMode>().asList(), Codec.INT.xmap(KeyBindingMode::byId, KeyBindingMode::getId)
        ),
        KeyBindingMode.Hybrid,
    ) {}

    override fun onInitializeClient() {
        RestrictionList.load()

        FSitClientNetworking.register()

        ClientPlayConnectionEvents.JOIN.register(ClientConnectionListener)
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickListener)

        enumValues<RestrictCommand>().forEach {
            ClientCommandRegistrationCallback.EVENT.register(it::register)
        }
    }

    fun saveConfig() {
        FSitMod.saveConfig()

        sendIfPossible(ConfigUpdateC2SPacket(FSitMod.config))
    }

    fun <T> sendIfPossible(packet: T, action: () -> Unit = {}) where T : FabricPacket {
        if (ClientPlayNetworking.canSend(packet.type)) {
            ClientPlayNetworking.send(packet)
        } else action()
    }
}
