package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.entity.SeatEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin {

	@Shadow
	private static void moveEntity(Direction direction, Entity entity, double distance, Direction movementDirection) {
	}

	@Redirect(method = "canMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isOnGround()Z"))
	private static boolean canMoveEntity$isOnGround(Entity entity) {
		return entity.isOnGround() || entity.getVehicle() instanceof SeatEntity;
	}

	@Redirect(method = "moveEntitiesInHoneyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/PistonBlockEntity;moveEntity(Lnet/minecraft/util/math/Direction;Lnet/minecraft/entity/Entity;DLnet/minecraft/util/math/Direction;)V"))
	private static void moveEntitiesInHoneyBlock$moveEntity(Direction direction, Entity entity, double distance, Direction movementDirection) {
		if (entity.getVehicle() instanceof SeatEntity seat)
			PistonBlockEntityMixin.moveEntity(direction, seat, distance, movementDirection);
		else PistonBlockEntityMixin.moveEntity(direction, entity, distance, movementDirection);
	}
}
