package dev.rvbsm.fsit.mixin;

import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {
}
