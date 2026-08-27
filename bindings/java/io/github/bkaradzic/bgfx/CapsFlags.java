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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static io.github.bkaradzic.bgfx.BGFX.*;
import static io.github.bkaradzic.bgfx.util.FFMUtil.*;

/**
 * Constants for Caps flags.
 */
@NullMarked
public final class CapsFlags {
	private CapsFlags() {
	}
	/**
	 * Alpha to coverage is supported.
	 */
	public static final long AlphaToCoverage = 0x0000000000000001L;

	/**
	 * Blend independent is supported.
	 */
	public static final long BlendIndependent = 0x0000000000000002L;

	/**
	 * Compute shaders are supported.
	 */
	public static final long Compute = 0x0000000000000004L;

	/**
	 * Conservative rasterization is supported.
	 */
	public static final long ConservativeRaster = 0x0000000000000008L;

	/**
	 * Draw indirect is supported.
	 */
	public static final long DrawIndirect = 0x0000000000000010L;

	/**
	 * Draw indirect with indirect count is supported.
	 */
	public static final long DrawIndirectCount = 0x0000000000000020L;

	/**
	 * Fragment depth is available in fragment shader.
	 */
	public static final long FragmentDepth = 0x0000000000000040L;

	/**
	 * Fragment ordering is available in fragment shader.
	 */
	public static final long FragmentOrdering = 0x0000000000000080L;

	/**
	 * Graphics debugger is present.
	 */
	public static final long GraphicsDebugger = 0x0000000000000100L;

	/**
	 * HDR10 rendering is supported.
	 */
	public static final long Hdr10 = 0x0000000000000200L;

	/**
	 * HiDPI rendering is supported.
	 */
	public static final long Hidpi = 0x0000000000000400L;

	/**
	 * Image Read/Write is supported.
	 */
	public static final long ImageRw = 0x0000000000000800L;

	/**
	 * 32-bit indices are supported.
	 */
	public static final long Index32 = 0x0000000000001000L;

	/**
	 * Instancing is supported.
	 */
	public static final long Instancing = 0x0000000000002000L;

	/**
	 * Occlusion query is supported.
	 */
	public static final long OcclusionQuery = 0x0000000000004000L;

	/**
	 * PrimitiveID is available in fragment shader.
	 */
	public static final long PrimitiveId = 0x0000000000008000L;

	/**
	 * Renderer is on separate thread.
	 */
	public static final long RendererMultithreaded = 0x0000000000010000L;

	/**
	 * Multiple windows are supported.
	 */
	public static final long SwapChain = 0x0000000000020000L;

	/**
	 * Texture blit is supported.
	 */
	public static final long TextureBlit = 0x0000000000040000L;

	/**
	 * Texture compare less equal mode is supported.
	 */
	public static final long TextureCompareLequal = 0x0000000000080000L;

	/**
	 * Caps flag value {@code TextureCompareReserved}.
	 */
	public static final long TextureCompareReserved = 0x0000000000100000L;

	/**
	 * Cubemap texture array is supported.
	 */
	public static final long TextureCubeArray = 0x0000000000200000L;

	/**
	 * CPU direct access to GPU texture memory.
	 */
	public static final long TextureDirectAccess = 0x0000000000400000L;

	/**
	 * External texture is supported.
	 */
	public static final long TextureExternal = 0x0000000000800000L;

	/**
	 * External shared texture is supported.
	 */
	public static final long TextureExternalShared = 0x0000000001000000L;

	/**
	 * Read-back texture is supported.
	 */
	public static final long TextureReadBack = 0x0000000002000000L;

	/**
	 * 2D texture array is supported.
	 */
	public static final long Texture2DArray = 0x0000000004000000L;

	/**
	 * 3D textures are supported.
	 */
	public static final long Texture3D = 0x0000000008000000L;

	/**
	 * Transparent back buffer supported.
	 */
	public static final long TransparentBackbuffer = 0x0000000010000000L;

	/**
	 * Variable Rate Shading
	 */
	public static final long VariableRateShading = 0x0000000020000000L;

	/**
	 * Vertex attribute half-float is supported.
	 */
	public static final long VertexAttribHalf = 0x0000000040000000L;

	/**
	 * Vertex attribute 10_10_10_2 is supported.
	 */
	public static final long VertexAttribUint10 = 0x0000000080000000L;

	/**
	 * Rendering with VertexID only is supported.
	 */
	public static final long VertexId = 0x0000000100000000L;

	/**
	 * Hardware video decode is supported.
	 */
	public static final long VideoDecode = 0x0000000200000000L;

	/**
	 * Viewport layer is available in vertex shader.
	 */
	public static final long ViewportLayerArray = 0x0000000400000000L;

	/**
	 * All texture compare modes are supported.
	 */
	public static final long TextureCompareAll = 0x0000000000180000L;
}
