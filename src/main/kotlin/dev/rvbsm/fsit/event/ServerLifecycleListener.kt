package dev.rvbsm.fsit.event

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

object ServerLifecycleListener : ServerLifecycleEvents.ServerStopping {
    override fun onServerStopping(server: MinecraftServer) {
        PassedUseEntityCallback.onServerStopping(server)
    }
}
