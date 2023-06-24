package dev.rvbsm.fsit.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CrawlEntity extends ShulkerEntity {

	public CrawlEntity(World world, Vec3d pos) {
		super(EntityType.SHULKER, world);
		super.setPosition(pos);

		super.setNoGravity(true);
		super.setSilent(true);
		super.setInvulnerable(true);
		super.setInvisible(true);

		super.setAiDisabled(true);
	}
}
