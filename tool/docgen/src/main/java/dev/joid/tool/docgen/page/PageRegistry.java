package dev.joid.tool.docgen.page;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.joid.tool.docgen.model.ApiType;
import lombok.Getter;

@Getter
public final class PageRegistry {

	private final Map<String, ApiType>  types;
	private final Map<String, TypePage> pages;

	private PageRegistry(final Map<String, ApiType> types) {
		this.types = types;
		this.pages = new LinkedHashMap<>();
	}

	public static PageRegistry create(final Map<String, ApiType> types) {
		return new PageRegistry(types);
	}

	public void add(final TypePage page) {
		this.pages.put(page.getType().getFqcn(), page);
	}

	public TypePage get(final String fqcn) {
		return this.pages.get(fqcn);
	}

	public List<TypePage> all() {
		return new ArrayList<>(this.pages.values());
	}

}