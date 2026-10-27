package dev.joid.tool.docgen.page;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import dev.joid.tool.docgen.model.ApiMessage;
import dev.joid.tool.docgen.model.Audience;
import dev.joid.tool.docgen.report.Coverage;
import dev.joid.tool.docgen.scope.Scope;

public final class ErrorsPage {

	private static final String PATH = "documentation/reference/errors.md";

	private static final Set<String>           BUILD_FAMILIES = new HashSet<>(Arrays.asList("bridges", "pipeline", "backend-testing"));
	private static final Map<String, Audience> OVERRIDES      = ErrorsPage.overrides();
	private static final Map<String, String>   USE_FAMILIES   = new LinkedHashMap<>();
	private static final Set<String>           STOP_WORDS     = new HashSet<>(Arrays.asList("a", "an", "the", "of", "is", "are", "to", "in", "on", "for", "and", "or", "its", "it", "be", "by", "with", "at", "as", "you", "your", "this", "that"));

	static {
		ErrorsPage.USE_FAMILIES.put("bridges", "shaders");
		ErrorsPage.USE_FAMILIES.put("pipeline", "shaders");
		ErrorsPage.USE_FAMILIES.put("base-glfw", "backends");
		ErrorsPage.USE_FAMILIES.put("base-openal", "backends");
		ErrorsPage.USE_FAMILIES.put("base-opengl", "backends");
		ErrorsPage.USE_FAMILIES.put("lwjgl2", "backends");
		ErrorsPage.USE_FAMILIES.put("lwjgl3", "backends");
		ErrorsPage.USE_FAMILIES.put("vulkan", "backends");
		ErrorsPage.USE_FAMILIES.put("backend-testing", "testing");
		ErrorsPage.USE_FAMILIES.put("tools", "text");
	}

	private final Map<String, List<ApiMessage>> messages;
	private final Map<String, String>           texts;
	private final Map<String, Audience>         audiences;
	private final Map<String, String>           families;
	private final Map<String, String>           anchors;
	private final Map<String, String>           titles;

	private ErrorsPage() {
		this.messages  = new LinkedHashMap<>();
		this.texts     = new LinkedHashMap<>();
		this.audiences = new LinkedHashMap<>();
		this.families  = new LinkedHashMap<>();
		this.anchors   = new LinkedHashMap<>();
		this.titles    = new LinkedHashMap<>();
	}

	public static ErrorsPage create(final List<ApiMessage> messages, final File descriptions, final Scope scope) throws IOException {
		final ErrorsPage page = new ErrorsPage();
		messages.stream().filter(ApiMessage::isWarning).forEach(message -> page.messages.computeIfAbsent(message.getKey(), key -> new ArrayList<>()).add(message));
		messages.stream().filter(message -> !message.isWarning()).forEach(message -> page.messages.computeIfAbsent(message.getKey(), key -> new ArrayList<>()).add(message));

		final Map<String, List<String>> bases = new LinkedHashMap<>();
		final Map<String, List<String>> titles = new LinkedHashMap<>();
		for (final Map.Entry<String, List<ApiMessage>> entry : page.messages.entrySet()) {
			final ApiMessage first = entry.getValue().get(0);
			final String family = ErrorsPage.family(scope.familyOfMessage(first), first);
			page.audiences.put(entry.getKey(), ErrorsPage.audience(entry.getKey(), family));
			page.families.put(entry.getKey(), family);
			bases.computeIfAbsent(ErrorsPage.base(page.wordsOf(entry.getKey())), base -> new ArrayList<>()).add(entry.getKey());
			titles.computeIfAbsent(ErrorsPage.title(first), title -> new ArrayList<>()).add(entry.getKey());
		}

		final Set<String> taken = new HashSet<>();
		for (final List<String> group : bases.values()) {
			for (final String key : group) {
				page.anchors.put(key, ErrorsPage.anchor(page.wordsOf(key), group.stream().filter(other -> !other.equals(key)).map(page::wordsOf).collect(Collectors.toList()), taken));
			}
		}

		for (final Map.Entry<String, List<String>> entry : titles.entrySet()) {
			for (final String key : entry.getValue()) {
				page.titles.put(key, entry.getValue().size() == 1 ? entry.getKey() : ErrorsPage.title(page.textOf(key), entry.getValue().stream().filter(other -> !other.equals(key)).map(page::textOf).collect(Collectors.toList())));
			}
		}

		if (descriptions.isFile()) {
			String key = null;
			final StringBuilder text = new StringBuilder();
			for (final String line : Files.readAllLines(descriptions.toPath(), StandardCharsets.UTF_8)) {
				if (line.startsWith("## ")) {
					page.store(key, text);
					key = line.substring(3).trim();
					continue;
				}
				text.append(line).append('\n');
			}
			page.store(key, text);
		}
		return page;
	}

