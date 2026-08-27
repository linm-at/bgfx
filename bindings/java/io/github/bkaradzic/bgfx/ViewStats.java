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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static io.github.bkaradzic.bgfx.BGFX.*;
import static io.github.bkaradzic.bgfx.util.FFMUtil.*;

/**
 * View stats.
 */
@NullMarked
public final class ViewStats extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_view_stats_t",
		MemoryLayout.sequenceLayout(256, ValueLayout.JAVA_BYTE).withName("name"),
		ValueLayout.JAVA_SHORT.withName("view"),
		ValueLayout.JAVA_LONG.withName("cpuTimeBegin"),
		ValueLayout.JAVA_LONG.withName("cpuTimeEnd"),
		ValueLayout.JAVA_LONG.withName("gpuTimeBegin"),
		ValueLayout.JAVA_LONG.withName("gpuTimeEnd"),
		ValueLayout.JAVA_INT.withName("gpuFrameNum"));
	private static final MethodHandle MH_NAME = LAYOUT.sliceHandle(
		MemoryLayout.PathElement.groupElement("name"));
	private static final VarHandle VH_VIEW = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("view"));
	private static final VarHandle VH_CPUTIMEBEGIN = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("cpuTimeBegin"));
	private static final VarHandle VH_CPUTIMEEND = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("cpuTimeEnd"));
	private static final VarHandle VH_GPUTIMEBEGIN = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("gpuTimeBegin"));
	private static final VarHandle VH_GPUTIMEEND = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("gpuTimeEnd"));
	private static final VarHandle VH_GPUFRAMENUM = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("gpuFrameNum"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public ViewStats(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public ViewStats(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * View name.
	 * @return a segment view of the inline array
	 */
	public MemorySegment name() {
		return slice(MH_NAME, segment());
	}

	/**
	 * View id.
	 * @return the field value
	 */
	public short view() {
		return (short) VH_VIEW.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code view} field.
	 * @param value the new field value
	 */
	public void view(short value) {
		VH_VIEW.set(segment(), 0L, value);
	}

	/**
	 * CPU (submit) begin time.
	 * @return the field value
	 */
	public long cpuTimeBegin() {
		return (long) VH_CPUTIMEBEGIN.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code cpuTimeBegin} field.
	 * @param value the new field value
	 */
	public void cpuTimeBegin(long value) {
		VH_CPUTIMEBEGIN.set(segment(), 0L, value);
	}

	/**
	 * CPU (submit) end time.
	 * @return the field value
	 */
	public long cpuTimeEnd() {
		return (long) VH_CPUTIMEEND.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code cpuTimeEnd} field.
	 * @param value the new field value
	 */
	public void cpuTimeEnd(long value) {
		VH_CPUTIMEEND.set(segment(), 0L, value);
	}

	/**
	 * GPU begin time.
	 * @return the field value
	 */
	public long gpuTimeBegin() {
		return (long) VH_GPUTIMEBEGIN.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code gpuTimeBegin} field.
	 * @param value the new field value
	 */
	public void gpuTimeBegin(long value) {
		VH_GPUTIMEBEGIN.set(segment(), 0L, value);
	}

	/**
	 * GPU end time.
	 * @return the field value
	 */
	public long gpuTimeEnd() {
		return (long) VH_GPUTIMEEND.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code gpuTimeEnd} field.
	 * @param value the new field value
	 */
	public void gpuTimeEnd(long value) {
		VH_GPUTIMEEND.set(segment(), 0L, value);
	}

	/**
	 * Frame which generated gpuTimeBegin, gpuTimeEnd.
	 * @return the field value
	 */
	public int gpuFrameNum() {
		return (int) VH_GPUFRAMENUM.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code gpuFrameNum} field.
	 * @param value the new field value
	 */
	public void gpuFrameNum(int value) {
		VH_GPUFRAMENUM.set(segment(), 0L, value);
	}
}
