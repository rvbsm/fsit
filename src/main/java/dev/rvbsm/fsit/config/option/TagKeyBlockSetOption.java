package dev.rvbsm.fsit.config.option;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.stream.Collectors;

public class TagKeyBlockSetOption extends SimpleOption<Set<String>> {

	public TagKeyBlockSetOption(String key, Set<String> value) {
		super(key, value);
	}

	public Set<TagKey<Block>> getTagKeySet() {
		final Set<String> tagNames = super.getValue();

		return tagNames.stream()
						.map(id -> TagKey.of(RegistryKeys.BLOCK, new Identifier(id)))
						.collect(Collectors.toSet());
	}
}
