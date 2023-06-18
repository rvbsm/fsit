package dev.rvbsm.fsit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class PlayerBlockList {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final File file;
	private final Set<UUID> blocklist = new HashSet<>();

	public PlayerBlockList(String filename) {
		this.file = new File(filename + ".json");
	}

	public void add(UUID uuid) {
		this.blocklist.add(uuid);

		this.save();
	}

	public void remove(UUID uuid) {
		this.blocklist.remove(uuid);

		this.save();
	}

	public boolean contains(UUID uuid) {
		return this.blocklist.contains(uuid);
	}

	private void save() {
		final JsonArray jsonArray = new JsonArray();
		blocklist.stream().map(UUID::toString).forEach(jsonArray::add);
		try (BufferedWriter writer = Files.newBufferedWriter(this.file.toPath(), StandardCharsets.UTF_8)) {
			GSON.toJson(jsonArray, writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void load() {
		if (!this.file.exists()) return;

		try (BufferedReader reader = Files.newBufferedReader(this.file.toPath(), StandardCharsets.UTF_8)) {
			final JsonArray jsonArray = GSON.fromJson(reader, JsonArray.class);
			this.blocklist.clear();
			jsonArray.asList().stream().map(JsonElement::getAsString).map(UUID::fromString).forEach(blocklist::add);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
