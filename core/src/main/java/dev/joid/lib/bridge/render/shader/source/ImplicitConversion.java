package dev.joid.lib.bridge.render.shader.source;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImplicitConversion {

	private static final Pattern TOKEN       = Pattern.compile("#[^\\n]*|[A-Za-z_]\\w*|(?:\\d+\\.\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?[fF]?|\\d+[eE][+-]?\\d+[fF]?|0[xX][0-9a-fA-F]+[uU]?|\\d+[uU]?|<=|>=|==|!=|&&|\\|\\||\\+=|-=|\\*=|/=|\\+\\+|--|\\S");
	private static final Pattern INTEGER     = Pattern.compile("\\d+");
	private static final Pattern INTEGRAL    = Pattern.compile("int|uint|bool|[iub]vec[234]");
	private static final Pattern OPERATOR    = Pattern.compile("[-+*/%<>=?:]|<=|>=|==|!=|\\+=|-=|\\*=|/=");
	private static final Pattern DECLARATION = Pattern.compile("\\b(?:int|uint|bool|[iub]vec[234])\\s+(\\w+)");

	public static @NonNull String apply(final @NonNull String body, final @NonNull List<@NonNull ShaderVariable> variables) {
		final Set<String> integralSet = new HashSet<>();
		for (final ShaderVariable variable : variables) {
			if (ImplicitConversion.INTEGRAL.matcher(variable.getType()).matches()) {
				integralSet.add(variable.getName());
			}
		}

		final Matcher declaration = ImplicitConversion.DECLARATION.matcher(body);
		while (declaration.find()) {
			integralSet.add(declaration.group(1));
		}

		final List<String> tokenList = new ArrayList<>();
		final List<Integer> endList = new ArrayList<>();
		final Matcher token = ImplicitConversion.TOKEN.matcher(body);
		while (token.find()) {
			tokenList.add(token.group());
			endList.add(token.end());
		}

		final StringBuilder converted = new StringBuilder(body);
		final Deque<Boolean> integralScopes = new ArrayDeque<>();
		int inserted = 0;
		for (int i = 0; i < tokenList.size(); i++) {
			final String current = tokenList.get(i);
			if ("(".equals(current) || "[".equals(current)) {
				final String callee = i > 0 ? tokenList.get(i - 1) : "";
				integralScopes.push("[".equals(current) || ImplicitConversion.INTEGRAL.matcher(callee).matches() || integralSet.contains(callee));
			} else if ((")".equals(current) || "]".equals(current)) && !integralScopes.isEmpty()) {
				integralScopes.pop();
			} else if (ImplicitConversion.INTEGER.matcher(current).matches() && !ImplicitConversion.isIntegral(tokenList, i, integralSet, integralScopes)) {
				converted.insert(endList.get(i) + inserted, ".0");
				inserted += 2;
			}
		}
		return converted.toString();
	}

	private static boolean isIntegral(final List<String> tokenList, final int index, final Set<String> integralSet, final Deque<Boolean> integralScopes) {
		if (!integralScopes.isEmpty() && integralScopes.peek()) {
			return true;
		}

		int previous = index - 1;
		if (previous >= 0 && ("-".equals(tokenList.get(previous)) || "+".equals(tokenList.get(previous))) && (previous == 0 || !ImplicitConversion.isOperand(tokenList.get(previous - 1)))) {
			previous--;
		}

		if (previous >= 0 && "case".equals(tokenList.get(previous))) {
			return true;
		}

		if (previous >= 1 && ImplicitConversion.OPERATOR.matcher(tokenList.get(previous)).matches() && integralSet.contains(tokenList.get(previous - 1))) {
			return true;
		}
		return index + 2 < tokenList.size() && ImplicitConversion.OPERATOR.matcher(tokenList.get(index + 1)).matches() && integralSet.contains(tokenList.get(index + 2));
	}

	private static boolean isOperand(final String token) {
		return ")".equals(token) || "]".equals(token) || Character.isLetterOrDigit(token.charAt(0)) || token.charAt(0) == '_' || token.charAt(0) == '.';
	}

}