package be.zeldown.joid.demo.ui.watch;

import java.util.Arrays;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.node.impl.design.shape.CircleNode;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.container.ContainerNode;
import be.zeldown.joid.lib.ui.node.property.watch.WatchProperty;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.signal.impl.iterable.ListSignal;

public class UIDemoWatch extends UIDemo {

	private final ListSignal<String> cardInformations = new ListSignal<>();

	@Override
	public void init() {
		this.cardInformations.set(Arrays.asList("auteur", "Gros Titre", "ceci est une description", "moyenne description", "petite description"));
		RectNode.create(10, 10, 400, 280).color(new Color(50, 50, 50)).body(node -> {
			ContainerNode.create(node).watch(this.cardInformations, WatchProperty.NONE).body(container -> {
				CircleNode.create(10, 10, 50).attach(container);
				TextNode.create(container.aw(-10), 10).text(Text.create(this.cardInformations.get(0), TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE), Align.END)).anchorX(Align.END).attach(container);
				TextNode.create(10, 90).text(Text.create(this.cardInformations.get(1), TextInfo.create(DemoFont.MONTSERRAT, 40, Color.WHITE))).attach(container);
				TextNode.create(10, 160).text(Text.create(this.cardInformations.get(2), TextInfo.create(DemoFont.MONTSERRAT, 25, Color.WHITE))).attach(container);
				TextNode.create(10, 195).text(Text.create(this.cardInformations.get(3), TextInfo.create(DemoFont.MONTSERRAT, 25, Color.WHITE))).attach(container);
				TextNode.create(10, 230).text(Text.create(this.cardInformations.get(4), TextInfo.create(DemoFont.MONTSERRAT, 25, Color.WHITE))).attach(container);
			}).onWatch((container, signal, properties) -> {
				for (int i = 0; i < this.cardInformations.getOrDefault().size(); i++) {
					container.getChild(i, TextNode.class).getText().text(this.cardInformations.getOrDefault().get(i));
				}
			});
		}).attach(this);

		new Thread(() -> {
			try {
				Thread.sleep(3000L);
				this.cardInformations.set(Arrays.asList("Zeldown", "Mon Titre", "ceci est la première ligne", "ma deuxième ligne", "troisième ligne"));
			} catch (final Exception silent) {}
		}).start();
	}

}