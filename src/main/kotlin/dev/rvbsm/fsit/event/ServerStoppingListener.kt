package dev.rvbsm.fsit.event

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents

val ServerStoppingListener = ServerLifecycleEvents.ServerStopping { server ->
    RidingServerStoppingEvent.onServerStopping(server)
}
