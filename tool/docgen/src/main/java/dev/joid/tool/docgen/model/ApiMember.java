package dev.joid.tool.docgen.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ApiMember {

	private final MemberKind kind;
	private final String     name;

	private final List<ApiParameter> parameters;
	private final List<ApiMessage>   throwsList;

	private String  returnType;
	private String  typeParameters;
	private String  defaultValue;
	private String  lombok;
	private String  thread;
	private String  declaringType;
	private boolean protectedAccess;
	private boolean staticMember;
	private boolean abstractMember;
	private boolean defaultMember;
	private boolean finalMember;
	private boolean hook;
	private boolean overriding;
	private boolean fluent;
	private boolean deprecated;

	private ApiMember(final MemberKind kind, final String name) {
		this.kind           = kind;
		this.name           = name;
		this.parameters     = new ArrayList<>();
		this.throwsList     = new ArrayList<>();
		this.typeParameters = "";
	}

	public static ApiMember create(final MemberKind kind, final String name) {
		return new ApiMember(kind, name);
	}

	public boolean hasParameters() {
		return this.kind == MemberKind.METHOD || this.kind == MemberKind.FACTORY || this.kind == MemberKind.CONSTRUCTOR || this.kind == MemberKind.ELEMENT;
	}

	public boolean isSignal() {
		return this.parameters.size() == 1 && TypeNames.erase(this.parameters.get(0).getType()).matches("(Boolean|Int|Long|Double)?Supplier");
	}

	public boolean isEvent() {
		return this.kind == MemberKind.METHOD && !this.staticMember && !this.parameters.isEmpty() && this.name.matches("on[A-Z].*");
	}

	public String getKey() {
		if (!this.hasParameters()) {
			return this.name;
		}
		return this.name + "(" + this.parameters.stream().map(ApiParameter::getErasedType).collect(Collectors.joining(",")) + ")";
	}

	public String getParameterList() {
		return this.parameters.stream().map(ApiParameter::toString).collect(Collectors.joining(", "));
	}

	public String getDisplayReturn() {
		return this.fluent ? "this" : this.returnType;
	}

	public String getSignature() {
		switch (this.kind) {
		case VALUE:
			return this.name;
		case FIELD:
		case CONSTANT:
			return this.returnType + " " + this.name;
		case ELEMENT:
			return this.returnType + " " + this.name + "()" + (this.defaultValue != null ? " default " + this.defaultValue : "");
		case CONSTRUCTOR:
			return this.name + "(" + this.getParameterList() + ")";
		default:
			return (this.typeParameters.isEmpty() || this.fluent ? "" : this.typeParameters + " ") + this.getDisplayReturn() + " " + this.name + "(" + this.getParameterList() + ")";
		}
	}

	public List<String> getQualifiers() {
		final List<String> qualifiers = new ArrayList<>();
		if (this.kind == MemberKind.FACTORY) {
			qualifiers.add("factory");
		} else if (this.staticMember && this.kind != MemberKind.CONSTANT) {
			qualifiers.add("static");
		}

		if (this.fluent) {
			qualifiers.add("fluent");
		}

		if (this.isSignal() && this.fluent) {
			qualifiers.add("signal");
		}

		if (this.hook) {
			qualifiers.add("hook");
		}

		if (this.abstractMember) {
			qualifiers.add("abstract");
		}

		if (this.defaultMember) {
			qualifiers.add("default");
		}

		if (this.protectedAccess) {
			qualifiers.add("protected");
		}

		if (this.deprecated) {
			qualifiers.add("deprecated");
		}
		return qualifiers;
	}

}