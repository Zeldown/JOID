package dev.joid.tool.docgen.page;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public final class Backlinks {

	private static final Pattern REFERENCE = Pattern.compile("\\[\\[([A-Za-z0-9_.$]+)(?:#[A-Za-z0-9_<>]+(?:\\([A-Za-z0-9_.$\\[\\], ]*\\))?)?\\]\\]");
	private static final Pattern LINK      = Pattern.compile("\\]\\((/reference/[^)#\\s]*?)/?(?:#[^)\\s]*)?\\)");
	private static final Pattern TITLE     = Pattern.compile("^title:\\s*\"?(.*?)\"?\\s*$", Pattern.MULTILINE);
	private static final Pattern HIDDEN    = Pattern.compile("^(?:.*/)?index\\.mdx?$|^use/get-started/quick-start\\.mdx?$");
	private static final Pattern START     = Pattern.compile("^/use/get-started/quick-start/$");
	private static final Pattern NOTION    = Pattern.compile("`@?([A-Za-z][\\w.]*)`");
	private static final Pattern OWNER     = Pattern.compile("^((?:use|build)(?:/[\\w<>.-]+)*)");

	private final Map<String, List<Backlink>> links;
	private final Map<String, String>         titles;
	private final Map<String, String>         owners;
	private final Set<String>                 hidden;

	private Backlinks() {
		this.links  = new LinkedHashMap<>();
		this.titles = new LinkedHashMap<>();
		this.owners = new LinkedHashMap<>();
		this.hidden = new LinkedHashSet<>();
	}

	public static Backlinks scan(final File docs, final PageRegistry registry, final Map<String, List<String>> names) throws IOException {
		final Backlinks backlinks = new Backlinks();
		final Map<String, String> urls = new LinkedHashMap<>();
		registry.all().forEach(page -> urls.put(page.getUrl(), page.getType().getFqcn()));
		if (!docs.isDirectory()) {
			return backlinks;
		}

		final List<Path> files;
		try (Stream<Path> walk = Files.walk(docs.toPath())) {
			files = walk.filter(path -> path.toString().endsWith(".md") || path.toString().endsWith(".mdx")).sorted().collect(Collectors.toList());
		}

		for (final Path path : files) {
			final String relative = docs.toPath().relativize(path).toString().replace('\\', '/');
			if (relative.startsWith("reference/") || relative.equals("use/catalog.md") || relative.equals("use/catalog.mdx")) {
				continue;
			}

			final String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
			final Matcher title = Backlinks.TITLE.matcher(text);
			final String url = "/" + relative.replaceAll("(/index)?\\.mdx?$", "") + "/";
			final Backlink backlink = new Backlink(title.find() ? title.group(1) : relative, url.equals("//") ? "/" : url);
			backlinks.titles.put(backlink.getUrl(), backlink.getTitle());
			if (Backlinks.HIDDEN.matcher(relative).matches()) {
				backlinks.hidden.add(backlink.getUrl());
				continue;
			}

			final Matcher reference = Backlinks.REFERENCE.matcher(text);
			while (reference.find()) {
				final List<String> fqcns = names.get(reference.group(1));
				if (fqcns != null && fqcns.size() == 1) {
					backlinks.add(fqcns.get(0), backlink);
				}
			}

			final Matcher link = Backlinks.LINK.matcher(text);
			while (link.find()) {
				final String fqcn = urls.get(link.group(1) + "/");
				if (fqcn != null) {
					backlinks.add(fqcn, backlink);
				}
			}
		}

		final File owners = new File(docs.getParentFile().getParentFile().getParentFile(), "rules/owners.md");
		if (owners.isFile()) {
			backlinks.owners(owners, names);
		}
		return backlinks;
	}

	public List<Backlink> of(final String fqcn) {
		return new ArrayList<>(this.links.getOrDefault(fqcn, new ArrayList<>()));
	}

	public String ownerOf(final String fqcn) {
		final String use = this.owners.get("use:" + fqcn);
		return use != null ? use : this.owners.get("build:" + fqcn);
	}

	public boolean isHidden(final String url) {
		return this.hidden.contains(url.replaceFirst("#.*$", ""));
	}

	private void add(final String fqcn, final Backlink backlink) {
		final List<Backlink> list = this.links.computeIfAbsent(fqcn, key -> new ArrayList<>());
		if (list.stream().noneMatch(other -> other.getUrl().equals(backlink.getUrl()))) {
			list.add(backlink);
		}
	}

	private void owners(final File file, final Map<String, List<String>> names) throws IOException {
		final Set<String> ambiguous = new LinkedHashSet<>();
		for (final String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
			final String[] cells = line.trim().replaceAll("^\\||\\|$", "").split("\\|");
			if (cells.length != 2) {
				continue;
			}

			final Matcher owner = Backlinks.OWNER.matcher(cells[1].trim());
			if (!owner.find() || owner.group(1).contains("<")) {
				continue;
			}

			final String url = "/" + owner.group(1).replaceFirst("/index$", "") + "/";
			if (Backlinks.START.matcher(url).matches()) {
				continue;
			}

			final String section = owner.group(1).substring(0, owner.group(1).indexOf('/'));
			final Matcher notion = Backlinks.NOTION.matcher(cells[0]);
			while (notion.find()) {
				final List<String> fqcns = names.get(notion.group(1));
				if (fqcns == null || fqcns.size() != 1) {
					continue;
				}

				final String key = section + ":" + fqcns.get(0);
				final String previous = this.owners.putIfAbsent(key, url);
				if (previous != null && !previous.equals(url)) {
					ambiguous.add(key);
				}
			}
		}
		ambiguous.forEach(this.owners::remove);
	}

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class Backlink {

		private final String title;
		private final String url;

	}

}