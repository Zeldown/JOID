package dev.joid.lib.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOIDEnv;

public class FileCacheTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private File previous;

	@Before
	public void useATemporaryCache() {
		this.previous = JOIDEnv.getCacheDir();
		JOIDEnv.cacheDir(this.folder.getRoot());
	}

	@After
	public void restoreTheCache() {
		JOIDEnv.cacheDir(this.previous);
	}

	@Test
	public void returnsTheSameAreaForTheSameName() {
		Assert.assertSame(FileCache.area("same"), FileCache.area("same"));
		Assert.assertEquals(new File(this.folder.getRoot(), "same"), FileCache.area("same").getDirectory());
	}

	@Test
	public void keepsThirtyDaysByDefault() {
		Assert.assertEquals(Duration.ofDays(30L), FileCache.area("default").getRetention());
	}

	@Test
	public void acceptsTheSameDeclarationTwice() {
		final FileCacheArea area = FileCache.area("twice").retention(Duration.ofDays(7L));
		Assert.assertSame(area, FileCache.area("twice").retention(Duration.ofDays(7L)));
		Assert.assertEquals(Duration.ofDays(7L), area.getRetention());
	}

	@Test
	public void refusesAnotherRetentionForADeclaredArea() {
		FileCache.area("conflict").retention(Duration.ofDays(7L));
		try {
			FileCache.area("conflict").retention(Duration.ofDays(14L));
			Assert.fail("The area must not be declared again");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The cache area conflict is already declared with a retention of 7 days, it cannot be declared again with 14 days: give one of them another name", expected.getMessage());
		}
		Assert.assertEquals(Duration.ofDays(7L), FileCache.area("conflict").getRetention());
	}

	@Test
	public void refusesANegativeRetention() {
		try {
			FileCache.area("negative").retention(Duration.ofHours(-1L));
			Assert.fail("The retention must be positive");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The retention PT-1H of the cache area negative is not positive, pass a duration above 0", expected.getMessage());
		}
	}

	@Test
	public void refusesANameThatIsNotAPlainFileName() {
		for (final String name : new String[] {"", ".hidden", "../escape", "a/b", "a\\b"}) {
			try {
				FileCache.area(name);
				Assert.fail(name);
			} catch (final IllegalArgumentException expected) {
				Assert.assertEquals("The cache area name " + name + " is not a plain file name: start with a letter or a digit, then use only letters, digits, '.', '-' and '_'", expected.getMessage());
			}
		}

		try {
			FileCache.area("keys").entry("../escape");
			Assert.fail("The key must be a plain file name");
		} catch (final IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().startsWith("The cache entry name ../escape is not a plain file name"));
		}
	}

	@Test
	public void computesAnEntryOnlyOnce() throws IOException {
		final FileCacheArea area = FileCache.area("once");
		final AtomicInteger writes = new AtomicInteger();
		Assert.assertNull(area.entry("key"));
		final File file = area.compute("key", entry -> FileCacheTest.write(entry, "value " + writes.incrementAndGet()));
		Assert.assertEquals(new File(area.getDirectory(), "key"), file);
		Assert.assertEquals(file, area.compute("key", entry -> FileCacheTest.write(entry, "value " + writes.incrementAndGet())));
		Assert.assertEquals(file, area.entry("key"));
		Assert.assertEquals("value 1", FileCacheTest.read(file));
		Assert.assertEquals(1, writes.get());
	}

	@Test
	public void writesIntoATemporaryFileBeforeTheEntryAppears() throws IOException {
		final FileCacheArea area = FileCache.area("atomic");
		final File file = area.compute("key", entry -> {
			Assert.assertEquals(area.getDirectory(), entry.getParentFile());
			Assert.assertTrue(entry.getName(), entry.getName().startsWith(".") && entry.getName().endsWith(".tmp"));
			FileCacheTest.write(entry, "half");
			Assert.assertNull(area.entry("key"));
			FileCacheTest.write(entry, "complete");
		});
		Assert.assertEquals("complete", FileCacheTest.read(file));
		Assert.assertArrayEquals(new String[] {"key"}, FileCacheTest.entries(area));
	}

	@Test
	public void leavesNothingWhenTheWriterFails() {
		final FileCacheArea area = FileCache.area("failure");
		try {
			area.compute("key", entry -> {
				FileCacheTest.write(entry, "half");
				throw new IOException("The download stopped");
			});
			Assert.fail("The writer must fail");
		} catch (final IOException expected) {
			Assert.assertEquals("The download stopped", expected.getMessage());
		}
		Assert.assertArrayEquals(new String[0], area.getDirectory().list((parent, name) -> !name.equals(".area")));
	}

	@Test
	public void refusesAWriterThatWritesNothing() {
		final FileCacheArea area = FileCache.area("nothing");
		try {
			area.compute("key", entry -> {
			});
			Assert.fail("The writer must write");
		} catch (final IOException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().startsWith("The writer of the cache entry key of the area nothing wrote nothing at "));
		}
	}

	@Test
	public void storesAnEntryUnderTheHashOfItsContent() throws IOException, NoSuchAlgorithmException {
		final FileCacheArea area = FileCache.area("content");
		final File file = area.store(entry -> FileCacheTest.write(entry, "video"));
		Assert.assertEquals(String.format(Locale.ROOT, "%040x", new BigInteger(1, MessageDigest.getInstance("SHA-1").digest("video".getBytes(StandardCharsets.UTF_8)))), file.getName());
		Assert.assertEquals(file, area.store(entry -> FileCacheTest.write(entry, "video")));
		Assert.assertArrayEquals(new String[] {file.getName()}, FileCacheTest.entries(area));
	}

	@Test
	public void removesAnEntry() throws IOException {
		final FileCacheArea area = FileCache.area("remove");
		area.compute("key", entry -> FileCacheTest.write(entry, "value"));
		Assert.assertTrue(area.remove("key"));
		Assert.assertFalse(area.remove("key"));
		Assert.assertNull(area.entry("key"));
		Assert.assertArrayEquals(new String[0], FileCacheTest.entries(area));
	}

	@Test
	public void datesTheLastUseOfAnEntry() throws IOException {
		final FileCacheArea area = FileCache.area("touch");
		final File file = area.compute("key", entry -> FileCacheTest.write(entry, "value"));
		Assert.assertTrue(file.setLastModified(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10L)));
		area.entry("key");
		Assert.assertTrue(System.currentTimeMillis() - file.lastModified() < TimeUnit.HOURS.toMillis(1L));
	}

	@Test
	public void cleansTheEntriesUnusedForTheRetentionOfTheirArea() throws IOException {
		final FileCacheArea area = FileCache.area("retention").retention(Duration.ofDays(7L));
		final File old = FileCacheTest.age(area.compute("old", entry -> FileCacheTest.write(entry, "12345")), 8L);
		final File recent = FileCacheTest.age(area.compute("recent", entry -> FileCacheTest.write(entry, "12345")), 6L);
		final File folder = FileCacheTest.age(area.compute("folder", entry -> {
			Files.createDirectories(entry.toPath());
			FileCacheTest.write(new File(entry, "library"), "123");
		}), 8L);
		final String output = FileCacheTest.capture(() -> Assert.assertEquals(8L, FileCache.clean()));
		Assert.assertFalse(old.exists());
		Assert.assertFalse(folder.exists());
		Assert.assertTrue(recent.isFile());
		Assert.assertEquals("[JOID] The cache freed 8 B (retention: 2 files)" + System.lineSeparator(), output);
	}

	@Test
	public void cleansAnUnknownAreaAfterThirtyDays() throws IOException {
		final File directory = this.folder.newFolder("unknown");
		Files.createFile(new File(directory, ".area").toPath());
		final File old = FileCacheTest.age(FileCacheTest.write(new File(directory, "old"), "1"), 31L);
		final File recent = FileCacheTest.age(FileCacheTest.write(new File(directory, "recent"), "1"), 29L);
		final String output = FileCacheTest.capture(() -> Assert.assertEquals(1L, FileCache.clean()));
		Assert.assertFalse(old.exists());
		Assert.assertTrue(recent.isFile());
		Assert.assertTrue(new File(directory, ".area").isFile());
		Assert.assertEquals("[JOID] The cache freed 1 B (unknown: 1 file)" + System.lineSeparator(), output);
	}

	@Test
	public void leavesAFolderThatIsNotAnArea() throws IOException {
		final File directory = this.folder.newFolder("foreign");
		final File file = FileCacheTest.age(FileCacheTest.write(new File(directory, "file"), "1"), 365L);
		final String output = FileCacheTest.capture(() -> Assert.assertEquals(0L, FileCache.clean()));
		Assert.assertTrue(file.isFile());
		Assert.assertEquals("", output);
	}

	@Test
	public void cleansTheOrphanTemporaryFilesAfterADay() throws IOException {
		final FileCacheArea area = FileCache.area("orphan");
		area.compute("key", entry -> FileCacheTest.write(entry, "value"));
		final File orphan = FileCacheTest.age(FileCacheTest.write(new File(area.getDirectory(), ".crashed.tmp"), "12"), 2L);
		final File writing = FileCacheTest.write(new File(area.getDirectory(), ".writing.tmp"), "12");
		final String output = FileCacheTest.capture(() -> Assert.assertEquals(2L, FileCache.clean()));
		Assert.assertFalse(orphan.exists());
		Assert.assertTrue(writing.isFile());
		Assert.assertNotNull(area.entry("key"));
		Assert.assertEquals("[JOID] The cache freed 2 B (orphan: 1 file)" + System.lineSeparator(), output);
	}

	@Test
	public void writesTheRetentionIntoTheMarkerOfItsArea() throws IOException {
		final FileCacheArea area = FileCache.area("marked").retention(Duration.ofDays(7L));
		area.compute("key", entry -> FileCacheTest.write(entry, "value"));
		Assert.assertEquals("PT168H", FileCacheTest.read(new File(area.getDirectory(), ".area")));
	}

	@Test
	public void updatesTheMarkerWhenTheAreaIsDeclaredAgain() throws IOException {
		final File directory = this.folder.newFolder("redeclared");
		FileCacheTest.write(new File(directory, ".area"), "PT1H");
		FileCache.area("redeclared").retention(Duration.ofDays(3L));
		Assert.assertEquals("PT72H", FileCacheTest.read(new File(directory, ".area")));
		FileCacheTest.write(new File(directory, ".area"), "PT1H");
		FileCache.area("redeclared").retention(Duration.ofDays(3L));
		Assert.assertEquals("PT72H", FileCacheTest.read(new File(directory, ".area")));
	}

	@Test
	public void cleansAnAreaNotDeclaredYetWithItsRecordedRetention() throws IOException {
		final File directory = this.folder.newFolder("recorded");
		FileCacheTest.write(new File(directory, ".area"), "PT48H");
		final File old = FileCacheTest.age(FileCacheTest.write(new File(directory, "old"), "1"), 3L);
		final File recent = FileCacheTest.age(FileCacheTest.write(new File(directory, "recent"), "1"), 1L);
		FileCacheTest.capture(() -> Assert.assertEquals(1L, FileCache.clean()));
		Assert.assertFalse(old.exists());
		Assert.assertTrue(recent.isFile());
	}

	@Test
	public void keepsTheAreaItCleansForItsLaterUse() throws IOException, ReflectiveOperationException {
		final File directory = this.folder.newFolder("cleaned");
		FileCacheTest.write(new File(directory, ".area"), "PT48H");
		FileCacheTest.capture(FileCache::clean);
		final Field field = FileCache.class.getDeclaredField("AREA_MAP");
		field.setAccessible(true);
		Assert.assertSame(((Map<?, ?>) field.get(null)).get("cleaned"), FileCache.area("cleaned"));
	}

	@Test
	public void keepsThirtyDaysForAnUnreadableMarker() throws IOException {
		final File directory = this.folder.newFolder("unreadable");
		FileCacheTest.write(new File(directory, ".area"), "a week");
		final File old = FileCacheTest.age(FileCacheTest.write(new File(directory, "old"), "1"), 31L);
		final File recent = FileCacheTest.age(FileCacheTest.write(new File(directory, "recent"), "1"), 29L);
		FileCacheTest.capture(() -> Assert.assertEquals(1L, FileCache.clean()));
		Assert.assertFalse(old.exists());
		Assert.assertTrue(recent.isFile());
	}

	@Test
	public void replacesAnEntryThatIsOpenedElsewhere() throws IOException {
		final FileCacheArea area = FileCache.area("opened");
		final File old = area.compute("key", entry -> FileCacheTest.write(entry, "old"));
		final File file;
		try (final InputStream reader = new FileInputStream(old)) {
			Assert.assertTrue(area.remove("key"));
			file = area.compute("key", entry -> FileCacheTest.write(entry, "new"));
			Assert.assertEquals("new", FileCacheTest.read(file));
			Assert.assertEquals(file, area.entry("key"));
			Assert.assertEquals('o', reader.read());
		}

		FileCacheTest.capture(FileCache::clean);
		Assert.assertArrayEquals(new String[] {file.getName()}, FileCacheTest.entries(area));
		Assert.assertEquals(file, area.entry("key"));
	}

	@Test
	public void leavesAnEntryItCannotRemoveToTheNextCleaning() throws IOException {
		final FileCacheArea area = FileCache.area("locked");
		final File file = area.compute("key", entry -> FileCacheTest.write(entry, "value"));
		try (final InputStream reader = new FileInputStream(file)) {
			Assert.assertTrue(area.remove("key"));
			Assert.assertNull(area.entry("key"));
			Assert.assertEquals('v', reader.read());
		}

		FileCacheTest.capture(FileCache::clean);
		Assert.assertArrayEquals(new String[0], FileCacheTest.entries(area));
	}

	@Test
	public void sharesItsFolderWithAnotherProcess() throws IOException {
		final FileCacheArea first = new FileCacheArea("shared");
		final FileCacheArea second = new FileCacheArea("shared");
		final File file = first.compute("key", entry -> {
			FileCacheTest.write(entry, "first");
			Assert.assertEquals(0L, second.clean(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1L), new AtomicInteger()));
			Assert.assertTrue(entry.isFile());
			Assert.assertEquals("second", FileCacheTest.read(second.compute("key", other -> FileCacheTest.write(other, "second"))));
		});
		Assert.assertEquals(new File(second.getDirectory(), "key"), file);
		Assert.assertTrue(FileCacheTest.read(file).equals("first") || FileCacheTest.read(file).equals("second"));
		Assert.assertArrayEquals(new String[] {"key"}, FileCacheTest.entries(first));
		Assert.assertEquals(file, second.entry("key"));
		Assert.assertEquals(0L, second.clean(System.currentTimeMillis(), new AtomicInteger()));
		Assert.assertTrue(file.isFile());
	}

	private static File write(final File file, final String text) throws IOException {
		Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
		return file;
	}

	private static String read(final File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}

	private static File age(final File file, final long days) {
		Assert.assertTrue(file.setLastModified(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days)));
		return file;
	}

	private static String[] entries(final FileCacheArea area) {
		return area.getDirectory().list((parent, name) -> !name.startsWith("."));
	}

	private static String capture(final Runnable runnable) {
		final PrintStream previous = System.out;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setOut(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setOut(previous);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

}