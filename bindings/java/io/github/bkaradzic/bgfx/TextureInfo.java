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
 * Texture info.
 */
public final class TextureInfo extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_texture_info_t",
		ValueLayout.JAVA_INT.withName("format"),
		ValueLayout.JAVA_INT.withName("storageSize"),
		ValueLayout.JAVA_SHORT.withName("width"),
		ValueLayout.JAVA_SHORT.withName("height"),
		ValueLayout.JAVA_SHORT.withName("depth"),
		ValueLayout.JAVA_SHORT.withName("numLayers"),
		ValueLayout.JAVA_BYTE.withName("numMips"),
		ValueLayout.JAVA_BYTE.withName("bitsPerPixel"),
		ValueLayout.JAVA_BOOLEAN.withName("cubeMap"));
	private static final VarHandle VH_FORMAT = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("format"));
	private static final VarHandle VH_STORAGESIZE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("storageSize"));
	private static final VarHandle VH_WIDTH = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("width"));
	private static final VarHandle VH_HEIGHT = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("height"));
	private static final VarHandle VH_DEPTH = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("depth"));
	private static final VarHandle VH_NUMLAYERS = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("numLayers"));
	private static final VarHandle VH_NUMMIPS = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("numMips"));
	private static final VarHandle VH_BITSPERPIXEL = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("bitsPerPixel"));
	private static final VarHandle VH_CUBEMAP = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("cubeMap"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public TextureInfo(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public TextureInfo(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Texture format.
	 * @return the field value
	 */
	public TextureFormat format() {
		return TextureFormat.fromValue((int) VH_FORMAT.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code format} field.
	 * @param value the new field value
	 */
	public void format(TextureFormat value) {
		VH_FORMAT.set(segment(), 0L, value.ordinal());
	}

	/**
	 * Total amount of bytes required to store texture.
	 * @return the field value
	 */
	public int storageSize() {
		return (int) VH_STORAGESIZE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code storageSize} field.
	 * @param value the new field value
	 */
	public void storageSize(int value) {
		VH_STORAGESIZE.set(segment(), 0L, value);
	}

	/**
	 * Texture width.
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
	 * Texture height.
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
	 * Texture depth.
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
	 * Number of layers in texture array.
	 * @return the field value
	 */
	public short numLayers() {
		return (short) VH_NUMLAYERS.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code numLayers} field.
	 * @param value the new field value
	 */
	public void numLayers(short value) {
		VH_NUMLAYERS.set(segment(), 0L, value);
	}

	/**
	 * Number of MIP maps.
	 * @return the field value
	 */
	public byte numMips() {
		return (byte) VH_NUMMIPS.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code numMips} field.
	 * @param value the new field value
	 */
	public void numMips(byte value) {
		VH_NUMMIPS.set(segment(), 0L, value);
	}

	/**
	 * Format bits per pixel.
	 * @return the field value
	 */
	public byte bitsPerPixel() {
		return (byte) VH_BITSPERPIXEL.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code bitsPerPixel} field.
	 * @param value the new field value
	 */
	public void bitsPerPixel(byte value) {
		VH_BITSPERPIXEL.set(segment(), 0L, value);
	}

	/**
	 * Texture is cubemap.
	 * @return the field value
	 */
	public boolean cubeMap() {
		return (boolean) VH_CUBEMAP.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code cubeMap} field.
	 * @param value the new field value
	 */
	public void cubeMap(boolean value) {
		VH_CUBEMAP.set(segment(), 0L, value);
	}
}
