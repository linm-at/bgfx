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
 * Instance data buffer info.
 */
public final class InstanceDataBuffer extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_instance_data_buffer_t",
		ValueLayout.ADDRESS.withName("data"),
		ValueLayout.JAVA_INT.withName("size"),
		ValueLayout.JAVA_INT.withName("offset"),
		ValueLayout.JAVA_INT.withName("num"),
		ValueLayout.JAVA_SHORT.withName("stride"),
		VertexBufferHandle.LAYOUT.withName("handle"));
	private static final VarHandle VH_DATA = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("data"));
	private static final VarHandle VH_SIZE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("size"));
	private static final VarHandle VH_OFFSET = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("offset"));
	private static final VarHandle VH_NUM = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("num"));
	private static final VarHandle VH_STRIDE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("stride"));
	private static final MethodHandle MH_HANDLE = LAYOUT.sliceHandle(
		MemoryLayout.PathElement.groupElement("handle"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public InstanceDataBuffer(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public InstanceDataBuffer(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Pointer to data.
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
	 * Offset in vertex buffer.
	 * @return the field value
	 */
	public int offset() {
		return (int) VH_OFFSET.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code offset} field.
	 * @param value the new field value
	 */
	public void offset(int value) {
		VH_OFFSET.set(segment(), 0L, value);
	}

	/**
	 * Number of instances.
	 * @return the field value
	 */
	public int num() {
		return (int) VH_NUM.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code num} field.
	 * @param value the new field value
	 */
	public void num(int value) {
		VH_NUM.set(segment(), 0L, value);
	}

	/**
	 * Vertex buffer stride.
	 * @return the field value
	 */
	public short stride() {
		return (short) VH_STRIDE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code stride} field.
	 * @param value the new field value
	 */
	public void stride(short value) {
		VH_STRIDE.set(segment(), 0L, value);
	}

	/**
	 * Vertex buffer object handle.
	 * @return the field value
	 */
	public VertexBufferHandle handle() {
		return VertexBufferHandle.read(slice(MH_HANDLE, segment()));
	}

	/**
	 * Sets the native {@code handle} field.
	 * @param value the new field value
	 */
	public void handle(VertexBufferHandle value) {
		value.write(slice(MH_HANDLE, segment()));
	}
}