	public String write(final Audience audience, final Map<String, String> labels, final PageRegistry registry, final LinkChecker links, final Coverage coverage) {
		final String path = audience.getSection() + "/reference/errors.md";
		final StringBuilder out = new StringBuilder("---\ntitle: \"Warnings and errors\"\n");
		out.append(audience == Audience.USER ? "description: \"Every warning and every exception message that JOID gives while you build interfaces, with its cause and its fix.\"\n" : "description: \"Every warning and every exception message that a backend author meets, with its cause and its fix.\"\n");
		out.append("genre: reference\ntopic: ").append(audience.getSection()).append("\nsidebar:\n  order: 1\ntableOfContents:\n  maxHeadingLevel: 3\n---\n\n<!-- Generated by tool-docgen from the sources and ").append(ErrorsPage.PATH).append(". Do not edit. -->\n\n");
		out.append(audience == Audience.USER ? "Each entry gives the exact message, the method that emits it when you can call that method, its cause and its fix.\n" : "Each entry gives the exact message, the method that emits it, its cause and its fix.\n");
		out.append("`…` stands for a part of the message that JOID fills at run time.\n");
		if (audience == Audience.CONTRIBUTOR) {
			out.append("The messages that any application can meet are on [Warnings and errors](/use/reference/errors/).\n");
		}
		out.append('\n');

		final Map<String, List<String>> sections = new LinkedHashMap<>();
		labels.keySet().forEach(family -> sections.put(family, new ArrayList<>()));
		for (final String key : this.messages.keySet()) {
			if (this.audiences.get(key) == audience) {
				final String family = this.families.get(key);
				sections.computeIfAbsent(audience == Audience.USER ? ErrorsPage.USE_FAMILIES.getOrDefault(family, family) : family, name -> new ArrayList<>()).add(key);
			}
		}

		for (final Map.Entry<String, List<String>> section : sections.entrySet()) {
			if (section.getValue().isEmpty()) {
				continue;
			}

			out.append("## ").append(labels.getOrDefault(section.getKey(), "Other")).append("\n\n");
			for (final String key : section.getValue()) {
				final ApiMessage first = this.messages.get(key).get(0);
				out.append("### ").append(ErrorsPage.markdown(this.titles.get(key))).append(" {#").append(this.anchors.get(key)).append("}\n\n");
				final boolean failure = "failure".equals(first.getType());
				if (first.getDetail() == null) {
					out.append("**").append(first.isWarning() ? "Warning" : failure ? "Failure" : "`" + first.getType() + "`").append(failure ? "** (exit code 1) · `" : "** · `").append(ErrorsPage.code(first.getText())).append("`\n\n");
				} else {
					out.append("**`").append(first.getType()).append("`** · one of the messages of the table below\n\n");
				}
				final List<String> origins = new ArrayList<>();
				for (final ApiMessage message : this.messages.get(key)) {
					final String origin = ErrorsPage.origin(message, audience, registry);
					if (origin != null && !origins.contains(origin)) {
						origins.add(origin);
					}
				}
				if (!origins.isEmpty()) {
					out.append("Emitted by ").append(String.join(", ", origins)).append(".\n\n");
				}

				final String text = this.texts.get(key);
				coverage.expect("errors", ErrorsPage.PATH, key, text != null);
				this.messages.get(key).stream().map(ApiMessage::getDetail).filter(Objects::nonNull).distinct().forEach(detail -> coverage.expect("errors", ErrorsPage.PATH, key + " · " + detail, text != null && text.contains("`" + ErrorsPage.code(detail) + "`")));
				out.append(text == null ? "<div class=\"joid-missing\">Missing cause and fix: add <code>## " + ErrorsPage.html(key) + "</code> to <code>" + ErrorsPage.PATH + "</code>.</div>" : links.resolve(audience, text, ErrorsPage.PATH + " (" + path + ")")).append("\n\n");
			}
		}
		return out.toString();
	}

