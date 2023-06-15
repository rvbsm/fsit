package dev.rvbsm.fsit.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class SeatEntity extends AreaEffectCloudEntity {

	private static final double offset = .5d;
	private boolean mounted = false;

	public SeatEntity(World world, @NotNull Vec3d pos) {
		super(world, pos.x, pos.y - offset, pos.z);

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
		this.mounted = true;
	}

	@Override
	public void tick() {
		if (this.mounted) {
			final BlockPos blockPos = super.getBlockPos();
			if (!super.hasPassengers() || super.getWorld().getBlockState(blockPos).isAir()) this.discard();
		}
	}
}
