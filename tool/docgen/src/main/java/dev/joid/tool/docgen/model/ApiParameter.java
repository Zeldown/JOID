package dev.joid.tool.docgen.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiParameter {

	private final String  type;
	private final String  name;
	private final boolean varargs;

	public static ApiParameter create(final String type, final String name, final boolean varargs) {
		return new ApiParameter(type, name, varargs);
	}

	public String getDisplayType() {
		return this.varargs ? this.type + "..." : this.type;
	}

	public String getErasedType() {
		return TypeNames.erase(this.type) + (this.varargs ? "[]" : "");
	}

	@Override
	public String toString() {
		return this.getDisplayType() + " " + this.name;
	}

}