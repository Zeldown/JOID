package dev.joid.lib.bridge.ui;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.UI;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public class UIBridgeRegistryTest {

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

	@Test(expected = NullPointerException.class)
	public void refusesToRouteAMissingUi() {
		UIBridgeRegistry.create().get((UI) null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToRouteAMissingClass() {
		UIBridgeRegistry.create().get((Class<? extends UI>) null);
	}

	public static final class MenuUI extends UI {}

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
		public void add(final @NonNull UI ui) {
			super.getUiList().add(ui);
		}

		@Override
		public void remove(final @NonNull UI ui) {
			super.getUiList().remove(ui);
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

}