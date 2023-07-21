package dev.rvbsm.fsit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class BlockedUUIDList {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final Path path;
	private final Set<UUID> list = new HashSet<>();

	public BlockedUUIDList(String filename) {
		this.path = Path.of(filename + ".json");

		if (!Files.exists(this.path)) this.save();
	}

	public void add(UUID uuid) {
		this.list.add(uuid);

		this.save();
	}

	public void remove(UUID uuid) {
		this.list.remove(uuid);

		this.save();
	}

	public boolean contains(UUID uuid) {
		return this.list.contains(uuid);
	}

	private void clear() {
		this.list.clear();
	}

	private void save() {
		try (var writer = Files.newBufferedWriter(this.path)) {
			GSON.toJson(this.list, writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void load() {
		try (var reader = Files.newBufferedReader(this.path)) {
			this.clear();
			final Set<?> list = GSON.fromJson(reader, Set.class);
			if (list != null) for (var obj : list)
				if (obj instanceof String str) this.add(UUID.fromString(str));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