	public void check(final Coverage coverage) {
		for (final String key : this.texts.keySet()) {
			if (!this.messages.containsKey(key)) {
				coverage.unknown(ErrorsPage.PATH, key);
			}
		}
	}

	public Map<String, List<ApiMessage>> getMessages() {
		return this.messages;
	}

	public Map<String, String> getAnchors() {
		return this.anchors;
	}

	public Map<String, Audience> getAudiences() {
		return this.audiences;
	}

	private void store(final String key, final StringBuilder text) {
		if (key != null && text.toString().trim().length() > 0) {
			this.texts.put(key, text.toString().trim());
		}
		text.setLength(0);
	}

	private static String origin(final ApiMessage message, final Audience audience, final PageRegistry registry) {
		final String origin = message.getOrigin();
		final String type = origin.replaceFirst("#.*$", "");
		final String method = origin.contains("#") ? origin.substring(origin.indexOf('#') + 1) : "";
		final StringBuilder name = new StringBuilder();
		for (final String part : type.split("\\.")) {
			if (name.length() > 0 || !part.isEmpty() && Character.isUpperCase(part.charAt(0))) {
				name.append(name.length() == 0 ? "" : ".").append(part);
			}
		}

		final String simple = name.substring(name.lastIndexOf(".") + 1);
		final String label = "`" + name + (method.isEmpty() || method.equals(simple) ? "" : "." + method) + "`";
		String candidate = type;
		while (candidate.contains(".") && Character.isUpperCase(candidate.charAt(candidate.lastIndexOf('.') + 1))) {
			final TypePage target = registry.linkable(audience, candidate);
			if (target != null) {
				final String anchor = candidate.equals(type) ? PageWriter.memberAnchors(target).get(method) : null;
				if (anchor == null && !method.isEmpty() && !method.equals(simple)) {
					return null;
				}
				return "[" + label + "](" + target.getUrl() + (anchor == null ? "" : "#" + anchor) + ")";
			}
			candidate = candidate.substring(0, candidate.lastIndexOf('.'));
		}
		return audience == Audience.USER ? null : label;
	}

	private static Audience audience(final String key, final String family) {
		for (final Map.Entry<String, Audience> override : ErrorsPage.OVERRIDES.entrySet()) {
			if (key.equals(override.getKey()) || !override.getKey().endsWith("…") && key.startsWith(override.getKey())) {
				return override.getValue();
			}
		}
		return ErrorsPage.BUILD_FAMILIES.contains(family) ? Audience.CONTRIBUTOR : Audience.USER;
	}

