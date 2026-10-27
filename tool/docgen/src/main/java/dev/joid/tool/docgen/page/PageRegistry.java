package dev.joid.tool.docgen.page;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.joid.tool.docgen.model.ApiType;
import dev.joid.tool.docgen.model.Audience;
import lombok.Getter;

@Getter
public final class PageRegistry {

	private final Map<String, ApiType>                 types;
	private final Map<String, Map<Audience, TypePage>> pages;

	private PageRegistry(final Map<String, ApiType> types) {
		this.types = types;
		this.pages = new LinkedHashMap<>();
	}

	public static PageRegistry create(final Map<String, ApiType> types) {
		return new PageRegistry(types);
	}

	public void add(final TypePage page) {
		this.pages.computeIfAbsent(page.getType().getFqcn(), key -> new EnumMap<>(Audience.class)).put(page.getAudience(), page);
	}

	public TypePage get(final String fqcn, final Audience audience) {
		final Map<Audience, TypePage> pages = this.pages.get(fqcn);
		return pages == null ? null : pages.get(audience);
	}

	public TypePage preferred(final String fqcn) {
		final Map<Audience, TypePage> pages = this.pages.get(fqcn);
		if (pages == null) {
			return null;
		}
		return pages.containsKey(Audience.USER) ? pages.get(Audience.USER) : pages.get(Audience.CONTRIBUTOR);
	}

	public TypePage linkable(final TypePage from, final String fqcn) {
		return this.linkable(from.getAudience(), fqcn);
	}

	public TypePage linkable(final Audience audience, final String fqcn) {
		final TypePage same = this.get(fqcn, audience);
		if (same != null) {
			return same;
		}
		return audience == Audience.CONTRIBUTOR ? this.get(fqcn, Audience.USER) : null;
	}

	public List<TypePage> all() {
		final List<TypePage> all = new ArrayList<>();
		this.pages.values().forEach(pages -> all.addAll(pages.values()));
		return all;
	}

}