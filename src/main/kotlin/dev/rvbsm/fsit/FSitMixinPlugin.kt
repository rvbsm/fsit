package dev.rvbsm.fsit

import dev.rvbsm.fsit.mixin.annotation.VersionedMixin
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.version.VersionPredicate
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.slf4j.LoggerFactory
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import org.spongepowered.asm.service.MixinService
import org.spongepowered.asm.util.Annotations

private val minecraftVersion = FabricLoader.getInstance().getModContainer("minecraft").get().metadata.version

class FSitMixinPlugin : IMixinConfigPlugin {
    private val logger = LoggerFactory.getLogger(FSitMixinPlugin::class.java)

    override fun onLoad(mixinPackage: String?) = Unit

    override fun getRefMapperConfig() = null

    /**
     * Checks if a mixin should be applied based on the [VersionedMixin] annotation.
     *
     * The mixin is only applied if the annotation's version predicate matches the current Minecraft version.
     * Otherwise, it allows the mixin application by default.
     *
     * @throws RuntimeException If there's an error processing the annotation.
     */
    override fun shouldApplyMixin(targetClassName: String?, mixinClassName: String?): Boolean {
        try {
            val annotationNodes =
                MixinService.getService().bytecodeProvider.getClassNode(mixinClassName).visibleAnnotations
                    ?: return true

            return annotationNodes.find { it.desc == Type.getDescriptor(VersionedMixin::class.java) }
                ?.let { Annotations.getValue<String>(it, "value") }
                ?.let { VersionPredicate.parse(it) }
                ?.test(minecraftVersion) ?: true
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun acceptTargets(myTargets: MutableSet<String>?, otherTargets: MutableSet<String>?) = Unit

    override fun getMixins() = null

    override fun preApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) = Unit

    override fun postApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) = Unit
}
