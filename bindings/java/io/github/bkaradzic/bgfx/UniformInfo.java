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
import java.lang.AutoCloseable;

import io.github.bkaradzic.bgfx.util.NativeObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static io.github.bkaradzic.bgfx.BGFX.*;
import static io.github.bkaradzic.bgfx.util.FFMUtil.*;

/**
 * Uniform info.
 */
@NullMarked
public final class UniformInfo extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_uniform_info_t",
		MemoryLayout.sequenceLayout(256, ValueLayout.JAVA_BYTE).withName("name"),
		ValueLayout.JAVA_INT.withName("type"),
		ValueLayout.JAVA_SHORT.withName("num"));
	private static final MethodHandle MH_NAME = LAYOUT.sliceHandle(
		MemoryLayout.PathElement.groupElement("name"));
	private static final VarHandle VH_TYPE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("type"));
	private static final VarHandle VH_NUM = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("num"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public UniformInfo(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public UniformInfo(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Uniform name.
	 * @return a segment view of the inline array
	 */
	public MemorySegment name() {
		return slice(MH_NAME, segment());
	}

	/**
	 * Uniform type.
	 * @return the field value
	 */
	public UniformType type() {
		return UniformType.fromValue((int) VH_TYPE.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code type} field.
	 * @param value the new field value
	 */
	public void type(UniformType value) {
		VH_TYPE.set(segment(), 0L, value.ordinal());
	}

	/**
	 * Number of elements in array.
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
