package dev.joid.test.snapshot;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.joid.internal.font.DevFont;
import dev.joid.lib.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.color.Color;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class SnapshotRunnerCommandTest {

	private static SnapshotRunner runner;

	@BeforeClass
	public static void startRunner() {
		DevFont.load();
		SnapshotRunnerCommandTest.runner = SnapshotRunner.start(new SnapshotRunnerTest.HeadlessBackend());
	}

	@AfterClass
	public static void stopRunner() {
		SnapshotRunnerCommandTest.runner.stop();
		HeadlessBridges.forgetTheCursor();
		HeadlessBridges.forgetTheScreenSize();
	}

	@Test
	public void resetsTheInterfaceScaleBetweenScenarios() {
		SnapshotRunnerCommandTest.runner.execute("ui " + TraceUI.class.getName(), "scale 2");
		SnapshotRunnerCommandTest.runner.execute("ui " + TraceUI.class.getName());
		Assert.assertEquals(1D, JOID.getUi(TraceUI.class).getView().getInterfaceScale(), 0D);
	}

	@Test
	public void rendersAFrameAfterAMove() {
		SnapshotRunnerCommandTest.runner.execute("ui " + TraceUI.class.getName(), "move 100 200");
		final TraceUI ui = JOID.getUi(TraceUI.class);
		Assert.assertEquals(ui.getView().toUiX(100D), ui.getMouseX(), 0D);
		Assert.assertEquals(ui.getView().toUiY(200D), ui.getMouseY(), 0D);
	}

	@Test
	public void dragsWithThePositionOfEachStep() {
		SnapshotRunnerCommandTest.runner.execute("ui " + TraceUI.class.getName(), "move 100 100", "press LEFT", "moveto 300 140 160", "release LEFT");
		final TraceUI ui = JOID.getUi(TraceUI.class);
		Assert.assertEquals(ui.getView().toUiX(300D), ui.getDragX(), 0D);
		Assert.assertEquals(ui.getView().toUiY(140D), ui.getDragY(), 0D);
	}

	@Test
	public void dragsWithMovesAndTheDefaultButton() {
		SnapshotRunnerCommandTest.runner.execute("ui " + TraceUI.class.getName(), "move 100 100", "press", "move 300 140", "release");
		final TraceUI ui = JOID.getUi(TraceUI.class);
		Assert.assertEquals(ui.getView().toUiX(300D), ui.getDragX(), 0D);
		Assert.assertEquals(ui.getView().toUiY(140D), ui.getDragY(), 0D);
	}

	@Test
	public void typesTheSpacesAtTheEndOfTheText() {
		SnapshotRunnerCommandTest.runner.execute("ui " + TypeUI.class.getName(), "\ttype   shout  ");
		Assert.assertEquals("  shout  ", JOID.getUi(TypeUI.class).getText().toString());
	}

	@Test
	public void holdsEachKeyWhileItTypesIt() {
		SnapshotRunnerCommandTest.runner.execute("ui " + HeldUI.class.getName(), "type ab");
		Assert.assertEquals(Arrays.asList(true, true), JOID.getUi(HeldUI.class).getHeld());
		Assert.assertFalse(Key.A.isDown());
		Assert.assertFalse(Key.B.isDown());
	}

	@Test
	public void createsAUIWhateverTheVisibilityOfItsConstructor() {
		SnapshotRunnerCommandTest.runner.execute("ui " + PrivateUI.class.getName());
		Assert.assertNotNull(JOID.getUi(PrivateUI.class));
	}

	@Test
	public void namesTheUIThatCannotBeCreated() {
		try {
			SnapshotRunnerCommandTest.runner.execute("ui " + ArgumentUI.class.getName());
			Assert.fail();
		} catch (final IllegalArgumentException exception) {
			Assert.assertEquals("The snapshot UI " + ArgumentUI.class.getName() + " cannot be created: give it a constructor without arguments, of any visibility", exception.getMessage());
		}
	}

	@Test
	public void namesTheExceptionThrownByTheConstructorOfTheUI() {
		try {
			SnapshotRunnerCommandTest.runner.execute("ui " + ThrowingUI.class.getName());
			Assert.fail();
		} catch (final IllegalArgumentException exception) {
			Assert.assertEquals("The snapshot UI " + ThrowingUI.class.getName() + " cannot be created: its constructor threw java.lang.IllegalStateException: Broken", exception.getMessage());
			Assert.assertEquals("Broken", exception.getCause().getMessage());
		}
	}

	@Test
	public void drawsTooltipsWithTheDevFont() {
		Assert.assertSame(DevFont.MONTSERRAT, BridgeHandler.UI.getBridge(SnapshotUIBridge.class).getHoverInfo().getFont());
	}

	@Test
	public void playsTheOutTransitionOfAPopupClosedByEscape() {
		SnapshotRunnerCommandTest.runner.execute("ui " + TraceUI.class.getName(), "open " + PopupUI.class.getName(), "shot opening", "wait 60", "shot growing", "wait 300", "shot opened", "key ESCAPE", "shot closing", "wait 60", "shot shrinking");
		final PopupUI ui = JOID.getUi(PopupUI.class);
		Assert.assertNotNull(ui);
		Assert.assertTrue(ui.getTransition().getOut().isRunning());
		Assert.assertTrue(ui.getTransition().getOut().getAnimator().peek() > 0F);
		Assert.assertTrue(ui.getTransition().getOut().getAnimator().peek() < 1F);
	}

	@Test
	public void advancesTheClockByTheExactDuration() {
		SnapshotRunnerCommandTest.runner.execute();
		final long start = BridgeHandler.CLOCK.get().currentTimeMillis();
		SnapshotRunnerCommandTest.runner.execute("wait 40");
		Assert.assertEquals(start + 40L, BridgeHandler.CLOCK.get().currentTimeMillis());
		SnapshotRunnerCommandTest.runner.execute("moveto 10 10 40");
		Assert.assertEquals(start + 40L, BridgeHandler.CLOCK.get().currentTimeMillis());
	}

	@Test
	public void waitsForTheDownloadsBeforeAShot() {
		final Resource resource = Resource.of(new DownloadAsset());
		SnapshotRunnerCommandTest.runner.execute("shot download");
		Assert.assertNotNull(resource.getResourceData().getDecoder());
	}

	@Test
	public void doesNotWaitForAFailedResourceBeforeAShot() {
		SnapshotRunnerCommandTest.runner.execute("ui " + FailedResourceUI.class.getName(), "shot failed");
		Assert.assertTrue(JOID.getUi(FailedResourceUI.class).getResource().isFailed());
	}

	@Test
	public void failsAWaitWhenAResourceNeverSettles() {
		Resource.of(new ByteArrayInputStream(new byte[] {0})).decoder(new RestlessDecoder());
		try {
			SnapshotRunnerCommandTest.runner.execute("wait 16");
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("A resource never settled in 30s: it kept decoding or rendering, check that the resources of the scenario stop decoding", exception.getMessage());
		} finally {
			SnapshotRunnerCommandTest.runner.execute("ui " + TraceUI.class.getName());
		}
	}

	@Test
	public void decodesTheResourcesOfEachUIAfresh() {
		SnapshotRunnerCommandTest.runner.execute("ui " + IconUI.class.getName());
		final ResourceData data = JOID.getUi(IconUI.class).getResource().getResourceData();
		SnapshotRunnerCommandTest.runner.execute("ui " + IconUI.class.getName());
		Assert.assertNotSame(data, JOID.getUi(IconUI.class).getResource().getResourceData());
	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class TraceUI extends UI {

		@Getter private double dragX;
		@Getter private double dragY;

		@Override
		public void init() {
			super.handleMouseDragged(event -> {
				this.dragX = event.getMouseX();
				this.dragY = event.getMouseY();
			});
		}

	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class TypeUI extends UI {

		@Getter
		private final StringBuilder text = new StringBuilder();

		@Override
		public void init() {
			super.handleCharTyped(event -> this.text.appendCodePoint(event.getCodepoint()));
		}

	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class HeldUI extends UI {

		@Getter
		private final List<Boolean> held = new ArrayList<>();

		@Override
		public void init() {
			super.handleKeyPressed(event -> this.held.add(event.getKey().isDown()));
		}

	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@UIDataDebug(profiler = false, hotreload = false)
	public static final class PrivateUI extends UI {

		@Override
		public void init() {}

	}

	@Getter
	@RequiredArgsConstructor
	@UIDataDebug(profiler = false, hotreload = false)
	public static final class ArgumentUI extends UI {

		private final int argument;

		@Override
		public void init() {}

	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class ThrowingUI extends UI {

		public ThrowingUI() {
			throw new IllegalStateException("Broken");
		}

		@Override
		public void init() {}

	}

	@UIDataPopup(active = true)
	@UIDataDebug(profiler = false, hotreload = false)
	public static final class PopupUI extends UI {

		@Override
		public void init() {
			RectNode.create(800, 430, 320, 220).color(Color.WHITE).attach(this);
		}

	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class FailedResourceUI extends UI {

		@Getter
		private final Resource resource = Resource.of(new ByteArrayInputStream(new byte[] {0, 1, 2, 3}));

		@Override
		public void init() {
			ResourceNode.create(0, 0, 10, 10).resource(this.resource).attach(this);
		}

	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class IconUI extends UI {

		@Getter
		private final Resource resource = Resource.of("assets/dev/textures/icons/eye.png");

		@Override
		public void init() {
			ResourceNode.create(0, 0, 10, 10).resource(this.resource).attach(this);
		}

	}

	private static final class RestlessDecoder implements IResourceDecoder {

		@Override
		public void prepare(final @NonNull ResourceData resource) {}

		@Override
		public void decode(final @NonNull ResourceData resource) {}

		@Override
		public void upload(final @NonNull ResourceData resource) {}

		@Override
		public void clear(final @NonNull ResourceData resource) {}

		@Override
		public boolean isSettled() {
			return false;
		}

	}

	private static final class DownloadAsset extends Asset {

		private DownloadAsset() {
			super("snapshot-download");
		}

		@Override
		public @NonNull InputStream open() {
			try {
				Thread.sleep(300L);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return JOID.class.getResourceAsStream("/assets/dev/textures/icons/eye.png");
		}

		@Override
		public boolean isRemote() {
			return true;
		}

	}

}