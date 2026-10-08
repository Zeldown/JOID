package dev.joid.lib.bridge.render.shader.uniform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.shader.source.ShaderVariable;

public class UniformBlockTest {

	private static final String[][] MEMBERS = {
			{"float", "a", ""},
			{"vec3", "b", ""},
			{"float", "c", ""},
			{"vec2", "d", ""},
			{"ivec3", "e", ""},
			{"bool", "f", ""},
			{"mat2", "g", ""},
			{"float", "h", "[3]"},
			{"vec3", "i", "[2]"},
			{"int", "j", ""},
			{"mat3", "k", ""},
			{"vec4", "l", "[COUNT]"},
			{"mat4", "m", ""},
			{"uvec2", "n", ""},
			{"mat4", "o", "[2]"},
			{"bvec4", "p", ""},
			{"float", "q", ""}
	};

	@Test
	public void packsAScalarAfterAVec3() {
		final UniformBlock block = UniformBlockTest.create();
		Assert.assertEquals(16, block.getMember("b").getOffset());
		Assert.assertEquals(28, block.getMember("c").getOffset());
	}

	@Test
	public void alignsAVec2OnEightBytes() {
		Assert.assertEquals(32, UniformBlockTest.create().getMember("d").getOffset());
	}

	@Test
	public void stridesArraysAndMatrixColumnsOnSixteenBytes() {
		final UniformBlock block = UniformBlockTest.create();
		Assert.assertEquals(16, block.getMember("g").getMatrixStride());
		Assert.assertEquals(32, block.getMember("g").getSize());
		Assert.assertEquals(16, block.getMember("h").getArrayStride());
		Assert.assertEquals(48, block.getMember("h").getSize());
		Assert.assertEquals(16, block.getMember("i").getArrayStride());
		Assert.assertEquals(48, block.getMember("k").getSize());
		Assert.assertEquals(64, block.getMember("o").getArrayStride());
	}

	@Test
	public void resolvesAnArrayLengthFromADefine() {
		Assert.assertEquals(80, UniformBlockTest.create().getMember("l").getSize());
	}

	@Test
	public void roundsTheBlockSizeToSixteenBytes() {
		final UniformBlock block = UniformBlockTest.create();
		Assert.assertEquals(0, block.getSize() % 16);
		Assert.assertEquals(block.getMember("q").getOffset() + 16 - block.getMember("q").getOffset() % 16, block.getSize());
	}

	@Test
	public void keepsSixteenBytesForAnEmptyBlock() {
		Assert.assertEquals(16, UniformBlock.create(Collections.emptyList(), "").getSize());
	}

	@Test
	public void packsAMatrixColumnByColumn() {
		final UniformBlock block = UniformBlockTest.create();
		final UniformMember member = block.getMember("k").value(1F, 2F, 3F, 4F, 5F, 6F, 7F, 8F, 9F);
		Assert.assertTrue(block.pack());
		Assert.assertEquals(1F, block.getData().getFloat(member.getOffset()), 0F);
		Assert.assertEquals(3F, block.getData().getFloat(member.getOffset() + 8), 0F);
		Assert.assertEquals(4F, block.getData().getFloat(member.getOffset() + 16), 0F);
		Assert.assertEquals(9F, block.getData().getFloat(member.getOffset() + 40), 0F);
	}

	@Test
	public void packsArrayElementsOnTheirStride() {
		final UniformBlock block = UniformBlockTest.create();
		final UniformMember member = block.getMember("h").value(1F, 2F, 3F);
		block.pack();
		Assert.assertEquals(2F, block.getData().getFloat(member.getOffset() + 16), 0F);
		Assert.assertEquals(3F, block.getData().getFloat(member.getOffset() + 32), 0F);
	}

	@Test
	public void packsScalarsAndVectorsAtTheirOffset() {
		final UniformBlock block = UniformBlockTest.create();
		block.getMember("j").value(7);
		block.getMember("b").value(1F, 2F, 3F);
		block.pack();
		Assert.assertEquals(7, block.getData().getInt(block.getMember("j").getOffset()));
		Assert.assertEquals(3F, block.getData().getFloat(block.getMember("b").getOffset() + 8), 0F);
		Assert.assertEquals(0F, block.getData().getFloat(block.getMember("c").getOffset()), 0F);
	}

	@Test
	public void packsOnlyWhenAValueChanged() {
		final UniformBlock block = UniformBlockTest.create();
		Assert.assertFalse(block.pack());
		block.value("a", 1F);
		Assert.assertTrue(block.pack());
		block.value("a", 1F);
		Assert.assertFalse(block.pack());
	}

	@Test
	public void uploadsEachChangedMemberOnce() {
		final UniformBlock block = UniformBlockTest.create();
		final List<String> uploaded = new ArrayList<>();
		block.value("f", true).value("c", 2F).upload(member -> uploaded.add(member.getName()));
		block.upload(member -> uploaded.add(member.getName()));
		Assert.assertEquals(2, uploaded.size());
		Assert.assertEquals("c", uploaded.get(0));
		Assert.assertEquals("f", uploaded.get(1));
	}

	@Test
	public void ignoresAValueForAnUndeclaredMember() {
		final UniformBlock block = UniformBlockTest.create();
		block.value("unknown", 1F).value("unknown", 1).value("unknown", true);
		Assert.assertFalse(block.pack());
	}

	@Test
	public void namesAnUnsupportedType() {
		try {
			UniformBlock.create(Collections.singletonList(ShaderVariable.create("dmat4", "x", "", false)), "");
			Assert.fail("An unsupported type must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("Unsupported uniform type dmat4", expected.getMessage());
		}
	}

	@Test
	public void namesAnUnresolvedArrayLength() {
		try {
			UniformBlock.create(Collections.singletonList(ShaderVariable.create("float", "x", "[SIZE]", false)), "");
			Assert.fail("An unresolved array length must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("Unable to resolve the length SIZE of the uniform array x", expected.getMessage());
		}
	}

	@Test
	public void returnsNoMemberForAnUnknownName() {
		Assert.assertNull(UniformBlockTest.create().getMember("unknown"));
	}

	private static UniformBlock create() {
		final List<ShaderVariable> variables = new ArrayList<>();
		for (final String[] member : UniformBlockTest.MEMBERS) {
			variables.add(ShaderVariable.create(member[0], member[1], member[2], false));
		}
		return UniformBlock.create(variables, "#define COUNT 5\n");
	}

}