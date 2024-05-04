package dev.rvbsm.fsit.command

import com.mojang.brigadier.context.CommandContext
import dev.rvbsm.fsit.entity.PlayerPose
import dev.rvbsm.fsit.network.isInPose
import dev.rvbsm.fsit.network.resetPose
import dev.rvbsm.fsit.network.setPose
import net.minecraft.server.command.ServerCommandSource

enum class PoseCommand(private val pose: PlayerPose) : ModCommand<ServerCommandSource> {
    Sit(PlayerPose.Sitting),
    Crawl(PlayerPose.Crawling),
//    Lay(PlayerPose.Laying),
//    Spin(PlayerPose.Spinning)
    ;

    override fun requires(src: ServerCommandSource) = src.isExecutedByPlayer

    override fun executes(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player ?: return -1
        if (player.hasVehicle()) return -1

        if (player.isInPose()) {
            player.resetPose()
        } else {
            player.setPose(pose)
        }

        return super.executes(ctx)
    }
}
