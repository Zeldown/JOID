package dev.joid.tool.docgen;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.joid.tool.docgen.description.DescriptionFile;
import dev.joid.tool.docgen.model.ApiMember;
import dev.joid.tool.docgen.model.ApiType;
import dev.joid.tool.docgen.model.Audience;
import dev.joid.tool.docgen.model.TypeKind;
import dev.joid.tool.docgen.page.Backlinks;
import dev.joid.tool.docgen.page.ErrorsPage;
import dev.joid.tool.docgen.page.FamilyPage;
import dev.joid.tool.docgen.page.LinkChecker;
import dev.joid.tool.docgen.page.PageRegistry;
import dev.joid.tool.docgen.page.PageWriter;
import dev.joid.tool.docgen.page.TypePage;
import dev.joid.tool.docgen.report.Coverage;
import dev.joid.tool.docgen.scope.Scope;
import dev.joid.tool.docgen.source.DefaultReader;
import dev.joid.tool.docgen.source.SourceReader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DocGenerator {

	private static final Map<String, String> MODULES  = new LinkedHashMap<>();
	private static final Map<String, String> FAMILIES = new LinkedHashMap<>();

	static {
		DocGenerator.MODULES.put("core", "core");
		DocGenerator.MODULES.put("base-glfw", "base/glfw");
		DocGenerator.MODULES.put("base-openal", "base/openal");
		DocGenerator.MODULES.put("base-opengl", "base/opengl");
		DocGenerator.MODULES.put("backend-lwjgl2", "backend/lwjgl2");
		DocGenerator.MODULES.put("backend-lwjgl3", "backend/lwjgl3");
		DocGenerator.MODULES.put("backend-vulkan", "backend/vulkan");
		DocGenerator.MODULES.put("tool-msdf", "tool/msdf");
		DocGenerator.MODULES.put("tool-testkit", "tool/testkit");

		DocGenerator.FAMILIES.put("ui", "UI");
		DocGenerator.FAMILIES.put("annotations", "Annotations");
		DocGenerator.FAMILIES.put("nodes", "Nodes");
		DocGenerator.FAMILIES.put("controls", "Controls");
		DocGenerator.FAMILIES.put("layout", "Layout");
		DocGenerator.FAMILIES.put("events", "Events");
		DocGenerator.FAMILIES.put("effects", "Effects");
		DocGenerator.FAMILIES.put("signals", "Signals");
		DocGenerator.FAMILIES.put("animation", "Animation");
		DocGenerator.FAMILIES.put("text", "Text");
		DocGenerator.FAMILIES.put("resources", "Resources");
		DocGenerator.FAMILIES.put("drawing", "Drawing");
		DocGenerator.FAMILIES.put("shaders", "Shaders");
		DocGenerator.FAMILIES.put("input", "Input and keys");
		DocGenerator.FAMILIES.put("backends", "Backends");
		DocGenerator.FAMILIES.put("testing", "Testing");
		DocGenerator.FAMILIES.put("utilities", "Utilities");
		DocGenerator.FAMILIES.put("bridges", "Bridges");
		DocGenerator.FAMILIES.put("pipeline", "Pipeline");
		DocGenerator.FAMILIES.put("base-glfw", "base-glfw");
		DocGenerator.FAMILIES.put("base-openal", "base-openal");
		DocGenerator.FAMILIES.put("base-opengl", "base-opengl");
		DocGenerator.FAMILIES.put("lwjgl2", "LWJGL 2 backend");
		DocGenerator.FAMILIES.put("lwjgl3", "LWJGL 3 backend");
		DocGenerator.FAMILIES.put("vulkan", "Vulkan backend");
		DocGenerator.FAMILIES.put("backend-testing", "Backend testing");
		DocGenerator.FAMILIES.put("tools", "Tools");
	}

	public static void main(final String[] arguments) throws Exception {
		final Map<String, String> options = DocGenerator.options(arguments);
		final File root = new File(options.getOrDefault("root", "."));
		final File documentation = new File(root, "documentation");
		final File docs = new File(documentation, "src/content/docs");
		final File report = new File(options.getOrDefault("report", new File(root, "build/docgen").getPath()));
		final boolean strict = options.containsKey("strict");

		final SourceReader reader = SourceReader.create(root);
		final Map<String, ApiType> types = reader.read(DocGenerator.MODULES);
		DocGenerator.inheritHooks(types);
		final Scope scope = Scope.load(new File(options.getOrDefault("scope", new File(documentation, "reference/scope.json").getPath())));
		final Coverage coverage = Coverage.create();
		final PageRegistry registry = PageRegistry.create(types);
		for (final ApiType type : types.values()) {
			final Audience audience = scope.audienceOf(type);
			if (!audience.isDocumented()) {
				continue;
			}

			final String family = scope.familyOf(type);
			registry.add(TypePage.create(type, audience, family).collect(types, scope));
			if (audience == Audience.USER) {
				final TypePage contributor = TypePage.create(type, Audience.CONTRIBUTOR, family).collect(types, scope);
				if (!contributor.isEmpty() && DocGenerator.hasOwnMember(contributor, type)) {
					registry.add(contributor);
				}
			}
		}
		coverage.getUnclassified().addAll(scope.getUnclassified());
		DocGenerator.disambiguate(registry);

		final DefaultReader defaults = DefaultReader.create(types, !options.containsKey("no-instances"));
		registry.all().forEach(defaults::apply);

		final Map<String, DescriptionFile> descriptions = DocGenerator.descriptions(new File(documentation, "reference"));
		for (final DescriptionFile description : descriptions.values()) {
			if (registry.preferred(description.getFqcn()) == null) {
				coverage.unknown("documentation/reference/" + description.getFile().getName(), description.getFqcn() + " is not a documented type");
			}
		}

		final LinkChecker links = LinkChecker.create(docs, registry, coverage);
		final Backlinks backlinks = Backlinks.scan(docs, registry, links.getNames());
		final PageWriter writer = PageWriter.create(registry, descriptions, coverage, links, backlinks, DocGenerator.captures(documentation));
		for (final Audience audience : new Audience[] {Audience.USER, Audience.CONTRIBUTOR}) {
			DocGenerator.clean(new File(docs, audience.getSection() + "/reference"));
		}

		for (final TypePage page : registry.all()) {
			DocGenerator.write(new File(docs, page.getFile()), writer.write(page));
		}

		final ErrorsPage errors = ErrorsPage.create(reader.getMessages(), new File(documentation, "reference/errors.md"), scope);
		for (final Audience audience : new Audience[] {Audience.USER, Audience.CONTRIBUTOR}) {
			DocGenerator.write(new File(docs, audience.getSection() + "/reference/errors.md"), errors.write(audience, DocGenerator.FAMILIES, registry, links, coverage));
		}
		errors.check(coverage);
		final Map<String, String> anchors = new LinkedHashMap<>();
		errors.getAnchors().forEach((key, anchor) -> anchors.put("message-" + Integer.toHexString(key.hashCode()), "/" + errors.getAudiences().get(key).getSection() + "/reference/errors/#" + anchor));
		DocGenerator.write(new File(report, "error-anchors.json"), new GsonBuilder().setPrettyPrinting().create().toJson(anchors));

		final FamilyPage families = FamilyPage.create(registry, descriptions, links, coverage, new File(documentation, "reference/families.md"));
		final Map<String, Object> sidebar = new LinkedHashMap<>();
		for (final Audience audience : new Audience[] {Audience.USER, Audience.CONTRIBUTOR}) {
			sidebar.put(audience.getSection(), families.write(docs, audience, DocGenerator.FAMILIES));
		}

		final File generated = new File(documentation, "src/generated");
		final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		DocGenerator.write(new File(generated, "reference-sidebar.json"), gson.toJson(sidebar));
		DocGenerator.write(new File(generated, "reference-index.json"), gson.toJson(DocGenerator.index(registry, descriptions, types)));

		coverage.write(report);
		System.out.println("Reference: " + registry.all().size() + " pages, defaults read on " + defaults.getInstances() + " instances (" + defaults.getFailures() + " types without instance)");
		System.out.println("Messages: " + errors.getMessages().size() + " warnings and exceptions");
		System.out.println("Coverage: " + coverage.summary());
		System.out.println("Report: " + new File(report, "coverage.md").getAbsolutePath());
		if (strict && coverage.hasErrors()) {
			System.err.println("[JOID] The reference is incomplete, see the report");
			System.exit(1);
		}
	}

	private static void inheritHooks(final Map<String, ApiType> types) {
		for (final ApiType type : types.values()) {
			for (final ApiMember member : type.getMembers()) {
				if (member.isOverriding() && !member.isHook() && DocGenerator.isHook(type, member.getKey(), types)) {
					member.setHook(true);
				}
			}
			if (type.isFinalType()) {
				type.getMembers().removeIf(member -> member.isOverriding() && member.isHook());
			}
		}
	}

	private static boolean isHook(final ApiType type, final String key, final Map<String, ApiType> types) {
		for (final ApiType ancestor : TypePage.ancestors(type, types)) {
			for (final ApiMember member : ancestor.getMembers()) {
				if (member.getKey().equals(key) && member.isHook() && !member.isAbstractMember()) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean hasOwnMember(final TypePage page, final ApiType type) {
		final List<ApiMember> all = new ArrayList<>(page.getMethods());
		all.addAll(page.getFactories());
		all.addAll(page.getConstructors());
		all.addAll(page.getEvents());
		all.addAll(page.getConstants());
		all.addAll(page.getFields());
		page.getProperties().values().forEach(property -> all.addAll(property.getMembers()));
		return all.stream().anyMatch(member -> member.getDeclaringType().equals(type.getFqcn()));
	}

	private static void disambiguate(final PageRegistry registry) {
		final Map<String, List<TypePage>> files = new HashMap<>();
		registry.all().forEach(page -> files.computeIfAbsent(page.getFile(), key -> new ArrayList<>()).add(page));
		for (final List<TypePage> pages : files.values()) {
			if (pages.size() < 2) {
				continue;
			}

			for (final TypePage page : pages) {
				final String[] own = page.getType().getPackageName().split("\\.");
				String segment = own[own.length - 1];
				for (int i = 0; i < own.length; i++) {
					final int index = i;
					final boolean shared = pages.stream().filter(other -> other != page).allMatch(other -> {
						final String[] parts = other.getType().getPackageName().split("\\.");
						return parts.length > index && parts[index].equals(own[index]);
					});
					if (!shared) {
						segment = own[i];
						break;
					}
				}
				page.setSlug(page.getSlug() + "-" + segment);
				page.setTitle(page.getType().getName() + " (" + segment + ")");
			}
		}
	}

	private static Map<String, String[]> captures(final File documentation) throws IOException {
		final Map<String, String[]> captures = new HashMap<>();
		final File catalog = new File(documentation, "catalog/catalog.json");
		if (!catalog.isFile()) {
			return captures;
		}

		try (Reader reader = new InputStreamReader(Files.newInputStream(catalog.toPath()), StandardCharsets.UTF_8)) {
			for (final JsonElement section : new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("sections")) {
				final String id = section.getAsJsonObject().get("id").getAsString();
				for (final JsonElement element : section.getAsJsonObject().getAsJsonArray("cards")) {
					final JsonObject card = element.getAsJsonObject();
					if (!card.has("type") || card.has("capture") && !card.get("capture").getAsBoolean()) {
						continue;
					}

					final String type = card.get("type").getAsString();
					final String name = card.has("name") ? card.get("name").getAsString() : type.substring(type.lastIndexOf('.') + 1);
					final String shot = "catalog-" + id + "-" + name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
					if (new File(documentation, "public/captures/" + shot + ".webp").isFile()) {
						captures.put(type, new String[] {shot, card.has("alt") ? card.get("alt").getAsString() : null});
					}
				}
			}
		}
		return captures;
	}

	private static Map<String, Object> index(final PageRegistry registry, final Map<String, DescriptionFile> descriptions, final Map<String, ApiType> api) {
		final Map<String, Object> types = new LinkedHashMap<>();
		final Map<String, Integer> names = new HashMap<>();
		registry.getPages().keySet().forEach(fqcn -> names.merge(registry.preferred(fqcn).getType().getName(), 1, Integer::sum));
		for (final String fqcn : registry.getPages().keySet()) {
			final TypePage page = registry.preferred(fqcn);
			final Map<String, Object> entry = new LinkedHashMap<>();
			final DescriptionFile description = descriptions.get(fqcn);
			entry.put("fqcn", fqcn);
			entry.put("name", page.getType().getName());
			entry.put("kind", page.getType().getKind().getKeyword());
			entry.put("abstract", page.getType().isAbstractType() || page.getType().getKind() == TypeKind.INTERFACE);
			entry.put("family", page.getFamily());
			entry.put("summary", description == null ? "" : description.getSummary());
			entry.put("ancestors", TypePage.ancestors(page.getType(), api).stream().map(ApiType::getFqcn).collect(Collectors.toList()));
			entry.put("url", page.getUrl());
			entry.put("section", page.getSection());
			entry.put("members", PageWriter.memberAnchors(page));
			entry.put("overloads", PageWriter.memberKeys(page));
			types.put(fqcn, entry);
			if (names.get(page.getType().getName()) == 1) {
				types.put(page.getType().getName(), entry);
			}
		}

		final Map<String, Object> index = new LinkedHashMap<>();
		index.put("types", types);
		return index;
	}

	private static Map<String, DescriptionFile> descriptions(final File directory) throws IOException {
		final Map<String, DescriptionFile> descriptions = new LinkedHashMap<>();
		final File[] files = directory.listFiles((parent, name) -> name.matches("[a-z]\\w*(\\.[\\w$]+)+\\.md"));
		if (files == null) {
			return descriptions;
		}

		for (final File file : files) {
			final DescriptionFile description = DescriptionFile.read(file);
			descriptions.put(description.getFqcn(), description);
		}
		return descriptions;
	}

	private static void clean(final File reference) throws IOException {
		final File[] families = reference.listFiles(File::isDirectory);
		if (families == null) {
			return;
		}

		for (final File family : families) {
			try (Stream<Path> files = Files.walk(family.toPath())) {
				for (final Path path : files.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
					Files.delete(path);
				}
			}
		}
	}

	private static void write(final File file, final String content) throws IOException {
		file.getParentFile().mkdirs();
		Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
	}

	private static Map<String, String> options(final String[] arguments) {
		final Map<String, String> options = new HashMap<>();
		for (int i = 0; i < arguments.length; i++) {
			final String key = arguments[i].replaceFirst("^--", "");
			if (i + 1 < arguments.length && !arguments[i + 1].startsWith("--")) {
				options.put(key, arguments[++i]);
			} else {
				options.put(key, "");
			}
		}
		return options;
	}

}