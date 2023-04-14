package dev.rvbsm.fsit.config.option;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockSetOption extends Option<List<String>> {

	public BlockSetOption(String key, List<String> defaultValue) {
		super(key, defaultValue);
	}

	public Set<Block> getBlocks() {
		return super.getValue().stream()
						.map(Identifier::new)
						.map(Registries.BLOCK::get)
						.filter(block -> !(block instanceof AirBlock))
						.collect(Collectors.toSet());
	}
}
