package dev.rvbsm.fsit.config.option;

import dev.rvbsm.fsit.FSitMod;

import java.util.List;

public class Option<T> {
	private final String key, translationKey;
	private final String group;
	private final T defaultValue;
	private T value;

	public Option(String group, String key, T defaultValue) {
		this.group = group;
		this.key = key;
		this.translationKey = FSitMod.getTranslationKey("option", key);
		this.value = defaultValue;
		this.defaultValue = defaultValue;
	}

	public List<String> getPath() {
		return List.of(this.group, this.key);
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
