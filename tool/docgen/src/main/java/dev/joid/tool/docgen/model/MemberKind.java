package dev.joid.tool.docgen.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MemberKind {

	VALUE("value"),
	FIELD("field"),
	METHOD("method"),
	FACTORY("factory"),
	ELEMENT("element"),
	CONSTANT("constant"),
	CONSTRUCTOR("constructor");

	private final String anchor;

}