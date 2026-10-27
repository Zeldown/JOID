package dev.joid.tool.docgen.page;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.joid.tool.docgen.model.TypeNames;
import dev.joid.tool.docgen.report.Coverage;
import lombok.Getter;

@Getter
public final class LinkChecker {

	private static final Pattern REFERENCE = Pattern.compile("\\[\\[([A-Za-z0-9_.$]+)(?:#([A-Za-z0-9_<>]+)(\\([A-Za-z0-9_.$\\[\\], ]*\\))?)?\\]\\]");
	private static final Pattern LINK      = Pattern.compile("\\]\\((/[^)#\\s]*)(?:#([^)\\s]*))?\\)");

	private final File                      docs;
	private final PageRegistry              registry;
	private final Coverage                  coverage;
	private final Map<String, List<String>> names;
	private final Map<String, Set<String>>  anchors;

	private LinkChecker(final File docs, final PageRegistry registry, final Coverage coverage) {
		this.docs     = docs;
		this.registry = registry;
		this.coverage = coverage;
		this.names    = new HashMap<>();
		this.anchors  = new HashMap<>();
	}

	public static LinkChecker create(final File docs, final PageRegistry registry, final Coverage coverage) {
		final LinkChecker checker = new LinkChecker(docs, registry, coverage);
		for (final TypePage page : registry.all()) {
			checker.anchors.put(page.getUrl(), PageWriter.anchors(page));
			checker.names.computeIfAbsent(page.getType().getName(), key -> new ArrayList<>()).add(page.getType().getFqcn());
			checker.names.computeIfAbsent(page.getType().getFqcn(), key -> new ArrayList<>()).add(page.getType().getFqcn());
		}
		checker.names.replaceAll((name, fqcns) -> new ArrayList<>(new LinkedHashSet<>(fqcns)));
		return checker;
	}

	public String resolve(final String text, final String path) {
		final Matcher matcher = LinkChecker.REFERENCE.matcher(text);
		final StringBuffer resolved = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(resolved, Matcher.quoteReplacement(this.reference(matcher.group(1), matcher.group(2), matcher.group(3), path, matcher.group())));
		}
		matcher.appendTail(resolved);

		final Matcher links = LinkChecker.LINK.matcher(resolved);
		while (links.find()) {
			this.check(links.group(1), links.group(2), path);
		}
		return resolved.toString();
	}

	public void check(final String url, final String anchor, final String path) {
		final String normalized = url.endsWith("/") ? url : url + "/";
		final Set<String> known = this.anchors.get(normalized);
		if (known != null) {
			if (anchor != null && !anchor.isEmpty() && !known.contains(anchor)) {
				this.coverage.broken(path, "no anchor #" + anchor + " on " + url);
			}
			return;
		}

		final String relative = normalized.substring(1, normalized.length() - 1);
		final boolean exists = new File(this.docs, relative + ".md").isFile() || new File(this.docs, relative + ".mdx").isFile() || new File(this.docs, relative + "/index.md").isFile() || new File(this.docs, relative + "/index.mdx").isFile();
		if (!exists) {
			this.coverage.broken(path, "no page at " + url);
		}
	}

	private String reference(final String name, final String member, final String parameters, final String path, final String raw) {
		final List<String> fqcns = this.names.get(name);
		if (fqcns == null || fqcns.size() != 1) {
			this.coverage.broken(path, raw + (fqcns == null ? " names no documented type" : " is ambiguous, use the fully qualified name"));
			return "`" + name + "`";
		}

		final TypePage target = this.registry.get(fqcns.get(0));
		if (member == null) {
			return "[`" + target.getType().getName() + "`](" + target.getUrl() + ")";
		}

		final String anchor = PageWriter.memberAnchors(target).get(member);
		if (anchor == null) {
			this.coverage.broken(path, raw + " names no documented member of " + target.getType().getName());
			return "`" + name + "." + member + "`";
		}
		if (parameters == null) {
			return "[`" + target.getType().getName() + "." + member + "`](" + target.getUrl() + "#" + anchor + ")";
		}

		if (!PageWriter.memberKeys(target).contains(TypeNames.normalizeKey(member + parameters))) {
			this.coverage.broken(path, raw + " names no overload of " + target.getType().getName() + "." + member);
		}
		return "[`" + target.getType().getName() + "." + member + parameters + "`](" + target.getUrl() + "#" + anchor + ")";
	}

}