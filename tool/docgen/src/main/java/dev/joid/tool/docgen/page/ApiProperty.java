package dev.joid.tool.docgen.page;

import java.util.ArrayList;
import java.util.List;

import dev.joid.tool.docgen.model.ApiMember;
import dev.joid.tool.docgen.model.TypeNames;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ApiProperty {

	private final String          name;
	private final List<ApiMember> setters;

	private ApiMember getter;
	private String    defaultValue;

	private ApiProperty(final String name) {
		this.name    = name;
		this.setters = new ArrayList<>();
	}

	public static ApiProperty create(final String name) {
		return new ApiProperty(name);
	}

	public boolean isSignal() {
		return this.setters.stream().anyMatch(ApiMember::isSignal);
	}

	public String getType() {
		for (final ApiMember setter : this.setters) {
			if (!setter.isSignal()) {
				return setter.getParameters().get(0).getDisplayType();
			}
		}

		if (this.getter != null) {
			return this.getter.getReturnType();
		}

		final String supplier = this.setters.get(0).getParameters().get(0).getType();
		final int generic = supplier.indexOf('<');
		if (generic > 0 && supplier.endsWith(">")) {
			return supplier.substring(generic + 1, supplier.length() - 1).trim();
		}
		return TypeNames.erase(supplier).replace("Supplier", "").toLowerCase();
	}

	public List<ApiMember> getMembers() {
		final List<ApiMember> members = new ArrayList<>(this.setters);
		if (this.getter != null) {
			members.add(this.getter);
		}
		return members;
	}

}