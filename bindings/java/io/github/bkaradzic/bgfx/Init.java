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
 * Initialization parameters used by {@code init}.
 */
public final class Init extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_init_t",
		ValueLayout.JAVA_INT.withName("type"),
		ValueLayout.JAVA_SHORT.withName("vendorId"),
		ValueLayout.JAVA_SHORT.withName("deviceId"),
		ValueLayout.JAVA_LONG.withName("capabilities"),
		ValueLayout.JAVA_BOOLEAN.withName("debug"),
		ValueLayout.JAVA_BOOLEAN.withName("profile"),
		ValueLayout.JAVA_BOOLEAN.withName("fallback"),
		ValueLayout.JAVA_BOOLEAN.withName("videoDecode"),
		PlatformData.LAYOUT.withName("platformData"),
		Resolution.LAYOUT.withName("resolution"),
		io.github.bkaradzic.bgfx.init.Limits.LAYOUT.withName("limits"),
		ValueLayout.ADDRESS.withName("callback"),
		ValueLayout.ADDRESS.withName("allocator"));
	private static final VarHandle VH_TYPE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("type"));
	private static final VarHandle VH_VENDORID = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("vendorId"));
	private static final VarHandle VH_DEVICEID = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("deviceId"));
	private static final VarHandle VH_CAPABILITIES = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("capabilities"));
	private static final VarHandle VH_DEBUG = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("debug"));
	private static final VarHandle VH_PROFILE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("profile"));
	private static final VarHandle VH_FALLBACK = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("fallback"));
	private static final VarHandle VH_VIDEODECODE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("videoDecode"));
	private static final MethodHandle MH_PLATFORMDATA = LAYOUT.sliceHandle(
		MemoryLayout.PathElement.groupElement("platformData"));
	private static final MethodHandle MH_RESOLUTION = LAYOUT.sliceHandle(
		MemoryLayout.PathElement.groupElement("resolution"));
	private static final MethodHandle MH_LIMITS = LAYOUT.sliceHandle(
		MemoryLayout.PathElement.groupElement("limits"));
	private static final VarHandle VH_CALLBACK = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("callback"));
	private static final VarHandle VH_ALLOCATOR = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("allocator"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public Init(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public Init(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Select rendering backend. When set to RendererType.COUNT
	 * a default rendering backend will be selected appropriate to the platform.
	 * See: {@code RendererType}
	 * @return the field value
	 */
	public RendererType type() {
		return RendererType.fromValue((int) VH_TYPE.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code type} field.
	 * @param value the new field value
	 */
	public void type(RendererType value) {
		VH_TYPE.set(segment(), 0L, value.ordinal());
	}

	/**
	 * Vendor PCI ID. If set to {@code BGFX_PCI_ID_NONE}, discrete and integrated
	 * GPUs will be prioritised.
	 *   - {@code BGFX_PCI_ID_NONE} - Autoselect adapter.
	 *   - {@code BGFX_PCI_ID_SOFTWARE_RASTERIZER} - Software rasterizer.
	 *   - {@code BGFX_PCI_ID_AMD} - AMD adapter.
	 *   - {@code BGFX_PCI_ID_APPLE} - Apple adapter.
	 *   - {@code BGFX_PCI_ID_INTEL} - Intel adapter.
	 *   - {@code BGFX_PCI_ID_NVIDIA} - NVIDIA adapter.
	 *   - {@code BGFX_PCI_ID_MICROSOFT} - Microsoft adapter.
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
	 * Device ID. If set to 0 it will select first device, or device with
	 * matching ID.
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

	/**
	 * Capabilities initialization mask (default: UINT64_MAX).
	 * @return the field value
	 */
	public long capabilities() {
		return (long) VH_CAPABILITIES.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code capabilities} field.
	 * @param value the new field value
	 */
	public void capabilities(long value) {
		VH_CAPABILITIES.set(segment(), 0L, value);
	}

	/**
	 * Enable device for debugging.
	 * @return the field value
	 */
	public boolean debug() {
		return (boolean) VH_DEBUG.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code debug} field.
	 * @param value the new field value
	 */
	public void debug(boolean value) {
		VH_DEBUG.set(segment(), 0L, value);
	}

	/**
	 * Enable device for profiling.
	 * @return the field value
	 */
	public boolean profile() {
		return (boolean) VH_PROFILE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code profile} field.
	 * @param value the new field value
	 */
	public void profile(boolean value) {
		VH_PROFILE.set(segment(), 0L, value);
	}

	/**
	 * Enable fallback to next available renderer.
	 * @return the field value
	 */
	public boolean fallback() {
		return (boolean) VH_FALLBACK.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code fallback} field.
	 * @param value the new field value
	 */
	public void fallback(boolean value) {
		VH_FALLBACK.set(segment(), 0L, value);
	}

	/**
	 * Enable video decoding.
	 * @return the field value
	 */
	public boolean videoDecode() {
		return (boolean) VH_VIDEODECODE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code videoDecode} field.
	 * @param value the new field value
	 */
	public void videoDecode(boolean value) {
		VH_VIDEODECODE.set(segment(), 0L, value);
	}

	/**
	 * Platform data.
	 * @return the field value
	 */
	public PlatformData platformData() {
		return new PlatformData(slice(MH_PLATFORMDATA, segment()));
	}

	/**
	 * Sets the native {@code platformData} field.
	 * @param value the new field value
	 */
	public void platformData(PlatformData value) {
		slice(MH_PLATFORMDATA, segment()).copyFrom(value.segment());
	}

	/**
	 * Backbuffer resolution and reset parameters. See: {@code Resolution}.
	 * @return the field value
	 */
	public Resolution resolution() {
		return new Resolution(slice(MH_RESOLUTION, segment()));
	}

	/**
	 * Sets the native {@code resolution} field.
	 * @param value the new field value
	 */
	public void resolution(Resolution value) {
		slice(MH_RESOLUTION, segment()).copyFrom(value.segment());
	}

	/**
	 * Configurable runtime limits parameters.
	 * @return the field value
	 */
	public io.github.bkaradzic.bgfx.init.Limits limits() {
		return new io.github.bkaradzic.bgfx.init.Limits(slice(MH_LIMITS, segment()));
	}

	/**
	 * Sets the native {@code limits} field.
	 * @param value the new field value
	 */
	public void limits(io.github.bkaradzic.bgfx.init.Limits value) {
		slice(MH_LIMITS, segment()).copyFrom(value.segment());
	}

	/**
	 * Provide application specific callback interface.
	 * See: {@code CallbackI}
	 * @return the field value
	 */
	public MemorySegment callback() {
		return (MemorySegment) VH_CALLBACK.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code callback} field.
	 * @param value the new field value
	 */
	public void callback(MemorySegment value) {
		VH_CALLBACK.set(segment(), 0L, address(value));
	}

	/**
	 * Custom allocator. When a custom allocator is not
	 * specified, bgfx uses the CRT allocator. Bgfx assumes
	 * custom allocator is thread safe.
	 * @return the field value
	 */
	public MemorySegment allocator() {
		return (MemorySegment) VH_ALLOCATOR.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code allocator} field.
	 * @param value the new field value
	 */
	public void allocator(MemorySegment value) {
		VH_ALLOCATOR.set(segment(), 0L, address(value));
	}
}
