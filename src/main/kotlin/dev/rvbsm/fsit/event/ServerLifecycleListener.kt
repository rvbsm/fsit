package dev.rvbsm.fsit.event

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents

val ServerLifecycleListener = ServerLifecycleEvents.ServerStopping { server ->
    PassedUseEntityCallback.onServerStopping(server)
}
