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
 * Backbuffer resolution and reset parameters.
 */
public final class Resolution extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_resolution_t",
		ValueLayout.JAVA_INT.withName("formatColor"),
		ValueLayout.JAVA_INT.withName("formatDepthStencil"),
		ValueLayout.JAVA_INT.withName("width"),
		ValueLayout.JAVA_INT.withName("height"),
		ValueLayout.JAVA_INT.withName("reset"),
		ValueLayout.JAVA_BYTE.withName("numBackBuffers"),
		ValueLayout.JAVA_BYTE.withName("maxFrameLatency"),
		ValueLayout.JAVA_BYTE.withName("debugTextScale"));
	private static final VarHandle VH_FORMATCOLOR = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("formatColor"));
	private static final VarHandle VH_FORMATDEPTHSTENCIL = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("formatDepthStencil"));
	private static final VarHandle VH_WIDTH = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("width"));
	private static final VarHandle VH_HEIGHT = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("height"));
	private static final VarHandle VH_RESET = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("reset"));
	private static final VarHandle VH_NUMBACKBUFFERS = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("numBackBuffers"));
	private static final VarHandle VH_MAXFRAMELATENCY = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("maxFrameLatency"));
	private static final VarHandle VH_DEBUGTEXTSCALE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("debugTextScale"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public Resolution(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public Resolution(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Backbuffer color format.
	 * @return the field value
	 */
	public TextureFormat formatColor() {
		return TextureFormat.fromValue((int) VH_FORMATCOLOR.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code formatColor} field.
	 * @param value the new field value
	 */
	public void formatColor(TextureFormat value) {
		VH_FORMATCOLOR.set(segment(), 0L, value.ordinal());
	}

	/**
	 * Backbuffer depth/stencil format.
	 * @return the field value
	 */
	public TextureFormat formatDepthStencil() {
		return TextureFormat.fromValue((int) VH_FORMATDEPTHSTENCIL.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code formatDepthStencil} field.
	 * @param value the new field value
	 */
	public void formatDepthStencil(TextureFormat value) {
		VH_FORMATDEPTHSTENCIL.set(segment(), 0L, value.ordinal());
	}

	/**
	 * Backbuffer width.
	 * @return the field value
	 */
	public int width() {
		return (int) VH_WIDTH.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code width} field.
	 * @param value the new field value
	 */
	public void width(int value) {
		VH_WIDTH.set(segment(), 0L, value);
	}

	/**
	 * Backbuffer height.
	 * @return the field value
	 */
	public int height() {
		return (int) VH_HEIGHT.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code height} field.
	 * @param value the new field value
	 */
	public void height(int value) {
		VH_HEIGHT.set(segment(), 0L, value);
	}

	/**
	 * Reset parameters.
	 * @return the field value
	 */
	public int reset() {
		return (int) VH_RESET.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code reset} field.
	 * @param value the new field value
	 */
	public void reset(int value) {
		VH_RESET.set(segment(), 0L, value);
	}

	/**
	 * Number of back buffers.
	 * @return the field value
	 */
	public byte numBackBuffers() {
		return (byte) VH_NUMBACKBUFFERS.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code numBackBuffers} field.
	 * @param value the new field value
	 */
	public void numBackBuffers(byte value) {
		VH_NUMBACKBUFFERS.set(segment(), 0L, value);
	}

	/**
	 * Maximum frame latency.
	 * @return the field value
	 */
	public byte maxFrameLatency() {
		return (byte) VH_MAXFRAMELATENCY.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code maxFrameLatency} field.
	 * @param value the new field value
	 */
	public void maxFrameLatency(byte value) {
		VH_MAXFRAMELATENCY.set(segment(), 0L, value);
	}

	/**
	 * Scale factor for debug text.
	 * @return the field value
	 */
	public byte debugTextScale() {
		return (byte) VH_DEBUGTEXTSCALE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code debugTextScale} field.
	 * @param value the new field value
	 */
	public void debugTextScale(byte value) {
		VH_DEBUGTEXTSCALE.set(segment(), 0L, value);
	}
}
