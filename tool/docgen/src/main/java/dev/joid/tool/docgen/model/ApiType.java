package dev.joid.tool.docgen.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ApiType {

	private final String   fqcn;
	private final String   name;
	private final String   module;
	private final String   file;
	private final TypeKind kind;

	private final List<String>    extendsTypes;
	private final List<String>    implementsTypes;
	private final List<String>    supertypes;
	private final List<ApiMember> members;

	private final Map<String, String> imports;
	private final Map<String, String> sourceDefaults;
	private final Set<String>         fieldNames;

	private String  typeParameters;
	private boolean abstractType;
	private boolean finalType;
	private boolean deprecated;

	private ApiType(final String fqcn, final String name, final String module, final String file, final TypeKind kind) {
		this.fqcn            = fqcn;
		this.name            = name;
		this.module          = module;
		this.file            = file;
		this.kind            = kind;
		this.extendsTypes    = new ArrayList<>();
		this.implementsTypes = new ArrayList<>();
		this.supertypes      = new ArrayList<>();
		this.members         = new ArrayList<>();
		this.imports         = new HashMap<>();
		this.sourceDefaults  = new HashMap<>();
		this.fieldNames      = new HashSet<>();
		this.typeParameters  = "";
	}

	public static ApiType create(final String fqcn, final String name, final String module, final String file, final TypeKind kind) {
		return new ApiType(fqcn, name, module, file, kind);
	}

	public String getSimpleName() {
		return this.name.substring(this.name.lastIndexOf('.') + 1);
	}

	public String getPackageName() {
		return this.fqcn.substring(0, this.fqcn.length() - this.name.length() - 1);
	}

	public String resolve(final String type) {
		final String raw = TypeNames.raw(type);
		final String head = raw.contains(".") ? raw.substring(0, raw.indexOf('.')) : raw;
		final String resolved = this.imports.get(head);
		if (resolved == null) {
			return null;
		}
		return raw.contains(".") ? resolved + raw.substring(raw.indexOf('.')) : resolved;
	}

	public boolean isExtensible() {
		return this.kind == TypeKind.INTERFACE || this.kind == TypeKind.CLASS && !this.finalType;
	}

	public String getDeclaration() {
		final String keyword = this.kind == TypeKind.ANNOTATION ? "@interface" : this.kind.getKeyword();
		return (this.abstractType && this.kind == TypeKind.CLASS ? "abstract " : "") + (this.finalType && this.kind == TypeKind.CLASS ? "final " : "") + keyword + " " + this.name + this.typeParameters;
	}

}