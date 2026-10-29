package dev.joid.tool.docgen.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import dev.joid.tool.docgen.description.DescriptionFile;
import dev.joid.tool.docgen.model.ApiMember;
import dev.joid.tool.docgen.model.ApiMessage;
import dev.joid.tool.docgen.model.ApiType;
import dev.joid.tool.docgen.model.MemberKind;
import dev.joid.tool.docgen.model.TypeNames;
import dev.joid.tool.docgen.page.Backlinks.Backlink;
import dev.joid.tool.docgen.report.Coverage;

public final class PageWriter {

	private static final String LEGEND = "`fluent` returns the instance, typed for chaining · `factory` creates an instance · `signal` also accepts a value that follows signals, as a signal, an expression or a lambda ([When does JOID compute this value again?](/use/learn/interactivity/signals/#ask-when-does-joid-compute-this-value-again)) · `hook` called by JOID, override it · `abstract` you implement it · `default` interface method with a body · `static` called on the class · `protected` visible to subclasses";

	private static final String SOURCE = "https://github.com/Zeldown/JOID/blob/main/";

	private static final Pattern RELATED  = Pattern.compile("\\[([^\\]]+)\\]\\((/[^)\\s]+)\\)");
	private static final Pattern PLUMBING = Pattern.compile("^(execute|register|compute|fire)[A-Z].*");
	private static final Pattern QUERY    = Pattern.compile("^(get|is|has|can|should|contains|count|find|to|as)([A-Z].*)?$|^(peek|size|length)$");

	private static final String   SUBCLASSES = "For subclasses";
	private static final String[] FALLBACKS  = {"Queries", "Actions", "Static methods", PageWriter.SUBCLASSES};
	private static final String[] ROLES      = {"Lifecycle", "Children", "Position and size", "Visibility", "Colors", "Hover and tooltips", "Input", "Scrolling", "Drag and drop", "Effects", "Drawing", "Animation", "Reactivity"};
	private static final Pattern[] PATTERNS  = {
		Pattern.compile("^(init|mount|unmount|update|render|load|reload|open|close|dispose|destroy|ready|on(Init|Mount|Unmount|Update|Render|Load|Reload|Open|Close|Dispose|Destroy|Ready|Append|Detach|Draw))([A-Z].*)?$"),
		Pattern.compile(".*(attach|Attach|detach|Detach|append|Append|Parent|Child|NodeList).*|^body$"),
		Pattern.compile("^(x|y|w|h|dw|dh|aw|ah|mw|mh|ax|ay|position|size|width|height|anchor|anchorX|anchorY|aspectRatio|bounds|center|zindex|zlevel)$|^(get|set|toDraw)(X|Y|Width|Height|Position|Size|Anchor|AnchorX|AnchorY|AspectRatio|Zindex|Zlevel|DrawX|DrawY|DrawWidth|DrawHeight|DefaultWidth|DefaultHeight|Absolute\\w*)$|^toDraw\\w*$"),
		Pattern.compile("^(is)?(visible|enabled|interactive|skeleton|Visible|Enabled|Interactive|Skeleton|Hidden|Shown)$|^(get)(Skeleton)$"),
		Pattern.compile(".*(color|Color|tint|Tint|background|Background|foreground|Foreground|opacity|Opacity).*"),
		Pattern.compile(".*(hover|Hover|tooltip|Tooltip|cursor|Cursor).*"),
		Pattern.compile(".*(click|Click|mouse|Mouse|key|Key|char|Char|press|Press|release|Release|focus|Focus).*"),
		Pattern.compile(".*(scroll|Scroll|overflow|Overflow).*"),
		Pattern.compile(".*(drag|Drag|snap|Snap|drop|Drop).*"),
		Pattern.compile(".*(effect|Effect|mask|Mask).*"),
		Pattern.compile(".*(layer|Layer|draw|Draw|skeleton|Skeleton).*"),
		Pattern.compile(".*(animat|Animat|tween|Tween|lerp|Lerp|transition|Transition).*"),
		Pattern.compile("^(watch|wait|bind|unbind|rebind|follow|sync|subscribe|unsubscribe|batch|publish|silent|writable|pull)([A-Z].*)?$|^onWatch$")
	};

	private static final int GROUPED    = 12;
	private static final int LEARN_MORE = 5;

	private final PageRegistry                 registry;
	private final Map<String, DescriptionFile> descriptions;
	private final Coverage                     coverage;
	private final LinkChecker                  links;
	private final Backlinks                    backlinks;
	private final Map<String, String[]>        captures;

	private PageWriter(final PageRegistry registry, final Map<String, DescriptionFile> descriptions, final Coverage coverage, final LinkChecker links, final Backlinks backlinks, final Map<String, String[]> captures) {
		this.registry     = registry;
		this.descriptions = descriptions;
		this.coverage     = coverage;
		this.links        = links;
		this.backlinks    = backlinks;
		this.captures     = captures;
	}

