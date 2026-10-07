package dev.joid.lib.bridge.ui;

import java.util.List;

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
		final RoutingBridge menus = new RoutingBridge(MenuUI.class, 0);
		final RoutingBridge huds = new RoutingBridge(HudUI.class, 0);
		registry.register(menus);
		registry.register(huds);
		Assert.assertSame(menus, registry.get(new MenuUI()));
		Assert.assertSame(huds, registry.get(new HudUI()));
		Assert.assertSame(menus, registry.get(MenuUI.class));
		Assert.assertSame(huds, registry.get(HudUI.class));
	}

	@Test
	public void routesAUiToNoBridgeWhenNoneHandlesIt() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		registry.register(new RoutingBridge(MenuUI.class, 0));
		Assert.assertNull(registry.get(new HudUI()));
		Assert.assertNull(registry.get(HudUI.class));
	}

	@Test
	public void prefersTheLatestBridgeOfTheSameIndex() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RoutingBridge latest = new RoutingBridge(UI.class, 0);
		registry.register(new RoutingBridge(UI.class, 0));
		registry.register(latest);
		Assert.assertSame(latest, registry.get(new MenuUI()));
		Assert.assertSame(latest, registry.get(MenuUI.class));
	}

	@Test
	public void prefersTheHighestIndexOverTheLatestBridge() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RoutingBridge highest = new RoutingBridge(UI.class, 5);
		registry.register(highest);
		registry.register(new RoutingBridge(UI.class, 0));
		Assert.assertSame(highest, registry.get(new MenuUI()));
		Assert.assertSame(highest, registry.get(MenuUI.class));
	}

	@Test
	public void forgetsAnUnregisteredBridge() {
		final UIBridgeRegistry registry = UIBridgeRegistry.create();
		final RoutingBridge bridge = new RoutingBridge(UI.class, 0);
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
		private final int                 index;

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
		public int getIndex() {
			return this.index;
		}

		@Override
		public void drawHover(final @NonNull UI ui, final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY) {}

	}

}