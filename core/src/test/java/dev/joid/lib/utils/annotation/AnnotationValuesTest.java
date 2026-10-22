package dev.joid.lib.utils.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Assert;
import org.junit.Test;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class AnnotationValuesTest {

	@Test
	public void writesEveryValueOfTheAnnotation() {
		final Values values = AnnotationValues.read(CustomType.class.getAnnotation(Sample.class), new Values());
		Assert.assertEquals(3, values.size());
		Assert.assertEquals("plain", values.name());
		Assert.assertTrue(values.nested().on());
	}

	@Test
	public void keepsTheTargetWithoutAnnotation() {
		final Values values = new Values().size(7);
		Assert.assertSame(values, AnnotationValues.read(null, values));
		Assert.assertEquals(7, values.size());
	}

	@Test
	public void writesOnlyTheValuesChangedBetweenTwoAnnotations() {
		final Values values = new Values().size(7).name("kept");
		Assert.assertSame(values, AnnotationValues.merge(values, DefaultType.class.getAnnotation(Sample.class), CustomType.class.getAnnotation(Sample.class)));
		Assert.assertEquals(3, values.size());
		Assert.assertEquals("kept", values.name());
		Assert.assertTrue(values.nested().on());
	}

	@Test
	public void comparesAMissingAnnotationWithItsDefaults() {
		final Values added = AnnotationValues.merge(new Values().name("kept"), null, CustomType.class.getAnnotation(Sample.class));
		Assert.assertEquals(3, added.size());
		Assert.assertEquals("kept", added.name());
		final Values removed = AnnotationValues.merge(new Values().size(3).name("kept"), CustomType.class.getAnnotation(Sample.class), null);
		Assert.assertEquals(1, removed.size());
		Assert.assertEquals("kept", removed.name());
		Assert.assertFalse(removed.nested().on());
	}

	@Test
	public void refusesATargetWithoutTheMethodOfAValue() {
		try {
			AnnotationValues.read(CustomType.class.getAnnotation(Sample.class), new Object());
			Assert.fail("A target without size(...) must be refused");
		} catch (final IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().startsWith("The class java.lang.Object has no public method "));
		}
	}

	@Test
	public void reportsTheErrorThrownByTheMethodOfAValue() {
		try {
			AnnotationValues.read(CustomType.class.getAnnotation(Sample.class), new RefusingValues());
			Assert.fail("A method of a value that throws must be reported");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("refused size 3", expected.getCause().getMessage());
			Assert.assertTrue(expected.getMessage(), expected.getMessage().endsWith("(java.lang.IllegalArgumentException: refused size 3)"));
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Sample {

		public int    size()   default 1;
		public String name()   default "plain";
		public Nested nested() default @Nested;

	}

	@Target({})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Nested {

		public boolean on() default false;

	}

	@Sample
	public static class DefaultType {}

	@Sample(size = 3, nested = @Nested(on = true))
	public static class CustomType {}

	@Getter
	@Setter
	@Accessors(fluent = true, chain = true)
	public static final class Values {

		private final NestedValues nested = new NestedValues();

		private int    size = 1;
		private String name = "plain";

	}

	public static final class RefusingValues {

		public RefusingValues size(final int size) {
			throw new IllegalArgumentException("refused size " + size);
		}

		public RefusingValues name(final String name) {
			return this;
		}

		public NestedValues nested() {
			return new NestedValues();
		}

	}

	@Getter
	@Setter
	@Accessors(fluent = true, chain = true)
	public static final class NestedValues {

		private boolean on;

	}

}