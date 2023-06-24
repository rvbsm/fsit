package dev.rvbsm.fsit.entity;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SeatEntity extends AreaEffectCloudEntity {

	private static final double OFFSET = .5d;
	private Entity mounted;

	public SeatEntity(World world, Vec3d pos) {
		super(world, pos.x, pos.y - OFFSET, pos.z);

		super.setNoGravity(true);
		super.setInvulnerable(true);
		super.setInvisible(true);
		super.setCustomName(Text.literal("FSit Seat"));

		super.setRadius(.0f);
		super.setDuration(Integer.MAX_VALUE);
		super.setWaitTime(0);
	}

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);
		if (this.mounted == null) this.mounted = passenger;
	}

	@Override
	public void tick() {
		if (this.mounted != null) {
			final BlockPos blockPos = super.getBlockPos();

			if (!super.hasPassenger(this.mounted) || !FSitMod.isInPose(this.mounted.getUuid(), PlayerPose.SIT) || super.getWorld().getBlockState(blockPos).isAir()) {
				super.detach();
				super.discard();
			}
		}
	}
}
