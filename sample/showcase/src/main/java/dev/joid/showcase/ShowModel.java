package dev.joid.showcase;

import java.util.Locale;
import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.obj.ObjModel;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.model.ModelViewerNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ShowModel extends ShowUI {

	@Override
	protected void scene() {
		super.backdrop();
		ShowUI.blob(-300, -250, 900, ShowUI.VIOLET.withAlpha(0.40F), 0D, 50D).attach(this);
		ShowUI.blob(1300, 550, 900, ShowUI.PINK.withAlpha(0.20F), 0.5D, 50D).attach(this);

		final ObjModel model = ObjModel.of("teapot", "assets/demo/models/model.obj", Resource.of("assets/showcase/teapot.png"));

		ShowUI.glass(110, 100, 1700, 880, 32F).attach(this);
		CircleNode.create(330, 220, 640).color(ShowUI.FUCHSIA.withAlpha(0.32F)).effect(BlurNodeEffect.create(110F)).attach(this);

		final ModelViewerNode viewer = ModelViewerNode
		.create(170, 150, 960, 780)
		.zoom(0.78D)
		.zoomRange(0.45D, 1D)
		.rotationPitchRange(-55D, 20D)
		.rotationYaw(-28D)
		.rotationPitch(-16D)
		.model(model)
		.attach(this);

		TextNode.create(1250, 190).text(Text.create("3D MODEL", ShowUI.font(FontWeight.BOLD, 18F, ShowUI.FAINT).letterSpacing(0.25F))).attach(this);
		TextNode.create(1246, 226).text(Text.create("Utah teapot", ShowUI.font(FontWeight.EXTRA_BOLD, 52F, ShowUI.TEXT))).attach(this);
		TextNode.create(1250, 304).text(Text.create("OBJ with smooth normals and a texture,", ShowUI.font(FontWeight.MEDIUM, 21F, ShowUI.MUTED))).attach(this);
		TextNode.create(1250, 336).text(Text.create("lit and depth-tested inside your UI.", ShowUI.font(FontWeight.MEDIUM, 21F, ShowUI.MUTED))).attach(this);

		this.stat(0, "Vertices", () -> "2,989", ShowUI.TEXT);
		this.stat(1, "Triangles", () -> "5,632", ShowUI.TEXT);
		this.stat(2, "Yaw", () -> String.format(Locale.US, "%.0f°", viewer.getDrawnRotationYaw()), ShowUI.PINK);
		this.stat(3, "Pitch", () -> String.format(Locale.US, "%.0f°", viewer.getDrawnRotationPitch()), ShowUI.SKY);
		this.stat(4, "Zoom", () -> String.format(Locale.US, "%.0f %%", viewer.getDrawnZoom() * 100D), ShowUI.AMBER);

		this.hint(1250, 800, "Drag to turn");
		this.hint(1470, 800, "Scroll to zoom");
	}

	private void stat(final int index, final String label, final Supplier<String> value, final Color color) {
		final double y = 420D + index * 66D;
		RectNode.create(1250, y + 52D, 510, 1).color(Color.WHITE.withAlpha(0.08F)).attach(this);
		TextNode.create(1250, y + 24D).text(Text.create(label, ShowUI.font(FontWeight.MEDIUM, 22F, ShowUI.MUTED))).anchorY(Align.CENTER).attach(this);
		TextNode.create(1760, y + 24D).text(Text.create(value, ShowUI.font(FontWeight.BOLD, 24F, color))).anchorX(Align.END).anchorY(Align.CENTER).attach(this);
	}

	private void hint(final double x, final double y, final String text) {
		final double width = ShowUI.font(FontWeight.SEMI_BOLD, 19F, ShowUI.TEXT).getWidth(text) + 44D;
		RectNode
		.create(x, y, width, 48)
		.color(Color.WHITE.withAlpha(0.08F))
		.effect(RoundedNodeEffect.create(24F))
		.body(chip -> {
			TextNode.create(width / 2D, 24).text(Text.create(text, ShowUI.font(FontWeight.SEMI_BOLD, 19F, ShowUI.TEXT))).anchor(Align.CENTER).attach(chip);
		})
		.attach(this);
	}

}