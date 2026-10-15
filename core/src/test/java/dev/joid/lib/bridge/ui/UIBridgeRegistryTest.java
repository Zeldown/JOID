package dev.joid.lib.bridge.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.ConsumableEvent;
import dev.joid.lib.ui.node.Node;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public class UIBridgeRegistryTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<String> trace = new ArrayList<>();

	@Test
	public void createsAnEmptyRegistryEachTime() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		Assert.assertNotSame(registry, UIBridgeRegistry.create());
		Assert.assertNull(registry.get(new MenuUI()));
		Assert.assertNull(registry.get(MenuUI.class));
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToGiveABridgeBeforeOneIsRegistered() {
		UIBridgeRegistry.create().get();
	}

	@Test
	public void routesAUiToTheBridgeThatHandlesIt() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RoutingBridge menus = new RoutingBridge(MenuUI.class);
		final RoutingBridge huds = new RoutingBridge(HudUI.class);
		registry.register(menus);
		registry.register(huds);
		Assert.assertSame(menus, registry.get(new MenuUI()));
		Assert.assertSame(huds, registry.get(new HudUI()));
		Assert.assertSame(menus, registry.get(MenuUI.class));
		Assert.assertSame(huds, registry.get(HudUI.class));
	}

	@Test
	public void routesAnOpenUiToTheBridgeThatHoldsIt() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RoutingBridge huds = new RoutingBridge(HudUI.class);
		final RoutingBridge latest = new RoutingBridge(UI.class);
		final MenuUI ui = new MenuUI();
		registry.register(huds);
		registry.register(latest, 5);
		huds.add(ui);
		Assert.assertSame(huds, registry.get(ui));
		Assert.assertSame(latest, registry.get(new MenuUI()));
		huds.remove(ui);
		Assert.assertSame(latest, registry.get(ui));
	}

	@Test
	public void routesAUiToNoBridgeWhenNoneHandlesIt() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		registry.register(new RoutingBridge(MenuUI.class));
		Assert.assertNull(registry.get(new HudUI()));
		Assert.assertNull(registry.get(HudUI.class));
	}

	@Test
	public void prefersTheLatestBridgeOfTheSamePriority() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RoutingBridge latest = new RoutingBridge(UI.class);
		registry.register(new RoutingBridge(UI.class));
		registry.register(latest);
		Assert.assertSame(latest, registry.get(new MenuUI()));
		Assert.assertSame(latest, registry.get(MenuUI.class));
	}

	@Test
	public void prefersTheHighestPriorityOverTheLatestBridge() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RoutingBridge highest = new RoutingBridge(UI.class);
		registry.register(highest, 5);
		registry.register(new RoutingBridge(UI.class));
		Assert.assertSame(highest, registry.get(new MenuUI()));
		Assert.assertSame(highest, registry.get(MenuUI.class));
	}

	@Test
	public void routesToARegisteredBridgeBeforeTheDefaultOne() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RoutingBridge fallback = new RoutingBridge(UI.class);
		final RoutingBridge lowest = new RoutingBridge(UI.class);
		registry.registerDefault(fallback);
		registry.register(lowest, -100);
		Assert.assertSame(lowest, registry.get(new MenuUI()));
		registry.unregister(lowest);
		Assert.assertSame(fallback, registry.get(new MenuUI()));
	}

	@Test
	public void routesAnOpenUiToItsHolderWhateverThePriority() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RoutingBridge low = new RoutingBridge(UI.class);
		final RoutingBridge high = new RoutingBridge(UI.class);
		final MenuUI ui = new MenuUI();
		registry.register(low, -1);
		registry.register(high, 1);
		low.add(ui);
		Assert.assertSame(low, registry.get(ui));
		Assert.assertSame(high, registry.get(new MenuUI()));
	}

	@Test
	public void forgetsAnUnregisteredBridge() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RoutingBridge bridge = new RoutingBridge(UI.class);
		registry.register(bridge);
		registry.unregister(bridge);
		Assert.assertNull(registry.get(new MenuUI()));
	}

	@Test
	public void framesEveryBridgeFromTheLowestPriority() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RecordingRenderBridge render = this.bridges.getRender();
		registry.register(this.bridge("high", Color.BLUE), 1);
		registry.register(this.bridge("low", Color.RED), -1);
		final int clears = render.getColorClears();
		registry.frame();
		Assert.assertEquals(Arrays.asList("update low", "update high", "draw low", "draw high"), this.trace);
		Assert.assertEquals(Color.RED, render.getClearedColor());
		Assert.assertEquals(clears + 1, render.getColorClears());
	}

	@Test
	public void runsItsFrameTasksOnceAFrameBeforeTheBridges() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		registry.register(this.bridge("high", Color.BLUE), 1);
		registry.register(this.bridge("low", Color.RED), -1);
		Assert.assertSame(registry, registry.onFrame(() -> this.trace.add("frame")));
		registry.frame();
		Assert.assertEquals(Arrays.asList("frame", "update low", "update high", "draw low", "draw high"), this.trace);
	}

	@Test
	public void advancesTheAutomaticAnimatorsOnceAFrameWithSeveralBridges() {
		final TweenAnimator animator = TweenAnimator.create().sequence(64F, 1F);
		final WindowUIBridge other = this.bridge("other", Color.RED);
		BridgeHandler.UI.register(other, -1);
		try {
			BridgeHandler.UI.frame();
			this.bridges.open(new AnimatorUI(animator));
			Assert.assertTrue(animator.isAutomatic());
			Assert.assertEquals(0.25F, animator.get(), 0F);
			this.bridges.getClock().advance(16L);
			this.bridges.getUi().update();
			other.update();
			Assert.assertEquals(0.25F, animator.get(), 0F);
			BridgeHandler.UI.frame();
			Assert.assertEquals(0.5F, animator.get(), 0F);
			this.bridges.getClock().advance(16L);
			this.bridges.getUi().frame();
			Assert.assertEquals(0.75F, animator.get(), 0F);
		} finally {
			BridgeHandler.UI.unregister(other);
		}
	}

	@Test
	public void clearsNothingWithoutUIBridge() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final int clears = this.bridges.getRender().getColorClears();
		registry.frame();
		Assert.assertEquals(clears, this.bridges.getRender().getColorClears());
		Assert.assertEquals(0, this.bridges.getRender().getFrameCommands());
	}

	@Test
	public void resizesTheBridgesWhenTheWindowChanges() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RecordingRenderBridge render = this.bridges.getRender();
		registry.register(this.bridge("menu", Color.RED));
		registry.frame();
		Assert.assertEquals(1920, render.getState().getViewportWidth());
		this.bridges.getWindow().setWidth(800);
		this.bridges.getWindow().setHeight(600);
		registry.frame();
		Assert.assertEquals(800, render.getState().getViewportWidth());
		Assert.assertEquals(600, render.getState().getViewportHeight());
		this.bridges.getWindow().setWidth(0);
		registry.frame();
		Assert.assertEquals(800, render.getState().getViewportWidth());
	}

	@Test
	public void sendsAnEventToTheHighestBridgeFirstAndStopsAtTheOneThatConsumesIt() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final TraceUI high = this.open(registry, "high", 1);
		this.open(registry, "low", 0);
		high.cancel = true;
		Assert.assertTrue(registry.mousePressed(MouseButton.LEFT));
		Assert.assertTrue(registry.keyPressed(Key.A, Key.A));
		Assert.assertTrue(registry.charTyped('a'));
		Assert.assertTrue(registry.mouseScroll(0D, 1D));
		Assert.assertEquals(Arrays.asList("pressed high", "key high", "char high", "scroll high"), this.trace);
	}

	@Test
	public void sendsAnEventNoBridgeConsumesToEveryBridge() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		this.open(registry, "high", 1);
		this.open(registry, "low", 0);
		Assert.assertFalse(registry.mousePressed(MouseButton.LEFT));
		Assert.assertFalse(registry.keyPressed(Key.A, Key.A));
		Assert.assertEquals(Arrays.asList("pressed high", "pressed low", "key high", "key low"), this.trace);
	}

	@Test
	public void releasesEveryBridgeThatSawThePress() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final TraceUI high = this.open(registry, "high", 1);
		this.open(registry, "low", 0);
		registry.mousePressed(MouseButton.LEFT);
		high.cancel = true;
		this.trace.clear();
		Assert.assertTrue(registry.mouseReleased(MouseButton.LEFT));
		Assert.assertEquals(Arrays.asList("released high", "released low"), this.trace);
	}

	@Test
	public void releasesOnlyTheBridgesThatSawThePress() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final TraceUI high = this.open(registry, "high", 1);
		this.open(registry, "low", 0);
		high.cancel = true;
		registry.mousePressed(MouseButton.LEFT);
		high.cancel = false;
		this.trace.clear();
		Assert.assertTrue(registry.mouseReleased(MouseButton.LEFT));
		Assert.assertEquals(Collections.singletonList("released high"), this.trace);
	}

	@Test
	public void consumesTheReleaseOfAKeyABridgeConsumed() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		this.open(registry, "high", 1);
		final TraceUI low = this.open(registry, "low", 0);
		low.cancel = true;
		Assert.assertTrue(registry.keyPressed(Key.A, Key.A));
		low.cancel = false;
		Assert.assertTrue(registry.keyReleased(Key.A));
		Assert.assertFalse(registry.keyReleased(Key.A));
		Assert.assertFalse(registry.keyReleased(Key.B));
	}

	@Test
	public void releasesAtItsPositionTheKeyOfThePress() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final TraceUI ui = this.open(registry, "ui", 0);
		ui.cancel = true;
		Assert.assertTrue(registry.keyPressed(Key.Q, Key.A));
		Assert.assertTrue(registry.keyReleased(Key.Q));
		Assert.assertFalse(registry.keyReleased(Key.A));
	}

	@Test(expected = NullPointerException.class)
	public void refusesToRouteAMissingUi() {
		UIBridgeRegistry.create().get((UI) null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToRouteAMissingClass() {
		UIBridgeRegistry.create().get((Class<? extends UI>) null);
	}

	private WindowUIBridge bridge(final String name, final Color background) {
		final WindowUIBridge bridge = WindowUIBridge.create().background(background);
		bridge.add(new TraceUI(name, this.trace));
		return bridge;
	}

	private TraceUI open(final UIBridgeRegistry registry, final String name, final int priority) {
		final TraceUI ui = new TraceUI(name, this.trace);
		final WindowUIBridge bridge = WindowUIBridge.create();
		bridge.add(ui);
		registry.register(bridge, priority);
		return ui;
	}

	public static final class MenuUI extends UI {}

	@AllArgsConstructor
	public static final class AnimatorUI extends UI {

		private final TweenAnimator animator;

		@Override
		public void init() {
			this.animator.start();
		}

	}

	public static final class HudUI extends UI {}

	@AllArgsConstructor
	public static final class RoutingBridge extends UIBridge {

		private final Class<? extends UI> type;

		@Override
		public void open(final @NonNull UI ui) {
			this.add(ui);
		}

		@Override
		public void close(final @NonNull UI ui) {
			this.remove(ui);
		}

		@Override
		public boolean canHandle(final @NonNull UI ui) {
			return this.type.isInstance(ui);
		}

		@Override
		public boolean canHandle(final @NonNull Class<? extends UI> clazz) {
			return this.type.isAssignableFrom(clazz);
		}

		@Override
		public void drawHover(final @NonNull UI ui, final @NonNull Object content, final double mouseX, final double mouseY) {}

	}

	public static final class TraceUI extends UI {

		private final String       name;
		private final List<String> trace;

		private boolean cancel;

		public TraceUI(final String name, final List<String> trace) {
			this.name = name;
			this.trace = trace;
			super.handleMousePressed(event -> this.record("pressed", event));
			super.handleMouseReleased(event -> this.record("released", event));
			super.handleMouseScroll(event -> this.record("scroll", event));
			super.handleKeyPressed(event -> this.record("key", event));
			super.handleCharTyped(event -> this.record("char", event));
		}

		@Override
		public void init() {
			super.add(new TraceNode(() -> this.trace.add("draw " + this.name)));
		}

		@Override
		public void update() {
			this.trace.add("update " + this.name);
		}

		private void record(final String event, final ConsumableEvent input) {
			this.trace.add(event + " " + this.name);
			if (this.cancel) {
				input.consume();
			}
		}

	}

	public static final class TraceNode extends Node {

		private final Runnable drawing;

		private TraceNode(final Runnable drawing) {
			super(0D, 0D, 0D, 0D);
			this.drawing = drawing;
		}

		@Override
		public void draw(final double mouseX, final double mouseY) {
			this.drawing.run();
		}

	}

}