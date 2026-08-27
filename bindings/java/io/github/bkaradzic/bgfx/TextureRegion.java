// Copyright 2011-2026 Branimir Karadzic. All rights reserved.
// License: https://github.com/bkaradzic/bgfx/blob/master/LICENSE


//
// AUTO GENERATED! DO NOT EDIT!
//

package io.github.bkaradzic.bgfx;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Objects;

import io.github.bkaradzic.bgfx.util.NativeObject;

import static io.github.bkaradzic.bgfx.BGFX.*;
import static io.github.bkaradzic.bgfx.util.FFMUtil.*;

/**
 * Region of a texture, used as the source or destination of a blit, or as
 * the region handed to {@code read}.
 * <p>
 * Every field defaults to zero, and zero always means "the natural whole".
 * {@code { .handle = tex }} therefore addresses all of mip 0.
 */
public final class TextureRegion extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_texture_region_t",
		TextureHandle.LAYOUT.withName("handle"),
		ValueLayout.JAVA_BYTE.withName("mip"),
		ValueLayout.JAVA_SHORT.withName("x"),
		ValueLayout.JAVA_SHORT.withName("y"),
		ValueLayout.JAVA_SHORT.withName("z"),
		ValueLayout.JAVA_SHORT.withName("width"),
		ValueLayout.JAVA_SHORT.withName("height"),
		ValueLayout.JAVA_SHORT.withName("depth"));
	private static final MethodHandle MH_HANDLE = LAYOUT.sliceHandle(
		MemoryLayout.PathElement.groupElement("handle"));
	private static final VarHandle VH_MIP = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("mip"));
	private static final VarHandle VH_X = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("x"));
	private static final VarHandle VH_Y = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("y"));
	private static final VarHandle VH_Z = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("z"));
	private static final VarHandle VH_WIDTH = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("width"));
	private static final VarHandle VH_HEIGHT = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("height"));
	private static final VarHandle VH_DEPTH = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("depth"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public TextureRegion(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public TextureRegion(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Texture handle.
	 * @return the field value
	 */
	public TextureHandle handle() {
		return TextureHandle.read(slice(MH_HANDLE, segment()));
	}

	/**
	 * Sets the native {@code handle} field.
	 * @param value the new field value
	 */
	public void handle(TextureHandle value) {
		value.write(slice(MH_HANDLE, segment()));
	}

	/**
	 * Mip level.
	 * @return the field value
	 */
	public byte mip() {
		return (byte) VH_MIP.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code mip} field.
	 * @param value the new field value
	 */
	public void mip(byte value) {
		VH_MIP.set(segment(), 0L, value);
	}

	/**
	 * X position of the region.
	 * @return the field value
	 */
	public short x() {
		return (short) VH_X.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code x} field.
	 * @param value the new field value
	 */
	public void x(short value) {
		VH_X.set(segment(), 0L, value);
	}

	/**
	 * Y position of the region.
	 * @return the field value
	 */
	public short y() {
		return (short) VH_Y.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code y} field.
	 * @param value the new field value
	 */
	public void y(short value) {
		VH_Y.set(segment(), 0L, value);
	}

	/**
	 * If texture is 2D this should be 0. If the texture is a cube map
	 * this is the cube face, for a 2D array it is the layer, and for a
	 * 3D texture it is the Z position.
	 * @return the field value
	 */
	public short z() {
		return (short) VH_Z.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code z} field.
	 * @param value the new field value
	 */
	public void z(short value) {
		VH_Z.set(segment(), 0L, value);
	}

	/**
	 * Width of the region. 0 uses the rest of the mip from {@code x}.
	 * @return the field value
	 */
	public short width() {
		return (short) VH_WIDTH.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code width} field.
	 * @param value the new field value
	 */
	public void width(short value) {
		VH_WIDTH.set(segment(), 0L, value);
	}

	/**
	 * Height of the region. 0 uses the rest of the mip from {@code y}.
	 * @return the field value
	 */
	public short height() {
		return (short) VH_HEIGHT.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code height} field.
	 * @param value the new field value
	 */
	public void height(short value) {
		VH_HEIGHT.set(segment(), 0L, value);
	}

	/**
	 * Depth of the region for a 3D texture, or the number of layers or
	 * cube faces otherwise. 0 uses the rest from {@code z}.
	 * @return the field value
	 */
	public short depth() {
		return (short) VH_DEPTH.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code depth} field.
	 * @param value the new field value
	 */
	public void depth(short value) {
		VH_DEPTH.set(segment(), 0L, value);
	}

	/**
	 * Fill in the region of a plain 2D texture. {@code mip}, {@code z} and {@code depth} are left
	 * at zero, which addresses mip 0 of the only slice a 2D texture has.
	 * @param _handle Texture handle.
	 * @param _x X position of the region.
	 * @param _y Y position of the region.
	 * @param _width Width of the region. 0 uses the rest of the mip from {@code _x}.
	 * @param _height Height of the region. 0 uses the rest of the mip from {@code _y}.
	 */
	public final void init(TextureHandle _handle, short _x, short _y, short _width, short _height) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_TEXTURE_REGION_INIT).invokeExact(segment(), _handle.allocate(arena), _x, _y, _width, _height);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}
}
