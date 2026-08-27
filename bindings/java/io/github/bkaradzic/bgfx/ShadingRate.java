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
 * Shading Rate.
 */
public enum ShadingRate {
	/**
	 * 1x1
	 */
	Rate1x1,
	/**
	 * 1x2
	 */
	Rate1x2,
	/**
	 * 2x1
	 */
	Rate2x1,
	/**
	 * 2x2
	 */
	Rate2x2,
	/**
	 * 2x4
	 */
	Rate2x4,
	/**
	 * 4x2
	 */
	Rate4x2,
	/**
	 * 4x4
	 */
	Rate4x4,

	/**
	 * Number of native enum values.
	 */
	Count;

	/**
	 * Native C enum layout.
	 */
	public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
	private static final ShadingRate[] VALUES = values();

	/**
	 * Returns the enum constant for a native C enum value.
	 * @param value the native enum value
	 * @return the matching enum constant
	 */
	public static ShadingRate fromValue(int value) {
		if (value >= 0 && value < VALUES.length) {
			return VALUES[value];
		}
		throw new IllegalArgumentException("Unknown ShadingRate value: " + value);
	}
}
