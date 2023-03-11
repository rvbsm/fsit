package dev.rvbsm.fsit.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SeatEntity extends AreaEffectCloudEntity {

	private boolean mounted = false;

	public SeatEntity(World world, double x, double y, double z) {
		super(world, x, y - .5f, z);

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
	protected boolean canAddPassenger(Entity passenger) {
		return true;
	}

	@Override
	public void tick() {
		if (this.mounted && super.isAlive() && !super.hasPassengers()) super.discard();

		// ! is there a better way to check if the block was broken?
		final BlockPos blockPos = this.getBlockPos();
		if (this.world.getBlockState(blockPos).isAir()) super.discard();
	}
}
