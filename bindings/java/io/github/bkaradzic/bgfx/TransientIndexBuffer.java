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
 * Transient index buffer.
 */
@NullMarked
public final class TransientIndexBuffer extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_transient_index_buffer_t",
		ValueLayout.ADDRESS.withName("data"),
		ValueLayout.JAVA_INT.withName("size"),
		ValueLayout.JAVA_INT.withName("startIndex"),
		IndexBufferHandle.LAYOUT.withName("handle"),
		ValueLayout.JAVA_BOOLEAN.withName("isIndex16"));
	private static final VarHandle VH_DATA = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("data"));
	private static final VarHandle VH_SIZE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("size"));
	private static final VarHandle VH_STARTINDEX = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("startIndex"));
	private static final MethodHandle MH_HANDLE = LAYOUT.sliceHandle(
		MemoryLayout.PathElement.groupElement("handle"));
	private static final VarHandle VH_ISINDEX16 = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("isIndex16"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public TransientIndexBuffer(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public TransientIndexBuffer(SegmentAllocator allocator) {
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

	/**
	 * First index.
	 * @return the field value
	 */
	public int startIndex() {
		return (int) VH_STARTINDEX.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code startIndex} field.
	 * @param value the new field value
	 */
	public void startIndex(int value) {
		VH_STARTINDEX.set(segment(), 0L, value);
	}

	/**
	 * Index buffer handle.
	 * @return the field value
	 */
	public IndexBufferHandle handle() {
		return IndexBufferHandle.read(slice(MH_HANDLE, segment()));
	}

	/**
	 * Sets the native {@code handle} field.
	 * @param value the new field value
	 */
	public void handle(IndexBufferHandle value) {
		value.write(slice(MH_HANDLE, segment()));
	}

	/**
	 * Index buffer format is 16-bits if true, otherwise it is 32-bit.
	 * @return the field value
	 */
	public boolean isIndex16() {
		return (boolean) VH_ISINDEX16.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code isIndex16} field.
	 * @param value the new field value
	 */
	public void isIndex16(boolean value) {
		VH_ISINDEX16.set(segment(), 0L, value);
	}
}
