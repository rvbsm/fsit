package dev.rvbsm.fsit.config.convertion;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class IdentifierTypeAdapter extends TypeAdapter<Identifier> {

	@Override
	public void write(JsonWriter writer, Identifier id) throws IOException {
		if (id != null) writer.value(id.toString());
		else writer.nullValue();
	}

	@Override
	public Identifier read(JsonReader reader) throws IOException {
		if (reader != null) return new Identifier(reader.nextString());
		else return null;
	}
}
