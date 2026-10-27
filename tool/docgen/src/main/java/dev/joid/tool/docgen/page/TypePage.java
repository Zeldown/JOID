package dev.joid.tool.docgen.page;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.joid.tool.docgen.model.ApiMember;
import dev.joid.tool.docgen.model.ApiType;
import dev.joid.tool.docgen.model.Audience;
import dev.joid.tool.docgen.model.MemberKind;
import dev.joid.tool.docgen.scope.Scope;
import lombok.Getter;
import lombok.Setter;

@Getter
public final class TypePage {

	private final ApiType  type;
	private final Audience audience;
	private final String   family;

	private final List<ApiMember>          factories;
	private final List<ApiMember>          constructors;
	private final Map<String, ApiProperty> properties;
	private final List<ApiMember>          methods;
	private final List<ApiMember>          events;
	private final List<ApiMember>          constants;
	private final List<ApiMember>          fields;
	private final List<ApiMember>          values;
	private final List<ApiMember>          elements;
	private final List<ApiType>            inherited;
	private final List<ApiType>            subtypes;

	@Setter private String slug;
	@Setter private String title;

	private TypePage(final ApiType type, final Audience audience, final String family) {
		this.type         = type;
		this.audience     = audience;
		this.family       = family;
		this.factories    = new ArrayList<>();
		this.constructors = new ArrayList<>();
		this.properties   = new LinkedHashMap<>();
		this.methods      = new ArrayList<>();
		this.events       = new ArrayList<>();
		this.constants    = new ArrayList<>();
		this.fields       = new ArrayList<>();
		this.values       = new ArrayList<>();
		this.elements     = new ArrayList<>();
		this.inherited    = new ArrayList<>();
		this.subtypes     = new ArrayList<>();
		this.slug         = type.getName().toLowerCase().replace('.', '-');
		this.title        = type.getName();
	}

	public static TypePage create(final ApiType type, final Audience audience, final String family) {
		return new TypePage(type, audience, family);
	}

	public TypePage collect(final Map<String, ApiType> types, final Scope scope) {
		final List<ApiMember> members = new ArrayList<>();
		final Set<String> keys = new HashSet<>();
		for (final ApiMember member : this.type.getMembers()) {
			if (scope.audienceOf(this.type, member) == this.audience) {
				members.add(member);
				keys.add(member.getKey());
			}
		}

		for (final ApiType ancestor : TypePage.ancestors(this.type, types)) {
			if (scope.audienceOf(ancestor).isDocumented()) {
				this.inherited.add(ancestor);
				continue;
			}

			for (final ApiMember member : ancestor.getMembers()) {
				final boolean instance = member.getKind() != MemberKind.CONSTRUCTOR && member.getKind() != MemberKind.FACTORY && !member.isStaticMember();
				if (instance && scope.audienceOf(ancestor, member) == this.audience && keys.add(member.getKey())) {
					members.add(member);
				}
			}
		}

		final Map<String, ApiProperty> candidates = new LinkedHashMap<>();
		for (final ApiMember member : members) {
			if (member.getKind() == MemberKind.METHOD && member.isFluent() && member.getParameters().size() == 1 && !member.isEvent() && !member.isStaticMember()) {
				candidates.computeIfAbsent(member.getName(), ApiProperty::create).getSetters().add(member);
			}
		}

		for (final ApiProperty candidate : candidates.values()) {
			if (candidate.isSignal() || TypePage.hasGetter(candidate.getName(), members) || TypePage.hasField(candidate, types)) {
				this.properties.put(candidate.getName(), candidate);
			}
		}

		for (final ApiMember member : members) {
			switch (member.getKind()) {
			case VALUE:
				this.values.add(member);
				break;
			case FIELD:
				this.fields.add(member);
				break;
			case ELEMENT:
				this.elements.add(member);
				break;
			case FACTORY:
				this.factories.add(member);
				break;
			case CONSTANT:
				this.constants.add(member);
				break;
			case CONSTRUCTOR:
				this.constructors.add(member);
				break;
			default:
				if (member.isEvent()) {
					this.events.add(member);
				} else if (!this.properties.containsKey(member.getName()) || !this.properties.get(member.getName()).getSetters().contains(member)) {
					final ApiProperty property = this.getterOf(member);
					if (property != null) {
						property.setGetter(member);
					} else {
						this.methods.add(member);
					}
				}
			}
		}
		return this;
	}

	public String getSection() {
		return this.audience.getSection();
	}

	public String getUrl() {
		return "/" + this.audience.getSection() + "/reference/" + this.family + "/" + this.slug + "/";
	}

	public String getFile() {
		return this.audience.getSection() + "/reference/" + this.family + "/" + this.slug + ".md";
	}

	public String getAnchorPrefix() {
		return this.type.getName().replace('.', '-');
	}

	public String anchor(final String kind, final String name) {
		return this.getAnchorPrefix() + "-" + kind + "-" + name;
	}

	public String anchorOf(final ApiMember member) {
		if (member.getKind() == MemberKind.CONSTRUCTOR) {
			return this.getAnchorPrefix() + "-constructor";
		}

		if (this.properties.containsKey(member.getName()) && this.properties.get(member.getName()).getSetters().contains(member)) {
			return this.anchor("property", member.getName());
		}

		for (final ApiProperty property : this.properties.values()) {
			if (property.getGetter() == member) {
				return this.anchor("property", property.getName());
			}
		}
		return this.anchor(member.isEvent() ? "event" : member.getKind().getAnchor(), member.getName());
	}

	public boolean isEmpty() {
		return this.factories.isEmpty() && this.constructors.isEmpty() && this.properties.isEmpty() && this.methods.isEmpty() && this.events.isEmpty() && this.constants.isEmpty() && this.fields.isEmpty() && this.values.isEmpty() && this.elements.isEmpty();
	}

	private ApiProperty getterOf(final ApiMember member) {
		if (member.getKind() != MemberKind.METHOD || !member.getParameters().isEmpty() || member.isStaticMember()) {
			return null;
		}

		final String name = member.getName();
		final String stripped = name.startsWith("get") && name.length() > 3 ? name.substring(3) : name.startsWith("is") && name.length() > 2 ? name.substring(2) : null;
		if (stripped == null) {
			return null;
		}
		return this.properties.get(Character.toLowerCase(stripped.charAt(0)) + stripped.substring(1));
	}

	private static boolean hasGetter(final String name, final List<ApiMember> members) {
		final String capitalized = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		return members.stream().anyMatch(member -> member.getParameters().isEmpty() && member.getKind() == MemberKind.METHOD && (member.getName().equals("get" + capitalized) || member.getName().equals("is" + capitalized)));
	}

	private static boolean hasField(final ApiProperty property, final Map<String, ApiType> types) {
		final ApiType declaring = types.get(property.getSetters().get(0).getDeclaringType());
		return declaring != null && (declaring.getFieldNames().contains(property.getName()) || declaring.getFieldNames().contains(property.getName() + "Supplier"));
	}

	public static List<ApiType> ancestors(final ApiType type, final Map<String, ApiType> types) {
		final List<ApiType> ancestors = new ArrayList<>();
		final Set<String> seen = new HashSet<>();
		final Deque<String> queue = new ArrayDeque<>(type.getSupertypes());
		while (!queue.isEmpty()) {
			final String fqcn = queue.poll();
			final ApiType ancestor = types.get(fqcn);
			if (ancestor == null || !seen.add(fqcn)) {
				continue;
			}

			ancestors.add(ancestor);
			queue.addAll(ancestor.getSupertypes());
		}
		return ancestors;
	}

}