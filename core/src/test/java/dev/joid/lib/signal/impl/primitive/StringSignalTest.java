package dev.joid.lib.signal.impl.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class StringSignalTest {

	@Test
	public void startsWithAnEmptyTextWithoutDefault() {
		final StringSignal signal = StringSignal.of();
		Assert.assertEquals("", signal.get());
		Assert.assertEquals("", signal.trim().toUpperCase().get());
	}

	@Test
	public void startsWithTheGivenValue() {
		final StringSignal signal = StringSignal.of("joid");
		Assert.assertEquals("joid", signal.get());
	}

	@Test
	public void resetsToTheGivenValue() {
		Assert.assertEquals("joid", StringSignal.of("joid").set("ui").reset().get());
	}

	@Test
	public void computesFromItsDefault() {
		final StringSignal signal = StringSignal.of("jo");
		signal.append("id");
		Assert.assertEquals("joid", signal.get());
	}

	@Test
	public void appendsAndConcatenates() {
		final StringSignal signal = StringSignal.of("jo");
		signal.append("id");
		Assert.assertEquals("joid", signal.get());
		signal.concat(" ui");
		Assert.assertEquals("joid ui", signal.get());
	}

	@Test
	public void replacesACharacter() {
		final StringSignal signal = StringSignal.of("joid");
		signal.replace('o', '0');
		Assert.assertEquals("j0id", signal.get());
	}

	@Test
	public void replacesASequence() {
		final StringSignal signal = StringSignal.of("joid ui ui");
		signal.replace("ui", "lib");
		Assert.assertEquals("joid lib lib", signal.get());
	}

	@Test
	public void changesItsCase() {
		final StringSignal signal = StringSignal.of("Shop");
		signal.toLowerCase();
		Assert.assertEquals("shop", signal.get());
		signal.toUpperCase();
		Assert.assertEquals("SHOP", signal.get());
	}

	@Test
	public void trimsItsValue() {
		final StringSignal signal = StringSignal.of(" \tjoid \n");
		signal.trim();
		Assert.assertEquals("joid", signal.get());
	}

	@Test
	public void keepsASubstring() {
		final StringSignal signal = StringSignal.of("joid lib ui");
		signal.substring(5);
		Assert.assertEquals("lib ui", signal.get());
		signal.substring(0, 3);
		Assert.assertEquals("lib", signal.get());
	}

	@Test(expected = StringIndexOutOfBoundsException.class)
	public void refusesASubstringOutOfBounds() {
		StringSignal.of("joid").substring(5);
	}

	@Test
	public void internsItsValueWithoutNotifying() {
		final List<String> received = new ArrayList<>();
		final StringSignal signal = StringSignal.of(new String("joid"));
		signal.subscribe(received::add);
		signal.intern();
		Assert.assertSame("joid", signal.get());
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void notifiesOnlyTheOperationsChangingItsValue() {
		final List<String> received = new ArrayList<>();
		final StringSignal signal = StringSignal.of("Shop");
		signal.subscribe(received::add);
		signal.toUpperCase();
		signal.trim();
		signal.append("");
		signal.toLowerCase();
		Assert.assertEquals(Arrays.asList("SHOP", "shop"), received);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("StringSignal{joid}", StringSignal.of("joid").toString());
		Assert.assertEquals("StringSignal{}", StringSignal.of().toString());
	}

	@Test
	public void appendsToAnEmptySignal() {
		final StringSignal signal = StringSignal.of();
		signal.append("joid");
		Assert.assertEquals("joid", signal.get());
	}

	@Test
	public void chainsItsOperations() {
		final StringSignal signal = StringSignal.of(" jo ");
		Assert.assertSame(signal, signal.trim().append("id").toUpperCase());
		Assert.assertEquals("JOID", signal.get());
	}

}