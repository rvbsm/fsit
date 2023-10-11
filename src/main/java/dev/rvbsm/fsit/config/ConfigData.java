package dev.rvbsm.fsit.config;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public final class ConfigData {

	public static final ConfigData DEFAULT = new ConfigData();
	public static final Map<String, String> MIGRATED_FIELDS = Map.of(
					"misc.riding", "riding",
					"misc.commands", "commands"
	);

	private final int configVersion = 4;

	private final SneakTable sneak = new SneakTable();
	private final SittableTable sittable = new SittableTable();
	private final RidingTable riding = new RidingTable();
	private final CommandsTable commandsServer = new CommandsTable();

	@Getter
	@Setter
	public static class SneakTable {
		private boolean enabled = true;
		private double angle = 66d;
		private int delay = 600;
	}

	@Getter
	@Setter
	public static class SittableTable {
		private boolean enabled = true;
		private int radius = 2;
		private Set<Identifier> blocks = Set.of();
		private Set<Identifier> tags = Set.of(BlockTags.SLABS.id(), BlockTags.STAIRS.id(), BlockTags.LOGS.id());

		public List<String> getBlocksString() {
			return this.blocks.stream().map(Identifier::toString).toList();
		}

		public void setBlocksString(List<String> blocks) {
			this.blocks = blocks.stream().map(Identifier::new).collect(Collectors.toUnmodifiableSet());
		}

		public List<String> getTagsString() {
			return this.tags.stream().map(Identifier::toString).toList();
		}

		public void setTagsString(List<String> tags) {
			this.tags = tags.stream().map(Identifier::new).collect(Collectors.toUnmodifiableSet());
		}
	}

	@Getter
	@Setter
	public static class RidingTable {
		private boolean enabled = false;
		private int radius = 3;
	}

	@Getter
	@Setter
	public static class CommandsTable {
		private boolean enabled = true;
		private String sit = "sit";
		private String crawl = "crawl";
	}
}
