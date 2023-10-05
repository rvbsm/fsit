package dev.rvbsm.fsit.config.convertion;

import com.google.common.base.CaseFormat;
import com.google.gson.FieldNamingStrategy;

import java.lang.reflect.Field;

public class EnvStripLowerUnderscoreNamingStrategy implements FieldNamingStrategy {

	private static final int SUFFIX_LENGTH = "Client".length();

	@Override
	public String translateName(Field f) {
		String name = f.getName();
		if (name.endsWith("Client") || name.endsWith("Server")) name = name.substring(0, name.length() - SUFFIX_LENGTH);
		return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
	}
}
