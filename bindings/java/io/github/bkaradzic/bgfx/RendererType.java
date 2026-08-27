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
 * Renderer backend type enum.
 */
public enum RendererType {
	/**
	 * No rendering.
	 */
	Noop,
	/**
	 * AGC
	 */
	Agc,
	/**
	 * Direct3D 11.0
	 */
	Direct3D11,
	/**
	 * Direct3D 12.0
	 */
	Direct3D12,
	/**
	 * GNM
	 */
	Gnm,
	/**
	 * Metal
	 */
	Metal,
	/**
	 * NVN
	 */
	Nvn,
	/**
	 * OpenGL ES 3.0+
	 */
	OpenGLES,
	/**
	 * OpenGL 4.3+
	 */
	OpenGL,
	/**
	 * Vulkan
	 */
	Vulkan,
	/**
	 * WebGPU
	 */
	WebGPU,

	/**
	 * Number of native enum values.
	 */
	Count;

	/**
	 * Native C enum layout.
	 */
	public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
	private static final RendererType[] VALUES = values();

	/**
	 * Returns the enum constant for a native C enum value.
	 * @param value the native enum value
	 * @return the matching enum constant
	 */
	public static RendererType fromValue(int value) {
		if (value >= 0 && value < VALUES.length) {
			return VALUES[value];
		}
		throw new IllegalArgumentException("Unknown RendererType value: " + value);
	}
}
