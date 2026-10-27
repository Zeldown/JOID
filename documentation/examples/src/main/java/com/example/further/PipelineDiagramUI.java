package com.example.further;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class PipelineDiagramUI extends UI {

	private static final Color LABEL  = new Color(85, 85, 85).lock();
	private static final Color ACCENT = new Color(124, 92, 255).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 22F).color(PipelineDiagramUI.LABEL);
		final String[] boxes = {"Your drawing", "Buffer A", "Buffer B", "Buffer A", "Screen"};
		final String[] arrows = {"draws into", "pass 1", "pass 2", "last pass"};

		ContainerNode
		.create(80, 80, 1240, 160)
		.body(container -> {
			for (int i = 0; i < boxes.length; i++) {
				RectNode.create(i * 260, 60, 180, 80).color(Color.WHITE).label(Text.create(boxes[i], info)).attach(container);
			}

			for (int i = 0; i < arrows.length; i++) {
				TextNode.create(i * 260 + 220, 20).text(Text.create(arrows[i], info)).anchorX(Align.CENTER).attach(container);
			}
		})
		.layer((node, mouseX, mouseY) -> {
			for (int i = 0; i < arrows.length; i++) {
				final double start = i * 260D + 184D;
				final double end = i * 260D + 254D;
				DrawUtils.SHAPE.drawLine(PipelineDiagramUI.ACCENT, 3F, Point.of(start, 100D), Point.of(end, 100D));
				final Point tip = Point.of(end, 100D);
				DrawUtils.SHAPE.drawPolygon(PipelineDiagramUI.ACCENT, tip, Point.of(end - 12D, 92D), Point.of(end - 12D, 108D));
			}
		})
		.attach(this);
	}

}