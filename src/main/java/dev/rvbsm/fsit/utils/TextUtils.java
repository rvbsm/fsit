package dev.rvbsm.fsit.utils;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {

	private static final Pattern SECTION_PATTERN = Pattern.compile("^\\[[A-Za-z0-9_+-]+]", Pattern.MULTILINE);
	private static final Pattern ENTRY_PATTERN = Pattern.compile("^[A-Za-z0-9_+-]+", Pattern.MULTILINE);
	private static final Pattern TRUE_PATTERN = Pattern.compile("true");
	private static final Pattern FALSE_PATTERN = Pattern.compile("false");
	private static final Pattern INTEGER_PATTERN = Pattern.compile("(?<!§)(0|[1-9]([0-9]+.?[0-9])?)");
	private static final Pattern STRING_PATTERN = Pattern.compile("\".*?\"");

	private static String colorizePattern(String input, Pattern pattern, String color) {
		final Matcher matcher = pattern.matcher(input);
		final StringBuilder builder = new StringBuilder();

		while (matcher.find()) {
			matcher.appendReplacement(builder, "§" + color + matcher.group() + "§r");
		}
		matcher.appendTail(builder);

		return builder.toString();
	}

	private static Text convertToModern(String input) {
		final MutableText text = Text.empty();
		final StringBuilder segment = new StringBuilder();

		Formatting currentFormatting = Formatting.RESET;
		for (int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);

			if (c == '§' && i + 1 < input.length()) {
				Formatting nextFormatting = Formatting.byCode(input.charAt(++i));

				if (nextFormatting != null) {
					if (!segment.isEmpty()) {
						text.append(Text.literal(segment.toString()).formatted(currentFormatting));
						segment.setLength(0);
					}
					currentFormatting = nextFormatting;
				}
			} else segment.append(c);
		}
		if (!segment.isEmpty()) text.append(Text.literal(segment.toString()).formatted(currentFormatting));

		return text;
	}

	public static Text getColorizedConfig() {
		final String toml = FSitMod.getConfigManager().tomlify().trim();
		String colorizedToml = colorizePattern(toml, SECTION_PATTERN, "6");
		colorizedToml = colorizePattern(colorizedToml, ENTRY_PATTERN, "b");
		colorizedToml = colorizePattern(colorizedToml, TRUE_PATTERN, "a");
		colorizedToml = colorizePattern(colorizedToml, FALSE_PATTERN, "c");
		colorizedToml = colorizePattern(colorizedToml, INTEGER_PATTERN, "6");
		colorizedToml = colorizePattern(colorizedToml, STRING_PATTERN, "a");

		return convertToModern(colorizedToml);
	}

}
