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
 * Transform data.
 */
public final class Transform extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_transform_t",
		ValueLayout.ADDRESS.withName("data"),
		ValueLayout.JAVA_SHORT.withName("num"));
	private static final VarHandle VH_DATA = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("data"));
	private static final VarHandle VH_NUM = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("num"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public Transform(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public Transform(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Pointer to first 4x4 matrix.
	 * @return the field value
	 */
	public MemorySegment data() {
		return (MemorySegment) VH_DATA.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code data} field.
	 * @param value the new field value
	 */
	public void data(MemorySegment value) {
		VH_DATA.set(segment(), 0L, address(value));
	}

	/**
	 * Number of matrices.
	 * @return the field value
	 */
	public short num() {
		return (short) VH_NUM.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code num} field.
	 * @param value the new field value
	 */
	public void num(short value) {
		VH_NUM.set(segment(), 0L, value);
	}
}