	public static PageWriter create(final PageRegistry registry, final Map<String, DescriptionFile> descriptions, final Coverage coverage, final LinkChecker links, final Backlinks backlinks, final Map<String, String[]> captures) {
		return new PageWriter(registry, descriptions, coverage, links, backlinks, captures);
	}

	public static Map<String, String> memberAnchors(final TypePage page) {
		final Map<String, String> anchors = PageWriter.callAnchors(page);
		page.getConstants().forEach(member -> anchors.put(member.getName(), page.anchorOf(member)));
		return anchors;
	}

	public static Map<String, String> callAnchors(final TypePage page) {
		final Map<String, String> anchors = new LinkedHashMap<>();
		page.getProperties().keySet().forEach(name -> anchors.put(name, page.anchor("property", name)));
		for (final List<ApiMember> group : PageWriter.groups(page)) {
			for (final ApiMember member : group) {
				anchors.putIfAbsent(member.getName(), page.anchorOf(member));
			}
		}
		return anchors;
	}

	public static Set<String> memberKeys(final TypePage page) {
		final Set<String> keys = new LinkedHashSet<>();
		page.getProperties().values().forEach(property -> property.getMembers().forEach(member -> keys.add(member.getKey())));
		PageWriter.groups(page).forEach(group -> group.forEach(member -> keys.add(member.getKey())));
		return keys;
	}

	public static Set<String> anchors(final TypePage page) {
		final Set<String> anchors = new LinkedHashSet<>(PageWriter.memberAnchors(page).values());
		anchors.addAll(PageWriter.callAnchors(page).values());
		anchors.add(page.getAnchorPrefix() + "-properties");
		anchors.add(page.getAnchorPrefix() + "-methods");
		anchors.add(page.getAnchorPrefix() + "-events");
		return anchors;
	}

	public String write(final TypePage page) {
		final ApiType type = page.getType();
		final DescriptionFile description = this.descriptions.get(type.getFqcn());
		final String descriptionPath = "documentation/reference/" + type.getFqcn() + ".md";
		final String summary = description == null ? "" : description.getSummary();
		final StringBuilder out = new StringBuilder();

		out.append("---\n");
		out.append("title: ").append(new Gson().toJson(page.getTitle())).append('\n');
		out.append("description: ").append(new Gson().toJson(summary.isEmpty() ? "Reference of " + type.getName() + "." : PageWriter.plain(summary))).append('\n');
		out.append("genre: reference\n");
		out.append("topic: reference\n");
		out.append("tableOfContents:\n  maxHeadingLevel: 3\n");
		out.append("editUrl: ").append(description == null ? "https://github.com/Zeldown/JOID/new/main/documentation/reference?filename=" + type.getFqcn() + ".md" : "https://github.com/Zeldown/JOID/edit/main/" + descriptionPath).append('\n');
		out.append("---\n\n");
		out.append("<!-- Generated by tool-docgen from ").append(type.getFile()).append(" and ").append(descriptionPath).append(". Do not edit. -->\n\n");

		out.append("`").append(type.getDeclaration()).append("`");
		if (type.isDeprecated()) {
			out.append(" <span class=\"joid-qualifier\">deprecated</span>");
		}
		out.append("<br/>\n");
		this.supertypes(page, out);
		out.append("**Package:** `").append(type.getPackageName()).append("` · **Module:** `").append(type.getModule()).append("` · **Source:** [").append(type.getFile().substring(type.getFile().lastIndexOf('/') + 1)).append("](").append(PageWriter.SOURCE).append(type.getFile()).append(")");
		final String backends = PageWriter.backends(type, description);
		if (backends != null) {
			out.append("<br/>\n**Backends:** ").append(Arrays.stream(backends.split(",")).map(backend -> "<span class=\"joid-badge\">" + backend.trim() + "</span>").collect(Collectors.joining(" ")));
		}
		out.append("\n\n");

		this.coverage.expect(page.getFamily().getId(), descriptionPath, "summary", !summary.isEmpty());
		out.append(summary.isEmpty() ? PageWriter.missing("summary", descriptionPath) : this.text(page, summary, descriptionPath)).append("\n\n");
		final String[] capture = description != null && description.option("capture") != null ? new String[] {description.option("capture"), description.option("alt")} : this.captures.get(type.getFqcn());
		if (capture != null) {
			final String alt = capture[1] != null ? capture[1] : summary.isEmpty() ? type.getName() : summary;
			out.append("![").append(alt.replace("[", "").replace("]", "")).append("](/captures/").append(capture[0]).append(".webp)\n\n");
		}

		final String example = description == null ? null : description.option("example");
		if (example != null) {
			final String[] parts = example.split("#", 2);
			out.append("```java file=\"").append(parts[0]).append("\"").append(parts.length > 1 ? " region=\"" + parts[1] + "\"" : "").append("\n```\n\n");
		}

		final Map<String, String> related = new LinkedHashMap<>();
		final Set<String> linked = new LinkedHashSet<>();
		final boolean described = description != null && !description.getDescription().isEmpty();
		if (described) {
			final StringBuilder text = new StringBuilder();
			for (final String line : description.getDescription().split("\n")) {
				if (line.startsWith("Learn:")) {
					final Matcher link = PageWriter.RELATED.matcher(line);
					while (link.find()) {
						related.putIfAbsent(link.group(2), link.group(1));
					}
					continue;
				}
				text.append(line).append('\n');
			}

			final Matcher link = PageWriter.RELATED.matcher(text);
			while (link.find()) {
				linked.add(link.group(2));
			}

			if (text.toString().trim().length() > 0) {
				out.append("## Description\n\n").append(this.text(page, text.toString().trim(), descriptionPath)).append("\n\n");
			}
		}

		this.related(page, related, linked, descriptionPath, out);
		this.tables(page, description, descriptionPath, out);
		this.details(page, description, descriptionPath, out);
		out.append("---\n\n<small>").append(PageWriter.LEGEND).append("</small>\n");
		return out.toString();
	}

