package dev.joid.lib.animation.tween;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.animation.tween.primitive.MutableFloat;

public class TimelineTest {

	@Test
	public void playsASequenceOneChildAfterTheOther() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).start();
		Assert.assertEquals(2F, timeline.getDuration(), 0F);
		timeline.update(0.5F);
		Assert.assertEquals(5F, first.floatValue(), 0F);
		Assert.assertEquals(0F, second.floatValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(20F, second.floatValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void delaysEachChildOfASequence() {
		final Tween first = TimelineTest.tween(new MutableFloat(0F), 10F);
		final Tween second = TimelineTest.tween(new MutableFloat(0F), 20F);
		Timeline.createSequence().push(first).push(second).build();
		Assert.assertEquals(0F, first.getDelay(), 0F);
		Assert.assertEquals(1F, second.getDelay(), 0F);
	}

	@Test
	public void playsAParallelTogether() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline timeline = Timeline.createParallel().push(TimelineTest.tween(first, 10F)).push(Tween.to(second, 0, 2F).target(20F).ease(TweenEquations.LINEAR)).start();
		Assert.assertEquals(2F, timeline.getDuration(), 0F);
		timeline.update(1F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
		timeline.update(1.5F);
		Assert.assertEquals(20F, second.floatValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void waitsDuringAPause() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).pushPause(0.5F).push(TimelineTest.tween(second, 20F)).start();
		Assert.assertEquals(2.5F, timeline.getDuration(), 0F);
		timeline.update(1.25F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(0F, second.floatValue(), 0F);
		timeline.update(0.75F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
	}

	@Test
	public void nestsAParallel() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final MutableFloat third = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).beginParallel().push(TimelineTest.tween(second, 20F)).push(TimelineTest.tween(third, 30F)).end().start();
		Assert.assertEquals(2F, timeline.getDuration(), 0F);
		timeline.update(1.5F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
		Assert.assertEquals(15F, third.floatValue(), 0F);
	}

	@Test
	public void nestsASequence() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final MutableFloat third = new MutableFloat(0F);
		final Timeline timeline = Timeline.createParallel().beginSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).end().push(TimelineTest.tween(third, 30F)).start();
		Assert.assertEquals(2F, timeline.getDuration(), 0F);
		timeline.update(1.5F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
		Assert.assertEquals(30F, third.floatValue(), 0F);
	}

	@Test
	public void pushesAnotherTimeline() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline inner = Timeline.createParallel().push(TimelineTest.tween(second, 20F));
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(inner).start();
		Assert.assertSame(inner, timeline.getChildren().get(1));
		timeline.update(1.5F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
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
	public void exposesReadOnlyChildrenOnceBuilt() {
		Timeline.createSequence().push(Tween.mark()).build().getChildren().clear();
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
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).repeat(1, 0F).start();
		timeline.update(2F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(20F, second.floatValue(), 0F);
		timeline.update(0.5F);
		Assert.assertEquals(5F, first.floatValue(), 0F);
		Assert.assertEquals(0F, second.floatValue(), 0F);
		timeline.update(2F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(20F, second.floatValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void playsItsChildrenBackwardOnAYoyo() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).repeatYoyo(1, 0F).start();
		timeline.update(2F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(20F, second.floatValue(), 0F);
		timeline.update(0.5F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(5F, first.floatValue(), 0F);
		Assert.assertEquals(0F, second.floatValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(0F, first.floatValue(), 0F);
		Assert.assertEquals(0F, second.floatValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void rewindsItsChildrenWithANegativeUpdate() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).start();
		timeline.update(1.5F);
		timeline.update(-1F);
		Assert.assertEquals(5F, first.floatValue(), 0F);
		Assert.assertEquals(0F, second.floatValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
	}

	@Test
	public void replaysItsPausesOnARepeat() {
		final MutableFloat value = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().pushPause(0.5F).push(TimelineTest.tween(value, 10F)).repeat(1, 0F).start();
		timeline.update(1.5F);
		Assert.assertEquals(10F, value.floatValue(), 0F);
		timeline.update(0.25F);
		Assert.assertEquals(0F, value.floatValue(), 0F);
		timeline.update(0.75F);
		Assert.assertEquals(5F, value.floatValue(), 0F);
	}

	@Test
	public void replaysItsPausesOnAYoyo() {
		final MutableFloat value = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().pushPause(0.5F).push(TimelineTest.tween(value, 10F)).repeatYoyo(1, 0F).start();
		timeline.update(1.5F);
		Assert.assertEquals(10F, value.floatValue(), 0F);
		timeline.update(0.75F);
		Assert.assertEquals(2.5F, value.floatValue(), 0F);
		timeline.update(1F);
		Assert.assertEquals(0F, value.floatValue(), 0F);
		Assert.assertTrue(timeline.isFinished());
	}

	@Test
	public void returnsItsChildrenToTheirPools() {
		final Timeline timeline = Timeline.createSequence().push(Tween.mark()).beginParallel().push(Tween.mark()).end();
		final int tweens = Tween.getPoolSize();
		final int timelines = Timeline.getPoolSize();
		timeline.free();
		Assert.assertEquals(tweens + 2, Tween.getPoolSize());
		Assert.assertEquals(timelines + 2, Timeline.getPoolSize());
	}

	@Test
	public void reusesAFreedTimeline() {
		final Timeline timeline = Timeline.createSequence();
		timeline.free();
		Assert.assertSame(timeline, Timeline.createParallel());
	}

	@Test
	public void reservesRoomInThePoolWithoutFillingIt() {
		final int size = Timeline.getPoolSize();
		Timeline.ensurePoolCapacity(size + 50);
		Assert.assertEquals(size, Timeline.getPoolSize());
	}

	@Test(expected = RuntimeException.class)
	public void refusesToEndTheRootTimeline() {
		Timeline.createSequence().end();
	}

	@Test(expected = RuntimeException.class)
	public void refusesATimelineLeftOpen() {
		Timeline.createSequence().push(Timeline.createSequence().beginSequence());
	}

	@Test(expected = RuntimeException.class)
	public void refusesAnInfiniteChild() {
		Timeline.createSequence().push(Tween.mark().repeat(Tween.INFINITY, 0F)).start();
	}

	@Test(expected = RuntimeException.class)
	public void refusesATweenOnceStarted() {
		Timeline.createSequence().start().push(Tween.mark());
	}

	@Test(expected = RuntimeException.class)
	public void refusesATimelineOnceStarted() {
		Timeline.createSequence().start().push(Timeline.createSequence());
	}

	@Test(expected = RuntimeException.class)
	public void refusesAPauseOnceStarted() {
		Timeline.createSequence().start().pushPause(1F);
	}

	@Test(expected = RuntimeException.class)
	public void refusesASequenceOnceStarted() {
		Timeline.createSequence().start().beginSequence();
	}

	@Test(expected = RuntimeException.class)
	public void refusesAParallelOnceStarted() {
		Timeline.createSequence().start().beginParallel();
	}

	@Test(expected = RuntimeException.class)
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
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).start();
		timeline.update(0.5F);
		timeline.update(1F);
		timeline.update(1F);
		timeline.update(-3F);
		Assert.assertEquals(0F, second.floatValue(), 0F);
		Assert.assertEquals(0F, first.floatValue(), 0F);
	}

	@Test
	public void keepsItsChildrenInStepWhenRewindingFromItsEnd() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).start();
		timeline.update(2.5F);
		timeline.update(-1F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
	}

	@Test
	public void keepsItsChildrenInStepAfterARepeatDelay() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).repeat(1, 0.5F).start();
		timeline.update(2.25F);
		timeline.update(0.5F);
		Assert.assertEquals(2.5F, first.floatValue(), 0F);
		Assert.assertEquals(0F, second.floatValue(), 0F);
	}

	@Test
	public void keepsItsChildrenInStepWhenRewindingAYoyoFromItsEnd() {
		final MutableFloat first = new MutableFloat(0F);
		final MutableFloat second = new MutableFloat(0F);
		final Timeline timeline = Timeline.createSequence().push(TimelineTest.tween(first, 10F)).push(TimelineTest.tween(second, 20F)).repeatYoyo(1, 0F).start();
		timeline.update(4.5F);
		Assert.assertEquals(0F, first.floatValue(), 0F);
		Assert.assertEquals(0F, second.floatValue(), 0F);
		timeline.update(-1F);
		Assert.assertEquals(5F, first.floatValue(), 0F);
		Assert.assertEquals(0F, second.floatValue(), 0F);
		timeline.update(-1F);
		Assert.assertEquals(10F, first.floatValue(), 0F);
		Assert.assertEquals(10F, second.floatValue(), 0F);
	}

	private static Tween tween(final MutableFloat value, final float target) {
		return Tween.to(value, 0, 1F).target(target).ease(TweenEquations.LINEAR);
	}

}