package dev.rvbsm.fsit.command

import com.mojang.brigadier.context.CommandContext
import dev.rvbsm.fsit.entity.Pose
import dev.rvbsm.fsit.network.isInPose
import dev.rvbsm.fsit.network.resetPose
import dev.rvbsm.fsit.network.setPose
import net.minecraft.server.command.ServerCommandSource

enum class PoseCommand(private val pose: Pose) : ModCommand<ServerCommandSource> {
    Sit(Pose.Sitting),
    Crawl(Pose.Crawling),
    /*Lay(Pose.Laying),
    Spin(Pose.Spinning)*/;

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