	private void related(final TypePage page, final Map<String, String> related, final Set<String> linked, final String path, final StringBuilder out) {
		final Map<String, String> all = new LinkedHashMap<>();
		final Set<String> shown = new LinkedHashSet<>(linked);
		final Set<String> pages = linked.stream().map(url -> url.replaceFirst("#.*$", "")).collect(Collectors.toCollection(LinkedHashSet::new));
		final String owner = this.backlinks.ownerOf(page.getType().getFqcn());
		if (owner != null && !pages.contains(owner)) {
			final String anchored = related.keySet().stream().filter(url -> url.replaceFirst("#.*$", "").equals(owner)).findFirst().orElse(null);
			final String title = anchored != null ? related.get(anchored) : this.backlinks.getTitles().get(owner);
			if (title != null) {
				final String key = anchored != null ? anchored : owner;
				pages.add(owner);
				shown.add(key);
				all.put(key, title);
			}
		}

		for (final Map.Entry<String, String> link : related.entrySet()) {
			if (!this.backlinks.isHidden(link.getKey()) && shown.add(link.getKey())) {
				pages.add(link.getKey().replaceFirst("#.*$", ""));
				all.put(link.getKey(), link.getValue());
			}
		}
		final List<Backlink> backlinks = new ArrayList<>(this.backlinks.of(page.getType().getFqcn()));
		backlinks.sort((left, right) -> Integer.compare(PageWriter.rank(left.getUrl()), PageWriter.rank(right.getUrl())));
		for (final Backlink backlink : backlinks) {
			if (pages.add(backlink.getUrl())) {
				all.put(backlink.getUrl(), backlink.getTitle());
			}
		}

		if (all.isEmpty()) {
			return;
		}

		out.append("## Learn more\n\n");
		all.entrySet().stream().limit(PageWriter.LEARN_MORE).forEach(link -> out.append(this.text(page, "- [" + link.getValue() + "](" + link.getKey() + ")", path)).append('\n'));
		out.append('\n');
	}

	private void supertypes(final TypePage page, final StringBuilder out) {
		final ApiType type = page.getType();
		final List<String> parents = new ArrayList<>();
		for (final String parent : type.getExtendsTypes()) {
			parents.add(this.typeLink(page, type, parent));
		}

		if (!parents.isEmpty()) {
			out.append("**Extends:** ").append(String.join(", ", parents)).append("<br/>\n");
		}

		final List<String> interfaces = new ArrayList<>();
		for (final String parent : type.getImplementsTypes()) {
			interfaces.add(this.typeLink(page, type, parent));
		}

		if (!interfaces.isEmpty()) {
			out.append("**Implements:** ").append(String.join(", ", interfaces)).append("<br/>\n");
		}

		final List<String> subtypes = new ArrayList<>();
		for (final ApiType candidate : this.registry.getTypes().values()) {
			if (candidate.getSupertypes().contains(type.getFqcn()) && this.registry.get(candidate.getFqcn()) != null) {
				subtypes.add(this.typeLink(page, candidate, candidate.getName()));
			}
		}

		if (!subtypes.isEmpty()) {
			out.append("**Known subtypes:** ").append(String.join(", ", subtypes)).append("<br/>\n");
		}
	}

