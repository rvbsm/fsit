package dev.rvbsm.fsit.config.option;

import dev.rvbsm.fsit.FSitMod;
import org.jetbrains.annotations.NotNull;

public class Option<T> {
	private final String key, translationKey;
	private final T defaultValue;
	private T value;

	public Option(@NotNull String key, T defaultValue) {
		this.key = key;
		this.translationKey = FSitMod.getTranslationKey("option", key.substring(key.lastIndexOf("\\.") + 1));
		this.value = defaultValue;
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return this.key;
	}

	public String getTranslationKey() {
		return this.translationKey;
	}

	public T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public T getDefaultValue() {
		return this.defaultValue;
	}
}
