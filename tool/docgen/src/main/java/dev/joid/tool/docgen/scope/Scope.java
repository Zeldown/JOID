package dev.joid.tool.docgen.scope;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.joid.tool.docgen.model.ApiMember;
import dev.joid.tool.docgen.model.ApiMessage;
import dev.joid.tool.docgen.model.ApiType;
import dev.joid.tool.docgen.model.Audience;
import dev.joid.tool.docgen.model.TypeNames;
import lombok.Getter;

@Getter
public final class Scope {

	private final boolean                            fallback;
	private final List<String>                       unclassified;
	private final Map<String, Audience>              types;
	private final Map<String, String>                families;
	private final Map<String, Map<String, Audience>> members;

	private Scope(final boolean fallback) {
		this.fallback     = fallback;
		this.unclassified = new ArrayList<>();
		this.types        = new HashMap<>();
		this.families     = new HashMap<>();
		this.members      = new HashMap<>();
	}

	public static Scope load(final File file) throws IOException {
		if (!file.isFile()) {
			System.err.println("[JOID] No scope file at " + file + ", the audience of every type is guessed from its package");
			return new Scope(true);
		}

		final Scope scope = new Scope(false);
		try (Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
			final JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
			for (final Map.Entry<String, JsonElement> entry : root.getAsJsonObject("types").entrySet()) {
				final JsonObject type = entry.getValue().getAsJsonObject();
				scope.types.put(entry.getKey(), Audience.parse(type.get("audience").getAsString()));
				if (type.has("family")) {
					scope.families.put(entry.getKey(), type.get("family").getAsString());
				}
			}

			if (root.has("members")) {
				for (final Map.Entry<String, JsonElement> entry : root.getAsJsonObject("members").entrySet()) {
					final int separator = entry.getKey().indexOf('#');
					final String type = entry.getKey().substring(0, separator);
					final String member = TypeNames.normalizeKey(entry.getKey().substring(separator + 1));
					scope.members.computeIfAbsent(type, key -> new HashMap<>()).put(member, Audience.parse(entry.getValue().getAsJsonObject().get("audience").getAsString()));
				}
			}
		}
		return scope;
	}

	public Audience audienceOf(final ApiType type) {
		final Audience audience = this.types.get(type.getFqcn());
		if (audience != null) {
			return audience;
		}

		final Audience guessed = Scope.guess(type);
		if (!this.fallback && guessed.isDocumented() && !this.unclassified.contains(type.getFqcn())) {
			this.unclassified.add(type.getFqcn());
		}
		return guessed;
	}

	public Audience audienceOf(final ApiType type, final ApiMember member) {
		final Map<String, Audience> exceptions = this.members.get(type.getFqcn());
		if (exceptions != null) {
			final Audience audience = exceptions.get(member.getKey());
			if (audience != null) {
				return audience;
			}
		}
		return this.audienceOf(type);
	}

	public String familyOf(final ApiType type) {
		final String family = this.families.get(type.getFqcn());
		return family != null ? family : Scope.packageFamily(type.getPackageName());
	}

	public String familyOfMessage(final ApiMessage message) {
		for (final String fqcn : Scope.enclosing(message.getOrigin())) {
			final String family = this.families.get(fqcn);
			if (family != null) {
				return family;
			}
		}

		final List<String> parts = new ArrayList<>();
		for (final String part : message.getOrigin().replaceFirst("#.*$", "").split("\\.")) {
			if (!part.isEmpty() && Character.isUpperCase(part.charAt(0))) {
				break;
			}
			parts.add(part);
		}
		return Scope.packageFamily(String.join(".", parts));
	}

	private static List<String> enclosing(final String origin) {
		final List<String> names = new ArrayList<>();
		String name = origin.replaceFirst("#.*$", "");
		while (name.contains(".") && Character.isUpperCase(name.charAt(name.lastIndexOf('.') + 1))) {
			names.add(name);
			name = name.substring(0, name.lastIndexOf('.'));
		}
		return names;
	}

	private static String packageFamily(final String packageName) {
		final List<String> parts = Arrays.asList(packageName.split("\\."));
		if (parts.size() > 3 && "lib".equals(parts.get(2))) {
			return parts.get(3);
		}
		return parts.size() > 2 ? parts.get(2) : "other";
	}

	private static Audience guess(final ApiType type) {
		final List<String> parts = Arrays.asList(type.getPackageName().split("\\."));
		if (parts.contains("internal") || parts.contains("demo") || type.getPackageName().startsWith("dev.joid.lib.ui.node.impl.dev") || parts.contains("snapshot") && !"tool-testkit".equals(type.getModule())) {
			return Audience.INTERNAL;
		}

		if (type.getModule().startsWith("base-") || type.getModule().startsWith("backend-") || type.getPackageName().startsWith("dev.joid.lib.bridge")) {
			return Audience.CONTRIBUTOR;
		}
		return Audience.USER;
	}

}