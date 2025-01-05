package be.zeldown.joid.demo.ui.wait;

import java.util.Arrays;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.container.ContainerNode;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.signal.impl.iterable.ListSignal;

public class UIDemoWait extends UIDemo {

	@Override
	public void init() {
		final ListSignal<String> cardInformations = new ListSignal<>();

		RectNode.create(10, 10, 400, 280).color(new Color(30, 29, 35)).body(node -> {
			ContainerNode.create(node).body(container -> {
				TextNode.create(container.aw(-10), 10).text(Text.create("auteur", TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE), Align.END)).anchorX(Align.END).attach(container);
				TextNode.create(10, 90).text(Text.create("Gros Titre", TextInfo.create(DemoFont.MONTSERRAT, 40, Color.WHITE))).attach(container);
				TextNode.create(10, 160).text(Text.create("ceci est une description", TextInfo.create(DemoFont.MONTSERRAT, 25, Color.WHITE))).attach(container);
				TextNode.create(10, 195).text(Text.create("moyenne description", TextInfo.create(DemoFont.MONTSERRAT, 25, Color.WHITE))).attach(container);
				TextNode.create(10, 230).text(Text.create("petite description", TextInfo.create(DemoFont.MONTSERRAT, 25, Color.WHITE))).attach(container);
			}).wait(cardInformations).onMount(container -> {
				for (int i = 0; i < cardInformations.getOrDefault().size(); i++) {
					container.getChild(i, TextNode.class).getText().text(cardInformations.getOrDefault().get(i));
				}
			});
		}).attach(this);

		new Thread(() -> {
			try {
				Thread.sleep(3000L);
				cardInformations.set(Arrays.asList("Zeldown", "Mon Titre", "ceci est la première ligne", "ma deuxième ligne", "troisième ligne"));
			} catch (final Exception silent) {}
		}).start();
	}

}