	private static Map<String, Audience> overrides() {
		final Map<String, Audience> overrides = new LinkedHashMap<>();
		try (InputStream input = ErrorsPage.class.getResourceAsStream("/errors-audience.txt")) {
			if (input == null) {
				return overrides;
			}

			for (final String line : new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.toList())) {
				final String trimmed = line.trim();
				if (trimmed.startsWith("use ")) {
					overrides.put(trimmed.substring(4).trim(), Audience.USER);
				} else if (trimmed.startsWith("build ")) {
					overrides.put(trimmed.substring(6).trim(), Audience.CONTRIBUTOR);
				}
			}
		} catch (final IOException exception) {
			throw new UncheckedIOException(exception);
		}
		return overrides;
	}

	private static String family(final String family, final ApiMessage message) {
		if (!"base".equals(family) && !"backend".equals(family) && !"test".equals(family) && !"other".equals(family)) {
			return family;
		}

		final String[] parts = message.getFile().split("/");
		if ("base".equals(parts[0])) {
			return "base-" + parts[1];
		}
		return "backend".equals(parts[0]) ? parts[1] : "tool".equals(parts[0]) && "testkit".equals(parts[1]) ? "testing" : "utilities";
	}

	private String textOf(final String key) {
		return this.messages.get(key).get(0).getText().replace("[JOID]", "").trim();
	}

	private List<String> wordsOf(final String key) {
		final ApiMessage message = this.messages.get(key).get(0);
		final List<String> words = new ArrayList<>();
		for (final String word : message.getText().replace("[JOID]", " ").toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
			if (!word.isEmpty() && !ErrorsPage.STOP_WORDS.contains(word)) {
				words.add(word);
			}
		}

		if (words.isEmpty()) {
			words.add(message.getType().toLowerCase(Locale.ROOT));
		}
		return words;
	}

	private static int length(final List<String> words) {
		for (int count = 1; count <= words.size(); count++) {
			if (count >= Math.min(4, words.size()) && String.join("-", words.subList(0, count)).length() >= 16) {
				return count;
			}
		}
		return words.size();
	}

	private static String base(final List<String> words) {
		return String.join("-", words.subList(0, ErrorsPage.length(words)));
	}

	private static String anchor(final List<String> words, final List<List<String>> others, final Set<String> taken) {
		final int length = ErrorsPage.length(words);
		String anchor = ErrorsPage.base(words);
		int unique = length;
		while (unique < words.size() && ErrorsPage.shared(words.subList(0, unique + 1), others)) {
			unique++;
		}
		if (!others.isEmpty() && unique < words.size()) {
			anchor += (unique > length ? "-" + words.get(unique - 1) : "") + "-" + words.get(unique);
		}

		final String base = anchor;
		int index = 2;
		while (taken.contains(anchor)) {
			anchor = base + "-" + index++;
		}
		taken.add(anchor);
		return anchor;
	}

	private static boolean shared(final List<String> prefix, final List<List<String>> others) {
		return others.stream().anyMatch(other -> other.size() >= prefix.size() && other.subList(0, prefix.size()).equals(prefix));
	}

	private static String title(final ApiMessage message) {
		String title = message.getText().replace("[JOID]", "").trim();
		if (!title.matches(".*[A-Za-z].*")) {
			return message.isWarning() ? "A warning that carries the message of an exception" : message.getType() + " that carries the message of its cause";
		}

		for (final String separator : new String[] {". ", ": ", ", call ", ", use ", ", set "}) {
			final int index = title.indexOf(separator);
			if (index > 12) {
				title = title.substring(0, index);
			}
		}

		if (title.length() > 72) {
			title = title.substring(0, title.lastIndexOf(' ', 72)) + " …";
		}
		return title.endsWith(".") ? title.substring(0, title.length() - 1) : title;
	}

	private static String title(final String text, final List<String> others) {
		int differ = 0;
		for (final String other : others) {
			int index = 0;
			while (index < Math.min(text.length(), other.length()) && text.charAt(index) == other.charAt(index)) {
				index++;
			}
			differ = Math.max(differ, index);
		}

		final int cut = text.indexOf(", ", differ);
		final String clause = cut > 0 ? text.substring(0, cut) : text;
		final String title = clause.chars().filter(character -> character == '(').count() == clause.chars().filter(character -> character == ')').count() ? clause : text;
		return title.endsWith(".") ? title.substring(0, title.length() - 1) : title;
	}

	private static String code(final String text) {
		return text.replace("`", "'");
	}

	private static String html(final String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String markdown(final String text) {
		return text.replaceAll("([\\\\`*_\\[\\]<>#|{}])", "\\\\$1");
	}

}