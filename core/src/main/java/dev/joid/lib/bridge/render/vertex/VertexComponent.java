package dev.joid.lib.bridge.render.vertex;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum VertexComponent {

	FLOAT(4),
	UNSIGNED_BYTE(1),
	BYTE(1);

	private final int size;

}