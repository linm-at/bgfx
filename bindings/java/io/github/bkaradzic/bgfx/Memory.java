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
 * Memory must be obtained by calling {@code alloc}, {@code copy}, or {@code makeRef}.
 * <p>
 * <strong>Attention:</strong> It is illegal to create this structure on stack and pass it to any bgfx API.
 */
@NullMarked
public final class Memory extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_memory_t",
		ValueLayout.ADDRESS.withName("data"),
		ValueLayout.JAVA_INT.withName("size"));
	private static final VarHandle VH_DATA = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("data"));
	private static final VarHandle VH_SIZE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("size"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public Memory(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public Memory(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Pointer to data.
	 * @return the field value
	 */
	public MemorySegment data() {
		return address((MemorySegment) VH_DATA.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code data} field.
	 * @param value the new field value
	 */
	public void data(MemorySegment value) {
		VH_DATA.set(segment(), 0L, address(value));
	}

	/**
	 * Data size.
	 * @return the field value
	 */
	public int size() {
		return (int) VH_SIZE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code size} field.
	 * @param value the new field value
	 */
	public void size(int value) {
		VH_SIZE.set(segment(), 0L, value);
	}
}
