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
 * Tagged buffer handle. All buffer handle types implicitly convert to it, and the tag
 * keeps track of which type the handle originally was.
 * @param idx native handle index
 * @param type native buffer handle tag
 */
public record BufferHandle(short idx, short type) {
	/**
	 * Native by-value handle layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_buffer_handle_t",
		ValueLayout.JAVA_SHORT.withName("idx"),
		ValueLayout.JAVA_SHORT.withName("type"));
	private static final VarHandle VH_IDX = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("idx"));
	private static final VarHandle VH_TYPE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("type"));
	/**
	 * Invalid handle sentinel.
	 */
	public static final BufferHandle INVALID =
		new BufferHandle((short) 0xffff, (short) 0xffff);

	/**
	 * Creates a tagged buffer handle.
	 * @param handle the source DynamicIndexBufferHandle
	 */
	public BufferHandle(DynamicIndexBufferHandle handle) {
		this(handle.idx(), (short) 0);
	}

	/**
	 * Creates a tagged buffer handle.
	 * @param handle the source DynamicVertexBufferHandle
	 */
	public BufferHandle(DynamicVertexBufferHandle handle) {
		this(handle.idx(), (short) 1);
	}

	/**
	 * Creates a tagged buffer handle.
	 * @param handle the source IndexBufferHandle
	 */
	public BufferHandle(IndexBufferHandle handle) {
		this(handle.idx(), (short) 2);
	}

	/**
	 * Creates a tagged buffer handle.
	 * @param handle the source IndirectBufferHandle
	 */
	public BufferHandle(IndirectBufferHandle handle) {
		this(handle.idx(), (short) 3);
	}

	/**
	 * Creates a tagged buffer handle.
	 * @param handle the source VertexBufferHandle
	 */
	public BufferHandle(VertexBufferHandle handle) {
		this(handle.idx(), (short) 4);
	}

	/**
	 * Returns whether this handle is valid.
	 * @return {@code true} when the handle index is not {@code UINT16_MAX}
	 */
	public boolean isValid() {
		return idx != (short) 0xffff;
	}

	/**
	 * Allocates and writes the native by-value handle representation.
	 * @param allocator the destination allocator
	 * @return the allocated native segment
	 */
	public MemorySegment allocate(SegmentAllocator allocator) {
		MemorySegment segment = allocator.allocate(LAYOUT);
		write(segment);
		return segment;
	}

	/**
	 * Writes this handle to an existing native segment.
	 * @param segment the destination segment
	 */
	public void write(MemorySegment segment) {
		segment = view(segment, LAYOUT);
		VH_IDX.set(segment, 0L, idx);
		VH_TYPE.set(segment, 0L, type);
	}

	/**
	 * Reads a by-value handle from native memory.
	 * @param segment the source segment
	 * @return the decoded handle
	 */
	public static BufferHandle read(MemorySegment segment) {
		segment = view(segment, LAYOUT);
		return new BufferHandle(
			(short) VH_IDX.get(segment, 0L),
			(short) VH_TYPE.get(segment, 0L));
	}
}
