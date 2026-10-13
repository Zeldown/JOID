package dev.joid.lib.ui.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.debug.UIDataDebug;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class TaskTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final AtomicInteger runs = new AtomicInteger();

	@BeforeClass
	public static void loadTheDevFont() {
		DevFont.load();
	}

	@Test
	public void runsOnceItsDelayElapsed() {
		final TaskUI ui = this.open();
		final Task task = Task.create(100L).onRun(event -> this.runs.incrementAndGet()).attach(ui);
		this.bridges.frames(6);
		Assert.assertEquals(0, this.runs.get());
		this.bridges.frame();
		Assert.assertEquals(1, this.runs.get());
		this.bridges.frames(5);
		Assert.assertEquals(1, this.runs.get());
		Assert.assertFalse(task.isAttached());
		Assert.assertTrue(ui.getTaskList().isEmpty());
	}

	@Test
	public void repeatsEveryPeriod() {
		final TaskUI ui = this.open();
		final Task task = Task.create(0L).every(48L).onRun(event -> this.runs.incrementAndGet()).attach(ui);
		this.bridges.frames(7);
		Assert.assertEquals(3, this.runs.get());
		Assert.assertTrue(task.isAttached());
		Assert.assertEquals(48L, task.getPeriod());
	}

	@Test
	public void stopsWhenDetachedFromItsOwnRun() {
		final TaskUI ui = this.open();
		Task.create(0L).every(0L).onRun(event -> {
			if (this.runs.incrementAndGet() == 2) {
				event.getTask().detach();
			}
		}).attach(ui);
		this.bridges.frames(5);
		Assert.assertEquals(2, this.runs.get());
		Assert.assertTrue(ui.getTaskList().isEmpty());
	}

	@Test
	public void stopsWhenItsNodeIsDetached() {
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final RectNode parent = RectNode.create(0, 0, 200, 200).append(node);
		this.open(parent);
		final Task task = Task.create(0L).every(0L).onRun(event -> this.runs.incrementAndGet()).attach(node);
		this.bridges.frames(2);
		Assert.assertEquals(2, this.runs.get());
		parent.remove(node);
		parent.append(node);
		this.bridges.frames(2);
		Assert.assertEquals(2, this.runs.get());
		Assert.assertFalse(task.isAttached());
		Assert.assertTrue(node.getTaskList().isEmpty());
	}

	@Test
	public void waitsForItsNodeToJoinAUi() {
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final TaskUI ui = this.open();
		final Task task = Task.create(0L).every(0L).onRun(event -> this.runs.incrementAndGet()).attach(node);
		this.bridges.frames(2);
		Assert.assertEquals(0, this.runs.get());
		node.attach(ui);
		this.bridges.frame();
		Assert.assertEquals(1, this.runs.get());
		Assert.assertTrue(task.isAttached());
	}

	@Test
	public void stopsWhenItsUiCloses() {
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final TaskUI ui = this.open(node);
		final Task first = Task.create(0L).every(0L).attach(ui);
		final Task second = Task.create(0L).every(0L).attach(node);
		JOID.close(ui);
		Assert.assertFalse(first.isAttached());
		Assert.assertFalse(second.isAttached());
		Assert.assertTrue(ui.getTaskList().isEmpty());
	}

	@Test
	public void stopsWhenItsUiReloads() {
		final TaskUI ui = this.open();
		final Task task = Task.create(0L).every(0L).onRun(event -> this.runs.incrementAndGet()).attach(ui);
		ui.reload();
		this.bridges.frames(2);
		Assert.assertEquals(0, this.runs.get());
		Assert.assertFalse(task.isAttached());
	}

	@Test
	public void givesItselfToItsEvent() {
		final TaskUI ui = this.open();
		final List<Task> tasks = new ArrayList<>();
		final Task task = Task.create(0L).onRun(event -> tasks.add(event.getTask())).attach(ui);
		this.bridges.frame();
		Assert.assertEquals(Collections.singletonList(task), tasks);
	}

	@Test
	public void runsInTheContextOfItsUi() {
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final TaskUI ui = this.open(node);
		final List<UI> currents = new ArrayList<>();
		Task.create(0L).onRun(event -> currents.add(UI.getCurrent())).attach(ui);
		Task.create(0L).onRun(event -> currents.add(UI.getCurrent())).attach(node);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(ui, ui), currents);
		Assert.assertNull(UI.getCurrent());
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullListener() {
		Task.create(0L).onRun(null);
	}

	private TaskUI open(final Node... nodes) {
		final TaskUI ui = new TaskUI(nodes);
		this.bridges.open(ui);
		return ui;
	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class TaskUI extends UI {

		private final Node[] nodes;

		private TaskUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}