	private void tables(final TypePage page, final DescriptionFile description, final String path, final StringBuilder out) {
		final ApiType type = page.getType();
		if (!page.getFactories().isEmpty() || !page.getConstructors().isEmpty()) {
			out.append("## Creation\n\n| Returns | Signature | Summary |\n|---|---|---|\n");
			for (final ApiMember member : PageWriter.concat(page.getFactories(), page.getConstructors())) {
				final String returns = member.getKind() == MemberKind.CONSTRUCTOR ? "" : this.typeLink(page, this.declaring(member), member.getReturnType());
				out.append("| ").append(returns).append(" | ").append(this.signature(page, member)).append(" | ").append(this.summary(page, description, path, member.getKey())).append(" |\n");
			}
			out.append('\n');
		}

		if (!page.getProperties().isEmpty() || this.hasInherited(page, "properties")) {
			out.append("<a id=\"").append(page.getAnchorPrefix()).append("-properties\"></a>\n\n## Properties\n\n");
			if (!page.getProperties().isEmpty()) {
				for (final Map.Entry<String, List<ApiProperty>> group : PageWriter.propertyGroups(page).entrySet()) {
					if (!group.getKey().isEmpty()) {
						out.append("### ").append(group.getKey()).append("\n\n");
					}

					out.append("| Property | Type | Default | Summary |\n|---|---|---|---|\n");
					for (final ApiProperty property : group.getValue()) {
						final List<String> keys = property.getMembers().stream().map(ApiMember::getKey).collect(Collectors.toList());
						keys.add(0, property.getName());
						out.append("| [").append(property.getName()).append("](#").append(page.anchor("property", property.getName())).append(")").append(property.isSignal() ? " <span class=\"joid-qualifier\">signal</span>" : "").append(" | ").append(this.typeLink(page, this.declaring(property.getSetters().get(0)), property.getType())).append(" | ").append(this.defaultValue(page, description, path, property)).append(" | ").append(this.summary(page, description, path, keys.toArray(new String[0]))).append(" |\n");
					}
					out.append('\n');
				}
			}
			this.inherited(page, "properties", out);
			this.redefined(page, out);
		}

		if (!page.getMethods().isEmpty() || this.hasInherited(page, "methods")) {
			out.append("<a id=\"").append(page.getAnchorPrefix()).append("-methods\"></a>\n\n## Methods\n\n");
			for (final Map.Entry<String, List<ApiMember>> group : PageWriter.memberGroups(page.getMethods(), false).entrySet()) {
				if (!group.getKey().isEmpty()) {
					out.append("### ").append(group.getKey()).append("\n\n");
				}

				out.append("| Returns | Signature | Summary |\n|---|---|---|\n");
				for (final ApiMember member : group.getValue()) {
					out.append("| ").append(this.typeLink(page, this.declaring(member), member.getDisplayReturn())).append(" | ").append(this.signature(page, member)).append(" | ").append(this.summary(page, description, path, member.getKey())).append(" |\n");
				}
				out.append('\n');
			}
			this.inherited(page, "methods", out);
		}

		if (!page.getEvents().isEmpty() || this.hasInherited(page, "events")) {
			out.append("<a id=\"").append(page.getAnchorPrefix()).append("-events\"></a>\n\n## Events\n\n");
			for (final Map.Entry<String, List<ApiMember>> group : PageWriter.memberGroups(page.getEvents(), true).entrySet()) {
				if (!group.getKey().isEmpty()) {
					out.append("### ").append(group.getKey()).append("\n\n");
				}

				out.append("| Event | Listener | Summary |\n|---|---|---|\n");
				for (final ApiMember member : group.getValue()) {
					out.append("| [").append(member.getName()).append("](#").append(page.anchorOf(member)).append(") | ").append(this.listener(page, member)).append(" | ").append(this.summary(page, description, path, member.getKey())).append(" |\n");
				}
				out.append('\n');
			}
			this.inherited(page, "events", out);
		}

		if (!page.getValues().isEmpty()) {
			out.append("## Values\n\n| Value | Summary |\n|---|---|\n");
			for (final ApiMember member : page.getValues()) {
				out.append("| [`").append(member.getName()).append("`](#").append(page.anchorOf(member)).append(") | ").append(this.summary(page, description, path, member.getKey())).append(" |\n");
			}
			out.append('\n');
		}

		if (!page.getElements().isEmpty()) {
			out.append("## Elements\n\n| Type | Element | Default | Summary |\n|---|---|---|---|\n");
			for (final ApiMember member : page.getElements()) {
				out.append("| ").append(this.typeLink(page, type, member.getReturnType())).append(" | [").append(member.getName()).append("](#").append(page.anchorOf(member)).append(") | ").append(member.getDefaultValue() == null ? "required" : "`" + PageWriter.escape(member.getDefaultValue()) + "`").append(" | ").append(this.summary(page, description, path, member.getKey())).append(" |\n");
			}
			out.append('\n');
		}

		if (!page.getConstants().isEmpty() || !page.getFields().isEmpty()) {
			out.append("## Constants and fields\n\n| Type | Name | Value | Summary |\n|---|---|---|---|\n");
			for (final ApiMember member : PageWriter.concat(page.getConstants(), page.getFields())) {
				out.append("| ").append(this.typeLink(page, this.declaring(member), member.getReturnType())).append(" | [").append(member.getName()).append("](#").append(page.anchorOf(member)).append(") | ").append(member.getKind() == MemberKind.CONSTANT && member.getDefaultValue() != null ? "`" + PageWriter.escape(member.getDefaultValue()) + "`" : "").append(" | ").append(this.summary(page, description, path, member.getKey())).append(" |\n");
			}
			out.append('\n');
		}
	}

	private String signature(final TypePage page, final ApiMember member) {
		final String qualifiers = PageWriter.qualifiers(member);
		return "[" + member.getName() + "](#" + page.anchorOf(member) + ")(" + PageWriter.escape(member.getParameterList()) + ")" + (qualifiers.isEmpty() ? "" : " " + qualifiers);
	}

