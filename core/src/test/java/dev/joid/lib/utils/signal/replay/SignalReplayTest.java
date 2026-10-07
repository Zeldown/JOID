package dev.joid.lib.utils.signal.replay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.ReflectPermission;
import java.nio.charset.StandardCharsets;
import java.security.Permission;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.replay.ReplayPitfallsUI;
import dev.joid.demo.replay.ReplayScenarioUI;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.replay.IReplayRemapper;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.signal.SignalContext;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import example.replay.LabelNode;
import example.replay.LabelUI;

public class SignalReplayTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void followsTheNativeExpressionsOfThePitfalls() {
		final ReplayPitfallsUI ui = new ReplayPitfallsUI();
		final String output = this.open(ui);
		Assert.assertEquals("", output);
		Assert.assertEquals("Next: 1, double: 0", SignalReplayTest.node(ui, 0).getText().get());
		ui.getClicks().set(5);
		Assert.assertEquals("Next: 6, double: 10", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertSame(Color.GREEN, SignalReplayTest.node(ui, 1).getColor().get());
		Assert.assertEquals(Boolean.TRUE, SignalReplayTest.node(ui, 2).getShown().get());
		Assert.assertEquals("Clicks + bonus: 105", SignalReplayTest.node(ui, 3).getText().get());
		Assert.assertEquals("ADA has 5 clicks", SignalReplayTest.node(ui, 4).getText().get());
		Assert.assertEquals("GRACE has 5 clicks", SignalReplayTest.node(ui, 5).getText().get());
		Assert.assertEquals("Clicks + offset: 15", SignalReplayTest.node(ui, 6).getText().get());
		Assert.assertEquals("Doubled: 10", SignalReplayTest.node(ui, 7).getText().get());
		Assert.assertEquals("A 5", SignalReplayTest.node(ui, 8).getText().get());
		Assert.assertEquals("B 0", SignalReplayTest.node(ui, 9).getText().get());
		Assert.assertEquals("5", SignalReplayTest.node(ui, 10).getText().get());
		Assert.assertEquals("0", SignalReplayTest.node(ui, 11).getText().get());
		Assert.assertEquals("Skip A 5", SignalReplayTest.node(ui, 12).getText().get());
		Assert.assertEquals("Frame 0, clicks 5", SignalReplayTest.node(ui, 13).getText().get());
		Assert.assertEquals(50D, SignalReplayTest.node(ui, 14).getSizeX().get(), 0D);
		Assert.assertEquals(5D, SignalReplayTest.node(ui, 14).getSizeY().get(), 0D);
		Assert.assertSame(Color.GREEN, ((RectNode) ui.getNodeList().get(15)).getColor());
		ui.getOther().set(7);
		Assert.assertEquals("B 7", SignalReplayTest.node(ui, 9).getText().get());
		Assert.assertEquals("7", SignalReplayTest.node(ui, 11).getText().get());
		Assert.assertEquals(12D, SignalReplayTest.node(ui, 14).getSizeY().get(), 0D);
	}

	@Test
	public void rereadsAPlainFieldOnlyWhenASignalOfTheExpressionChanges() {
		final ReplayPitfallsUI ui = new ReplayPitfallsUI();
		this.bridges.open(ui);
		ui.setBonus(200);
		Assert.assertEquals("Clicks + bonus: 100", SignalReplayTest.node(ui, 3).getText().get());
		Assert.assertEquals("Clicks + offset: 10", SignalReplayTest.node(ui, 6).getText().get());
		ui.getClicks().set(1);
		Assert.assertEquals("Clicks + bonus: 201", SignalReplayTest.node(ui, 3).getText().get());
		Assert.assertEquals("Clicks + offset: 21", SignalReplayTest.node(ui, 6).getText().get());
	}

