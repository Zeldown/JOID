package dev.joid.lib.draw;

import dev.joid.lib.draw.model.DrawModel;
import dev.joid.lib.draw.resource.DrawResource;
import dev.joid.lib.draw.shape.DrawShape;
import dev.joid.lib.draw.text.DrawText;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrawUtils {

	public static final DrawText     TEXT;
	public static final DrawShape    SHAPE;
	public static final DrawModel    MODEL;
	public static final DrawResource RESOURCE;

	static {
		RESOURCE = new DrawResource();
		SHAPE    = new DrawShape();
		TEXT     = new DrawText();
		MODEL    = new DrawModel();
	}

}