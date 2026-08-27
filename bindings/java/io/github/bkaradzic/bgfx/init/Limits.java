// Copyright 2011-2026 Branimir Karadzic. All rights reserved.
// License: https://github.com/bkaradzic/bgfx/blob/master/LICENSE


//
// AUTO GENERATED! DO NOT EDIT!
//

package io.github.bkaradzic.bgfx.init;

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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static io.github.bkaradzic.bgfx.BGFX.*;
import static io.github.bkaradzic.bgfx.util.FFMUtil.*;
import io.github.bkaradzic.bgfx.*;

/**
 * Configurable runtime limits parameters.
 */
@NullMarked
public final class Limits extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_init_limits_t",
		ValueLayout.JAVA_SHORT.withName("maxEncoders"),
		ValueLayout.JAVA_INT.withName("numDrawCalls"),
		ValueLayout.JAVA_INT.withName("numDrawCallPeakFrames"),
		ValueLayout.JAVA_INT.withName("minResourceCbSize"),
		ValueLayout.JAVA_INT.withName("maxTransientVbSize"),
		ValueLayout.JAVA_INT.withName("maxTransientIbSize"),
		ValueLayout.JAVA_INT.withName("minUniformBufferSize"));
	private static final VarHandle VH_MAXENCODERS = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("maxEncoders"));
	private static final VarHandle VH_NUMDRAWCALLS = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("numDrawCalls"));
	private static final VarHandle VH_NUMDRAWCALLPEAKFRAMES = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("numDrawCallPeakFrames"));
	private static final VarHandle VH_MINRESOURCECBSIZE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("minResourceCbSize"));
	private static final VarHandle VH_MAXTRANSIENTVBSIZE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("maxTransientVbSize"));
	private static final VarHandle VH_MAXTRANSIENTIBSIZE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("maxTransientIbSize"));
	private static final VarHandle VH_MINUNIFORMBUFFERSIZE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("minUniformBufferSize"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public Limits(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public Limits(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Maximum number of encoder threads.
	 * @return the field value
	 */
	public short maxEncoders() {
		return (short) VH_MAXENCODERS.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code maxEncoders} field.
	 * @param value the new field value
	 */
	public void maxEncoders(short value) {
		VH_MAXENCODERS.set(segment(), 0L, value);
	}

	/**
	 * Number of draw calls per frame to reserve storage for. Rounded
	 * up to a multiple of {@code BGFX_CONFIG_DRAW_CALL_BLOCK}, which is also
	 * the minimum. This is a reservation, not a limit: submitting more
	 * than this grows the storage during the frame, up to
	 * {@code BGFX_CONFIG_MAX_DRAW_CALLS}. With
	 * {@code BGFX_CONFIG_DYNAMIC_FRAME_STORAGE} disabled nothing grows, and
	 * this is a hard limit that {@code Caps.Limits.maxDrawCalls} reports
	 * back; submissions past it are dropped. See
	 * {@code Stats.numDrawCallsPeak} to size it.
	 * @return the field value
	 */
	public int numDrawCalls() {
		return (int) VH_NUMDRAWCALLS.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code numDrawCalls} field.
	 * @param value the new field value
	 */
	public void numDrawCalls(int value) {
		VH_NUMDRAWCALLS.set(segment(), 0L, value);
	}

	/**
	 * Number of frames the draw-call peak (high-water mark) is observed
	 * before unused storage is released. Set to 0 to keep whatever has
	 * been allocated for the lifetime of the context. With
	 * {@code BGFX_CONFIG_DYNAMIC_FRAME_STORAGE} disabled nothing per frame is
	 * resized at all, and this only releases unused uniform buffer space.
	 * @return the field value
	 */
	public int numDrawCallPeakFrames() {
		return (int) VH_NUMDRAWCALLPEAKFRAMES.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code numDrawCallPeakFrames} field.
	 * @param value the new field value
	 */
	public void numDrawCallPeakFrames(int value) {
		VH_NUMDRAWCALLPEAKFRAMES.set(segment(), 0L, value);
	}

	/**
	 * Minimum resource command buffer size.
	 * @return the field value
	 */
	public int minResourceCbSize() {
		return (int) VH_MINRESOURCECBSIZE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code minResourceCbSize} field.
	 * @param value the new field value
	 */
	public void minResourceCbSize(int value) {
		VH_MINRESOURCECBSIZE.set(segment(), 0L, value);
	}

	/**
	 * Maximum transient vertex buffer size.
	 * @return the field value
	 */
	public int maxTransientVbSize() {
		return (int) VH_MAXTRANSIENTVBSIZE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code maxTransientVbSize} field.
	 * @param value the new field value
	 */
	public void maxTransientVbSize(int value) {
		VH_MAXTRANSIENTVBSIZE.set(segment(), 0L, value);
	}

	/**
	 * Maximum transient index buffer size.
	 * @return the field value
	 */
	public int maxTransientIbSize() {
		return (int) VH_MAXTRANSIENTIBSIZE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code maxTransientIbSize} field.
	 * @param value the new field value
	 */
	public void maxTransientIbSize(int value) {
		VH_MAXTRANSIENTIBSIZE.set(segment(), 0L, value);
	}

	/**
	 * Mimimum uniform buffer size.
	 * @return the field value
	 */
	public int minUniformBufferSize() {
		return (int) VH_MINUNIFORMBUFFERSIZE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code minUniformBufferSize} field.
	 * @param value the new field value
	 */
	public void minUniformBufferSize(int value) {
		VH_MINUNIFORMBUFFERSIZE.set(segment(), 0L, value);
	}
}
