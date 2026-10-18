package dev.joid.lib.animation.tween;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TimelineTest {

	@Test
	public void playsASequenceOneChildAfterTheOther() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).start();
		Assert.assertEquals(2F, timeline.getDuration(), 0F);
		timeline.update(0.5F);
		Assert.assertEquals(5F, first.getValue(), 0F);
		Assert.assertEquals(0F, second.getValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(10F, second.getValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(20F, second.getValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void delaysEachChildOfASequence() {
		final Tween first = TimelineTest.tween(new TweenValue(0F), 10F);
		final Tween second = TimelineTest.tween(new TweenValue(0F), 20F);
		Timeline.createSequence().push(first).push(second).build();
		Assert.assertEquals(0F, first.getDelay(), 0F);
		Assert.assertEquals(1F, second.getDelay(), 0F);
	}

	@Test
	public void playsAParallelTogether() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createParallel().push(TimelineTest.tween(first, 10F)).push(second.to(2F).target(20F).ease(TweenEquations.LINEAR)).start();
		Assert.assertEquals(2F, timeline.getDuration(), 0F);
		timeline.update(1F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(10F, second.getValue(), 0F);
		timeline.update(1.5F);
		Assert.assertEquals(20F, second.getValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void waitsDuringAPause() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).pushPause(0.5F).push(TimelineTest.tween(second, 20F)).start();
		Assert.assertEquals(2.5F, timeline.getDuration(), 0F);
		timeline.update(1.25F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(0F, second.getValue(), 0F);
		timeline.update(0.75F);
		Assert.assertEquals(10F, second.getValue(), 0F);
	}

	@Test
	public void nestsAParallel() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final TweenValue third = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).beginParallel().push(TimelineTest.tween(second, 20F)).push(TimelineTest.tween(third, 30F)).end().start();
		Assert.assertEquals(2F, timeline.getDuration(), 0F);
		timeline.update(1.5F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(10F, second.getValue(), 0F);
		Assert.assertEquals(15F, third.getValue(), 0F);
	}

	@Test
	public void nestsASequence() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final TweenValue third = new TweenValue(0F);
		final Timeline timeline = Timeline.createParallel().beginSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).end().push(TimelineTest.tween(third, 30F)).start();
		Assert.assertEquals(2F, timeline.getDuration(), 0F);
		timeline.update(1.5F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(10F, second.getValue(), 0F);
		Assert.assertEquals(30F, third.getValue(), 0F);
	}

	@Test
	public void pushesAnotherTimeline() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline inner = Timeline.createParallel().push(TimelineTest.tween(second, 20F));
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(inner).start();
		Assert.assertSame(inner, timeline.getChildren().get(1));
		timeline.update(1.5F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(10F, second.getValue(), 0F);
	}

	@Test
	public void listsTheChildrenOfTheOpenedTimeline() {
		final Tween first = Tween.mark();
		final Tween second = Tween.mark();
		final Timeline timeline = Timeline.createSequence().push(first).beginParallel().push(second);
		Assert.assertEquals(Collections.singletonList(second), timeline.getChildren());
		timeline.end();
		Assert.assertEquals(2, timeline.getChildren().size());
		Assert.assertSame(first, timeline.getChildren().get(0));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesReadOnlyChildren() {
		Timeline.createSequence().push(Tween.mark()).getChildren().clear();
	}

	@Test
	public void buildsOnlyOnce() {
		final Tween tween = Tween.mark();
		final Timeline timeline = Timeline.createSequence().push(Tween.mark().delay(1F)).push(tween).build();
		timeline.build();
		Assert.assertEquals(1F, tween.getDelay(), 0F);
		Assert.assertEquals(1F, timeline.getDuration(), 0F);
	}

	@Test
	public void restartsItsChildrenOnARepeat() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).repeat(1, 0F).start();
		timeline.update(2F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(20F, second.getValue(), 0F);
		timeline.update(0.5F);
		Assert.assertEquals(5F, first.getValue(), 0F);
		Assert.assertEquals(0F, second.getValue(), 0F);
		timeline.update(2F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(20F, second.getValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void playsItsChildrenBackwardOnAYoyo() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).repeatYoyo(1, 0F).start();
		timeline.update(2F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(20F, second.getValue(), 0F);
		timeline.update(0.5F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(10F, second.getValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(5F, first.getValue(), 0F);
		Assert.assertEquals(0F, second.getValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(0F, first.getValue(), 0F);
		Assert.assertEquals(0F, second.getValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void rewindsItsChildrenWithANegativeUpdate() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).start();
		timeline.update(1.5F);
		timeline.update(-1F);
		Assert.assertEquals(5F, first.getValue(), 0F);
		Assert.assertEquals(0F, second.getValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(10F, second.getValue(), 0F);
	}

	@Test
	public void replaysItsPausesOnARepeat() {
		final TweenValue value = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().pushPause(0.5F).push(TimelineTest.tween(value, 10F)).repeat(1, 0F).start();
		timeline.update(1.5F);
		Assert.assertEquals(10F, value.getValue(), 0F);
		timeline.update(0.25F);
		Assert.assertEquals(0F, value.getValue(), 0F);
		timeline.update(0.75F);
		Assert.assertEquals(5F, value.getValue(), 0F);
	}

	@Test
	public void replaysItsPausesOnAYoyo() {
		final TweenValue value = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().pushPause(0.5F).push(TimelineTest.tween(value, 10F)).repeatYoyo(1, 0F).start();
		timeline.update(1.5F);
		Assert.assertEquals(10F, value.getValue(), 0F);
		timeline.update(0.75F);
		Assert.assertEquals(2.5F, value.getValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(0F, value.getValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void refusesToEndTheRootTimeline() {
		try {
			Timeline.createSequence().end();
			Assert.fail("The root timeline has nothing to end");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The timeline has no open beginSequence() or beginParallel() to close, remove this end()", expected.getMessage());
		}
	}

	@Test
	public void refusesATimelineLeftOpen() {
		try {
			Timeline.createSequence().push(Timeline.createSequence().beginSequence());
			Assert.fail("A timeline left open must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The pushed timeline still has an open beginSequence() or beginParallel(), close each one with end() before pushing it", expected.getMessage());
		}
	}

	@Test
	public void refusesAnInfiniteChild() {
		try {
			Timeline.createSequence().push(Tween.mark().repeat(Tween.INFINITY, 0F)).start();
			Assert.fail("A child that repeats forever must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("A timeline cannot hold a child that repeats forever (repeat count -1), give it a finite repeat count", expected.getMessage());
		}
	}

	@Test
	public void refusesATweenOnceStarted() {
		try {
			Timeline.createSequence().start().push(Tween.mark());
			Assert.fail("A started timeline must refuse a new child");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("A timeline takes no new child once it is built (start() builds it), push its children before start()", expected.getMessage());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void refusesATimelineOnceStarted() {
		Timeline.createSequence().start().push(Timeline.createSequence());
	}

	@Test(expected = IllegalStateException.class)
	public void refusesAPauseOnceStarted() {
		Timeline.createSequence().start().pushPause(1F);
	}

	@Test(expected = IllegalStateException.class)
	public void refusesASequenceOnceStarted() {
		Timeline.createSequence().start().beginSequence();
	}

	@Test(expected = IllegalStateException.class)
	public void refusesAParallelOnceStarted() {
		Timeline.createSequence().start().beginParallel();
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToEndOnceStarted() {
		Timeline.createSequence().start().end();
	}

	@Test
	public void listsTheChildrenOfANestedSequence() {
		final Tween tween = Tween.mark();
		final Timeline timeline = Timeline.createSequence().beginSequence().push(tween).end().build();
		final Timeline nested = (Timeline) timeline.getChildren().get(0);
		Assert.assertEquals(1, nested.getChildren().size());
		Assert.assertSame(tween, nested.getChildren().get(0));
	}

	@Test
	public void rewindsEveryChildPastItsStart() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).start();
		timeline.update(0.5F);
		timeline.update(1F);
		timeline.update(1F);
		timeline.update(-3F);
		Assert.assertEquals(0F, second.getValue(), 0F);
		Assert.assertEquals(0F, first.getValue(), 0F);
	}

	@Test
	public void keepsItsChildrenInStepWhenRewindingFromItsEnd() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).start();
		timeline.update(2.5F);
		timeline.update(-1F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(10F, second.getValue(), 0F);
	}

	@Test
	public void keepsItsChildrenInStepAfterARepeatDelay() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).repeat(1, 0.5F).start();
		timeline.update(2.25F);
		timeline.update(0.5F);
		Assert.assertEquals(2.5F, first.getValue(), 0F);
		Assert.assertEquals(0F, second.getValue(), 0F);
	}

	@Test
	public void keepsItsChildrenInStepWhenRewindingAYoyoFromItsEnd() {
		final TweenValue first = new TweenValue(0F);
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).repeatYoyo(1, 0F).start();
		timeline.update(4.5F);
		Assert.assertEquals(0F, first.getValue(), 0F);
		Assert.assertEquals(0F, second.getValue(), 0F);
		timeline.update(-1F);
		Assert.assertEquals(5F, first.getValue(), 0F);
		Assert.assertEquals(0F, second.getValue(), 0F);
		timeline.update(-1F);
		Assert.assertEquals(10F, first.getValue(), 0F);
		Assert.assertEquals(10F, second.getValue(), 0F);
	}

	@Test
	public void notifiesItsChildrenBeforeItsCompletion() {
		final List<String> events = new ArrayList<>();
		final Tween first = TimelineTest.tween(new TweenValue(0F), 10F).onComplete(event -> events.add("first"));
		final Tween second = TimelineTest.tween(new TweenValue(0F), 20F).onComplete(event -> events.add("second"));
		final Timeline timeline = Timeline.createSequence().push(first).push(second).onStart(event -> events.add("start")).onComplete(event -> events.add("complete")).start();
		timeline.update(3F);
		Assert.assertEquals(Arrays.asList("start", "first", "second", "complete"), events);
	}

	@Test
	public void stopsItsNextChildrenWhenAChildKillsIt() {
		final TweenValue second = new TweenValue(0F);
		final Timeline timeline = Timeline.createSequence();
		timeline.push(TimelineTest.tween(new TweenValue(0F), 10F).onComplete(event -> timeline.kill())).push(TimelineTest.tween(second, 20F)).start();
		timeline.update(1.5F);
		Assert.assertEquals(0F, second.getValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	private static Tween tween(final TweenValue value, final float target) {
		return value.to(1F).target(target).ease(TweenEquations.LINEAR);
	}

}