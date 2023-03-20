package dev.rvbsm.fsit.config.option;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TagKeyBlockSetOption extends Option<List<String>> {

	public TagKeyBlockSetOption(String key, List<String> defaultValue) {
		super(key, defaultValue);
	}

	public Set<TagKey<Block>> getTagKeySet() {
		return super.getValue().stream()
						.map(id -> TagKey.of(RegistryKeys.BLOCK, new Identifier(id)))
						.collect(Collectors.toSet());
	}
}
