package dev.rvbsm.fsit.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SeatEntity extends AreaEffectCloudEntity {

	private static final double OFFSET = .5d;
	private Entity mounted;
	private boolean prevTickNoAir = false;

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
	public void move(MovementType movementType, Vec3d movement) {
		super.move(movementType, movement);

		if (this.mounted instanceof ServerPlayerEntity player)
			player.networkHandler.sendPacket(new EntityPositionS2CPacket(this));
	}

	@Override
	protected void addPassenger(Entity passenger) {
		if (passenger.isPlayer()) {
			super.addPassenger(passenger);
			if (this.mounted == null) this.mounted = passenger;
		}
	}

	@Override
	public void tick() {
		if (this.mounted != null) {
			if (!super.hasPassenger(this.mounted) || !((PoseHandler) this.mounted).isInPose(PlayerPose.SIT))
				this.detachAndDiscard();
			else if (super.getWorld().getBlockState(super.getBlockPos()).isAir()) {
				if (this.prevTickNoAir) this.detachAndDiscard();
				else this.prevTickNoAir = true;
			} else this.prevTickNoAir = false;
		}
	}

	private void detachAndDiscard() {
		super.detach();
		super.discard();
	}
}
