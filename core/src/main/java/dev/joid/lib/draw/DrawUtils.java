package dev.joid.lib.draw;

import dev.joid.lib.draw.model.DrawModel;
import dev.joid.lib.draw.raster.DrawRaster;
import dev.joid.lib.draw.resource.DrawResource;
import dev.joid.lib.draw.shape.DrawShape;
import dev.joid.lib.draw.text.DrawText;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrawUtils {

	public static final DrawText     TEXT     = DrawText.inst();
	public static final DrawShape    SHAPE    = DrawShape.inst();
	public static final DrawModel    MODEL    = DrawModel.inst();
	public static final DrawRaster   RASTER   = DrawRaster.inst();
	public static final DrawResource RESOURCE = DrawResource.inst();

}