	@Test
	public void followsTheExpressionsAgainAfterAReload() {
		final ReplayPitfallsUI ui = new ReplayPitfallsUI();
		this.bridges.open(ui);
		ui.reload();
		ui.getClicks().set(2);
		Assert.assertEquals("Next: 3, double: 4", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertEquals("ADA has 2 clicks", SignalReplayTest.node(ui, 4).getText().get());
	}

	@Test
	public void warnsOnceWhenTwoCallsOnALineGiveTheSameFingerprint() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("ambiguous");
		Assert.assertEquals("[JOID] ReplayScenarioUI.java:N text(...) reads clicks but cannot follow it: several calls to text(...) on this line give the same value from the same signals. The value stays \"v0\". Write one call per line.\n", this.open(ui));
		ui.getClicks().set(3);
		Assert.assertEquals("v0", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertEquals("v0", SignalReplayTest.node(ui, 1).getText().get());
	}

	@Test
	public void warnsOnAnUnsupportedInstruction() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("unsupported");
		Assert.assertEquals("[JOID] ReplayScenarioUI.java:N text(...) reads clicks but cannot follow it: the instruction PUTFIELD is not supported. The value stays \"v0\". Use map(...) or a lambda.\n", this.open(ui));
	}

	@Test
	public void warnsOnALambdaInsideTheExpression() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("lambda");
		Assert.assertEquals("[JOID] ReplayScenarioUI.java:N text(...) reads clicks but cannot follow it: the expression contains a lambda. The value stays \"v20\". Move the lambda out of the expression or use Signal.from(() -> ...).\n", this.open(ui));
	}

	@Test
	public void warnsOnALocalVariableCombinedWithASignal() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("combined");
		Assert.assertEquals("[JOID] ReplayScenarioUI.java:N text(...) reads clicks but cannot follow it: the local variable index is combined with a signal. The value stays \"Row 0\". Use a field, map(...) or a lambda.\n", this.open(ui));
		ui.getClicks().set(3);
		Assert.assertEquals("Row 0", SignalReplayTest.node(ui, 0).getText().get());
	}

	@Test
	public void warnsOnALocalVariableDecidingACondition() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("condition");
		Assert.assertEquals("[JOID] ReplayScenarioUI.java:N text(...) reads clicks but cannot follow it: the local variable name decides a condition. The value stays \"n0\". Use a field, map(...) or a lambda.\n", this.open(ui));
	}

	@Test
	public void warnsWhenTheReplayGivesAnotherValue() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("differs");
		Assert.assertEquals("[JOID] ReplayScenarioUI.java:N text(...) reads clicks but cannot follow it: replaying the expression gives \"n2 0\" (side effects, random, time). The value stays \"n1 0\". Use a lambda.\n", this.open(ui));
	}

	@Test
	public void warnsWhenTheReplayReadsOtherSignals() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("signals");
		Assert.assertEquals("[JOID] ReplayScenarioUI.java:N text(...) reads alternate() but cannot follow it: replaying the expression reads other signals. The value stays \"s0\". Use map(...) or a lambda.\n", this.open(ui));
	}

	@Test
	public void keepsTheLastValueWhenALaterReplayFails() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("later");
		Assert.assertEquals("", this.open(ui));
		Assert.assertEquals("iAda", SignalReplayTest.node(ui, 0).getText().get());
		final String output = SignalReplayTest.capture(true, () -> {
			ui.getClicks().set(5);
			SignalReplayTest.node(ui, 0).getText().get();
			ui.getClicks().set(6);
			SignalReplayTest.node(ui, 0).getText().get();
		});
		Assert.assertEquals("iAda", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertTrue(output, output.startsWith("[JOID] ReplayScenarioUI.java:"));
		Assert.assertTrue(output, output.contains(" text(...) reads clicks but cannot follow it: replaying the expression failed (java.lang.IndexOutOfBoundsException"));
		Assert.assertTrue(output, output.endsWith("). The value stays \"iAda\". Use map(...) or a lambda.\n"));
		Assert.assertEquals(1, output.split("\n").length);
	}

	@Test
	public void staysSilentWithoutASetterAfterTheRead() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("silent");
		Assert.assertEquals("", this.open(ui));
		ui.getClicks().set(2);
		Assert.assertEquals("plain", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertEquals("lambda 2", SignalReplayTest.node(ui, 1).getText().get());
	}

	@Test
	public void staysSilentOutsideTheDevMode() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("combined");
		Assert.assertEquals("", SignalReplayTest.capture(false, () -> this.bridges.getUi().add(ui)));
		Assert.assertEquals("Row 0", SignalReplayTest.node(ui, 0).getText().get());
	}

	@Test
	public void followsTheSignalsOfAnAnonymousClassByTheirPosition() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("anonymous");
		Assert.assertEquals("", this.open(ui));
		ui.getClicks().set(4);
		Assert.assertEquals("Inner 4", SignalReplayTest.node(ui, 0).getText().get());
	}

	@Test
	public void followsANativeExpressionInABody() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("body");
		Assert.assertEquals("", this.open(ui));
		ui.getClicks().set(2);
		Assert.assertEquals("Body 2", SignalReplayTest.node(ui, 0).getText().get());
	}

	@Test
	public void followsANativeExpressionInACallbackRunOutsideTheInit() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("update");
		this.bridges.open(ui);
		ui.getClicks().set(2);
		Assert.assertEquals("Update 2", SignalReplayTest.node(ui, 0).getText().get());
	}

	@Test
	public void followsTheSettersOfACustomNodeOutsideTheLibrary() {
		final LabelUI ui = new LabelUI();
		Assert.assertEquals("", this.open(ui));
		ui.getClicks().set(3);
		Assert.assertEquals("Label 3", ((LabelNode) ui.getNodeList().get(0)).getLabel().get());
		Assert.assertEquals("Title 3", ((LabelNode) ui.getNodeList().get(1)).getLabel().get());
		Assert.assertEquals("Caption 3", SignalReplayTest.node(ui, 2).getText().get());
		Assert.assertEquals("Titled 3", SignalReplayTest.node(ui, 3).getText().get());
		Assert.assertEquals("Direct 3", ((LabelNode) ui.getNodeList().get(5)).getLabel().get());
	}

	@Test
	public void keepsAValueComputedInsideTheLibraryFixed() {
		final LabelUI ui = new LabelUI();
		Assert.assertEquals("", this.open(ui));
		ui.getClicks().set(3);
		Assert.assertEquals("> Prefixed 0", SignalReplayTest.node(ui, 4).getText().get());
	}

	@Test
	public void leavesNoReadBehindAFrame() {
		final IntegerSignal clicks = IntegerSignal.of(1);
		final UI ui = new UI() {

			@Override
			public void init() {
				RectNode.create(0D, 0D, 10D, 10D).color(() -> clicks.get() > 0 ? Color.GREEN : Color.GRAY).attach(this);
			}

		};
		this.bridges.open(ui);
		final long total = SignalContext.current().getReadTotal();
		this.bridges.frames(3);
		Assert.assertTrue(SignalContext.current().getReadTotal() > total);
		Assert.assertFalse(SignalContext.current().hasReads());
	}

	@Test
	public void warnsWhenTheModuleRefusesTheAccess() {
		final SecurityManager previous = System.getSecurityManager();
		final SecurityManager refusing = new SecurityManager() {

			@Override
			public void checkPermission(final Permission permission) {
				if (permission instanceof ReflectPermission && permission.getName().equals("suppressAccessChecks")) {
					for (final StackTraceElement element : Thread.currentThread().getStackTrace()) {
						if (element.getClassName().equals(ReplayMethod.class.getName())) {
							throw new SecurityException("refused");
						}
					}
				}
			}

		};
		try {
			System.setSecurityManager(refusing);
		} catch (final UnsupportedOperationException exception) {
			Assume.assumeNoException(exception);
		}

		final String output;
		try {
			output = this.open(new ReplayScenarioUI("access"));
		} finally {
			System.setSecurityManager(previous);
		}
		Assert.assertEquals("[JOID] ReplayScenarioUI.java:N text(...) reads clicks but cannot follow it: the module of dev.joid.demo.replay.ReplayScenarioUI.secret refuses the access. The value stays \"p70\". Add opens dev.joid.demo.replay to dev.joid in its module-info.\n", output);
	}

	@Test
	public void warnsWhenTheBytecodeOfTheCallerCannotBeRead() throws IOException {
		final ReplayHiddenLoader loader = ReplayHiddenLoader.create("ReplayHiddenMissing", true, false, null, null);
		final ReplayNode node = ReplayNode.create();
		final IntegerSignal clicks = IntegerSignal.of(0);
		Assert.assertEquals("[JOID] ReplayHiddenFixture.java:N text(...) reads a signal but cannot follow it: the bytecode of dev.joid.demo.replay.ReplayHiddenMissing cannot be read. The value stays \"Hidden 0\". Use map(...) or a lambda.\n", SignalReplayTest.hidden(loader, node, clicks));
		clicks.set(2);
		Assert.assertEquals("Hidden 0", node.getText().get());
	}

	@Test
	public void readsTheBytecodeThroughTheContextLoader() throws IOException {
		final ReplayHiddenLoader loader = ReplayHiddenLoader.create("ReplayHiddenContext", true, true, null, null);
		final ReplayNode node = ReplayNode.create();
		final IntegerSignal clicks = IntegerSignal.of(0);
		Assert.assertEquals("", SignalReplayTest.hidden(loader, node, clicks));
		clicks.set(2);
		Assert.assertEquals("Hidden 2", node.getText().get());
	}

	@Test
	public void searchesTheWholeMethodWithoutLineNumbers() throws IOException {
		final ReplayHiddenLoader loader = ReplayHiddenLoader.create("ReplayHiddenLineless", false, true, null, null);
		final ReplayNode node = ReplayNode.create();
		final IntegerSignal clicks = IntegerSignal.of(0);
		Assert.assertEquals("", SignalReplayTest.hidden(loader, node, clicks));
		clicks.set(2);
		Assert.assertEquals("Hidden 2", node.getText().get());
	}

	@Test
	public void warnsWhenTheCallIsNotOnTheLine() throws IOException {
		final ReplayHiddenLoader loader = ReplayHiddenLoader.create("ReplayHiddenMoved", true, true, "text", "texture");
		final ReplayNode node = ReplayNode.create();
		final IntegerSignal clicks = IntegerSignal.of(0);
		Assert.assertEquals("[JOID] ReplayHiddenFixture.java:N text(...) reads a signal but cannot follow it: no call to text(...) is found on this line. The value stays \"Hidden 0\". Use map(...) or a lambda.\n", SignalReplayTest.hidden(loader, node, clicks));
	}

	@Test
	public void warnsWhenAMemberIsRenamedWithoutRemapper() throws IOException {
		final ReplayHiddenLoader loader = ReplayHiddenLoader.create("ReplayHiddenRenamed", true, true, "get", "obtain");
		final ReplayNode node = ReplayNode.create();
		final IntegerSignal clicks = IntegerSignal.of(0);
		Assert.assertEquals("[JOID] ReplayHiddenFixture.java:N text(...) reads clicks but cannot follow it: the method dev.joid.lib.utils.signal.impl.primitive.IntegerSignal.obtain(...) does not exist at runtime. The value stays \"Hidden 0\". Configure the IReplayRemapper of the bridge or use map(...).\n", SignalReplayTest.hidden(loader, node, clicks));
	}

	@Test
	public void mapsTheRenamedMembersThroughTheBridgeRemapper() throws IOException {
		final ReplayHiddenLoader loader = ReplayHiddenLoader.create("ReplayHiddenRemapped", true, true, "get", "obtain");
		final ReplayNode node = ReplayNode.create();
		final IntegerSignal clicks = IntegerSignal.of(0);
		final IReplayRemapper remapper = new IReplayRemapper() {

			@Override
			public String mapMethod(final String owner, final String name, final String descriptor) {
				return name.equals("obtain") ? "get" : name;
			}

		};
		BridgeHandler.REPLAY.register(remapper);
		try {
			Assert.assertEquals("", SignalReplayTest.hidden(loader, node, clicks));
		} finally {
			BridgeHandler.REPLAY.unregister(remapper);
		}
		clicks.set(2);
		Assert.assertEquals("Hidden 2", node.getText().get());
	}

	private static ReplayNode node(final UI ui, final int index) {
		return (ReplayNode) ui.getNodeList().get(index);
	}

	private String open(final UI ui) {
		return SignalReplayTest.capture(true, () -> this.bridges.getUi().add(ui)).replaceAll("\\.java:\\d+ ", ".java:N ");
	}

	private static String hidden(final ReplayHiddenLoader loader, final ReplayNode node, final IntegerSignal clicks) {
		final ClassLoader previous = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(loader);
		try {
			SignalReplay.reset();
			return SignalReplayTest.capture(true, () -> loader.run(node, clicks)).replaceAll("\\.java:\\d+ ", ".java:N ");
		} finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	private static String capture(final boolean devMode, final Runnable runnable) {
		final PrintStream error = System.err;
		final boolean previous = JOID.inst().isDevMode();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			JOID.inst().setDevMode(devMode);
			System.setErr(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setErr(error);
			JOID.inst().setDevMode(previous);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8).replace("\r\n", "\n");
	}

}