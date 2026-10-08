package dev.joid.test.snapshot;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import lombok.NonNull;

public class SnapshotBaselineTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test(expected = ClassNotFoundException.class)
	public void refusesAnUnknownBackend() throws Exception {
		SnapshotBaseline.main(new String[] {"dev.joid.test.snapshot.MissingBackend", this.folder.getRoot().getPath()});
	}

	@Test(expected = ClassCastException.class)
	public void refusesAClassThatIsNotABackend() throws Exception {
		SnapshotBaseline.main(new String[] {Object.class.getName(), this.folder.getRoot().getPath()});
	}

	@Test
	public void clearsThePreviousShotsBeforeRendering() throws IOException, ReflectiveOperationException {
		final File output = this.folder.newFolder("renders");
		Assert.assertTrue(new File(output, "static-home.png").createNewFile());
		Assert.assertTrue(new File(output, "notes.txt").createNewFile());
		try {
			SnapshotBaseline.main(new String[] {UnavailableBackend.class.getName(), output.getPath()});
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("No window", e.getMessage());
		}
		Assert.assertArrayEquals(new String[] {"notes.txt"}, output.list());
	}

	@Test(expected = IllegalStateException.class)
	public void reachesTheBackendWithoutAnOutputFolder() throws Exception {
		SnapshotBaseline.main(new String[] {UnavailableBackend.class.getName(), new File(this.folder.getRoot(), "missing").getPath()});
	}

	public static final class UnavailableBackend implements ISnapshotBackend {

		@Override
		public void destroy() {}

		@Override
		public void create(final int width, final int height) {
			throw new IllegalStateException("No window");
		}

		@Override
		public void present() {}

		@Override
		public @NonNull SnapshotImage capture(final int width, final int height) {
			throw new IllegalStateException("No window");
		}

		@Override
		public @NonNull String getRenderer() {
			return "unavailable";
		}

	}

}