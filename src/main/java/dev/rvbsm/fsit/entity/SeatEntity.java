package dev.rvbsm.fsit.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SeatEntity extends AreaEffectCloudEntity {

	private static final Map<Identifier, Set<Vec3d>> existingSeats = new HashMap<>();
	private static final double offset = .5d;
	private boolean mounted = false;

	public SeatEntity(World world, @NotNull Vec3d pos) {
		super(world, pos.x, pos.y - offset, pos.z);
		SeatEntity.addSeatAt(world, pos.add(0, -offset, 0));

		super.setNoGravity(true);
		super.setInvulnerable(true);
		super.setInvisible(true);
		super.setCustomName(Text.literal("FSit Seat"));

		super.setRadius(.0f);
		super.setDuration(Integer.MAX_VALUE);
		super.setWaitTime(0);
	}

	private static void addSeatAt(@NotNull World world, Vec3d pos) {
		final Identifier worldId = world.getRegistryKey().getValue();
		final Set<Vec3d> worldSeats = SeatEntity.existingSeats.getOrDefault(worldId, new LinkedHashSet<>());
		worldSeats.add(pos);
		SeatEntity.existingSeats.put(worldId, worldSeats);
	}

	public static boolean hasSeatAt(@NotNull World world, Vec3d pos) {
		final Identifier worldId = world.getRegistryKey().getValue();
		final Set<Vec3d> worldSeats = SeatEntity.existingSeats.getOrDefault(worldId, new LinkedHashSet<>());
		return worldSeats.contains(pos);
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
			if (!super.hasPassengers() || super.getWorld().getBlockState(blockPos).isAir()) this.discardSeat();
		}
	}

	private void discardSeat() {
		this.removeSeatAt(super.getWorld(), super.getPos());
		super.discard();
	}

	private void removeSeatAt(@NotNull World world, Vec3d pos) {
		final Identifier worldId = world.getRegistryKey().getValue();
		final Set<Vec3d> worldSeats = SeatEntity.existingSeats.getOrDefault(worldId, new LinkedHashSet<>());
		worldSeats.remove(pos);
		SeatEntity.existingSeats.put(worldId, worldSeats);
	}
}
