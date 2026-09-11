// Copyright 2011-2026 Branimir Karadzic. All rights reserved.
// License: https://github.com/bkaradzic/bgfx/blob/master/LICENSE


//
// AUTO GENERATED! DO NOT EDIT!
//

package io.github.bkaradzic.bgfx;

import java.lang.AutoCloseable;
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
import java.nio.file.Path;
import java.util.Objects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import io.github.bkaradzic.bgfx.*;
import io.github.bkaradzic.bgfx.util.FFMUtil;
import io.github.bkaradzic.bgfx.util.NativeObject;
import io.github.bkaradzic.bgfx.util.Unsigned;
import static io.github.bkaradzic.bgfx.Bgfx.*;
import static io.github.bkaradzic.bgfx.util.FFMUtil.*;

/**
 * Platform data.
 */
@NullMarked
public final class PlatformData extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_platform_data_t",
		ValueLayout.ADDRESS.withName("ndt"),
		ValueLayout.ADDRESS.withName("nwh"),
		ValueLayout.ADDRESS.withName("context"),
		ValueLayout.ADDRESS.withName("queue"),
		ValueLayout.ADDRESS.withName("backBuffer"),
		ValueLayout.ADDRESS.withName("backBufferDS"),
		ValueLayout.JAVA_INT.withName("type"));
	private static final VarHandle VH_NDT = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("ndt"));
	private static final VarHandle VH_NWH = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("nwh"));
	private static final VarHandle VH_CONTEXT = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("context"));
	private static final VarHandle VH_QUEUE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("queue"));
	private static final VarHandle VH_BACKBUFFER = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("backBuffer"));
	private static final VarHandle VH_BACKBUFFERDS = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("backBufferDS"));
	private static final VarHandle VH_TYPE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("type"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public PlatformData(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public PlatformData(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Native display type (*nix specific).
	 * @return the field value
	 */
	public MemorySegment ndt() {
		return address((MemorySegment) VH_NDT.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code ndt} field and returns {@code this}.
	 * @param value the new field value
	 */
	public PlatformData ndt(MemorySegment value) {
		VH_NDT.set(segment(), 0L, address(value));
		return this;
	}

	/**
	 * Native window handle. If {@code NULL}, bgfx will create a headless
	 * context/device, provided the rendering API supports it.
	 * @return the field value
	 */
	public MemorySegment nwh() {
		return address((MemorySegment) VH_NWH.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code nwh} field and returns {@code this}.
	 * @param value the new field value
	 */
	public PlatformData nwh(MemorySegment value) {
		VH_NWH.set(segment(), 0L, address(value));
		return this;
	}

	/**
	 * GL context, D3D device, or Vulkan device. If {@code NULL}, bgfx
	 * will create context/device.
	 * @return the field value
	 */
	public MemorySegment context() {
		return address((MemorySegment) VH_CONTEXT.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code context} field and returns {@code this}.
	 * @param value the new field value
	 */
	public PlatformData context(MemorySegment value) {
		VH_CONTEXT.set(segment(), 0L, address(value));
		return this;
	}

	/**
	 * D3D12 Queue. If {@code NULL} bgfx will create queue.
	 * @return the field value
	 */
	public MemorySegment queue() {
		return address((MemorySegment) VH_QUEUE.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code queue} field and returns {@code this}.
	 * @param value the new field value
	 */
	public PlatformData queue(MemorySegment value) {
		VH_QUEUE.set(segment(), 0L, address(value));
		return this;
	}

	/**
	 * GL back-buffer, or D3D render target view. If {@code NULL} bgfx will
	 * create back-buffer color surface.
	 * @return the field value
	 */
	public MemorySegment backBuffer() {
		return address((MemorySegment) VH_BACKBUFFER.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code backBuffer} field and returns {@code this}.
	 * @param value the new field value
	 */
	public PlatformData backBuffer(MemorySegment value) {
		VH_BACKBUFFER.set(segment(), 0L, address(value));
		return this;
	}

	/**
	 * Backbuffer depth/stencil. If {@code NULL}, bgfx will create a back-buffer
	 * depth/stencil surface.
	 * @return the field value
	 */
	public MemorySegment backBufferDS() {
		return address((MemorySegment) VH_BACKBUFFERDS.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code backBufferDS} field and returns {@code this}.
	 * @param value the new field value
	 */
	public PlatformData backBufferDS(MemorySegment value) {
		VH_BACKBUFFERDS.set(segment(), 0L, address(value));
		return this;
	}

	/**
	 * Handle type. Needed for platforms having more than one option.
	 * @return the field value
	 */
	public NativeWindowHandleType type() {
		return NativeWindowHandleType.fromValue((int) VH_TYPE.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code type} field and returns {@code this}.
	 * @param value the new field value
	 */
	public PlatformData type(NativeWindowHandleType value) {
		VH_TYPE.set(segment(), 0L, value.ordinal());
		return this;
	}
}