	private String summary(final TypePage page, final DescriptionFile description, final String path, final String... keys) {
		if (description == null) {
			return "";
		}

		for (final String key : keys) {
			if (description.has(key)) {
				final String line = description.get(key).trim().split("\n")[0].trim();
				if (line.isEmpty() || line.startsWith("-") || line.startsWith("|") || line.startsWith("```") || line.startsWith("<") || line.startsWith(":::")) {
					return "";
				}
				return PageWriter.escape(this.text(page, line, path));
			}
		}
		return "";
	}

	private void details(final TypePage page, final DescriptionFile description, final String path, final StringBuilder out) {
		if (!page.getFactories().isEmpty() || !page.getConstructors().isEmpty()) {
			out.append("## Creation details\n\n");
			this.groups(page, PageWriter.concat(page.getFactories(), page.getConstructors()), description, path, out);
		}

		if (!page.getProperties().isEmpty()) {
			out.append("## Property details\n\n");
			for (final List<ApiProperty> group : PageWriter.propertyGroups(page).values()) {
				for (final ApiProperty property : group) {
					out.append("<a id=\"").append(page.anchor("property", property.getName())).append("\"></a>\n\n### ").append(property.getName()).append("\n\n");
					for (final ApiMember member : property.getMembers()) {
						out.append("- `").append(PageWriter.escapeCode(member.getSignature())).append("` ").append(PageWriter.qualifiers(member)).append('\n');
					}
					final String value = this.defaultValue(page, description, path, property);
					out.append(value.isEmpty() ? "\n" : "\n**Default:** " + value + "\n\n");
					property.getMembers().forEach(member -> PageWriter.behavior(member, out));
					final List<String> keys = property.getMembers().stream().map(ApiMember::getKey).collect(Collectors.toList());
					this.entry(page, description, path, property.getName(), keys, out);
					this.masked(description, path, property.getName(), keys);
				}
			}
		}

		if (!page.getMethods().isEmpty()) {
			out.append("## Method details\n\n");
			this.groups(page, PageWriter.flatten(PageWriter.memberGroups(page.getMethods(), false)), description, path, out);
		}

		if (!page.getEvents().isEmpty()) {
			out.append("## Event details\n\n");
			this.groups(page, PageWriter.flatten(PageWriter.memberGroups(page.getEvents(), true)), description, path, out);
		}

		if (!page.getValues().isEmpty()) {
			out.append("## Value details\n\n");
			this.groups(page, page.getValues(), description, path, out);
		}

		if (!page.getElements().isEmpty()) {
			out.append("## Element details\n\n");
			this.groups(page, page.getElements(), description, path, out);
		}

		if (!page.getConstants().isEmpty() || !page.getFields().isEmpty()) {
			out.append("## Constant and field details\n\n");
			this.groups(page, PageWriter.concat(page.getConstants(), page.getFields()), description, path, out);
		}

		if (description != null) {
			final Set<String> known = new LinkedHashSet<>(page.getProperties().keySet());
			for (final List<ApiMember> group : PageWriter.groups(page)) {
				group.forEach(member -> known.add(member.getKey()));
			}
			page.getProperties().values().forEach(property -> property.getMembers().forEach(member -> known.add(member.getKey())));
			for (final String key : description.getEntries().keySet()) {
				if (!known.contains(key)) {
					this.coverage.unknown(path, key);
				}
			}
		}
	}

	private void groups(final TypePage page, final List<ApiMember> members, final DescriptionFile description, final String path, final StringBuilder out) {
		final Map<String, List<ApiMember>> byAnchor = new LinkedHashMap<>();
		for (final ApiMember member : members) {
			byAnchor.computeIfAbsent(page.anchorOf(member), key -> new ArrayList<>()).add(member);
		}

		for (final Map.Entry<String, List<ApiMember>> group : byAnchor.entrySet()) {
			final ApiMember first = group.getValue().get(0);
			out.append("<a id=\"").append(group.getKey()).append("\"></a>\n\n### ").append(first.getName()).append("\n\n");
			for (final ApiMember member : group.getValue()) {
				out.append("`").append(PageWriter.escapeCode(member.getSignature())).append("` ").append(PageWriter.qualifiers(member)).append("\n\n");
				if (member.getKind() == MemberKind.FIELD || member.getKind() == MemberKind.CONSTANT) {
					if (!member.getDeclaringType().equals(page.getType().getFqcn())) {
						out.append("Declared in `").append(this.registry.getTypes().get(member.getDeclaringType()).getName()).append("`.\n\n");
					}
				}

				PageWriter.behavior(member, out);
				final List<String> keys = new ArrayList<>();
				keys.add(member.getKey());
				this.entry(page, description, path, member.getKey(), keys, out);
			}
		}
	}

