package dev.joid.lib.ui.node.effect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPassContext;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public class NodeEffectTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<String> trace = new ArrayList<>();

	@Test
	public void startsWithoutPriorityOnItsOwnNode() {
		final PlainEffect effect = new PlainEffect();
		Assert.assertEquals(0, effect.getPriority());
		Assert.assertEquals(NodeEffectScope.SELF, effect.getScope());
	}

	@Test
	public void ordersTheBuiltInShaderEffectsByTheirDefaultPriority() {
		Assert.assertEquals(100, RoundedNodeEffect.create(4F).getPriority());
		Assert.assertEquals(100, CircleNodeEffect.create().getPriority());
		Assert.assertEquals(150, BlurNodeEffect.create(4F).getPriority());
		Assert.assertEquals(200, BorderNodeEffect.create(Color.RED, 2F).getPriority());
	}

	@Test
	public void appliesToEveryNodeWithoutShader() {
		final PlainEffect effect = new PlainEffect();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
		Assert.assertTrue(effect.shouldApply(node));
		Assert.assertFalse(effect.isShaderEffect());
		Assert.assertNull(effect.toShaderPass(node));
		Assert.assertTrue(effect.toShaderPasses(node).isEmpty());
	}

	@Test
	public void wrapsItsShaderPassInAList() {
		final ShaderEffect effect = new ShaderEffect(this.trace);
		Assert.assertTrue(effect.isShaderEffect());
		Assert.assertEquals(Collections.singletonList(effect.pass), effect.toShaderPasses(RectNode.create(0D, 0D, 10D, 10D)));
	}

	@Test
	public void returnsItselfFromItsSetters() {
		final PlainEffect effect = new PlainEffect();
		Assert.assertSame(effect, effect.priority(3));
		Assert.assertSame(effect, effect.scope(NodeEffectScope.CHILDREN));
		Assert.assertEquals(3, effect.getPriority());
		Assert.assertEquals(NodeEffectScope.CHILDREN, effect.getScope());
	}

	@Test
	public void keepsTheTypeOfACustomEffectThroughItsSetters() {
		final PlainEffect effect = new PlainEffect().priority(3).scope(NodeEffectScope.CHILDREN);
		Assert.assertEquals(3, effect.getPriority());
		Assert.assertEquals(NodeEffectScope.CHILDREN, effect.getScope());
	}

	@Test
	public void keepsItsOwnTypeThroughItsScope() {
		final RoundedNodeEffect effect = RoundedNodeEffect.create(4F).scope(NodeEffectScope.CHILDREN);
		Assert.assertEquals(NodeEffectScope.CHILDREN, effect.getScope());
		Assert.assertEquals(4F, effect.getRadius(), 0F);
	}

	@Test
	public void listsItsScopesFromTheNodeToItsChildren() {
		Assert.assertArrayEquals(new NodeEffectScope[] {NodeEffectScope.SELF, NodeEffectScope.CHILDREN}, NodeEffectScope.values());
		Assert.assertEquals(NodeEffectScope.CHILDREN, NodeEffectScope.valueOf("CHILDREN"));
	}

	@Test
	public void leavesTheDrawOfItsNodeUntouchedByDefault() {
		this.bridges.open(new NodeUI(RectNode.create(100D, 50D, 40D, 30D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).effect(new PlainEffect()))).frame();
		final Draw draw = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).get(0);
		Assert.assertEquals(100D, draw.getLeft(), 0.001D);
		Assert.assertEquals(50D, draw.getTop(), 0.001D);
		Assert.assertEquals(140D, draw.getRight(), 0.001D);
		Assert.assertEquals(80D, draw.getBottom(), 0.001D);
	}

	@Test
	public void startsWithItsNodeAndItsUi() {
		final TraceNode node = new TraceNode("node", this.trace);
		final TraceEffect effect = new TraceEffect("effect", this.trace, true);
		final NodeUI ui = new NodeUI(node.effect(effect));
		this.bridges.open(ui);
		Assert.assertSame(ui, effect.ui);
		Assert.assertEquals("init effect", this.trace.get(0));
	}

	@Test
	public void wrapsTheDrawOfItsNode() {
		this.bridges.open(new NodeUI(new TraceNode("node", this.trace).effect(new TraceEffect("effect", this.trace, true))));
		this.trace.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("open effect", "draw node", "close effect"), this.trace);
	}

	@Test
	public void skipsANodeItDoesNotApplyTo() {
		this.bridges.open(new NodeUI(new TraceNode("node", this.trace).effect(new TraceEffect("effect", this.trace, false)))).frame();
		Assert.assertEquals(Arrays.asList("init effect", "draw node", "draw node"), this.trace);
	}

	@Test
	public void runsTheLowestPriorityFirst() {
		final TraceNode node = new TraceNode("node", this.trace);
		node.effect(new TraceEffect("late", this.trace, true).priority(2));
		node.effect(new OtherTraceEffect("early", this.trace).priority(1));
		this.bridges.open(new NodeUI(node));
		this.trace.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("open early", "open late", "draw node", "close late", "close early"), this.trace);
	}

	@Test
	public void composesItsShaderOverItsNodeOnly() {
		final TraceNode node = new TraceNode("parent", this.trace);
		new TraceNode("child", this.trace).attach(node);
		this.bridges.open(new NodeUI(node.effect(new ShaderEffect(this.trace))));
		this.trace.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("draw parent", "bind", "unbind", "draw child"), this.trace);
	}

	@Test
	public void composesItsShaderOverTheChildrenWithTheChildrenScope() {
		final TraceNode node = new TraceNode("parent", this.trace);
		new TraceNode("child", this.trace).attach(node);
		this.bridges.open(new NodeUI(node.effect(new ShaderEffect(this.trace).scope(NodeEffectScope.CHILDREN))));
		this.trace.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("draw parent", "draw child", "bind", "unbind"), this.trace);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingScope() {
		new PlainEffect().scope((NodeEffectScope) null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToApplyToAMissingNode() {
		new PlainEffect().shouldApply(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToWrapAMissingNode() {
		new PlainEffect().draw(null, () -> {});
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	public static final class TraceNode extends Node {

		private final String       name;
		private final List<String> trace;

		private TraceNode(final String name, final List<String> trace) {
			super(0D, 0D, 100D, 100D);
			this.name = name;
			this.trace = trace;
		}

		@Override
		public void draw(final double mouseX, final double mouseY) {
			this.trace.add("draw " + this.name);
		}

	}

	public static final class PlainEffect extends NodeEffect<Node> {}

	public static class TraceEffect extends NodeEffect<Node> {

		private final String       name;
		private final boolean      applies;
		private final List<String> trace;

		private UI ui;

		private TraceEffect(final String name, final List<String> trace, final boolean applies) {
			this.name = name;
			this.trace = trace;
			this.applies = applies;
		}

		@Override
		public void init(final @NonNull Node node, final @NonNull UI ui) {
			this.ui = ui;
			this.trace.add("init " + this.name);
		}

		@Override
		public void draw(final @NonNull Node node, final @NonNull Runnable body) {
			this.trace.add("open " + this.name);
			body.run();
			this.trace.add("close " + this.name);
		}

		@Override
		public boolean shouldApply(final @NonNull Node node) {
			return this.applies;
		}

	}

	public static final class OtherTraceEffect extends TraceEffect {

		private OtherTraceEffect(final String name, final List<String> trace) {
			super(name, trace, true);
		}

	}

	public static final class ShaderEffect extends NodeEffect<Node> {

		private final IShaderPass pass;

		private ShaderEffect(final List<String> trace) {
			this.pass = new TracePass(trace);
		}

		@Override
		public boolean isShaderEffect() {
			return true;
		}

		@Override
		public IShaderPass toShaderPass(final @NonNull Node node) {
			return this.pass;
		}

	}

	@AllArgsConstructor
	public static final class TracePass implements IShaderPass {

		private final List<String> trace;

		@Override
		public void unbind() {
			this.trace.add("unbind");
		}

		@Override
		public void bind(final @NonNull ShaderPassContext context) {
			this.trace.add("bind");
		}

	}

}