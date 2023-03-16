package dev.rvbsm.fsit;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class FSitConfig {

	private static final Path configDir = FabricLoader.getInstance().getConfigDir();
	private static final Path configPath = FSitConfig.getConfigPath();
	private static final TomlWriter configWriter = new TomlWriter.Builder().indentValuesBy(2).build();
	private static final String minAngleKey = "min_angle";
	private static final String shiftDelayKey = "shift_delay";
	private static final String sittableBlocksKey = "sittable_blocks";
	private static final Double minAngleDefault = 66.6D;
	private static final Long shiftDelayDefault = 600L;
	private static final List<String> sittableBlocksDefault = List.of("#slabs", "#stairs", "#logs");
	private static final Map<String, Object> defaultConfig = Map.ofEntries(
					Map.entry(minAngleKey, minAngleDefault),
					Map.entry(shiftDelayKey, shiftDelayDefault),
					Map.entry(sittableBlocksKey, sittableBlocksDefault)
	);
	private final Toml config;
	public Double minAngle;
	public Long shiftDelay;
	public List<Block> sittableBlocks;
	public List<TagKey<Block>> sittableBlockTags;

	FSitConfig() {
		this.config = this.load();

		try {
			minAngle = this.getMinAngle();
			shiftDelay = this.getShiftDelay();
			sittableBlocks = this.getSittableBlocks();
			sittableBlockTags = this.getSittableBlockTags();
		} catch (ClassCastException e) {
			throw new ClassCastException("Illegal configuration values. Loading defaults");
		}
	}

	private static @NotNull Path getConfigPath() {
		return configDir.resolve(FSitMod.getModId().concat(".toml"));
	}

	private Toml load() {
		if (Files.exists(configPath)) return new Toml().read(configPath.toFile());
		else return this.create();
	}

	private Toml create() {
		try {
			configWriter.write(defaultConfig, configPath.toFile());
			return load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void save() {
		try {
			Files.createDirectories(configDir);
			configWriter.write(config, configPath.toFile());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Double getMinAngle() {
		return config.getDouble(minAngleKey, minAngleDefault);
	}

	private Long getShiftDelay() {
		return config.getLong(shiftDelayKey, shiftDelayDefault);
	}

	private List<Block> getSittableBlocks() {
		return config.getList(sittableBlocksKey, sittableBlocksDefault).stream()
						.filter(block -> !block.startsWith("#"))
						.map(Identifier::new)
						.map(Registries.BLOCK::get)
						.filter(Registries.BLOCK.stream().toList()::contains)
						.filter(block -> !(block instanceof AirBlock))
						.toList();
	}

	private List<TagKey<Block>> getSittableBlockTags() {
		return config.getList(sittableBlocksKey, sittableBlocksDefault).stream()
						.filter(tag -> tag.startsWith("#"))
						.map(tag -> tag.substring(1))
						.map(id -> TagKey.of(RegistryKeys.BLOCK, new Identifier(id)))
						// ? Filter of non-existing tags? (Pre-existing Set with all tags?)
						.toList();
	}
}