	private void entry(final TypePage page, final DescriptionFile description, final String path, final String key, final List<String> alternatives, final StringBuilder out) {
		String text = description != null && description.has(key) ? description.get(key) : null;
		if (text == null && description != null) {
			for (final String alternative : alternatives) {
				if (description.has(alternative)) {
					text = description.get(alternative);
					break;
				}
			}
		}

		this.coverage.expect(page.getFamily().getId(), path, key, text != null);
		out.append(text == null ? PageWriter.missing(key, path) : this.text(page, text, path)).append("\n\n");
	}

	private void masked(final DescriptionFile description, final String path, final String property, final List<String> keys) {
		if (description == null) {
			return;
		}

		final List<String> described = new ArrayList<>();
		if (description.has(property)) {
			described.add(property);
		}
		keys.stream().filter(description::has).forEach(described::add);
		described.stream().skip(1).forEach(key -> this.coverage.masked(path, key, property));
	}

	private String defaultValue(final TypePage page, final DescriptionFile description, final String path, final ApiProperty property) {
		String explained = description == null ? null : description.getDefaults().get(property.getName());
		for (final ApiMember member : property.getMembers()) {
			if (explained == null && description != null) {
				explained = description.getDefaults().get(member.getKey());
			}
		}
		return explained == null ? PageWriter.defaultValue(property.getDefaultValue()) : PageWriter.escape(this.text(page, explained, path));
	}

	private String text(final TypePage page, final String text, final String path) {
		return this.links.resolve(text, path);
	}

	private boolean hasInherited(final TypePage page, final String category) {
		return !this.inheritedNames(page, category).isEmpty();
	}

	private void inherited(final TypePage page, final String category, final StringBuilder out) {
		for (final Map.Entry<ApiType, Map<String, String>> ancestor : this.inheritedNames(page, category).entrySet()) {
			final TypePage target = this.registry.get(ancestor.getKey().getFqcn());
			final String name = ancestor.getKey().getName();
			out.append("<details>\n<summary>Inherited from ").append(target == null ? "<code>" + name + "</code>" : "<a href=\"" + target.getUrl() + "#" + target.getAnchorPrefix() + "-" + category + "\"><code>" + name + "</code></a>").append(" (").append(ancestor.getValue().size()).append(")</summary>\n\n");
			final List<String> items = new ArrayList<>();
			for (final Map.Entry<String, String> member : ancestor.getValue().entrySet()) {
				items.add(target == null ? "`" + member.getKey() + "`" : "[`" + member.getKey() + "`](" + target.getUrl() + "#" + member.getValue() + ")");
			}
			out.append(String.join(" · ", items)).append("\n\n</details>\n\n");
		}
	}

	private Map<ApiType, Map<String, String>> inheritedNames(final TypePage page, final String category) {
		final Set<String> seen = new LinkedHashSet<>();
		page.getProperties().keySet().forEach(seen::add);
		PageWriter.groups(page).forEach(group -> group.forEach(member -> seen.add(member.getName())));

		final Map<ApiType, Map<String, String>> inherited = new LinkedHashMap<>();
		for (final ApiType ancestor : page.getInherited()) {
			final TypePage source = this.registry.get(ancestor.getFqcn());
			if (source == null) {
				continue;
			}

			final Map<String, String> names = new LinkedHashMap<>();
			if ("properties".equals(category)) {
				for (final List<ApiProperty> group : PageWriter.propertyGroups(source).values()) {
					group.stream().map(ApiProperty::getName).filter(seen::add).forEach(name -> names.put(name, source.anchor("property", name)));
				}
			} else {
				final List<ApiMember> members = PageWriter.flatten(PageWriter.memberGroups("methods".equals(category) ? source.getMethods() : source.getEvents(), !"methods".equals(category)));
				members.stream().filter(member -> !PageWriter.isPlumbing(member)).filter(member -> seen.add(member.getName()) || names.containsKey(member.getName())).forEach(member -> names.putIfAbsent(member.getName(), source.anchorOf(member)));
			}

			if (!names.isEmpty()) {
				inherited.put(ancestor, names);
			}
		}
		return inherited;
	}

	private ApiType declaring(final ApiMember member) {
		return this.registry.getTypes().get(member.getDeclaringType());
	}

	private String listener(final TypePage page, final ApiMember member) {
		if (member.getParameters().size() != 1) {
			return PageWriter.escape(member.getParameterList());
		}

		final String type = member.getParameters().get(0).getDisplayType();
		final ApiType context = this.declaring(member);
		final int open = type.indexOf('<');
		final String received = open < 0 || context == null ? null : context.resolve(TypeNames.split(type.substring(open + 1, type.lastIndexOf('>'))).get(0).replaceFirst("^\\?\\s+(super|extends)\\s+", ""));
		final TypePage target = received == null ? null : this.registry.get(received);
		return target == null ? this.typeLink(page, context, type) : "[`" + PageWriter.escape(type) + "`](" + target.getUrl() + ")";
	}

