package dev.joid.lib.ui.node.impl.dev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.internal.font.InternalFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.debug.UIDataDebug;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import lombok.NonNull;

public class DevNodeTest {

	private static final Color ACTION = new Color(57, 120, 255);
	private static final Color UPDATE = new Color(239, 57, 38);
	private static final Color BLACK  = new Color(23, 23, 25);

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private boolean devMode;

	@BeforeClass
	public static void loadTheDevFont() {
		InternalFont.load();
	}

	@Before
	public void enableTheDevMode() {
		this.devMode = JOID.inst().isDevMode();
		JOID.inst().setDevMode(true);
	}

	@After
	public void restoreTheDevMode() {
		JOID.inst().setDevMode(this.devMode);
	}

	@Test
	public void startsInspectingAndHighlightingTheUpdates() {
		final DevNode panel = DevNode.create(1625D, 1007D);
		Assert.assertEquals(1625D, panel.getX(), 0D);
		Assert.assertEquals(1007D, panel.getY(), 0D);
		Assert.assertEquals(275D, panel.getWidth(), 0D);
		Assert.assertEquals(53D, panel.getHeight(), 0D);
		Assert.assertSame(Align.END, panel.getAnchorX());
		Assert.assertSame(Align.END, panel.getAnchorY());
		Assert.assertTrue(panel.getInspectSignal().getOrDefault());
		Assert.assertTrue(panel.getEyeSignal().getOrDefault());
		Assert.assertFalse(panel.getGridSignal().getOrDefault());
		Assert.assertFalse(panel.getReloadSignal().getOrDefault());
		Assert.assertFalse(panel.getInspectedNodeLocked().getOrDefault());
		Assert.assertNull(panel.getInspectedNode().getOrDefault());
		Assert.assertEquals(0F, panel.getReloadAnimator().getValue(), 0F);
		Assert.assertEquals(0, panel.getGridColorIndex());
	}

	@Test
	public void drawsItsPanelInTheBottomRightCorner() {
		final DevUI ui = new DevUI();
		DevNodeTest.open(this.bridges, ui);
		final Draw background = DevNodeTest.draws(this.bridges, DevNodeTest.BLACK, 1F).get(0);
		Assert.assertEquals(1625D, background.getLeft(), 0.001D);
		Assert.assertEquals(1007D, background.getTop(), 0.001D);
		Assert.assertEquals(1900D, background.getRight(), 0.001D);
		Assert.assertEquals(1060D, background.getBottom(), 0.001D);
	}

	@Test
	public void showsFourButtonsBesideTheFramerate() {
		final DevUI ui = new DevUI();
		final DevNode panel = DevNodeTest.open(this.bridges, ui);
		final List<ResourceNode> buttons = DevNodeTest.buttons(panel);
		Assert.assertEquals(4, buttons.size());
		for (int i = 0; i < buttons.size(); i++) {
			Assert.assertEquals(1640D + i * 34D, buttons.get(i).getAbsoluteX(), 0D);
			Assert.assertEquals(1022D, buttons.get(i).getAbsoluteY(), 0D);
			Assert.assertEquals(24D, buttons.get(i).getWidth(), 0D);
		}
		this.bridges.frames(70);
		Assert.assertTrue(DevNodeTest.texts(panel).contains(String.format("%.0f fps", ui.getFps())));
	}

	@Test
	public void namesEachButtonInItsTooltip() {
		final DevUI ui = new DevUI();
		final DevNode panel = DevNodeTest.open(this.bridges, ui);
		for (final ResourceNode button : DevNodeTest.buttons(panel)) {
			this.bridges.move(button.getAbsoluteX() + 12D, button.getAbsoluteY() + 12D).frames(2);
		}
		Assert.assertEquals(Arrays.asList("[I] Inspect", "[R] Reload", "[U] Update", "[G] Grid"), ui.hovers.stream().distinct().collect(Collectors.toList()));
	}

	@Test
	public void hidesItselfBelowAnotherUi() {
		final DevUI ui = new DevUI();
		final DevNode panel = DevNodeTest.open(this.bridges, ui);
		this.bridges.open(new DevUI()).frame();
		Assert.assertFalse(panel.isVisible());
		Assert.assertFalse(panel.isEnabled());
		Assert.assertTrue(DevNodeTest.draws(this.bridges, DevNodeTest.BLACK, 1F).isEmpty());
	}

