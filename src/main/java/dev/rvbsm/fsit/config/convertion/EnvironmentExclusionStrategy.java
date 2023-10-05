package dev.rvbsm.fsit.config.convertion;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class EnvironmentExclusionStrategy implements ExclusionStrategy {

	private static final EnvType ENV_TYPE = FabricLoader.getInstance().getEnvironmentType();

	@Override
	public boolean shouldSkipField(FieldAttributes fieldAttributes) {
		final String fieldName = fieldAttributes.getName();
		return fieldName.endsWith("Client") && ENV_TYPE != EnvType.CLIENT || fieldName.endsWith("Server") && ENV_TYPE != EnvType.SERVER;
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}
}