	private String typeLink(final TypePage page, final ApiType context, final String type) {
		final String code = "`" + PageWriter.escape(type) + "`";
		if (context == null) {
			return code;
		}

		final String resolved = context.resolve(type);
		if (resolved == null) {
			return code;
		}

		final TypePage target = this.registry.get(resolved);
		return target == null ? code : "[" + code + "](" + target.getUrl() + ")";
	}

	private void redefined(final TypePage page, final StringBuilder out) {
		final ApiType type = page.getType();
		final List<String> rows = new ArrayList<>();
		for (final ApiType ancestor : page.getInherited()) {
			final TypePage origin = this.registry.get(ancestor.getFqcn());
			if (origin == null) {
				continue;
			}

			for (final ApiProperty property : origin.getProperties().values()) {
				final String field = type.getSourceDefaults().containsKey(property.getName() + "#constructor") ? property.getName() : type.getSourceDefaults().containsKey(property.getName() + "Supplier#constructor") ? property.getName() + "Supplier" : null;
				final String value = field == null ? type.getSourceDefaults().get(property.getName() + "#call") : type.getSourceDefaults().get(field);
				if (value != null && !value.equals(property.getDefaultValue()) && !page.getProperties().containsKey(property.getName())) {
					rows.add("| [" + property.getName() + "](" + origin.getUrl() + "#" + origin.anchor("property", property.getName()) + ") | " + PageWriter.defaultValue(value) + " | " + (property.getDefaultValue() == null ? "not read" : PageWriter.defaultValue(property.getDefaultValue())) + " in `" + ancestor.getName() + "` |");
				}
			}
		}

		if (!rows.isEmpty()) {
			out.append("**Redefined defaults:**\n\n| Property | Default here | Inherited default |\n|---|---|---|\n");
			rows.forEach(row -> out.append(row).append('\n'));
			out.append('\n');
		}
	}

	private static Map<String, List<ApiProperty>> propertyGroups(final TypePage page) {
		final Map<String, List<ApiProperty>> groups = PageWriter.emptyGroups();
		for (final ApiProperty property : page.getProperties().values()) {
			groups.get(PageWriter.role(property.getName(), false)).add(property);
		}

		if (page.getProperties().size() <= PageWriter.GROUPED) {
			final Map<String, List<ApiProperty>> single = new LinkedHashMap<>();
			single.put("", new ArrayList<>(page.getProperties().values()));
			return single;
		}

		final List<ApiProperty> others = groups.get(PageWriter.FALLBACKS[1]);
		for (final String role : PageWriter.ROLES) {
			if (groups.get(role).size() < 2) {
				others.addAll(groups.get(role));
				groups.get(role).clear();
			}
		}

		groups.put("Other properties", new ArrayList<>(groups.get(PageWriter.FALLBACKS[0])));
		groups.get("Other properties").addAll(others);
		for (final String fallback : PageWriter.FALLBACKS) {
			groups.remove(fallback);
		}
		groups.values().removeIf(List::isEmpty);
		return groups;
	}

	private static Map<String, List<ApiMember>> memberGroups(final List<ApiMember> members, final boolean events) {
		final Map<String, List<ApiMember>> groups = PageWriter.emptyGroups();
		if (members.size() <= PageWriter.GROUPED) {
			final Map<String, List<ApiMember>> single = new LinkedHashMap<>();
			if (!members.isEmpty()) {
				single.put("", new ArrayList<>());
				single.put(PageWriter.SUBCLASSES, new ArrayList<>());
			}
			for (final ApiMember member : members) {
				single.get(!events && member.isHook() ? PageWriter.SUBCLASSES : "").add(member);
			}
			single.values().removeIf(List::isEmpty);
			return single;
		}

		for (final ApiMember member : members) {
			groups.get(!events && PageWriter.isPlumbing(member) ? PageWriter.SUBCLASSES : PageWriter.role(member, events)).add(member);
		}

		for (final String role : PageWriter.ROLES) {
			if (groups.get(role).size() < 2) {
				for (final ApiMember member : groups.get(role)) {
					groups.get(PageWriter.fallback(member, events)).add(member);
				}
				groups.get(role).clear();
			}
		}

		if (events) {
			groups.put("Other events", new ArrayList<>(groups.get(PageWriter.FALLBACKS[0])));
			groups.get("Other events").addAll(groups.get(PageWriter.FALLBACKS[1]));
			groups.remove(PageWriter.FALLBACKS[0]);
			groups.remove(PageWriter.FALLBACKS[1]);
		}
		groups.values().removeIf(List::isEmpty);
		return groups;
	}

	private static <T> Map<String, List<T>> emptyGroups() {
		final Map<String, List<T>> groups = new LinkedHashMap<>();
		for (final String role : PageWriter.ROLES) {
			groups.put(role, new ArrayList<>());
		}

		for (final String fallback : PageWriter.FALLBACKS) {
			groups.put(fallback, new ArrayList<>());
		}
		return groups;
	}

