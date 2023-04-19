package dev.rvbsm.fsit.config.option;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockSetOption extends Option<List<String>> implements Identifiable {

	public BlockSetOption(String key, List<String> defaultValue) {
		super(key, defaultValue);
	}

	@Override
	public Set<Identifier> getIds() {
		return super.getValue().stream().map(Identifier::new).collect(Collectors.toUnmodifiableSet());
	}
}
