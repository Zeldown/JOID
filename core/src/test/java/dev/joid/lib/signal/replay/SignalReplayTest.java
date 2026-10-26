package dev.joid.lib.signal.replay;

import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.replay.ReplayAnimatorUI;
import dev.joid.demo.replay.ReplayCountedSignal;
import dev.joid.demo.replay.ReplayLocalUI;
import dev.joid.demo.replay.ReplayPitfallsUI;
import dev.joid.demo.replay.ReplayScenarioUI;
import dev.joid.internal.TestUtils;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.signal.ISignalReplayBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.signal.SignalContext;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.showcase.ReplayShowcaseFixture;
import example.replay.LabelNode;
import example.replay.LabelUI;
import example.replay.OverridingLabelUI;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

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
		this.bridges.frame();
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
	public void followsTheAnimatorsReadInTheSetters() {
		final ReplayAnimatorUI ui = new ReplayAnimatorUI(TweenAnimator.create().sequence(32F, 1F), TweenAnimator.create().sequence(64F, 1F));
		Assert.assertEquals("", this.open(ui));
		final Node node = ui.getNodeList().get(0);
		this.bridges.frame();
		Assert.assertEquals(55D, node.getWidth(), 1E-9D);
		Assert.assertEquals(32.5D, node.getHeight(), 1E-9D);
		this.bridges.frame();
		Assert.assertEquals(100D, node.getWidth(), 1E-9D);
		Assert.assertEquals(55D, node.getHeight(), 1E-9D);
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
	public void warnsOnASignalReadOnlyInsideALambdaOfTheExpression() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("hidden");
		Assert.assertEquals("[JOID] ReplayScenarioUI.java:N text(...) reads a signal but cannot follow it: the expression contains a lambda. The value stays \"h0\". Move the lambda out of the expression or use Signal.from(() -> ...).\n", this.open(ui));
		ui.getClicks().set(3);
		Assert.assertEquals("h0", SignalReplayTest.node(ui, 0).getText().get());
	}

	@Test
	public void leavesTheReadsOfAnEnclosingCallToIt() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("enclosed");
		Assert.assertEquals("", this.open(ui));
		Assert.assertEquals("o0t1", SignalReplayTest.node(ui, 0).getText().get());
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
	public void warnsOnceWithItsTraceWhenTheReplayFailsInside() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("equals");
		final String output = this.open(ui);
		Assert.assertTrue(output, output.startsWith("[JOID] The replay of a signal read failed on an internal error, the value \"" + SignalReplayTest.node(ui, 0).getTag().get() + "\" stays fixed\njava.lang.IllegalStateException: Tag 0 cannot be compared\n"));
		Assert.assertTrue(output, output.contains("\tat dev.joid.demo.replay.ReplayScenarioUI$Tag.equals("));
		Assert.assertEquals("", this.open(new ReplayScenarioUI("equals")));
		final ReplayScenarioUI next = new ReplayScenarioUI("equals");
		next.getClicks().set(1);
		Assert.assertEquals("", this.open(next));
	}

	@Test
	public void keepsTheLastValueWhenALaterReplayFails() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("later");
		Assert.assertEquals("", this.open(ui));
		Assert.assertEquals("iAda", SignalReplayTest.node(ui, 0).getText().get());
		final String output = TestUtils.captureErr(true, () -> {
			ui.getClicks().set(5);
			SignalReplayTest.node(ui, 0).getText().get();
			ui.getClicks().set(6);
			SignalReplayTest.node(ui, 0).getText().get();
		}).replace("\r\n", "\n");
		Assert.assertEquals("iAda", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertTrue(output, output.startsWith("[JOID] ReplayScenarioUI.java:"));
		Assert.assertTrue(output, output.contains(" text(...) reads clicks but cannot follow it: replaying the expression failed (java.lang.IndexOutOfBoundsException"));
		Assert.assertTrue(output, output.endsWith("). The value stays \"iAda\". Use map(...) or a lambda.\n"));
		Assert.assertEquals(1, output.split("\n").length);
	}

	@Test
	public void warnsOnceAndFollowsAgainWhenTheReplayFailsBeforeReadingASignal() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("before");
		Assert.assertEquals("", this.open(ui));
		Assert.assertEquals("bAda0", SignalReplayTest.node(ui, 0).getText().get());
		ui.setShift(1);
		final String output = TestUtils.captureErr(true, () -> {
			ui.getClicks().set(5);
			SignalReplayTest.node(ui, 0).getText().get();
		}).replace("\r\n", "\n");
		Assert.assertEquals("bAda0", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertTrue(output, output.contains(" text(...) reads clicks but cannot follow it: replaying the expression failed (java.lang.IndexOutOfBoundsException"));
		Assert.assertEquals(output, 1, output.split("\n").length);
		ui.setShift(0);
		ui.getClicks().set(7);
		Assert.assertEquals("bAda7", SignalReplayTest.node(ui, 0).getText().get());
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
		Assert.assertEquals("", TestUtils.captureErr(false, () -> this.bridges.getUi().add(ui)));
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
	public void followsAPublicMethodDeclaredInAHiddenClassOfTheJdk() {
		final ReplayScenarioUI ui = new ReplayScenarioUI("view");
		Assert.assertEquals("", this.open(ui));
		ui.getKeys().put("Grace", 2);
		ui.getClicks().set(3);
		Assert.assertEquals("k23", SignalReplayTest.node(ui, 0).getText().get());
	}

	@Test
	public void followsASignalHeldInALocalVariable() {
		final ReplayLocalUI ui = new ReplayLocalUI("simple");
		Assert.assertEquals("", this.open(ui));
		ui.getSignalList().get(0).set(3);
		Assert.assertEquals("Clicks: 3", SignalReplayTest.node(ui, 0).getText().get());
		ui.getSignalList().get(0).set(4);
		Assert.assertEquals("Clicks: 4", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertEquals(1, ui.getCreated());
	}

	@Test
	public void followsASignalCreatedInALocalVariableWithoutCreatingItAgain() {
		final ReplayLocalUI ui = new ReplayLocalUI("direct");
		ReplayCountedSignal.CREATED.set(0);
		Assert.assertEquals("", this.open(ui));
		ui.getSignalList().get(0).set(2);
		Assert.assertEquals("Direct 2", SignalReplayTest.node(ui, 0).getText().get());
		ui.getSignalList().get(0).set(5);
		Assert.assertEquals("Direct 5", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertEquals(1, ReplayCountedSignal.CREATED.get());
	}

	@Test
	public void followsSeveralSignalsHeldInLocalVariables() {
		final ReplayLocalUI ui = new ReplayLocalUI("several");
		Assert.assertEquals("", this.open(ui));
		ui.getSignalList().get(0).set(5);
		ui.getSignalList().get(1).set(7);
		Assert.assertEquals("5 / 7", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertEquals("Right 7, left 5", SignalReplayTest.node(ui, 1).getText().get());
		Assert.assertEquals("Sum 15", SignalReplayTest.node(ui, 2).getText().get());
		Assert.assertEquals(2, ui.getCreated());
	}

	@Test
	public void followsALocalSignalCombinedWithAField() {
		final ReplayLocalUI ui = new ReplayLocalUI("field");
		Assert.assertEquals("", this.open(ui));
		ui.getSignalList().get(0).set(2);
		Assert.assertEquals("Total 102", SignalReplayTest.node(ui, 0).getText().get());
		ui.getOther().set(200);
		Assert.assertEquals("Total 202", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertEquals(1, ui.getCreated());
	}

	@Test
	public void followsALocalSignalCapturedByABody() {
		final ReplayLocalUI ui = new ReplayLocalUI("body");
		Assert.assertEquals("", this.open(ui));
		ui.getSignalList().get(0).set(6);
		Assert.assertEquals("Body 6", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertEquals(1, ui.getCreated());
	}

	@Test
	public void followsALocalSignalReadAgainInABranchTakenLater() {
		final ReplayLocalUI ui = new ReplayLocalUI("branch");
		Assert.assertEquals("", this.open(ui));
		Assert.assertEquals("none", SignalReplayTest.node(ui, 0).getText().get());
		ui.getListList().get(0).add("Ada");
		Assert.assertEquals("Ada and Ada", SignalReplayTest.node(ui, 0).getText().get());
		ui.getListList().get(0).clear();
		Assert.assertEquals("none", SignalReplayTest.node(ui, 0).getText().get());
	}

	@Test
	public void followsALocalSignalWrittenByASubscriber() {
		final ReplayLocalUI ui = new ReplayLocalUI("subscribed");
		Assert.assertEquals("", this.open(ui));
		ui.getSignalList().get(0).set(4);
		Assert.assertEquals("Saved: 4", SignalReplayTest.node(ui, 0).getText().get());
	}

	@Test
	public void ignoresTheReadsLeftByAnEarlierLineWhenTheyDoNotFit() {
		final ReplayLocalUI ui = new ReplayLocalUI("stale");
		Assert.assertEquals("", this.open(ui));
		ui.getSignalList().get(0).set(4);
		Assert.assertEquals("Saved 4", SignalReplayTest.node(ui, 1).getCaption().get().getText().get());
	}

	@Test
	public void followsTheLocalSignalOfEachTurnOfALoop() {
		final ReplayLocalUI ui = new ReplayLocalUI("loop");
		Assert.assertEquals("", this.open(ui));
		ui.getSignalList().get(0).set(10);
		ui.getSignalList().get(2).set(30);
		Assert.assertEquals("Row 10", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertEquals("Row 1", SignalReplayTest.node(ui, 1).getText().get());
		Assert.assertEquals("Row 30", SignalReplayTest.node(ui, 2).getText().get());
		Assert.assertEquals(3, ui.getCreated());
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
		Assert.assertEquals("Label 3", ((LabelNode) ui.getNodeList().get(0)).getName().get());
		Assert.assertEquals("Title 3", ((LabelNode) ui.getNodeList().get(1)).getName().get());
		Assert.assertEquals("Caption 3", SignalReplayTest.node(ui, 2).getText().get());
		Assert.assertEquals("Titled 3", SignalReplayTest.node(ui, 3).getText().get());
		Assert.assertEquals("Direct 3", ((LabelNode) ui.getNodeList().get(5)).getName().get());
	}

	@Test
	public void keepsAValueComputedInsideTheLibraryFixed() {
		final LabelUI ui = new LabelUI();
		Assert.assertEquals("", this.open(ui));
		ui.getClicks().set(3);
		Assert.assertEquals("> Prefixed 0", SignalReplayTest.node(ui, 4).getText().get());
	}

	@Test
	public void keepsASuperCallToAnOverriddenMethodFixed() {
		final OverridingLabelUI ui = new OverridingLabelUI();
		Assert.assertEquals("[JOID] OverridingLabelUI.java:N text(...) reads getClicks() but cannot follow it: the instruction INVOKESPECIAL label is not supported. The value stays \"a0\". Use map(...) or a lambda.\n", this.open(ui));
		ui.getClicks().set(2);
		Assert.assertEquals("a0", SignalReplayTest.node(ui, 0).getText().get());
		Assert.assertEquals("p2", SignalReplayTest.node(ui, 1).getText().get());
	}

	@Test
	public void followsTheNativeExpressionsOfTheShowcase() {
		final SignalReplayNode node = SignalReplayNode.create();
		final IntegerSignal clicks = new IntegerSignal(0);
		Assert.assertEquals("", TestUtils.captureErr(true, SignalReplayTest.traced(() -> ReplayShowcaseFixture.run(node, clicks))));
		clicks.set(2);
		Assert.assertEquals("Showcase 2", node.getText().get());
	}

	@Test
	public void leavesNoReadBehindAFrame() {
		this.bridges.open(new ClicksUI(new IntegerSignal(1)));
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
						if (element.getClassName().equals(SignalReplayMethod.class.getName())) {
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
		Assert.assertEquals("[JOID] ReplayScenarioUI.java:N text(...) reads clicks but cannot follow it: the module of dev.joid.demo.replay.ReplayScenarioUI.secret refuses the access. The value stays \"p70\". Add \"opens dev.joid.demo.replay;\" to its module-info.\n", output);
	}

	@Test
	public void warnsWhenTheBytecodeOfTheCallSiteCannotBeRead() throws IOException {
		final SignalReplayHiddenLoader loader = SignalReplayHiddenLoader.create("ReplayHiddenMissing", true, false, null, null);
		final SignalReplayNode node = SignalReplayNode.create();
		final IntegerSignal clicks = new IntegerSignal(0);
		Assert.assertEquals("[JOID] ReplayHiddenFixture.java:N text(...) reads a signal but cannot follow it: the bytecode of dev.joid.demo.replay.ReplayHiddenMissing cannot be read. The value stays \"Hidden 0\". Use map(...) or a lambda.\n", SignalReplayTest.hidden(loader, node, clicks));
		clicks.set(2);
		Assert.assertEquals("Hidden 0", node.getText().get());
	}

	@Test
	public void readsTheBytecodeThroughTheContextLoader() throws IOException {
		final SignalReplayHiddenLoader loader = SignalReplayHiddenLoader.create("ReplayHiddenContext", true, true, null, null);
		final SignalReplayNode node = SignalReplayNode.create();
		final IntegerSignal clicks = new IntegerSignal(0);
		Assert.assertEquals("", SignalReplayTest.hidden(loader, node, clicks));
		clicks.set(2);
		Assert.assertEquals("Hidden 2", node.getText().get());
	}

	@Test
	public void searchesTheWholeMethodWithoutLineNumbers() throws IOException {
		final SignalReplayHiddenLoader loader = SignalReplayHiddenLoader.create("ReplayHiddenLineless", false, true, null, null);
		final SignalReplayNode node = SignalReplayNode.create();
		final IntegerSignal clicks = new IntegerSignal(0);
		Assert.assertEquals("", SignalReplayTest.hidden(loader, node, clicks));
		clicks.set(2);
		Assert.assertEquals("Hidden 2", node.getText().get());
	}

	@Test
	public void warnsWhenTheCallIsNotOnTheLine() throws IOException {
		final SignalReplayHiddenLoader loader = SignalReplayHiddenLoader.create("ReplayHiddenMoved", true, true, "text", "texture");
		final SignalReplayNode node = SignalReplayNode.create();
		final IntegerSignal clicks = new IntegerSignal(0);
		Assert.assertEquals("[JOID] ReplayHiddenFixture.java:N text(...) reads a signal but cannot follow it: no call to text(...) is found on this line, the .class file on disk may no longer match the loaded class (recompiled since the launch). The value stays \"Hidden 0\". Restart the application, or use map(...) or a lambda.\n", SignalReplayTest.hidden(loader, node, clicks));
	}

	@Test
	public void warnsWhenAMemberIsRenamedWithoutSignalReplayBridge() throws IOException {
		final SignalReplayHiddenLoader loader = SignalReplayHiddenLoader.create("ReplayHiddenRenamed", true, true, "get", "obtain");
		final SignalReplayNode node = SignalReplayNode.create();
		final IntegerSignal clicks = new IntegerSignal(0);
		Assert.assertEquals("[JOID] ReplayHiddenFixture.java:N text(...) reads clicks but cannot follow it: the method dev.joid.lib.signal.impl.primitive.IntegerSignal.obtain(...) does not exist at runtime. The value stays \"Hidden 0\". Configure the ISignalReplayBridge or use map(...).\n", SignalReplayTest.hidden(loader, node, clicks));
	}

	@Test
	public void mapsTheRenamedMembersThroughTheSignalReplayBridge() throws IOException {
		final SignalReplayHiddenLoader loader = SignalReplayHiddenLoader.create("ReplayHiddenRemapped", true, true, "get", "obtain");
		final SignalReplayNode node = SignalReplayNode.create();
		final IntegerSignal clicks = new IntegerSignal(0);
		final List<String> mapped = new ArrayList<>();
		final ISignalReplayBridge bridge = new ISignalReplayBridge() {

			@Override
			public String mapMethod(final String owner, final String name, final String descriptor) {
				mapped.add(name);
				return name.equals("obtain") ? "get" : name;
			}

		};
		BridgeHandler.SIGNAL_REPLAY.register(bridge);
		try {
			Assert.assertEquals("", SignalReplayTest.hidden(loader, node, clicks));
		} finally {
			BridgeHandler.SIGNAL_REPLAY.unregister(bridge);
		}
		clicks.set(2);
		Assert.assertEquals("Hidden 2", node.getText().get());
		Assert.assertTrue(mapped.contains("obtain"));
		Assert.assertFalse(mapped.contains("<init>"));
	}

	private String open(final UI ui) {
		return TestUtils.captureErr(true, () -> this.bridges.getUi().add(ui)).replace("\r\n", "\n").replaceAll("\\.java:\\d+ ", ".java:N ");
	}

	private static SignalReplayNode node(final UI ui, final int index) {
		return (SignalReplayNode) ui.getNodeList().get(index);
	}

	private static String hidden(final SignalReplayHiddenLoader loader, final SignalReplayNode node, final IntegerSignal clicks) {
		final ClassLoader previous = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(loader);
		try {
			SignalReplay.reset();
			return TestUtils.captureErr(true, SignalReplayTest.traced(() -> loader.run(node, clicks))).replace("\r\n", "\n").replaceAll("\\.java:\\d+ ", ".java:N ");
		} finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	private static Runnable traced(final Runnable runnable) {
		return () -> {
			final boolean tracing = SignalContext.current().tracing(true);
			try {
				runnable.run();
			} finally {
				SignalContext.current().tracing(tracing);
			}
		};
	}

	@UIDataDebug(profiler = false, hotreload = false)
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class ClicksUI extends UI {

		private final IntegerSignal clicks;

		@Override
		public void init() {
			RectNode.create(0D, 0D, 10D, 10D).color(() -> this.clicks.get() > 0 ? Color.GREEN : Color.GRAY).attach(this);
		}

	}

}