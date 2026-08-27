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
 * Vertex attribute enum.
 */
public enum Attrib {
	/**
	 * a_position
	 */
	Position,
	/**
	 * a_normal
	 */
	Normal,
	/**
	 * a_tangent
	 */
	Tangent,
	/**
	 * a_bitangent
	 */
	Bitangent,
	/**
	 * a_color0
	 */
	Color0,
	/**
	 * a_color1
	 */
	Color1,
	/**
	 * a_color2
	 */
	Color2,
	/**
	 * a_color3
	 */
	Color3,
	/**
	 * a_indices
	 */
	Indices,
	/**
	 * a_weight
	 */
	Weight,
	/**
	 * a_texcoord0
	 */
	TexCoord0,
	/**
	 * a_texcoord1
	 */
	TexCoord1,
	/**
	 * a_texcoord2
	 */
	TexCoord2,
	/**
	 * a_texcoord3
	 */
	TexCoord3,
	/**
	 * a_texcoord4
	 */
	TexCoord4,
	/**
	 * a_texcoord5
	 */
	TexCoord5,
	/**
	 * a_texcoord6
	 */
	TexCoord6,
	/**
	 * a_texcoord7
	 */
	TexCoord7,
	/**
	 * a_texcoord8
	 */
	TexCoord8,
	/**
	 * a_texcoord9
	 */
	TexCoord9,
	/**
	 * a_texcoord10
	 */
	TexCoord10,
	/**
	 * a_texcoord11
	 */
	TexCoord11,
	/**
	 * a_texcoord12
	 */
	TexCoord12,
	/**
	 * a_texcoord13
	 */
	TexCoord13,
	/**
	 * a_texcoord14
	 */
	TexCoord14,
	/**
	 * a_texcoord15
	 */
	TexCoord15,

	/**
	 * Number of native enum values.
	 */
	Count;

	/**
	 * Native C enum layout.
	 */
	public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
	private static final Attrib[] VALUES = values();

	/**
	 * Returns the enum constant for a native C enum value.
	 * @param value the native enum value
	 * @return the matching enum constant
	 */
	public static Attrib fromValue(int value) {
		if (value >= 0 && value < VALUES.length) {
			return VALUES[value];
		}
		throw new IllegalArgumentException("Unknown Attrib value: " + value);
	}
}
