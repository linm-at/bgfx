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
 * Vertex attribute type enum.
 */
public enum AttribType {
	/**
	 * Int8
	 */
	Int8,
	/**
	 * Uint8
	 */
	Uint8,
	/**
	 * Uint10, availability depends on: {@code BGFX_CAPS_VERTEX_ATTRIB_UINT10}.
	 */
	Uint10,
	/**
	 * Int16
	 */
	Int16,
	/**
	 * Uint16
	 */
	Uint16,
	/**
	 * Half, availability depends on: {@code BGFX_CAPS_VERTEX_ATTRIB_HALF}.
	 */
	Half,
	/**
	 * Float
	 */
	Float,
	/**
	 * Int32
	 */
	Int32,
	/**
	 * Uint32
	 */
	Uint32,

	/**
	 * Number of native enum values.
	 */
	Count;

	/**
	 * Native C enum layout.
	 */
	public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
	private static final AttribType[] VALUES = values();

	/**
	 * Returns the enum constant for a native C enum value.
	 * @param value the native enum value
	 * @return the matching enum constant
	 */
	public static AttribType fromValue(int value) {
		if (value >= 0 && value < VALUES.length) {
			return VALUES[value];
		}
		throw new IllegalArgumentException("Unknown AttribType value: " + value);
	}
}
