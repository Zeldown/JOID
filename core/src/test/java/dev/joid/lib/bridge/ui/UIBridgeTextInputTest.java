package dev.joid.lib.bridge.ui;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.node.impl.control.field.MultilineTextFieldNode;
import dev.joid.lib.ui.node.impl.control.field.MultilineTextFieldNodeTest.FieldFont;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.utils.geom.BoundingBox;

public class UIBridgeTextInputTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final FieldFont font = new FieldFont();

	@Test
	public void leavesTheTextInputAloneWhileNoFieldIsFocused() {
		this.bridges.open(new NodeUI(this.field("ab"))).frames(3);
		Assert.assertEquals(Collections.emptyList(), this.bridges.getWindow().getTextInputAreas());
	}

	@Test
	public void givesTheAreaOfTheCursorOfTheFocusedField() {
		this.bridges.open(new NodeUI(this.field("ab").focused(true).moveCursor(2)));
		Assert.assertEquals(Collections.singletonList(BoundingBox.create(122D, 110D, 2D, 20D)), this.bridges.getWindow().getTextInputAreas());
	}

	@Test
	public void setsTheAreaOnlyWhenItChanges() {
		final TextFieldNode field = this.field("ab").focused(true).moveCursor(2);
		this.bridges.open(new NodeUI(field)).frames(3);
		this.bridges.getUi().charTyped('c');
		this.bridges.frames(3);
		field.blur();
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(BoundingBox.create(122D, 110D, 2D, 20D), BoundingBox.create(132D, 110D, 2D, 20D), null), this.bridges.getWindow().getTextInputAreas());
	}

	@Test
	public void givesTheAreaInWindowPixels() {
		this.bridges.resize(960, 540).open(new NodeUI(this.field("ab").focused(true).moveCursor(2)));
		Assert.assertEquals(Collections.singletonList(BoundingBox.create(61D, 55D, 1D, 10D)), this.bridges.getWindow().getTextInputAreas());
	}

	@Test
	public void followsTheLineOfAMultilineField() {
		this.bridges.open(new NodeUI(MultilineTextFieldNode.create(100D, 100D, 400D, 200D).info(TextInfo.create(this.font, 10F)).text("ab\ncd").focused(true).moveCursor(4)));
		Assert.assertEquals(Collections.singletonList(BoundingBox.create(112D, 122D, 2D, 20D)), this.bridges.getWindow().getTextInputAreas());
	}

	@Test
	public void clearsTheAreaOnceTheUiCloses() {
		final NodeUI ui = new NodeUI(this.field("ab").focused(true).moveCursor(2));
		this.bridges.open(ui);
		this.bridges.getUi().close(ui);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(BoundingBox.create(122D, 110D, 2D, 20D), null), this.bridges.getWindow().getTextInputAreas());
	}

	private TextFieldNode field(final String text) {
		return TextFieldNode.create(100D, 100D, 200D).info(TextInfo.create(this.font, 10F)).text(text);
	}

}