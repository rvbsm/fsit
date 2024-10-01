package dev.rvbsm.fsit.command

import net.minecraft.server.command.ServerCommandSource

fun ServerCommandSource.isGameMaster() = hasPermissionLevel(2)