	private static String role(final String name, final boolean events) {
		for (int i = 0; i < PageWriter.ROLES.length; i++) {
			if (PageWriter.PATTERNS[i].matcher(name).matches()) {
				return PageWriter.ROLES[i];
			}
		}
		return events ? PageWriter.FALLBACKS[1] : PageWriter.QUERY.matcher(name).matches() ? PageWriter.FALLBACKS[0] : PageWriter.FALLBACKS[1];
	}

	private static String role(final ApiMember member, final boolean events) {
		final String role = PageWriter.role(member.getName(), events);
		return !events && role.equals(PageWriter.FALLBACKS[1]) && PageWriter.isQuery(member) ? PageWriter.FALLBACKS[0] : role;
	}

	private static String fallback(final ApiMember member, final boolean events) {
		if (events) {
			return PageWriter.FALLBACKS[1];
		}

		if (member.isStaticMember()) {
			return PageWriter.FALLBACKS[2];
		}
		return PageWriter.isQuery(member) ? PageWriter.FALLBACKS[0] : PageWriter.FALLBACKS[1];
	}

	private static boolean isQuery(final ApiMember member) {
		return PageWriter.QUERY.matcher(member.getName()).matches() || member.getParameters().isEmpty() && "boolean".equals(member.getReturnType());
	}

	private static boolean isPlumbing(final ApiMember member) {
		return member.isProtectedAccess() || member.isHook() || member.isAbstractMember() || PageWriter.PLUMBING.matcher(member.getName()).matches();
	}

	private static int rank(final String url) {
		final String[] order = {"/use/learn/", "/use/guides/", "/use/tutorial/", "/use/in-depth/", "/use/", "/build/"};
		for (int i = 0; i < order.length; i++) {
			if (url.startsWith(order[i])) {
				return i;
			}
		}
		return order.length;
	}

	private static <T> List<T> flatten(final Map<String, List<T>> groups) {
		final List<T> all = new ArrayList<>();
		groups.values().forEach(all::addAll);
		return all;
	}

	private static void behavior(final ApiMember member, final StringBuilder out) {
		if (!member.getThrowsList().isEmpty()) {
			out.append("**Throws:**\n\n");
			final Set<String> seen = new LinkedHashSet<>();
			for (final ApiMessage thrown : member.getThrowsList()) {
				if (seen.add(thrown.getType() + thrown.getText())) {
					out.append("- `").append(thrown.getType()).append("`").append(thrown.getText().isEmpty() ? "" : ": " + PageWriter.html(thrown.getText())).append('\n');
				}
			}
			out.append('\n');
		}

		if (member.getThread() != null) {
			out.append("**Thread:** runs on the ").append(member.getThread()).append(".\n\n");
		}
	}

	private static String backends(final ApiType type, final DescriptionFile description) {
		if (description != null && description.option("backends") != null) {
			return description.option("backends");
		}

		switch (type.getModule()) {
		case "backend-lwjgl2":
			return "LWJGL 2";
		case "backend-lwjgl3":
			return "LWJGL 3";
		case "backend-vulkan":
			return "Vulkan";
		case "base-opengl":
			return "LWJGL 2, LWJGL 3";
		case "base-lwjgl3-glfw":
		case "base-lwjgl3-openal":
			return "LWJGL 3, Vulkan";
		default:
			return null;
		}
	}

	public static String backendsOf(final ApiType type, final DescriptionFile description) {
		final String backends = PageWriter.backends(type, description);
		return backends == null ? "LWJGL 2, LWJGL 3, Vulkan" : backends;
	}

	private static List<List<ApiMember>> groups(final TypePage page) {
		final List<List<ApiMember>> groups = new ArrayList<>();
		groups.add(page.getFactories());
		groups.add(page.getConstructors());
		groups.add(page.getMethods());
		groups.add(page.getEvents());
		groups.add(page.getValues());
		groups.add(page.getElements());
		groups.add(page.getConstants());
		groups.add(page.getFields());
		return groups;
	}

	private static List<ApiMember> concat(final List<ApiMember> first, final List<ApiMember> second) {
		final List<ApiMember> all = new ArrayList<>(first);
		all.addAll(second);
		return all;
	}

	private static String qualifiers(final ApiMember member) {
		return member.getQualifiers().stream().map(qualifier -> "<span class=\"joid-qualifier\">" + qualifier + "</span>").collect(Collectors.joining(" "));
	}

	private static String defaultValue(final String value) {
		if (value == null) {
			return "";
		}
		return "@parameter".equals(value) ? "set at creation" : "`" + PageWriter.escape(value) + "`";
	}

	private static String missing(final String key, final String path) {
		return "<div class=\"joid-missing\">Missing description: add <code>## " + PageWriter.html(key.replace(",", ", ")) + "</code> to <code>" + path + "</code>.</div>";
	}

	private static String escape(final String text) {
		return text.replace("|", "\\|");
	}

	private static String escapeCode(final String text) {
		return text.replace("`", "'");
	}

	private static String plain(final String text) {
		return text.replaceAll("\\[\\[([A-Za-z0-9_.$]+)(?:#[^\\]]*)?\\]\\]", "$1").replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1").replace("`", "");
	}

	private static String html(final String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

}