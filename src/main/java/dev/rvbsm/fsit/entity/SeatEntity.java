package dev.rvbsm.fsit.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class SeatEntity extends AreaEffectCloudEntity {

	private boolean mounted = false;

	public SeatEntity(World world, double x, double y, double z) {
		super(world, x, y - .5f, z);

		super.setNoGravity(true);
		super.setInvulnerable(true);
		super.setInvisible(true);
		super.setCustomName(Text.literal("seat"));

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
	}
}
