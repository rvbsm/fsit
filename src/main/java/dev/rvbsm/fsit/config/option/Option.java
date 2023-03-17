package dev.rvbsm.fsit.config.option;

import dev.rvbsm.fsit.FSitMod;

public class Option<T> {
	private final String key, translationKey;
	private T value;

	public Option(String key, T value) {
		this.key = key;
		this.translationKey = FSitMod.getTranslationKey("option", key);
		this.value = value;
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
}
