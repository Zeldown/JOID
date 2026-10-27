package dev.joid.tool.docgen.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TypeKind {

	ENUM("enum"),
	CLASS("class"),
	INTERFACE("interface"),
	ANNOTATION("annotation");

	private final String keyword;

}