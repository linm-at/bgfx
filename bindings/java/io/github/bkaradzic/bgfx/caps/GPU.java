// Copyright 2011-2026 Branimir Karadzic. All rights reserved.
// License: https://github.com/bkaradzic/bgfx/blob/master/LICENSE


//
// AUTO GENERATED! DO NOT EDIT!
//

package io.github.bkaradzic.bgfx.caps;

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
import io.github.bkaradzic.bgfx.*;

/**
 * GPU info.
 */
public final class GPU extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_caps_gpu_t",
		ValueLayout.JAVA_SHORT.withName("vendorId"),
		ValueLayout.JAVA_SHORT.withName("deviceId"));
	private static final VarHandle VH_VENDORID = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("vendorId"));
	private static final VarHandle VH_DEVICEID = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("deviceId"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public GPU(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public GPU(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Vendor PCI id. See {@code BGFX_PCI_ID_*}.
	 * @return the field value
	 */
	public short vendorId() {
		return (short) VH_VENDORID.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code vendorId} field.
	 * @param value the new field value
	 */
	public void vendorId(short value) {
		VH_VENDORID.set(segment(), 0L, value);
	}

	/**
	 * Device id.
	 * @return the field value
	 */
	public short deviceId() {
		return (short) VH_DEVICEID.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code deviceId} field.
	 * @param value the new field value
	 */
	public void deviceId(short value) {
		VH_DEVICEID.set(segment(), 0L, value);
	}
}
