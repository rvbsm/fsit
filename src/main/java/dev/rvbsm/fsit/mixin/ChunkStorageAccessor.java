package dev.rvbsm.fsit.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if <1.21-rc.1
@Mixin(net.minecraft.server.world.ThreadedAnvilChunkStorage.class)
//? if >=1.21-rc.1
/*@Mixin(net.minecraft.server.world.ServerChunkLoadingManager.class)*/
public interface ChunkStorageAccessor {
    @Accessor
    Int2ObjectMap<EntityTrackerAccessor> getEntityTrackers();

    //? if <1.21-rc.1
    @Mixin(targets = "net/minecraft/server/world/ThreadedAnvilChunkStorage$EntityTracker")
    //? if >=1.21-rc.1
    /*@Mixin(targets = "net/minecraft/server/world/ServerChunkLoadingManager$EntityTracker")*/
    interface EntityTrackerAccessor {
        @Accessor
        EntityTrackerEntry getEntry();
    }
}
