package dev.joid.test.snapshot;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SnapshotReportTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void fillsTheTemplateWithTheTitleAndTheEntries() throws IOException {
		final File shot = this.file("shot.png", "shot");
		final File report = new File(this.folder.getRoot(), "out/report.html");
		SnapshotReport.write(report, "Backends <lwjgl3> & vulkan", Arrays.asList(SnapshotEntry.create("vulkan/say \"hi\\", SnapshotStatus.DIFFERENT, 12, shot, shot), SnapshotEntry.create("vulkan/home", SnapshotStatus.UPDATED, 0, shot, shot)));
		final String html = SnapshotReportTest.text(report);
		Assert.assertTrue(html, html.contains("<title>Backends &lt;lwjgl3&gt; &amp; vulkan</title>"));
		Assert.assertTrue(html, html.contains("[{\"name\":\"vulkan/say \\\"hi\\\\\",\"status\":\"different\",\"pixels\":12},{\"name\":\"vulkan/home\",\"status\":\"updated\",\"pixels\":0}]"));
	}

	@Test
	public void writesTheImagesOfEachShotIntoAScript() throws IOException {
		final File render = this.file("render.png", "render");
		final File reference = this.file("reference.png", "reference");
		SnapshotReport.write(new File(this.folder.getRoot(), "report.html"), "Snapshots", Arrays.asList(SnapshotEntry.create("a", SnapshotStatus.DIFFERENT, 3, reference, render), SnapshotEntry.create("b", SnapshotStatus.IDENTICAL, 0, reference, render)));
		Assert.assertEquals("joidShot(0, \"data:image/png;base64,cmVuZGVy\", \"data:image/png;base64,cmVmZXJlbmNl\");", SnapshotReportTest.text(new File(this.folder.getRoot(), "report/0.js")));
		Assert.assertEquals("joidShot(1, \"data:image/png;base64,cmVuZGVy\", null);", SnapshotReportTest.text(new File(this.folder.getRoot(), "report/1.js")));
	}

	@Test
	public void writesAnEmptyReport() throws IOException {
		final File report = new File(this.folder.getRoot(), "report.html");
		SnapshotReport.write(report, "Snapshots", Collections.emptyList());
		Assert.assertTrue(SnapshotReportTest.text(report).contains("const ENTRIES = [];"));
		Assert.assertEquals(0, new File(this.folder.getRoot(), "report").list().length);
	}

	@Test(expected = UncheckedIOException.class)
	public void failsOnAMissingRender() {
		final File missing = new File(this.folder.getRoot(), "missing.png");
		SnapshotReport.write(new File(this.folder.getRoot(), "report.html"), "Snapshots", Collections.singletonList(SnapshotEntry.create("a", SnapshotStatus.IDENTICAL, 0, missing, missing)));
	}

	@Test(expected = UncheckedIOException.class)
	public void failsWhenTheReportCannotBeWritten() throws IOException {
		SnapshotReport.write(new File(this.folder.newFile("blocked"), "report.html"), "Snapshots", Collections.emptyList());
	}

	private File file(final String name, final String content) throws IOException {
		final File file = this.folder.newFile(name);
		Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
		return file;
	}

	private static String text(final File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}

}