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
 * Frame buffer texture attachment info.
 */
@NullMarked
public final class Attachment extends NativeObject {
	/**
	 * Native C structure layout.
	 */
	public static final StructLayout LAYOUT = cStruct("bgfx_attachment_t",
		ValueLayout.JAVA_INT.withName("access"),
		TextureHandle.LAYOUT.withName("handle"),
		ValueLayout.JAVA_SHORT.withName("mip"),
		ValueLayout.JAVA_SHORT.withName("layer"),
		ValueLayout.JAVA_SHORT.withName("numLayers"),
		ValueLayout.JAVA_BYTE.withName("resolve"));
	private static final VarHandle VH_ACCESS = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("access"));
	private static final MethodHandle MH_HANDLE = LAYOUT.sliceHandle(
		MemoryLayout.PathElement.groupElement("handle"));
	private static final VarHandle VH_MIP = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("mip"));
	private static final VarHandle VH_LAYER = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("layer"));
	private static final VarHandle VH_NUMLAYERS = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("numLayers"));
	private static final VarHandle VH_RESOLVE = LAYOUT.varHandle(
		MemoryLayout.PathElement.groupElement("resolve"));
	/**
	 * Wraps an existing native structure.
	 * @param segment native memory segment
	 */
	public Attachment(MemorySegment segment) {
		super(segment, LAYOUT);
	}

	/**
	 * Allocates a native structure.
	 * @param allocator destination allocator
	 */
	public Attachment(SegmentAllocator allocator) {
		super(allocator, LAYOUT);
	}

	/**
	 * Attachment access. See {@code Access}.
	 * @return the field value
	 */
	public Access access() {
		return Access.fromValue((int) VH_ACCESS.get(segment(), 0L));
	}

	/**
	 * Sets the native {@code access} field.
	 * @param value the new field value
	 */
	public void access(Access value) {
		VH_ACCESS.set(segment(), 0L, value.ordinal());
	}

	/**
	 * Render target texture handle.
	 * @return the field value
	 */
	public TextureHandle handle() {
		return TextureHandle.read(slice(MH_HANDLE, segment()));
	}

	/**
	 * Sets the native {@code handle} field.
	 * @param value the new field value
	 */
	public void handle(TextureHandle value) {
		value.write(slice(MH_HANDLE, segment()));
	}

	/**
	 * Mip level.
	 * @return the field value
	 */
	public short mip() {
		return (short) VH_MIP.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code mip} field.
	 * @param value the new field value
	 */
	public void mip(short value) {
		VH_MIP.set(segment(), 0L, value);
	}

	/**
	 * Cubemap side or depth layer/slice to use.
	 * @return the field value
	 */
	public short layer() {
		return (short) VH_LAYER.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code layer} field.
	 * @param value the new field value
	 */
	public void layer(short value) {
		VH_LAYER.set(segment(), 0L, value);
	}

	/**
	 * Number of texture layer/slice(s) in array to use.
	 * @return the field value
	 */
	public short numLayers() {
		return (short) VH_NUMLAYERS.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code numLayers} field.
	 * @param value the new field value
	 */
	public void numLayers(short value) {
		VH_NUMLAYERS.set(segment(), 0L, value);
	}

	/**
	 * Resolve flags. See: {@code BGFX_RESOLVE_*}
	 * @return the field value
	 */
	public byte resolve() {
		return (byte) VH_RESOLVE.get(segment(), 0L);
	}

	/**
	 * Sets the native {@code resolve} field.
	 * @param value the new field value
	 */
	public void resolve(byte value) {
		VH_RESOLVE.set(segment(), 0L, value);
	}

	/**
	 * Init attachment.
	 * @param _handle Render target texture handle.
	 * @param _access Access. See {@code Access}.
	 * @param _layer Cubemap side or depth layer/slice to use.
	 * @param _numLayers Number of texture layer/slice(s) in array to use.
	 * @param _mip Mip level.
	 * @param _resolve Resolve flags. See: {@code BGFX_RESOLVE_*}
	 */
	public final void init(TextureHandle _handle, Access _access, short _layer, short _numLayers, short _mip, byte _resolve) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				MH_ATTACHMENT_INIT.invokeExact(segment(), _handle.allocate(arena), _access.ordinal(), _layer, _numLayers, _mip, _resolve);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}
}
