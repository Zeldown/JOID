package dev.joid.lib.bridge.render.vertex;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;

public class VertexAttributeTest {

	@Test
	public void findsTheAttributeOfEachBuiltin() {
		for (final VertexAttribute attribute : VertexAttribute.values()) {
			Assert.assertSame(attribute, VertexAttribute.of(attribute.getBuiltin()));
			Assert.assertSame(ShaderBuiltin.Kind.ATTRIBUTE, attribute.getBuiltin().getKind());
		}
	}

	@Test
	public void findsNoAttributeForAUniform() {
		Assert.assertNull(VertexAttribute.of(ShaderBuiltin.PROJECTION_MATRIX));
	}

	@Test
	public void numbersTheLocationsInOrder() {
		for (final VertexAttribute attribute : VertexAttribute.values()) {
			Assert.assertEquals(attribute.ordinal(), attribute.getLocation());
		}
	}

	@Test
	public void fitsEveryAttributeInTheStride() {
		int end = 0;
		for (final VertexAttribute attribute : VertexAttribute.values()) {
			Assert.assertTrue(attribute.name(), attribute.getOffset() >= end);
			end = attribute.getOffset() + attribute.getComponents() * attribute.getComponent().getSize();
		}
		Assert.assertTrue(end <= VertexBuffer.STRIDE);
	}

	@Test
	public void coversEveryBuiltinAttribute() {
		for (final ShaderBuiltin builtin : ShaderBuiltin.values()) {
			Assert.assertEquals(builtin.name(), builtin.getKind() == ShaderBuiltin.Kind.ATTRIBUTE, VertexAttribute.of(builtin) != null);
		}
	}

}