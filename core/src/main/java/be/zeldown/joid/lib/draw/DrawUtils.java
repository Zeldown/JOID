package be.zeldown.joid.lib.draw;

import be.zeldown.joid.lib.draw.model.DrawModel;
import be.zeldown.joid.lib.draw.resource.DrawResource;
import be.zeldown.joid.lib.draw.shape.DrawShape;
import be.zeldown.joid.lib.draw.text.DrawText;

public final class DrawUtils {

	public static final DrawResource RESOURCE;
	public static final DrawShape    SHAPE;
	public static final DrawText     TEXT;
	public static final DrawModel    MODEL;

	static {
		RESOURCE = new DrawResource();
		SHAPE    = new DrawShape();
		TEXT     = new DrawText();
		MODEL    = new DrawModel();
	}

}