	@Test
	public void inspectsTheHoveredNode() {
		final RectNode rect = RectNode.create(200D, 200D, 300D, 150D);
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(rect));
		this.bridges.move(250D, 250D).frames(60);
		Assert.assertSame(rect, panel.getInspectedNode().getOrDefault());
		Assert.assertEquals(200D, panel.getInspectX(), 0D);
		Assert.assertEquals(200D, panel.getInspectY(), 0D);
		Assert.assertEquals(300D, panel.getInspectWidth(), 0D);
		Assert.assertEquals(150D, panel.getInspectHeight(), 0D);
		final Draw box = DevNodeTest.draws(this.bridges, DevNodeTest.ACTION, 0.3F).get(0);
		Assert.assertEquals(200D, box.getLeft(), 0.001D);
		Assert.assertEquals(200D, box.getTop(), 0.001D);
		Assert.assertEquals(500D, box.getRight(), 0.001D);
		Assert.assertEquals(350D, box.getBottom(), 0.001D);
	}

	@Test
	public void movesItsInspectionBoxTowardTheHoveredNode() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		this.bridges.move(250D, 250D).frame();
		Assert.assertTrue(panel.getInspectX() > 0D && panel.getInspectX() < 200D);
		Assert.assertEquals(200D, panel.getTargetInspectX(), 0D);
		Assert.assertEquals(200D, panel.getTargetInspectY(), 0D);
		Assert.assertEquals(300D, panel.getTargetInspectWidth(), 0D);
		Assert.assertEquals(150D, panel.getTargetInspectHeight(), 0D);
	}

	@Test
	public void inspectsTheDeepestHoveredNodeFirst() {
		final ContainerNode container = ContainerNode.create(100D, 100D, 600D, 400D);
		final RectNode rect = RectNode.create(100D, 100D, 300D, 150D).attach(container);
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(container));
		this.bridges.move(250D, 250D).frame();
		Assert.assertSame(rect, panel.getInspectedNode().getOrDefault());
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.frame();
		Assert.assertSame(container, panel.getInspectedNode().getOrDefault());
	}

	@Test
	public void inspectsNothingAwayFromTheNodes() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		this.bridges.move(10D, 10D).frames(2);
		Assert.assertNull(panel.getInspectedNode().getOrDefault());
		Assert.assertTrue(DevNodeTest.draws(this.bridges, DevNodeTest.ACTION, 0.3F).isEmpty());
	}

	@Test
	public void neverInspectsItself() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI());
		this.bridges.move(1652D, 1034D).frames(2);
		Assert.assertNull(panel.getInspectedNode().getOrDefault());
		Assert.assertTrue(DevNodeTest.draws(this.bridges, DevNodeTest.ACTION, 0.3F).isEmpty());
	}

	@Test
	public void placesTheInfoBoxAboveTheInspectedNode() {
		DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		this.bridges.move(250D, 250D).frames(60);
		final Draw box = DevNodeTest.draws(this.bridges, DevNodeTest.BLACK, 0.8F).get(0);
		Assert.assertEquals(200D, box.getLeft(), 0.001D);
		Assert.assertEquals(165D, box.getTop(), 0.001D);
		Assert.assertEquals(195D, box.getBottom(), 0.001D);
	}

	@Test
	public void placesTheInfoBoxBelowANodeAtTheTop() {
		DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 10D, 300D, 100D)));
		this.bridges.move(250D, 50D).frames(60);
		final Draw box = DevNodeTest.draws(this.bridges, DevNodeTest.BLACK, 0.8F).get(0);
		Assert.assertEquals(115D, box.getTop(), 0.001D);
		Assert.assertEquals(145D, box.getBottom(), 0.001D);
	}

	@Test
	public void locksTheInspectionOnALeftClick() {
		final RectNode rect = RectNode.create(200D, 200D, 300D, 150D);
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(rect));
		this.bridges.move(250D, 250D).frame();
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.frames(2);
		Assert.assertTrue(panel.getInspectedNodeLocked().getOrDefault());
		Assert.assertEquals(1425D, panel.getX(), 0D);
		Assert.assertEquals(787D, panel.getY(), 0D);
		Assert.assertEquals(475D, panel.getWidth(), 0D);
		Assert.assertEquals(273D, panel.getHeight(), 0D);
		this.bridges.move(10D, 10D).frames(2);
		Assert.assertSame(rect, panel.getInspectedNode().getOrDefault());
	}

	@Test
	public void describesTheLockedNode() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		DevNodeTest.lock(this.bridges, 250D, 250D);
		final List<String> texts = DevNodeTest.texts(panel);
		for (final String text : Arrays.asList("RectNode", "1 update", "Bounds", "position: RELATIVE", "x: 200.0 / 200.0 (200.0)", "y: 200.0 / 200.0 (200.0)", "width: 300.0 (300.0)", "height: 150.0 (150.0)", "Hierarchy", "hierarchy: RectNode", "children: 0", "Properties", "hoveredColor: null")) {
			Assert.assertTrue(text + " in " + texts, texts.contains(text));
		}
		Assert.assertFalse(texts.contains("Callbacks"));
	}

	@Test
	public void countsTheCallbacksAndTheUpdatesOfTheLockedNode() {
		final DevUI ui = new DevUI(RectNode.create(200D, 200D, 300D, 150D).onClick((node, mouseX, mouseY, clickType) -> {}));
		final DevNode panel = DevNodeTest.open(this.bridges, ui);
		ui.reload();
		DevNodeTest.lock(this.bridges, 250D, 250D);
		final List<String> texts = DevNodeTest.texts(panel);
		Assert.assertTrue(texts.toString(), texts.contains("2 updates"));
		Assert.assertTrue(texts.toString(), texts.contains("Callbacks"));
		Assert.assertTrue(texts.toString(), texts.contains("NodeMousePressedCallback: 1"));
	}

	@Test
	public void listsTheFieldsOfTheLockedNode() {
		final LabelNode node = new LabelNode();
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(node));
		DevNodeTest.lock(this.bridges, 250D, 250D);
		final List<String> texts = DevNodeTest.texts(panel);
		Assert.assertTrue(texts.toString(), texts.contains("label: " + node.label));
		Assert.assertTrue(texts.toString(), texts.contains("target: " + node.target));
		Assert.assertTrue(texts.toString(), texts.stream().noneMatch(text -> text.startsWith("FILL")));
	}

	@Test
	public void unlocksTheInspectionOnARightClick() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		DevNodeTest.lock(this.bridges, 250D, 250D);
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		this.bridges.frames(2);
		Assert.assertFalse(panel.getInspectedNodeLocked().getOrDefault());
		Assert.assertEquals(1625D, panel.getX(), 0D);
		Assert.assertEquals(1007D, panel.getY(), 0D);
		Assert.assertEquals(275D, panel.getWidth(), 0D);
		Assert.assertEquals(53D, panel.getHeight(), 0D);
		Assert.assertFalse(DevNodeTest.texts(panel).contains("Bounds"));
	}

	@Test
	public void keepsItsInspectionUnlockedOnARightClick() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		this.bridges.move(250D, 250D).frame();
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		Assert.assertFalse(panel.getInspectedNodeLocked().getOrDefault());
		Assert.assertNotNull(panel.getInspectedNode().getOrDefault());
	}

	@Test
	public void keepsItsLockOnAnotherLeftClick() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		DevNodeTest.lock(this.bridges, 250D, 250D);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(panel.getInspectedNodeLocked().getOrDefault());
	}

	@Test
	public void followsTheHierarchyUpOnEnter() {
		final ContainerNode container = ContainerNode.create(100D, 100D, 600D, 400D);
		final RectNode rect = RectNode.create(100D, 100D, 300D, 150D).attach(container);
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(container));
		DevNodeTest.lock(this.bridges, 250D, 250D);
		Assert.assertSame(rect, panel.getInspectedNode().getOrDefault());
		this.bridges.getUi().keyTyped('\n', Key.ENTER);
		Assert.assertSame(container, panel.getInspectedNode().getOrDefault());
		Assert.assertTrue(panel.getInspectedNodeLocked().getOrDefault());
		this.bridges.getUi().keyTyped('\n', Key.NUMPAD_ENTER);
		Assert.assertSame(container, panel.getInspectedNode().getOrDefault());
	}

	@Test
	public void ignoresEnterWithoutInspectedNode() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI());
		this.bridges.getUi().keyTyped('\n', Key.ENTER);
		Assert.assertNull(panel.getInspectedNode().getOrDefault());
		Assert.assertFalse(panel.getInspectedNodeLocked().getOrDefault());
	}

	@Test
	public void selectsTheParentFromTheHierarchyLine() {
		final ContainerNode container = ContainerNode.create(100D, 100D, 600D, 400D);
		RectNode.create(100D, 100D, 300D, 150D).attach(container);
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(container));
		DevNodeTest.lock(this.bridges, 250D, 250D);
		DevNodeTest.click(this.bridges, DevNodeTest.text(panel, "hierarchy: ContainerNode - RectNode"));
		Assert.assertSame(container, panel.getInspectedNode().getOrDefault());
		Assert.assertTrue(panel.getInspectedNodeLocked().getOrDefault());
		DevNodeTest.click(this.bridges, DevNodeTest.text(panel, "hierarchy: ContainerNode"));
		Assert.assertSame(container, panel.getInspectedNode().getOrDefault());
	}

	@Test
	public void selectsAChildFromItsList() {
		final ContainerNode container = ContainerNode.create(100D, 100D, 600D, 400D);
		final RectNode rect = RectNode.create(500D, 300D, 50D, 50D).attach(container);
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(container));
		DevNodeTest.lock(this.bridges, 150D, 150D);
		Assert.assertSame(container, panel.getInspectedNode().getOrDefault());
		Assert.assertTrue(DevNodeTest.texts(panel).contains("children: 1"));
		this.bridges.move(1600D, 900D).frame();
		this.bridges.scroll(-120).frames(10).scroll(-120).frames(60);
		DevNodeTest.click(this.bridges, DevNodeTest.text(panel, "RectNode"));
		Assert.assertSame(rect, panel.getInspectedNode().getOrDefault());
		Assert.assertTrue(panel.getInspectedNodeLocked().getOrDefault());
	}

	@Test
	public void togglesTheInspectionWithItsButton() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		final ResourceNode button = DevNodeTest.buttons(panel).get(0);
		Assert.assertEquals(DevNodeTest.ACTION.getRGB(), button.getColor().getRGB());
		DevNodeTest.click(this.bridges, button);
		Assert.assertFalse(panel.getInspectSignal().getOrDefault());
		Assert.assertEquals(new Color(250, 250, 250).getRGB(), DevNodeTest.buttons(panel).get(0).getColor().getRGB());
		this.bridges.move(250D, 250D).frames(2);
		Assert.assertNull(panel.getInspectedNode().getOrDefault());
		Assert.assertTrue(DevNodeTest.draws(this.bridges, DevNodeTest.ACTION, 0.3F).isEmpty());
		DevNodeTest.click(this.bridges, DevNodeTest.buttons(panel).get(0));
		Assert.assertTrue(panel.getInspectSignal().getOrDefault());
	}

	@Test
	public void togglesTheInspectionWithI() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		DevNodeTest.lock(this.bridges, 250D, 250D);
		this.bridges.getUi().keyTyped('i', Key.I);
		Assert.assertFalse(panel.getInspectSignal().getOrDefault());
		Assert.assertFalse(panel.getInspectedNodeLocked().getOrDefault());
		Assert.assertNull(panel.getInspectedNode().getOrDefault());
		this.bridges.getUi().keyTyped('i', Key.I);
		Assert.assertTrue(panel.getInspectSignal().getOrDefault());
	}

	@Test
	public void reloadsTheUiWithItsButton() {
		final DevUI ui = new DevUI();
		final DevNode panel = DevNodeTest.open(this.bridges, ui);
		DevNodeTest.click(this.bridges, DevNodeTest.buttons(panel).get(1));
		Assert.assertEquals(2, ui.inits);
		this.bridges.frames(2);
		Assert.assertTrue(panel.getReloadSignal().getOrDefault());
		Assert.assertTrue(panel.getReloadAnimator().getValue() > 0F);
		this.bridges.frames(20);
		Assert.assertFalse(panel.getReloadSignal().getOrDefault());
		Assert.assertEquals(0F, panel.getReloadAnimator().getValue(), 0F);
	}

	@Test
	public void reloadsTheUiWithR() {
		final DevUI ui = new DevUI();
		DevNodeTest.open(this.bridges, ui);
		this.bridges.getUi().keyTyped('r', Key.R);
		Assert.assertEquals(2, ui.inits);
	}

	@Test
	public void reloadsTheUiOnceWithControlR() {
		final DevUI ui = new DevUI();
		DevNodeTest.open(this.bridges, ui);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyTyped('r', Key.R);
		Assert.assertEquals(2, ui.inits);
	}

	@Test
	public void highlightsTheNodesUpdatedInTheLastTwoSeconds() {
		DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		final Draw highlight = DevNodeTest.draws(this.bridges, DevNodeTest.UPDATE, 0.3F).get(0);
		Assert.assertEquals(200D, highlight.getLeft(), 0.001D);
		Assert.assertEquals(200D, highlight.getTop(), 0.001D);
		Assert.assertEquals(500D, highlight.getRight(), 0.001D);
		Assert.assertEquals(350D, highlight.getBottom(), 0.001D);
		this.bridges.getClock().advance(1500L);
		this.bridges.frame();
		Assert.assertEquals(1, DevNodeTest.draws(this.bridges, DevNodeTest.UPDATE, 0.3F * (2F - 1564F / 1000F)).size());
		this.bridges.getClock().advance(500L);
		this.bridges.frame();
		Assert.assertTrue(this.bridges.getRender().getDraws(DevNodeTest.UPDATE.r, DevNodeTest.UPDATE.g, DevNodeTest.UPDATE.b).isEmpty());
	}

	@Test
	public void togglesTheUpdateHighlightWithItsButton() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		DevNodeTest.click(this.bridges, DevNodeTest.buttons(panel).get(2));
		Assert.assertFalse(panel.getEyeSignal().getOrDefault());
		this.bridges.frame();
		Assert.assertTrue(this.bridges.getRender().getDraws(DevNodeTest.UPDATE.r, DevNodeTest.UPDATE.g, DevNodeTest.UPDATE.b).isEmpty());
		DevNodeTest.click(this.bridges, DevNodeTest.buttons(panel).get(2));
		Assert.assertTrue(panel.getEyeSignal().getOrDefault());
	}

	@Test
	public void togglesTheUpdateHighlightWithU() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI());
		this.bridges.getUi().keyTyped('u', Key.U);
		Assert.assertFalse(panel.getEyeSignal().getOrDefault());
		this.bridges.getUi().keyTyped('u', Key.U);
		Assert.assertTrue(panel.getEyeSignal().getOrDefault());
	}

	@Test
	public void drawsAGridAroundTheMouse() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI());
		this.bridges.getUi().keyTyped('g', Key.G);
		Assert.assertTrue(panel.getGridSignal().getOrDefault());
		this.bridges.move(400D, 300D).frame();
		final List<Draw> rulers = DevNodeTest.draws(this.bridges, DevNodeTest.ACTION, 1F).stream().filter(draw -> draw.getShader() == null && draw.getTop() < 1000D).collect(Collectors.toList());
		Assert.assertEquals(4, rulers.size());
		Assert.assertEquals(Arrays.asList(0D, 300D, 400D, 303D), DevNodeTest.bounds(rulers.get(0)));
		Assert.assertEquals(Arrays.asList(400D, 300D, 1920D, 303D), DevNodeTest.bounds(rulers.get(1)));
		Assert.assertEquals(Arrays.asList(400D, 0D, 403D, 300D), DevNodeTest.bounds(rulers.get(2)));
		Assert.assertEquals(Arrays.asList(400D, 300D, 403D, 1080D), DevNodeTest.bounds(rulers.get(3)));
	}

	@Test
	public void togglesTheGridWithG() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI());
		this.bridges.getUi().keyTyped('g', Key.G);
		this.bridges.getUi().keyTyped('g', Key.G);
		Assert.assertFalse(panel.getGridSignal().getOrDefault());
	}

	@Test
	public void changesTheGridColorOnARightClick() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI());
		this.bridges.getUi().keyTyped('g', Key.G);
		this.bridges.move(400D, 300D).frame();
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		Assert.assertEquals(1, panel.getGridColorIndex());
		this.bridges.frame();
		Assert.assertEquals(4, DevNodeTest.draws(this.bridges, DevNodeTest.UPDATE, 1F).stream().filter(draw -> draw.getShader() == null && draw.getTop() < 1000D).count());
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		Assert.assertEquals(0, panel.getGridColorIndex());
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(0, panel.getGridColorIndex());
	}

	@Test
	public void keepsItsGridColorWithoutGrid() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI());
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		Assert.assertEquals(0, panel.getGridColorIndex());
	}

	@Test
	public void togglesTheGridWithItsButton() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI());
		DevNodeTest.click(this.bridges, DevNodeTest.buttons(panel).get(3));
		Assert.assertTrue(panel.getGridSignal().getOrDefault());
		DevNodeTest.click(this.bridges, DevNodeTest.buttons(panel).get(3));
		Assert.assertFalse(panel.getGridSignal().getOrDefault());
	}

	@Test
	public void ignoresTheKeysBelowAnotherUi() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI());
		this.bridges.open(new DevUI());
		panel.onKeyPressed('g', Key.G, InternalContext.create());
		Assert.assertFalse(panel.getGridSignal().getOrDefault());
	}

	@Test
	public void ignoresACancelledInput() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		this.bridges.move(250D, 250D).frame();
		panel.onKeyPressed('g', Key.G, InternalContext.create(true));
		panel.onMousePressed(250D, 250D, ClickType.LEFT, InternalContext.create(true));
		Assert.assertFalse(panel.getGridSignal().getOrDefault());
		Assert.assertFalse(panel.getInspectedNodeLocked().getOrDefault());
	}

	@Test
	public void ignoresTheClicksBelowAnotherUi() {
		final DevNode panel = DevNodeTest.open(this.bridges, new DevUI(RectNode.create(200D, 200D, 300D, 150D)));
		this.bridges.move(250D, 250D).frame();
		this.bridges.open(new DevUI());
		panel.onMousePressed(250D, 250D, ClickType.LEFT, InternalContext.create());
		Assert.assertFalse(panel.getInspectedNodeLocked().getOrDefault());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAClickWithoutButton() {
		DevNode.create(0D, 0D).mousePressed(0D, 0D, null, InternalContext.create());
	}

	private static DevNode open(final HeadlessBridges bridges, final DevUI ui) {
		bridges.open(ui);
		bridges.getUi().keyTyped('\0', Key.F3);
		bridges.frames(2);
		return (DevNode) ui.getDevNode();
	}

	private static void lock(final HeadlessBridges bridges, final double mouseX, final double mouseY) {
		bridges.move(mouseX, mouseY).frame();
		bridges.getUi().mousePressed(ClickType.LEFT);
		bridges.frames(2);
	}

	private static void click(final HeadlessBridges bridges, final Node node) {
		bridges.move(node.getAbsoluteX() + node.getWidth() / 2D, node.getAbsoluteY() + node.getHeight() / 2D).frame();
		bridges.getUi().mousePressed(ClickType.LEFT);
		bridges.frames(2);
	}

	private static List<ResourceNode> buttons(final DevNode panel) {
		final List<ResourceNode> buttons = new ArrayList<>();
		for (final Node node : panel.getChildren().recursive()) {
			if (node instanceof ResourceNode) {
				buttons.add((ResourceNode) node);
			}
		}
		return buttons;
	}

	private static List<String> texts(final DevNode panel) {
		final List<String> texts = new ArrayList<>();
		for (final Node node : panel.getChildren().recursive()) {
			if (node instanceof TextNode) {
				texts.add(((TextNode) node).getText().getText());
			}
		}
		return texts;
	}

	private static TextNode text(final DevNode panel, final String text) {
		for (final Node node : panel.getChildren().recursive()) {
			if (node instanceof TextNode && ((TextNode) node).getText().getText().equals(text)) {
				return (TextNode) node;
			}
		}
		throw new AssertionError(text + " in " + DevNodeTest.texts(panel));
	}

	private static List<Draw> draws(final HeadlessBridges bridges, final Color color, final float alpha) {
		return bridges.getRender().getDraws(color.r, color.g, color.b).stream().filter(draw -> Math.abs(draw.getAlpha() - alpha) < 0.0001F).collect(Collectors.toList());
	}

	private static List<Double> bounds(final Draw draw) {
		return Arrays.asList((double) Math.round(draw.getLeft()), (double) Math.round(draw.getTop()), (double) Math.round(draw.getRight()), (double) Math.round(draw.getBottom()));
	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class DevUI extends UI {

		private final List<String> hovers = new ArrayList<>();
		private final Node[]       nodes;

		private int inits;

		public DevUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			this.inits++;
			super.add(this.nodes);
		}

		@Override
		public void drawHover(final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY) {
			this.hovers.addAll(lines);
		}

	}

	public static final class LabelNode extends Node {

		private static final Color FILL = new Color(0.2F, 0.4F, 0.6F, 1F);

		private String label;
		private Object target;

		private LabelNode() {
			super(200D, 200D, 300D, 150D);
			this.label = "Play";
		}

		@Override
		public void draw(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), LabelNode.FILL);
		}

	}

}