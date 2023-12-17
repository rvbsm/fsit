package dev.rvbsm.fsit.text;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TextUtils {

	public static Text convertToModern(String input) {
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

	public static String colorizeValues(String input) {
		input = FormattingPattern.TRUE.colorize(input);
		input = FormattingPattern.FALSE.colorize(input);
		input = FormattingPattern.INTEGER.colorize(input);
		return FormattingPattern.STRING.colorize(input);
	}

	public static String colorizeChatEntries(String input) {
		input = FormattingPattern.STRING_NO_QUOTES.colorize(input);
		return colorizeValues(input);
	}

	public static String colorizeTomlEntries(String input) {
		input = FormattingPattern.ENTRY.colorize(input);
		return colorizeValues(input);
	}

	public static Text getColorizedConfig() {
		final String toml = FSitMod.getConfigManager().tomlify().trim();
		String colorizedToml = FormattingPattern.SECTION.colorize(toml);
		colorizedToml = colorizeTomlEntries(colorizedToml);

		return convertToModern(colorizedToml);
	}
}
