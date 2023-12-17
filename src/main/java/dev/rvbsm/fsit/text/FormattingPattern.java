package dev.rvbsm.fsit.text;

import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum FormattingPattern {
	SECTION(Pattern.compile("^\\[[\\w_+-]+]", Pattern.MULTILINE), Formatting.GOLD),
	ENTRY(Pattern.compile("^(?<!§)[\\w_+-]+", Pattern.MULTILINE), Formatting.AQUA),
	TRUE(Pattern.compile("(?<!§)true"), Formatting.GREEN),
	FALSE(Pattern.compile("(?<!§)false"), Formatting.RED),
	INTEGER(Pattern.compile("(?<!§)(0|[1-9]([0-9]+.?[0-9])?)"), Formatting.GOLD),
	STRING(Pattern.compile("\".*?\""), Formatting.GREEN),

	STRING_NO_QUOTES(Pattern.compile("(?<=➡)\\s.+"), Formatting.GREEN);

	private final Pattern pattern;
	private final Formatting formatting;

	FormattingPattern(Pattern pattern, Formatting formatting) {
		this.pattern = pattern;
		this.formatting = formatting;
	}

	public String colorize(String input) {
		final Matcher matcher = pattern.matcher(input);
		final StringBuilder builder = new StringBuilder();

		while (matcher.find()) {
			matcher.appendReplacement(builder, "§" + this.formatting.getCode() + matcher.group() + "§r");
		}
		matcher.appendTail(builder);

		return builder.toString();
	}
}
