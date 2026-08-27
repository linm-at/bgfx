// Copyright 2011-2026 Branimir Karadzic. All rights reserved.
// License: https://github.com/bkaradzic/bgfx/blob/master/LICENSE


//
// AUTO GENERATED! DO NOT EDIT!
//

package io.github.bkaradzic.bgfx;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.file.Path;
import java.util.Objects;

import io.github.bkaradzic.bgfx.util.FFMUtil;

import static io.github.bkaradzic.bgfx.util.FFMUtil.*;


/**
 * Modern Java FFM bindings for the bgfx C99 API.
 * <p>
 * Call {@link #load(Path)}, {@link #load(String)}, or {@link #link()} before
 * invoking a native method. Linking resolves every native entry point eagerly.
 */
@SuppressWarnings("restricted")
public final class BGFX {

	private static volatile MethodHandle[] downcalls;
	private static volatile MemorySegment[] variadicSymbols;
	private static Arena libraryArena;

	private BGFX() {
	}

	/**
	 * Loads a bgfx shared library and eagerly links all native entry points.
	 * @param library path to the bgfx shared library
	 */
	public static synchronized void load(Path library) {
		Objects.requireNonNull(library, "library");
		Arena arena = newLibraryArena();
		try {
			installLibrary(SymbolLookup.libraryLookup(library, arena), arena);
		} catch (RuntimeException | Error ex) {
			arena.close();
			throw ex;
		}
	}

	/**
	 * Loads a bgfx shared library by platform-dependent name and eagerly links
	 * all native entry points.
	 * @param library platform-dependent library name
	 */
	public static synchronized void load(String library) {
		Objects.requireNonNull(library, "library");
		Arena arena = newLibraryArena();
		try {
			installLibrary(SymbolLookup.libraryLookup(library, arena), arena);
		} catch (RuntimeException | Error ex) {
			arena.close();
			throw ex;
		}
	}

	/**
	 * Eagerly links all native entry points from libraries already made visible
	 * through {@link System#load} or {@link System#loadLibrary}.
	 */
	public static synchronized void link() {
		ensureUnlinked();
		installLibrary(SymbolLookup.loaderLookup().or(LINKER.defaultLookup()), null);
	}

	private static Arena newLibraryArena() {
		ensureUnlinked();
		return Arena.ofShared();
	}

	private static void ensureUnlinked() {
		if (downcalls != null || libraryArena != null) {
			throw new IllegalStateException("bgfx native calls are already linked");
		}
	}

	private static void installLibrary(SymbolLookup library, Arena arena) {
		SymbolLookup lookup = library.or(SymbolLookup.loaderLookup()).or(LINKER.defaultLookup());
		MethodHandle[] handles = linkAll(lookup);
		MemorySegment[] variadics = linkVariadicSymbols(lookup);
		libraryArena = arena;
		variadicSymbols = variadics;
		downcalls = handles;
	}

	private static MemorySegment symbol(SymbolLookup lookup, String name) {
		return lookup.find(name).orElseThrow(
			() -> new UnsatisfiedLinkError("Unable to find native symbol " + name));
	}

	private static MethodHandle downcall(
		SymbolLookup lookup, String name, FunctionDescriptor descriptor) {
		return LINKER.downcallHandle(symbol(lookup, name), descriptor);
	}

	static MethodHandle downcallHandle(int index) {
		MethodHandle[] handles = downcalls;
		if (handles == null) {
			throw new IllegalStateException(
				"bgfx native calls are not linked; call BGFX.load(...) or BGFX.link()");
		}
		return handles[index];
	}

	private static MemorySegment variadicSymbol(int index) {
		MemorySegment[] addresses = variadicSymbols;
		if (addresses == null) {
			throw new IllegalStateException(
				"bgfx native calls are not linked; call BGFX.load(...) or BGFX.link()");
		}
		return addresses[index];
	}

	static Object invoke(int index, Object... args) {
		return FFMUtil.invoke(downcallHandle(index), args);
	}

	/**
	 * A promoted C variadic argument for {@link #dbgTextPrintf}.
	 * @param layout promoted native value layout
	 * @param value boxed value matching the layout carrier
	 */
	public record VarArg(ValueLayout layout, Object value) {
		/** Validates that the value uses a C variadic promoted carrier. */
		public VarArg {
			Objects.requireNonNull(layout, "layout");
			Objects.requireNonNull(value, "value");
			Class<?> carrier = MethodType.methodType(layout.carrier()).wrap().returnType();
			if (!carrier.isInstance(value)) {
				throw new IllegalArgumentException(
					"Value " + value + " does not match " + layout.carrier().getName());
			}
			Class<?> nativeCarrier = layout.carrier();
			if (nativeCarrier != int.class && nativeCarrier != long.class
				&& nativeCarrier != double.class && nativeCarrier != MemorySegment.class) {
				throw new IllegalArgumentException("C variadic value must use its promoted layout");
			}
		}

		/**
		 * Creates a promoted C {@code int} argument.
		 * @param value argument value
		 * @return a promoted C {@code int} argument
		 */
		public static VarArg ofInt(int value) {
			return new VarArg(ValueLayout.JAVA_INT, value);
		}

		/**
		 * Creates a promoted C {@code long long} argument.
		 * @param value argument value
		 * @return a promoted C {@code long long} argument
		 */
		public static VarArg ofLong(long value) {
			return new VarArg(ValueLayout.JAVA_LONG, value);
		}

		/**
		 * Creates a promoted C {@code double} argument.
		 * @param value argument value
		 * @return a promoted C {@code double} argument
		 */
		public static VarArg ofDouble(double value) {
			return new VarArg(ValueLayout.JAVA_DOUBLE, value);
		}

		/**
		 * Creates a C pointer argument.
		 * @param value pointer value
		 * @return a C pointer argument
		 */
		public static VarArg ofAddress(MemorySegment value) {
			return new VarArg(ValueLayout.ADDRESS, address(value));
		}
	}

	// -------------------------------------------------------------------------
	// Generated native entry points. Descriptors and methods intentionally live
	// in this section rather than in per-function holder classes.
	// -------------------------------------------------------------------------
	static final int DC_TEXTURE_REGION_INIT = 0;
	private static final FunctionDescriptor FD_TEXTURE_REGION_INIT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT);
	static final int DC_BUFFER_REGION_INIT_TEXTURE = 1;
	private static final FunctionDescriptor FD_BUFFER_REGION_INIT_TEXTURE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	static final int DC_BUFFER_REGION_INIT_BUFFER = 2;
	private static final FunctionDescriptor FD_BUFFER_REGION_INIT_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, BufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ATTACHMENT_INIT = 3;
	private static final FunctionDescriptor FD_ATTACHMENT_INIT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, TextureHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE);
	static final int DC_VERTEX_LAYOUT_BEGIN = 4;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_BEGIN = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	static final int DC_VERTEX_LAYOUT_ADD = 5;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_ADD = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_BOOLEAN);
	static final int DC_VERTEX_LAYOUT_DECODE = 6;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_DECODE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	static final int DC_VERTEX_LAYOUT_HAS = 7;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_HAS = FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	static final int DC_VERTEX_LAYOUT_SKIP = 8;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_SKIP = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE);
	static final int DC_VERTEX_LAYOUT_END = 9;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_END = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
	static final int DC_VERTEX_LAYOUT_GET_OFFSET = 10;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_GET_OFFSET = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	static final int DC_VERTEX_LAYOUT_GET_STRIDE = 11;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_GET_STRIDE = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS);
	static final int DC_VERTEX_LAYOUT_GET_SIZE = 12;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_GET_SIZE = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Pack vertex attribute into vertex stream format.
	 * @param _input Value to be packed into vertex stream.
	 * @param _inputNormalized {@code true} if input value is already normalized.
	 * @param _attr Attribute to pack.
	 * @param _layout Vertex stream layout.
	 * @param _data Destination vertex stream where data will be packed.
	 * @param _index Vertex index that will be modified.
	 */
	public static final void vertexPack(MemorySegment _input, boolean _inputNormalized, Attrib _attr, VertexLayout _layout, MemorySegment _data, int _index) {
		try {
			downcallHandle(DC_VERTEX_PACK).invokeExact(address(_input), _inputNormalized, _attr.ordinal(), address(_layout), address(_data), _index);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_VERTEX_PACK = 13;
	private static final FunctionDescriptor FD_VERTEX_PACK = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Unpack vertex attribute from vertex stream format.
	 * @param _output Result of unpacking.
	 * @param _attr Attribute to unpack.
	 * @param _layout Vertex stream layout.
	 * @param _data Source vertex stream from where data will be unpacked.
	 * @param _index Vertex index that will be unpacked.
	 */
	public static final void vertexUnpack(MemorySegment _output, Attrib _attr, VertexLayout _layout, MemorySegment _data, int _index) {
		try {
			downcallHandle(DC_VERTEX_UNPACK).invokeExact(address(_output), _attr.ordinal(), address(_layout), address(_data), _index);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_VERTEX_UNPACK = 14;
	private static final FunctionDescriptor FD_VERTEX_UNPACK = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Converts vertex stream data from one vertex stream format to another.
	 * @param _dstLayout Destination vertex stream layout.
	 * @param _dstData Destination vertex stream.
	 * @param _srcLayout Source vertex stream layout.
	 * @param _srcData Source vertex stream data.
	 * @param _num Number of vertices to convert from source to destination.
	 */
	public static final void vertexConvert(VertexLayout _dstLayout, MemorySegment _dstData, VertexLayout _srcLayout, MemorySegment _srcData, int _num) {
		try {
			downcallHandle(DC_VERTEX_CONVERT).invokeExact(address(_dstLayout), address(_dstData), address(_srcLayout), address(_srcData), _num);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_VERTEX_CONVERT = 15;
	private static final FunctionDescriptor FD_VERTEX_CONVERT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Convert index buffer for use with different primitive topologies.
	 * @param _conversion Conversion type, see {@code TopologyConvert}.
	 * @param _dst Destination index buffer. If this argument is NULL function will return number of indices after conversion.
	 * @param _dstSize Destination index buffer in bytes. It must be large enough to contain output indices. If destination size is insufficient index buffer will be truncated.
	 * @param _indices Source indices.
	 * @param _numIndices Number of input indices.
	 * @param _index32 Set to {@code true} if input indices are 32-bit.
	 * @return Number of output indices after conversion.
	 */
	public static final int topologyConvert(TopologyConvert _conversion, MemorySegment _dst, int _dstSize, MemorySegment _indices, int _numIndices, boolean _index32) {
		try {
			return (int) downcallHandle(DC_TOPOLOGY_CONVERT).invokeExact(_conversion.ordinal(), address(_dst), _dstSize, address(_indices), _numIndices, _index32);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_TOPOLOGY_CONVERT = 16;
	private static final FunctionDescriptor FD_TOPOLOGY_CONVERT = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Sort indices.
	 * @param _sort Sort order, see {@code TopologySort}.
	 * @param _dst Destination index buffer.
	 * @param _dstSize Destination index buffer in bytes. It must be large enough to contain output indices. If destination size is insufficient index buffer will be truncated.
	 * @param _dir Direction (vector must be normalized).
	 * @param _pos Position.
	 * @param _vertices Pointer to first vertex represented as float x, y, z. Must contain at least number of vertices referencende by index buffer.
	 * @param _stride Vertex stride.
	 * @param _indices Source indices.
	 * @param _numIndices Number of input indices.
	 * @param _index32 Set to {@code true} if input indices are 32-bit.
	 */
	public static final void topologySortTriList(TopologySort _sort, MemorySegment _dst, int _dstSize, MemorySegment _dir, MemorySegment _pos, MemorySegment _vertices, int _stride, MemorySegment _indices, int _numIndices, boolean _index32) {
		try {
			downcallHandle(DC_TOPOLOGY_SORT_TRI_LIST).invokeExact(_sort.ordinal(), address(_dst), _dstSize, address(_dir), address(_pos), address(_vertices), _stride, address(_indices), _numIndices, _index32);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_TOPOLOGY_SORT_TRI_LIST = 17;
	private static final FunctionDescriptor FD_TOPOLOGY_SORT_TRI_LIST = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Returns supported backend API renderers.
	 * @param _max Maximum number of elements in _enum array.
	 * @param _enum Array where supported renderers will be written.
	 * @return Number of supported renderers.
	 */
	public static final byte getSupportedRenderers(byte _max, MemorySegment _enum) {
		try {
			return (byte) downcallHandle(DC_GET_SUPPORTED_RENDERERS).invokeExact(_max, address(_enum));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_SUPPORTED_RENDERERS = 18;
	private static final FunctionDescriptor FD_GET_SUPPORTED_RENDERERS = FunctionDescriptor.of(ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS);
	/**
	 * Returns name of renderer.
	 * @param _type Renderer backend type. See: {@code RendererType}
	 * @return Name of renderer.
	 */
	public static final String getRendererName(RendererType _type) {
		try {
			return readString((MemorySegment) downcallHandle(DC_GET_RENDERER_NAME).invokeExact(_type.ordinal()));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_RENDERER_NAME = 19;
	private static final FunctionDescriptor FD_GET_RENDERER_NAME = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Fill Init struct with default values, before using it to initialize the library.
	 * @param _init Pointer to structure to be initialized. See: {@code Init} for more info.
	 */
	public static final void initCtor(Init _init) {
		try {
			downcallHandle(DC_INIT_CTOR).invokeExact(address(_init));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_INIT_CTOR = 20;
	private static final FunctionDescriptor FD_INIT_CTOR = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
	/**
	 * Initialize the bgfx library.
	 * @param _init Initialization parameters. See: {@code Init} for more info.
	 * @return {@code true} if initialization was successful.
	 */
	public static final boolean init(Init _init) {
		try {
			return (boolean) downcallHandle(DC_INIT).invokeExact(address(_init));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_INIT = 21;
	private static final FunctionDescriptor FD_INIT = FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS);
	/**
	 * Shutdown bgfx library.
	 */
	public static final void shutdown() {
		try {
			downcallHandle(DC_SHUTDOWN).invokeExact();
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SHUTDOWN = 22;
	private static final FunctionDescriptor FD_SHUTDOWN = FunctionDescriptor.ofVoid();
	/**
	 * Reset graphic settings and back-buffer size.
	 * <p>
	 * <strong>Attention:</strong> This call doesn’t change the window size, it just resizes
	 *   the back-buffer. Your windowing code controls the window size.
	 * @param _width Back-buffer width.
	 * @param _height Back-buffer height.
	 * @param _flags See: {@code BGFX_RESET_*} for more info.   - {@code BGFX_RESET_NONE} - No reset flags.   - {@code BGFX_RESET_FULLSCREEN} - Not supported yet.   - {@code BGFX_RESET_MSAA_X[2/4/8/16]} - Enable 2, 4, 8 or 16 x MSAA.   - {@code BGFX_RESET_VSYNC} - Enable V-Sync.   - {@code BGFX_RESET_MAXANISOTROPY} - Turn on/off max anisotropy.   - {@code BGFX_RESET_CAPTURE} - Begin screen capture.   - {@code BGFX_RESET_FLUSH_AFTER_RENDER} - Flush rendering after submitting to GPU.   - {@code BGFX_RESET_FLIP_AFTER_RENDER} - This flag  specifies where flip     occurs. Default behaviour is that flip occurs before rendering new     frame. This flag only has effect when {@code BGFX_CONFIG_MULTITHREADED=0}.   - {@code BGFX_RESET_SRGB_BACKBUFFER} - Enable sRGB back-buffer.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 */
	public static final void reset(int _width, int _height, int _flags, TextureFormat _format) {
		try {
			downcallHandle(DC_RESET).invokeExact(_width, _height, _flags, _format.ordinal());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_RESET = 23;
	private static final FunctionDescriptor FD_RESET = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Advance to next frame. This is the main frame-advancement call on the
	 * API thread (the thread from which {@code init} was called).
	 * <p>
	 * **Multithreaded renderer** ({@code BGFX_CONFIG_MULTITHREADED=1}, default):
	 * This call waits for the render thread to finish processing the previous
	 * frame, then swaps internal submit/render buffers, signals the render
	 * thread to begin processing the new frame via {@code renderFrame}, and
	 * returns immediately. The render thread and API thread then run in
	 * parallel: the API thread builds the next frame while the render thread
	 * executes GPU commands for the current frame.
	 * <p>
	 * **Single-threaded renderer** ({@code BGFX_CONFIG_MULTITHREADED=0}, or when
	 * {@code renderFrame} and {@code init} are called from the same thread):
	 * This call swaps internal buffers and performs frame rendering inline
	 * (internally calls {@code renderFrame}), then returns.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Must be called from the API thread (the thread that called
	 *   {@code init}). In multithreaded mode, this call synchronizes with
	 *   {@code renderFrame} running on the render thread via semaphores:
	 *   {@code frame} waits for the render thread to finish, then posts a
	 *   signal that {@code renderFrame} waits on to begin the next frame.
	 *   See also: {@code renderFrame}.
	 * @param _flags Frame flags. See: {@code BGFX_FRAME_*} for more info.   - {@code BGFX_FRAME_NONE} - No frame flag.   - {@code BGFX_FRAME_DEBUG_CAPTURE} - Capture frame with graphics debugger.   - {@code BGFX_FRAME_DISCARD} - Discard all draw calls.   - {@code BGFX_FRAME_FLUSH} - Execute all rendering commands     without presenting the backbuffer.
	 * @return Current frame number. This might be used in conjunction with double/multi buffering data outside the library and passing it to library via {@code makeRef} calls.
	 */
	public static final int frame(byte _flags) {
		try {
			return (int) downcallHandle(DC_FRAME).invokeExact(_flags);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_FRAME = 24;
	private static final FunctionDescriptor FD_FRAME = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	/**
	 * Returns current renderer backend API type.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Library must be initialized.
	 * @return Renderer backend type. See: {@code RendererType}
	 */
	public static final RendererType getRendererType() {
		try {
			return RendererType.fromValue((int) downcallHandle(DC_GET_RENDERER_TYPE).invokeExact());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_RENDERER_TYPE = 25;
	private static final FunctionDescriptor FD_GET_RENDERER_TYPE = FunctionDescriptor.of(ValueLayout.JAVA_INT);
	/**
	 * Returns renderer capabilities.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Library must be initialized.
	 * @return Pointer to static {@code Caps} structure.
	 */
	public static final Caps getCaps() {
		try {
			return new Caps((MemorySegment) downcallHandle(DC_GET_CAPS).invokeExact());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_CAPS = 26;
	private static final FunctionDescriptor FD_GET_CAPS = FunctionDescriptor.of(ValueLayout.ADDRESS);
	/**
	 * Returns performance counters.
	 * <p>
	 * <strong>Attention:</strong> Pointer returned is valid until {@code frame} is called.
	 * @return Performance counters.
	 */
	public static final Stats getStats() {
		try {
			return new Stats((MemorySegment) downcallHandle(DC_GET_STATS).invokeExact());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_STATS = 27;
	private static final FunctionDescriptor FD_GET_STATS = FunctionDescriptor.of(ValueLayout.ADDRESS);
	/**
	 * Allocate buffer to pass to bgfx calls. Data will be freed inside bgfx.
	 * @param _size Size to allocate.
	 * @return Allocated memory.
	 */
	public static final Memory alloc(int _size) {
		try {
			return new Memory((MemorySegment) downcallHandle(DC_ALLOC).invokeExact(_size));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_ALLOC = 28;
	private static final FunctionDescriptor FD_ALLOC = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Allocate buffer and copy data into it. Data will be freed inside bgfx.
	 * @param _data Pointer to data to be copied.
	 * @param _size Size of data to be copied.
	 * @return Allocated memory.
	 */
	public static final Memory copy(MemorySegment _data, int _size) {
		try {
			return new Memory((MemorySegment) downcallHandle(DC_COPY).invokeExact(address(_data), _size));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_COPY = 29;
	private static final FunctionDescriptor FD_COPY = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Make reference to data to pass to bgfx. Unlike {@code alloc}, this call
	 * doesn't allocate memory for data. It just copies the _data pointer. You
	 * can pass {@code ReleaseFn} function pointer to release this memory after it's
	 * consumed, otherwise you must make sure _data is available for at least 2
	 * {@code frame} calls. {@code ReleaseFn} function must be able to be called
	 * from any thread.
	 * <p>
	 * <strong>Attention:</strong> Data passed must be available for at least 2 {@code frame} calls.
	 * @param _data Pointer to data.
	 * @param _size Size of data.
	 * @return Referenced memory.
	 */
	public static final Memory makeRef(MemorySegment _data, int _size) {
		try {
			return new Memory((MemorySegment) downcallHandle(DC_MAKE_REF).invokeExact(address(_data), _size));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_MAKE_REF = 30;
	private static final FunctionDescriptor FD_MAKE_REF = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Make reference to data to pass to bgfx. Unlike {@code alloc}, this call
	 * doesn't allocate memory for data. It just copies the _data pointer. You
	 * can pass {@code ReleaseFn} function pointer to release this memory after it's
	 * consumed, otherwise you must make sure _data is available for at least 2
	 * {@code frame} calls. {@code ReleaseFn} function must be able to be called
	 * from any thread.
	 * <p>
	 * <strong>Attention:</strong> Data passed must be available for at least 2 {@code frame} calls.
	 * @param _data Pointer to data.
	 * @param _size Size of data.
	 * @param _releaseFn Callback function to release memory after use.
	 * @param _userData User data to be passed to callback function.
	 * @return Referenced memory.
	 */
	public static final Memory makeRefRelease(MemorySegment _data, int _size, MemorySegment _releaseFn, MemorySegment _userData) {
		try {
			return new Memory((MemorySegment) downcallHandle(DC_MAKE_REF_RELEASE).invokeExact(address(_data), _size, address(_releaseFn), address(_userData)));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_MAKE_REF_RELEASE = 31;
	private static final FunctionDescriptor FD_MAKE_REF_RELEASE = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	/**
	 * Set debug flags.
	 * @param _debug Available flags:   - {@code BGFX_DEBUG_IFH} - Infinitely fast hardware. When this flag is set     all rendering calls will be skipped. This is useful when profiling     to quickly assess potential bottlenecks between CPU and GPU.   - {@code BGFX_DEBUG_PROFILER} - Enable profiler.   - {@code BGFX_DEBUG_STATS} - Display internal statistics.   - {@code BGFX_DEBUG_TEXT} - Display debug text.   - {@code BGFX_DEBUG_WIREFRAME} - Wireframe rendering. All rendering     primitives will be rendered as lines.
	 */
	public static final void setDebug(int _debug) {
		try {
			downcallHandle(DC_SET_DEBUG).invokeExact(_debug);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_DEBUG = 32;
	private static final FunctionDescriptor FD_SET_DEBUG = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT);
	/**
	 * Clear internal debug text buffer.
	 * @param _attr Background color.
	 * @param _small Default 8x16 or 8x8 font.
	 */
	public static final void dbgTextClear(byte _attr, boolean _small) {
		try {
			downcallHandle(DC_DBG_TEXT_CLEAR).invokeExact(_attr, _small);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DBG_TEXT_CLEAR = 33;
	private static final FunctionDescriptor FD_DBG_TEXT_CLEAR = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Print formatted data to internal debug text character-buffer (VGA-compatible text mode).
	 * @param _x Position x from the left corner of the window.
	 * @param _y Position y from the top corner of the window.
	 * @param _attr Color palette. Where top 4-bits represent index of background, and bottom 4-bits represent foreground color from standard VGA text palette (ANSI escape codes).
	 * @param _format {@code printf} style format.
	 * @param _args promoted C variadic arguments
	 */
	public static final void dbgTextPrintf(short _x, short _y, byte _attr, String _format, VarArg... _args) {
		Objects.requireNonNull(_args, "_args");
		try (Arena arena = Arena.ofConfined()) {
			MemoryLayout[] layouts = new MemoryLayout[_args.length];
			Object[] nativeArgs = new Object[4 + _args.length];
			nativeArgs[0] = _x;
			nativeArgs[1] = _y;
			nativeArgs[2] = _attr;
			nativeArgs[3] = cString(arena, _format);
			for (int index = 0; index < _args.length; ++index) {
				layouts[index] = _args[index].layout();
				nativeArgs[4 + index] = _args[index].value();
			}
			FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS).appendArgumentLayouts(layouts);
			MethodHandle handle = LINKER.downcallHandle(
				variadicSymbol(VC_DBG_TEXT_PRINTF),
				descriptor, Linker.Option.firstVariadicArg(4));
			FFMUtil.invoke(handle, nativeArgs);
		}
	}

	private static final int VC_DBG_TEXT_PRINTF = 0;
	/**
	 * Print formatted data from variable argument list to internal debug text character-buffer (VGA-compatible text mode).
	 * @param _x Position x from the left corner of the window.
	 * @param _y Position y from the top corner of the window.
	 * @param _attr Color palette. Where top 4-bits represent index of background, and bottom 4-bits represent foreground color from standard VGA text palette (ANSI escape codes).
	 * @param _format {@code printf} style format.
	 * @param _argList Variable arguments list for format string.
	 */
	public static final void dbgTextVprintf(short _x, short _y, byte _attr, String _format, MemorySegment _argList) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DBG_TEXT_VPRINTF).invokeExact(_x, _y, _attr, cString(arena, _format), address(_argList));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DBG_TEXT_VPRINTF = 34;
	private static final FunctionDescriptor FD_DBG_TEXT_VPRINTF = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	/**
	 * Draw image into internal debug text buffer.
	 * @param _x Position x from the left corner of the window.
	 * @param _y Position y from the top corner of the window.
	 * @param _width Image width.
	 * @param _height Image height.
	 * @param _data Raw image data (character/attribute raw encoding).
	 * @param _pitch Image pitch in bytes.
	 */
	public static final void dbgTextImage(short _x, short _y, short _width, short _height, MemorySegment _data, short _pitch) {
		try {
			downcallHandle(DC_DBG_TEXT_IMAGE).invokeExact(_x, _y, _width, _height, address(_data), _pitch);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DBG_TEXT_IMAGE = 35;
	private static final FunctionDescriptor FD_DBG_TEXT_IMAGE = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Create static index buffer.
	 * @param _mem Index buffer data.
	 * @param _flags Buffer creation flags.   - {@code BGFX_BUFFER_NONE} - No flags.   - {@code BGFX_BUFFER_COMPUTE_READ} - Buffer will be read from by compute shader.   - {@code BGFX_BUFFER_COMPUTE_WRITE} - Buffer will be written into by compute shader. When buffer       is created with {@code BGFX_BUFFER_COMPUTE_WRITE} flag it cannot be updated from CPU.   - {@code BGFX_BUFFER_COMPUTE_READ_WRITE} - Buffer will be used for read/write by compute shader.   - {@code BGFX_BUFFER_ALLOW_RESIZE} - Buffer will resize on buffer update if a different amount of       data is passed. If this flag is not specified, and more data is passed on update, the buffer       will be trimmed to fit the existing buffer size. This flag has effect only on dynamic       buffers.   - {@code BGFX_BUFFER_INDEX32} - Buffer is using 32-bit indices. This flag has effect only on       index buffers.
	 * @return the native function result
	 */
	public static final IndexBufferHandle createIndexBuffer(Memory _mem, short _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return IndexBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_INDEX_BUFFER).invokeExact((SegmentAllocator) arena, address(_mem), _flags));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_INDEX_BUFFER = 36;
	private static final FunctionDescriptor FD_CREATE_INDEX_BUFFER = FunctionDescriptor.of(IndexBufferHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Read back contents of buffer.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Read back is asynchronous, and the result is available at the returned frame.
	 *   A zero {@code size} reads the rest of the buffer. {@code rowPitch} and {@code slicePitch} are
	 *   unused.
	 * <p>
	 *   Read back is intended for reading GPU written (compute, or draw indirect) buffers
	 *   back to the CPU. It's not intended to be used in the main render loop, since it
	 *   stalls the GPU.
	 * <p>
	 * <strong>Attention:</strong> Buffer must be created with one of {@code BGFX_BUFFER_COMPUTE_*}, or
	 *   {@code BGFX_BUFFER_DRAW_INDIRECT} flags.
	 * @param _src Source buffer region.
	 * @param _data Destination buffer.
	 * @return Frame number when the result will be available. See: {@code frame}.
	 */
	public static final int readBuffer(BufferRegion _src, MemorySegment _data) {
		try {
			return (int) downcallHandle(DC_READ_BUFFER).invokeExact(address(_src), address(_data));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_READ_BUFFER = 37;
	private static final FunctionDescriptor FD_READ_BUFFER = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	/**
	 * Set static index buffer debug name.
	 * @param _handle Static index buffer handle.
	 * @param _name Static index buffer name.
	 * @param _len Static index buffer name length (if length is INT32_MAX, it's expected that _name is zero terminated string.
	 */
	public static final void setIndexBufferName(IndexBufferHandle _handle, String _name, int _len) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_INDEX_BUFFER_NAME).invokeExact(_handle.allocate(arena), cString(arena, _name), _len);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_INDEX_BUFFER_NAME = 38;
	private static final FunctionDescriptor FD_SET_INDEX_BUFFER_NAME = FunctionDescriptor.ofVoid(IndexBufferHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Destroy static index buffer.
	 * @param _handle Static index buffer handle.
	 */
	public static final void destroyIndexBuffer(IndexBufferHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_INDEX_BUFFER).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_INDEX_BUFFER = 39;
	private static final FunctionDescriptor FD_DESTROY_INDEX_BUFFER = FunctionDescriptor.ofVoid(IndexBufferHandle.LAYOUT);
	/**
	 * Create vertex layout. Vertex layouts are used to describe the format of vertex data.
	 * @param _layout Vertex layout.
	 * @return the native function result
	 */
	public static final VertexLayoutHandle createVertexLayout(VertexLayout _layout) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return VertexLayoutHandle.read((MemorySegment) downcallHandle(DC_CREATE_VERTEX_LAYOUT).invokeExact((SegmentAllocator) arena, address(_layout)));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_VERTEX_LAYOUT = 40;
	private static final FunctionDescriptor FD_CREATE_VERTEX_LAYOUT = FunctionDescriptor.of(VertexLayoutHandle.LAYOUT, ValueLayout.ADDRESS);
	/**
	 * Destroy vertex layout.
	 * @param _layoutHandle Vertex layout handle.
	 */
	public static final void destroyVertexLayout(VertexLayoutHandle _layoutHandle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_VERTEX_LAYOUT).invokeExact(_layoutHandle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_VERTEX_LAYOUT = 41;
	private static final FunctionDescriptor FD_DESTROY_VERTEX_LAYOUT = FunctionDescriptor.ofVoid(VertexLayoutHandle.LAYOUT);
	/**
	 * Create static vertex buffer.
	 * @param _mem Vertex buffer data.
	 * @param _layout Vertex layout.
	 * @param _flags Buffer creation flags.  - {@code BGFX_BUFFER_NONE} - No flags.  - {@code BGFX_BUFFER_COMPUTE_READ} - Buffer will be read from by compute shader.  - {@code BGFX_BUFFER_COMPUTE_WRITE} - Buffer will be written into by compute shader. When buffer      is created with {@code BGFX_BUFFER_COMPUTE_WRITE} flag it cannot be updated from CPU.  - {@code BGFX_BUFFER_COMPUTE_READ_WRITE} - Buffer will be used for read/write by compute shader.  - {@code BGFX_BUFFER_ALLOW_RESIZE} - Buffer will resize on buffer update if a different amount of      data is passed. If this flag is not specified, and more data is passed on update, the buffer      will be trimmed to fit the existing buffer size. This flag has effect only on dynamic buffers.  - {@code BGFX_BUFFER_INDEX32} - Buffer is using 32-bit indices. This flag has effect only on index buffers.
	 * @return Static vertex buffer handle.
	 */
	public static final VertexBufferHandle createVertexBuffer(Memory _mem, VertexLayout _layout, short _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return VertexBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_VERTEX_BUFFER).invokeExact((SegmentAllocator) arena, address(_mem), address(_layout), _flags));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_VERTEX_BUFFER = 42;
	private static final FunctionDescriptor FD_CREATE_VERTEX_BUFFER = FunctionDescriptor.of(VertexBufferHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Set static vertex buffer debug name.
	 * @param _handle Static vertex buffer handle.
	 * @param _name Static vertex buffer name.
	 * @param _len Static vertex buffer name length (if length is INT32_MAX, it's expected that _name is zero terminated string.
	 */
	public static final void setVertexBufferName(VertexBufferHandle _handle, String _name, int _len) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_VERTEX_BUFFER_NAME).invokeExact(_handle.allocate(arena), cString(arena, _name), _len);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VERTEX_BUFFER_NAME = 43;
	private static final FunctionDescriptor FD_SET_VERTEX_BUFFER_NAME = FunctionDescriptor.ofVoid(VertexBufferHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Destroy static vertex buffer.
	 * @param _handle Static vertex buffer handle.
	 */
	public static final void destroyVertexBuffer(VertexBufferHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_VERTEX_BUFFER).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_VERTEX_BUFFER = 44;
	private static final FunctionDescriptor FD_DESTROY_VERTEX_BUFFER = FunctionDescriptor.ofVoid(VertexBufferHandle.LAYOUT);
	/**
	 * Create empty dynamic index buffer.
	 * @param _num Number of indices.
	 * @param _flags Buffer creation flags.   - {@code BGFX_BUFFER_NONE} - No flags.   - {@code BGFX_BUFFER_COMPUTE_READ} - Buffer will be read from by compute shader.   - {@code BGFX_BUFFER_COMPUTE_WRITE} - Buffer will be written into by compute shader. When buffer       is created with {@code BGFX_BUFFER_COMPUTE_WRITE} flag it cannot be updated from CPU.   - {@code BGFX_BUFFER_COMPUTE_READ_WRITE} - Buffer will be used for read/write by compute shader.   - {@code BGFX_BUFFER_ALLOW_RESIZE} - Buffer will resize on buffer update if a different amount of       data is passed. If this flag is not specified, and more data is passed on update, the buffer       will be trimmed to fit the existing buffer size. This flag has effect only on dynamic       buffers.   - {@code BGFX_BUFFER_INDEX32} - Buffer is using 32-bit indices. This flag has effect only on       index buffers.
	 * @return Dynamic index buffer handle.
	 */
	public static final DynamicIndexBufferHandle createDynamicIndexBuffer(int _num, short _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return DynamicIndexBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_DYNAMIC_INDEX_BUFFER).invokeExact((SegmentAllocator) arena, _num, _flags));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_DYNAMIC_INDEX_BUFFER = 45;
	private static final FunctionDescriptor FD_CREATE_DYNAMIC_INDEX_BUFFER = FunctionDescriptor.of(DynamicIndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT);
	/**
	 * Create a dynamic index buffer and initialize it.
	 * @param _mem Index buffer data.
	 * @param _flags Buffer creation flags.   - {@code BGFX_BUFFER_NONE} - No flags.   - {@code BGFX_BUFFER_COMPUTE_READ} - Buffer will be read from by compute shader.   - {@code BGFX_BUFFER_COMPUTE_WRITE} - Buffer will be written into by compute shader. When buffer       is created with {@code BGFX_BUFFER_COMPUTE_WRITE} flag it cannot be updated from CPU.   - {@code BGFX_BUFFER_COMPUTE_READ_WRITE} - Buffer will be used for read/write by compute shader.   - {@code BGFX_BUFFER_ALLOW_RESIZE} - Buffer will resize on buffer update if a different amount of       data is passed. If this flag is not specified, and more data is passed on update, the buffer       will be trimmed to fit the existing buffer size. This flag has effect only on dynamic       buffers.   - {@code BGFX_BUFFER_INDEX32} - Buffer is using 32-bit indices. This flag has effect only on       index buffers.
	 * @return Dynamic index buffer handle.
	 */
	public static final DynamicIndexBufferHandle createDynamicIndexBufferMem(Memory _mem, short _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return DynamicIndexBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_DYNAMIC_INDEX_BUFFER_MEM).invokeExact((SegmentAllocator) arena, address(_mem), _flags));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_DYNAMIC_INDEX_BUFFER_MEM = 46;
	private static final FunctionDescriptor FD_CREATE_DYNAMIC_INDEX_BUFFER_MEM = FunctionDescriptor.of(DynamicIndexBufferHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Update dynamic index buffer.
	 * @param _handle Dynamic index buffer handle.
	 * @param _startIndex Start index.
	 * @param _mem Index buffer data.
	 */
	public static final void updateDynamicIndexBuffer(DynamicIndexBufferHandle _handle, int _startIndex, Memory _mem) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_UPDATE_DYNAMIC_INDEX_BUFFER).invokeExact(_handle.allocate(arena), _startIndex, address(_mem));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_UPDATE_DYNAMIC_INDEX_BUFFER = 47;
	private static final FunctionDescriptor FD_UPDATE_DYNAMIC_INDEX_BUFFER = FunctionDescriptor.ofVoid(DynamicIndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
	/**
	 * Destroy dynamic index buffer.
	 * @param _handle Dynamic index buffer handle.
	 */
	public static final void destroyDynamicIndexBuffer(DynamicIndexBufferHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_DYNAMIC_INDEX_BUFFER).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_DYNAMIC_INDEX_BUFFER = 48;
	private static final FunctionDescriptor FD_DESTROY_DYNAMIC_INDEX_BUFFER = FunctionDescriptor.ofVoid(DynamicIndexBufferHandle.LAYOUT);
	/**
	 * Create empty dynamic vertex buffer.
	 * @param _num Number of vertices.
	 * @param _layout Vertex layout.
	 * @param _flags Buffer creation flags.   - {@code BGFX_BUFFER_NONE} - No flags.   - {@code BGFX_BUFFER_COMPUTE_READ} - Buffer will be read from by compute shader.   - {@code BGFX_BUFFER_COMPUTE_WRITE} - Buffer will be written into by compute shader. When buffer       is created with {@code BGFX_BUFFER_COMPUTE_WRITE} flag it cannot be updated from CPU.   - {@code BGFX_BUFFER_COMPUTE_READ_WRITE} - Buffer will be used for read/write by compute shader.   - {@code BGFX_BUFFER_ALLOW_RESIZE} - Buffer will resize on buffer update if a different amount of       data is passed. If this flag is not specified, and more data is passed on update, the buffer       will be trimmed to fit the existing buffer size. This flag has effect only on dynamic       buffers.   - {@code BGFX_BUFFER_INDEX32} - Buffer is using 32-bit indices. This flag has effect only on       index buffers.
	 * @return Dynamic vertex buffer handle.
	 */
	public static final DynamicVertexBufferHandle createDynamicVertexBuffer(int _num, VertexLayout _layout, short _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return DynamicVertexBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_DYNAMIC_VERTEX_BUFFER).invokeExact((SegmentAllocator) arena, _num, address(_layout), _flags));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_DYNAMIC_VERTEX_BUFFER = 49;
	private static final FunctionDescriptor FD_CREATE_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.of(DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Create dynamic vertex buffer and initialize it.
	 * @param _mem Vertex buffer data.
	 * @param _layout Vertex layout.
	 * @param _flags Buffer creation flags.   - {@code BGFX_BUFFER_NONE} - No flags.   - {@code BGFX_BUFFER_COMPUTE_READ} - Buffer will be read from by compute shader.   - {@code BGFX_BUFFER_COMPUTE_WRITE} - Buffer will be written into by compute shader. When buffer       is created with {@code BGFX_BUFFER_COMPUTE_WRITE} flag it cannot be updated from CPU.   - {@code BGFX_BUFFER_COMPUTE_READ_WRITE} - Buffer will be used for read/write by compute shader.   - {@code BGFX_BUFFER_ALLOW_RESIZE} - Buffer will resize on buffer update if a different amount of       data is passed. If this flag is not specified, and more data is passed on update, the buffer       will be trimmed to fit the existing buffer size. This flag has effect only on dynamic       buffers.   - {@code BGFX_BUFFER_INDEX32} - Buffer is using 32-bit indices. This flag has effect only on       index buffers.
	 * @return Dynamic vertex buffer handle.
	 */
	public static final DynamicVertexBufferHandle createDynamicVertexBufferMem(Memory _mem, VertexLayout _layout, short _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return DynamicVertexBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_DYNAMIC_VERTEX_BUFFER_MEM).invokeExact((SegmentAllocator) arena, address(_mem), address(_layout), _flags));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_DYNAMIC_VERTEX_BUFFER_MEM = 50;
	private static final FunctionDescriptor FD_CREATE_DYNAMIC_VERTEX_BUFFER_MEM = FunctionDescriptor.of(DynamicVertexBufferHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Update dynamic vertex buffer.
	 * @param _handle Dynamic vertex buffer handle.
	 * @param _startVertex Start vertex.
	 * @param _mem Vertex buffer data.
	 */
	public static final void updateDynamicVertexBuffer(DynamicVertexBufferHandle _handle, int _startVertex, Memory _mem) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_UPDATE_DYNAMIC_VERTEX_BUFFER).invokeExact(_handle.allocate(arena), _startVertex, address(_mem));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_UPDATE_DYNAMIC_VERTEX_BUFFER = 51;
	private static final FunctionDescriptor FD_UPDATE_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
	/**
	 * Destroy dynamic vertex buffer.
	 * @param _handle Dynamic vertex buffer handle.
	 */
	public static final void destroyDynamicVertexBuffer(DynamicVertexBufferHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_DYNAMIC_VERTEX_BUFFER).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_DYNAMIC_VERTEX_BUFFER = 52;
	private static final FunctionDescriptor FD_DESTROY_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(DynamicVertexBufferHandle.LAYOUT);
	/**
	 * Returns number of requested or maximum available indices.
	 * @param _num Number of required indices.
	 * @param _index32 Set to {@code true} if input indices will be 32-bit.
	 * @return Number of requested or maximum available indices.
	 */
	public static final int getAvailTransientIndexBuffer(int _num, boolean _index32) {
		try {
			return (int) downcallHandle(DC_GET_AVAIL_TRANSIENT_INDEX_BUFFER).invokeExact(_num, _index32);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_AVAIL_TRANSIENT_INDEX_BUFFER = 53;
	private static final FunctionDescriptor FD_GET_AVAIL_TRANSIENT_INDEX_BUFFER = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Returns number of requested or maximum available vertices.
	 * @param _num Number of required vertices.
	 * @param _layout Vertex layout.
	 * @return Number of requested or maximum available vertices.
	 */
	public static final int getAvailTransientVertexBuffer(int _num, VertexLayout _layout) {
		try {
			return (int) downcallHandle(DC_GET_AVAIL_TRANSIENT_VERTEX_BUFFER).invokeExact(_num, address(_layout));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_AVAIL_TRANSIENT_VERTEX_BUFFER = 54;
	private static final FunctionDescriptor FD_GET_AVAIL_TRANSIENT_VERTEX_BUFFER = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
	/**
	 * Returns number of requested or maximum available instance buffer slots.
	 * @param _num Number of required instances.
	 * @param _stride Stride per instance.
	 * @return Number of requested or maximum available instance buffer slots.
	 */
	public static final int getAvailInstanceDataBuffer(int _num, short _stride) {
		try {
			return (int) downcallHandle(DC_GET_AVAIL_INSTANCE_DATA_BUFFER).invokeExact(_num, _stride);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_AVAIL_INSTANCE_DATA_BUFFER = 55;
	private static final FunctionDescriptor FD_GET_AVAIL_INSTANCE_DATA_BUFFER = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT);
	/**
	 * Allocate transient index buffer.
	 * @param _tib TransientIndexBuffer structure will be filled, and will be valid for the duration of frame, and can be reused for multiple draw calls.
	 * @param _num Number of indices to allocate.
	 * @param _index32 Set to {@code true} if input indices will be 32-bit.
	 */
	public static final void allocTransientIndexBuffer(TransientIndexBuffer _tib, int _num, boolean _index32) {
		try {
			downcallHandle(DC_ALLOC_TRANSIENT_INDEX_BUFFER).invokeExact(address(_tib), _num, _index32);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_ALLOC_TRANSIENT_INDEX_BUFFER = 56;
	private static final FunctionDescriptor FD_ALLOC_TRANSIENT_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Allocate transient vertex buffer.
	 * @param _tvb TransientVertexBuffer structure will be filled, and will be valid for the duration of frame, and can be reused for multiple draw calls.
	 * @param _num Number of vertices to allocate.
	 * @param _layout Vertex layout.
	 */
	public static final void allocTransientVertexBuffer(TransientVertexBuffer _tvb, int _num, VertexLayout _layout) {
		try {
			downcallHandle(DC_ALLOC_TRANSIENT_VERTEX_BUFFER).invokeExact(address(_tvb), _num, address(_layout));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_ALLOC_TRANSIENT_VERTEX_BUFFER = 57;
	private static final FunctionDescriptor FD_ALLOC_TRANSIENT_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
	/**
	 * Check for required space and allocate transient vertex and index
	 * buffers. If both space requirements are satisfied function returns
	 * true.
	 * @param _tvb TransientVertexBuffer structure will be filled, and will be valid for the duration of frame, and can be reused for multiple draw calls.
	 * @param _layout Vertex layout.
	 * @param _numVertices Number of vertices to allocate.
	 * @param _tib TransientIndexBuffer structure will be filled, and will be valid for the duration of frame, and can be reused for multiple draw calls.
	 * @param _numIndices Number of indices to allocate.
	 * @param _index32 Set to {@code true} if input indices will be 32-bit.
	 * @return the native function result
	 */
	public static final boolean allocTransientBuffers(TransientVertexBuffer _tvb, VertexLayout _layout, int _numVertices, TransientIndexBuffer _tib, int _numIndices, boolean _index32) {
		try {
			return (boolean) downcallHandle(DC_ALLOC_TRANSIENT_BUFFERS).invokeExact(address(_tvb), address(_layout), _numVertices, address(_tib), _numIndices, _index32);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_ALLOC_TRANSIENT_BUFFERS = 58;
	private static final FunctionDescriptor FD_ALLOC_TRANSIENT_BUFFERS = FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Allocate instance data buffer.
	 * @param _idb InstanceDataBuffer structure will be filled, and will be valid for duration of frame, and can be reused for multiple draw calls.
	 * @param _num Number of instances.
	 * @param _stride Instance stride. Must be multiple of 16.
	 */
	public static final void allocInstanceDataBuffer(InstanceDataBuffer _idb, int _num, short _stride) {
		try {
			downcallHandle(DC_ALLOC_INSTANCE_DATA_BUFFER).invokeExact(address(_idb), _num, _stride);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_ALLOC_INSTANCE_DATA_BUFFER = 59;
	private static final FunctionDescriptor FD_ALLOC_INSTANCE_DATA_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT);
	/**
	 * Create draw indirect buffer.
	 * @param _num Number of indirect calls.
	 * @return Indirect buffer handle.
	 */
	public static final IndirectBufferHandle createIndirectBuffer(int _num) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return IndirectBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_INDIRECT_BUFFER).invokeExact((SegmentAllocator) arena, _num));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_INDIRECT_BUFFER = 60;
	private static final FunctionDescriptor FD_CREATE_INDIRECT_BUFFER = FunctionDescriptor.of(IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	/**
	 * Destroy draw indirect buffer.
	 * @param _handle Indirect buffer handle.
	 */
	public static final void destroyIndirectBuffer(IndirectBufferHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_INDIRECT_BUFFER).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_INDIRECT_BUFFER = 61;
	private static final FunctionDescriptor FD_DESTROY_INDIRECT_BUFFER = FunctionDescriptor.ofVoid(IndirectBufferHandle.LAYOUT);
	/**
	 * Create shader from memory buffer.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Shader binary is obtained by compiling shader offline with shaderc command line tool.
	 * @param _mem Shader binary.
	 * @return Shader handle.
	 */
	public static final ShaderHandle createShader(Memory _mem) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return ShaderHandle.read((MemorySegment) downcallHandle(DC_CREATE_SHADER).invokeExact((SegmentAllocator) arena, address(_mem)));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_SHADER = 62;
	private static final FunctionDescriptor FD_CREATE_SHADER = FunctionDescriptor.of(ShaderHandle.LAYOUT, ValueLayout.ADDRESS);
	/**
	 * Returns the number of uniforms and uniform handles used inside a shader.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Only non-predefined uniforms are returned.
	 * @param _handle Shader handle.
	 * @param _uniforms UniformHandle array where data will be stored.
	 * @param _max Maximum capacity of array.
	 * @return Number of uniforms used by shader.
	 */
	public static final short getShaderUniforms(ShaderHandle _handle, MemorySegment _uniforms, short _max) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return (short) downcallHandle(DC_GET_SHADER_UNIFORMS).invokeExact(_handle.allocate(arena), address(_uniforms), _max);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_SHADER_UNIFORMS = 63;
	private static final FunctionDescriptor FD_GET_SHADER_UNIFORMS = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ShaderHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Set shader debug name.
	 * @param _handle Shader handle.
	 * @param _name Shader name.
	 * @param _len Shader name length (if length is INT32_MAX, it's expected that _name is zero terminated string).
	 */
	public static final void setShaderName(ShaderHandle _handle, String _name, int _len) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_SHADER_NAME).invokeExact(_handle.allocate(arena), cString(arena, _name), _len);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_SHADER_NAME = 64;
	private static final FunctionDescriptor FD_SET_SHADER_NAME = FunctionDescriptor.ofVoid(ShaderHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Destroy shader.
	 * <p>
	 * <strong>Remarks:</strong> Once a shader program is created with _handle,
	 *   it is safe to destroy that shader.
	 * @param _handle Shader handle.
	 */
	public static final void destroyShader(ShaderHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_SHADER).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_SHADER = 65;
	private static final FunctionDescriptor FD_DESTROY_SHADER = FunctionDescriptor.ofVoid(ShaderHandle.LAYOUT);
	/**
	 * Create program with vertex and fragment shaders.
	 * @param _vsh Vertex shader.
	 * @param _fsh Fragment shader.
	 * @param _destroyShaders If true, shaders will be destroyed when program is destroyed.
	 * @return Program handle if vertex shader output and fragment shader input are matching, otherwise returns invalid program handle.
	 */
	public static final ProgramHandle createProgram(ShaderHandle _vsh, ShaderHandle _fsh, boolean _destroyShaders) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return ProgramHandle.read((MemorySegment) downcallHandle(DC_CREATE_PROGRAM).invokeExact((SegmentAllocator) arena, _vsh.allocate(arena), _fsh.allocate(arena), _destroyShaders));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_PROGRAM = 66;
	private static final FunctionDescriptor FD_CREATE_PROGRAM = FunctionDescriptor.of(ProgramHandle.LAYOUT, ShaderHandle.LAYOUT, ShaderHandle.LAYOUT, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Create program with compute shader.
	 * @param _csh Compute shader.
	 * @param _destroyShaders If true, shaders will be destroyed when program is destroyed.
	 * @return Program handle.
	 */
	public static final ProgramHandle createComputeProgram(ShaderHandle _csh, boolean _destroyShaders) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return ProgramHandle.read((MemorySegment) downcallHandle(DC_CREATE_COMPUTE_PROGRAM).invokeExact((SegmentAllocator) arena, _csh.allocate(arena), _destroyShaders));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_COMPUTE_PROGRAM = 67;
	private static final FunctionDescriptor FD_CREATE_COMPUTE_PROGRAM = FunctionDescriptor.of(ProgramHandle.LAYOUT, ShaderHandle.LAYOUT, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Destroy program.
	 * @param _handle Program handle.
	 */
	public static final void destroyProgram(ProgramHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_PROGRAM).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_PROGRAM = 68;
	private static final FunctionDescriptor FD_DESTROY_PROGRAM = FunctionDescriptor.ofVoid(ProgramHandle.LAYOUT);
	/**
	 * Validate texture parameters.
	 * @param _depth Depth dimension of volume texture.
	 * @param _cubeMap Indicates that texture contains cubemap.
	 * @param _numLayers Number of layers in texture array.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 * @param _flags Texture flags. See {@code BGFX_TEXTURE_*}.
	 * @return True if a texture with the same parameters can be created.
	 */
	public static final boolean isTextureValid(short _depth, boolean _cubeMap, short _numLayers, TextureFormat _format, long _flags) {
		try {
			return (boolean) downcallHandle(DC_IS_TEXTURE_VALID).invokeExact(_depth, _cubeMap, _numLayers, _format.ordinal(), _flags);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_IS_TEXTURE_VALID = 69;
	private static final FunctionDescriptor FD_IS_TEXTURE_VALID = FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
	/**
	 * Validate video codec parameters. Use to check whether the requested
	 * combination of codec / bit depth / chroma / dimensions / DPB layout can
	 * be hardware decoded on the current device. Coarse capability discovery
	 * is {@code Caps.supported &amp; BGFX_CAPS_VIDEO_DECODE} and {@code Caps.codecs[]}.
	 * @param _codec Video codec. See: {@code VideoCodec}.
	 * @param _chroma Chroma subsampling. 0 = 4:2:0, 2 = 4:2:2, 4 = 4:4:4.
	 * @param _bitDepth Bit depth per component. 8, 10 or 12.
	 * @param _codedWidth Coded picture width (macroblock / CTU / superblock aligned).
	 * @param _codedHeight Coded picture height.
	 * @param _maxDpbSlots Maximum decoded picture buffer slot count.
	 * @param _maxActiveReferences Maximum number of reference frames active at once.
	 * @return True if a video decoder with the same parameters can be created.
	 */
	public static final boolean isVideoCodecValid(VideoCodec _codec, byte _chroma, byte _bitDepth, short _codedWidth, short _codedHeight, byte _maxDpbSlots, byte _maxActiveReferences) {
		try {
			return (boolean) downcallHandle(DC_IS_VIDEO_CODEC_VALID).invokeExact(_codec.ordinal(), _chroma, _bitDepth, _codedWidth, _codedHeight, _maxDpbSlots, _maxActiveReferences);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_IS_VIDEO_CODEC_VALID = 70;
	private static final FunctionDescriptor FD_IS_VIDEO_CODEC_VALID = FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE);
	/**
	 * Validate frame buffer parameters.
	 * @param _num Number of attachments.
	 * @param _attachment Attachment texture info. See: {@code Attachment}.
	 * @return True if a frame buffer with the same parameters can be created.
	 */
	public static final boolean isFrameBufferValid(byte _num, Attachment _attachment) {
		try {
			return (boolean) downcallHandle(DC_IS_FRAME_BUFFER_VALID).invokeExact(_num, address(_attachment));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_IS_FRAME_BUFFER_VALID = 71;
	private static final FunctionDescriptor FD_IS_FRAME_BUFFER_VALID = FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS);
	/**
	 * Calculate amount of memory required for texture.
	 * @param _info Resulting texture info structure. See: {@code TextureInfo}.
	 * @param _width Width.
	 * @param _height Height.
	 * @param _depth Depth dimension of volume texture.
	 * @param _cubeMap Indicates that texture contains cubemap.
	 * @param _hasMips Indicates that texture contains full mip-map chain.
	 * @param _numLayers Number of layers in texture array.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 */
	public static final void calcTextureSize(TextureInfo _info, short _width, short _height, short _depth, boolean _cubeMap, boolean _hasMips, short _numLayers, TextureFormat _format) {
		try {
			downcallHandle(DC_CALC_TEXTURE_SIZE).invokeExact(address(_info), _width, _height, _depth, _cubeMap, _hasMips, _numLayers, _format.ordinal());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CALC_TEXTURE_SIZE = 72;
	private static final FunctionDescriptor FD_CALC_TEXTURE_SIZE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT);
	/**
	 * Create texture from memory buffer.
	 * @param _mem DDS, KTX or PVR texture binary data.
	 * @param _flags Texture creation (see {@code BGFX_TEXTURE_*}.), and sampler (see {@code BGFX_SAMPLER_*}) flags. Default texture sampling mode is linear, and wrap mode is repeat. - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap   mode. - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic   sampling.
	 * @param _skip Skip top level mips when parsing texture.
	 * @param _info When non-{@code NULL} is specified it returns parsed texture information.
	 * @return Texture handle.
	 */
	public static final TextureHandle createTexture(Memory _mem, long _flags, byte _skip, TextureInfo _info) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return TextureHandle.read((MemorySegment) downcallHandle(DC_CREATE_TEXTURE).invokeExact((SegmentAllocator) arena, address(_mem), _flags, _skip, address(_info)));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_TEXTURE = 73;
	private static final FunctionDescriptor FD_CREATE_TEXTURE = FunctionDescriptor.of(TextureHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS);
	/**
	 * Create 2D texture.
	 * @param _width Width.
	 * @param _height Height.
	 * @param _hasMips Indicates that texture contains full mip-map chain.
	 * @param _numLayers Number of layers in texture array. Must be 1 if caps {@code BGFX_CAPS_TEXTURE_2D_ARRAY} flag is not set.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 * @param _flags Texture creation (see {@code BGFX_TEXTURE_*}.), and sampler (see {@code BGFX_SAMPLER_*}) flags. Default texture sampling mode is linear, and wrap mode is repeat. - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap   mode. - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic   sampling.
	 * @param _mem Texture data. If {@code _mem} is non-NULL, created texture will be immutable. If {@code _mem} is NULL content of the texture is uninitialized. When {@code _numLayers} is more than 1, expected memory layout is texture and all mips together for each array element.
	 * @param _external Native API pointer to texture.
	 * @return Texture handle.
	 */
	public static final TextureHandle createTexture2D(short _width, short _height, boolean _hasMips, short _numLayers, TextureFormat _format, long _flags, Memory _mem, long _external) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return TextureHandle.read((MemorySegment) downcallHandle(DC_CREATE_TEXTURE_2D).invokeExact((SegmentAllocator) arena, _width, _height, _hasMips, _numLayers, _format.ordinal(), _flags, address(_mem), _external));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_TEXTURE_2D = 74;
	private static final FunctionDescriptor FD_CREATE_TEXTURE_2D = FunctionDescriptor.of(TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG);
	/**
	 * Create texture with size based on back-buffer ratio. Texture will maintain ratio
	 * if back buffer resolution changes.
	 * @param _ratio Texture size in respect to back-buffer size. See: {@code BackbufferRatio}.
	 * @param _hasMips Indicates that texture contains full mip-map chain.
	 * @param _numLayers Number of layers in texture array. Must be 1 if caps {@code BGFX_CAPS_TEXTURE_2D_ARRAY} flag is not set.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 * @param _flags Texture creation (see {@code BGFX_TEXTURE_*}.), and sampler (see {@code BGFX_SAMPLER_*}) flags. Default texture sampling mode is linear, and wrap mode is repeat. - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap   mode. - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic   sampling.
	 * @return Texture handle.
	 */
	public static final TextureHandle createTexture2DScaled(BackbufferRatio _ratio, boolean _hasMips, short _numLayers, TextureFormat _format, long _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return TextureHandle.read((MemorySegment) downcallHandle(DC_CREATE_TEXTURE_2D_SCALED).invokeExact((SegmentAllocator) arena, _ratio.ordinal(), _hasMips, _numLayers, _format.ordinal(), _flags));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_TEXTURE_2D_SCALED = 75;
	private static final FunctionDescriptor FD_CREATE_TEXTURE_2D_SCALED = FunctionDescriptor.of(TextureHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
	/**
	 * Create 3D texture.
	 * @param _width Width.
	 * @param _height Height.
	 * @param _depth Depth.
	 * @param _hasMips Indicates that texture contains full mip-map chain.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 * @param _flags Texture creation (see {@code BGFX_TEXTURE_*}.), and sampler (see {@code BGFX_SAMPLER_*}) flags. Default texture sampling mode is linear, and wrap mode is repeat. - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap   mode. - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic   sampling.
	 * @param _mem Texture data. If {@code _mem} is non-NULL, created texture will be immutable. If {@code _mem} is NULL content of the texture is uninitialized. When {@code _numLayers} is more than 1, expected memory layout is texture and all mips together for each array element.
	 * @param _external Native API pointer to texture.
	 * @return Texture handle.
	 */
	public static final TextureHandle createTexture3D(short _width, short _height, short _depth, boolean _hasMips, TextureFormat _format, long _flags, Memory _mem, long _external) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return TextureHandle.read((MemorySegment) downcallHandle(DC_CREATE_TEXTURE_3D).invokeExact((SegmentAllocator) arena, _width, _height, _depth, _hasMips, _format.ordinal(), _flags, address(_mem), _external));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_TEXTURE_3D = 76;
	private static final FunctionDescriptor FD_CREATE_TEXTURE_3D = FunctionDescriptor.of(TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG);
	/**
	 * Create Cube texture.
	 * @param _size Cube side size.
	 * @param _hasMips Indicates that texture contains full mip-map chain.
	 * @param _numLayers Number of layers in texture array. Must be 1 if caps {@code BGFX_CAPS_TEXTURE_2D_ARRAY} flag is not set.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 * @param _flags Texture creation (see {@code BGFX_TEXTURE_*}.), and sampler (see {@code BGFX_SAMPLER_*}) flags. Default texture sampling mode is linear, and wrap mode is repeat. - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap   mode. - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic   sampling.
	 * @param _mem Texture data. If {@code _mem} is non-NULL, created texture will be immutable. If {@code _mem} is NULL content of the texture is uninitialized. When {@code _numLayers} is more than
	 * @param _external Native API pointer to texture.
	 * @return Texture handle.
	 */
	public static final TextureHandle createTextureCube(short _size, boolean _hasMips, short _numLayers, TextureFormat _format, long _flags, Memory _mem, long _external) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return TextureHandle.read((MemorySegment) downcallHandle(DC_CREATE_TEXTURE_CUBE).invokeExact((SegmentAllocator) arena, _size, _hasMips, _numLayers, _format.ordinal(), _flags, address(_mem), _external));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_TEXTURE_CUBE = 77;
	private static final FunctionDescriptor FD_CREATE_TEXTURE_CUBE = FunctionDescriptor.of(TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG);
	/**
	 * Update 2D texture.
	 * <p>
	 * <strong>Attention:</strong> It's valid to update only mutable texture. See {@code createTexture2D} for more info.
	 * @param _handle Texture handle.
	 * @param _layer Layer in texture array.
	 * @param _mip Mip level.
	 * @param _x X offset in texture.
	 * @param _y Y offset in texture.
	 * @param _width Width of texture block.
	 * @param _height Height of texture block.
	 * @param _mem Texture update data.
	 * @param _pitch Pitch of input image (bytes). When _pitch is set to UINT16_MAX, it will be calculated internally based on _width.
	 */
	public static final void updateTexture2D(TextureHandle _handle, short _layer, byte _mip, short _x, short _y, short _width, short _height, Memory _mem, short _pitch) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_UPDATE_TEXTURE_2D).invokeExact(_handle.allocate(arena), _layer, _mip, _x, _y, _width, _height, address(_mem), _pitch);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_UPDATE_TEXTURE_2D = 78;
	private static final FunctionDescriptor FD_UPDATE_TEXTURE_2D = FunctionDescriptor.ofVoid(TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Update 3D texture.
	 * <p>
	 * <strong>Attention:</strong> It's valid to update only mutable texture. See {@code createTexture3D} for more info.
	 * @param _handle Texture handle.
	 * @param _mip Mip level.
	 * @param _x X offset in texture.
	 * @param _y Y offset in texture.
	 * @param _z Z offset in texture.
	 * @param _width Width of texture block.
	 * @param _height Height of texture block.
	 * @param _depth Depth of texture block.
	 * @param _mem Texture update data.
	 */
	public static final void updateTexture3D(TextureHandle _handle, byte _mip, short _x, short _y, short _z, short _width, short _height, short _depth, Memory _mem) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_UPDATE_TEXTURE_3D).invokeExact(_handle.allocate(arena), _mip, _x, _y, _z, _width, _height, _depth, address(_mem));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_UPDATE_TEXTURE_3D = 79;
	private static final FunctionDescriptor FD_UPDATE_TEXTURE_3D = FunctionDescriptor.ofVoid(TextureHandle.LAYOUT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS);
	/**
	 * Update Cube texture.
	 * <p>
	 * <strong>Attention:</strong> It's valid to update only mutable texture. See {@code createTextureCube} for more info.
	 * @param _handle Texture handle.
	 * @param _layer Layer in texture array.
	 * @param _side Cubemap side {@code BGFX_CUBE_MAP_&lt;POSITIVE or NEGATIVE&gt;_&lt;X, Y or Z&gt;},   where 0 is +X, 1 is -X, 2 is +Y, 3 is -Y, 4 is +Z, and 5 is -Z.                  +----------+                  |-z       2|                  | ^  +y    |                  | |        |    Unfolded cube:                  | +----&gt;+x |       +----------+----------+----------+----------+       |+y       1|+y       4|+y       0|+y       5|       | ^  -x    | ^  +z    | ^  +x    | ^  -z    |       | |        | |        | |        | |        |       | +----&gt;+z | +----&gt;+x | +----&gt;-z | +----&gt;-x |       +----------+----------+----------+----------+                  |+z       3|                  | ^  -y    |                  | |        |                  | +----&gt;+x |                  +----------+
	 * @param _mip Mip level.
	 * @param _x X offset in texture.
	 * @param _y Y offset in texture.
	 * @param _width Width of texture block.
	 * @param _height Height of texture block.
	 * @param _mem Texture update data.
	 * @param _pitch Pitch of input image (bytes). When _pitch is set to UINT16_MAX, it will be calculated internally based on _width.
	 */
	public static final void updateTextureCube(TextureHandle _handle, short _layer, byte _side, byte _mip, short _x, short _y, short _width, short _height, Memory _mem, short _pitch) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_UPDATE_TEXTURE_CUBE).invokeExact(_handle.allocate(arena), _layer, _side, _mip, _x, _y, _width, _height, address(_mem), _pitch);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_UPDATE_TEXTURE_CUBE = 80;
	private static final FunctionDescriptor FD_UPDATE_TEXTURE_CUBE = FunctionDescriptor.ofVoid(TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Clear a texture subresource range to zero.
	 * @param _handle Texture handle.
	 * @param _mip First mip level.
	 * @param _numMips Number of mip levels.
	 * @param _layer First array layer (or 3D depth slice base).
	 * @param _numLayers Number of layers.
	 */
	public static final void clearTexture(TextureHandle _handle, byte _mip, byte _numMips, short _layer, short _numLayers) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_CLEAR_TEXTURE).invokeExact(_handle.allocate(arena), _mip, _numMips, _layer, _numLayers);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CLEAR_TEXTURE = 81;
	private static final FunctionDescriptor FD_CLEAR_TEXTURE = FunctionDescriptor.ofVoid(TextureHandle.LAYOUT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT);
	/**
	 * Read back texture content.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Read back is asynchronous, and the result is available at the returned frame.
	 *   {@code TextureRegion.z} selects cube face, 3D slice, or array layer. The region must
	 *   cover the whole mip.
	 * <p>
	 *   Read back is not intended to be used in the main render loop, since it stalls
	 *   the GPU.
	 * <p>
	 * <strong>Attention:</strong> Texture must be created with {@code BGFX_TEXTURE_READ_BACK} flag.
	 *            It's a texture for CPU readback, and can't be a GPU resource
	 *            at the same time. See {@code examples/30-picking}.
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_TEXTURE_READ_BACK}.
	 * @param _src Source texture region.
	 * @param _data Destination buffer.
	 * @return Frame number when the result will be available. See: {@code frame}.
	 */
	public static final int readTexture(TextureRegion _src, MemorySegment _data) {
		try {
			return (int) downcallHandle(DC_READ_TEXTURE).invokeExact(address(_src), address(_data));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_READ_TEXTURE = 82;
	private static final FunctionDescriptor FD_READ_TEXTURE = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	/**
	 * Set texture debug name.
	 * @param _handle Texture handle.
	 * @param _name Texture name.
	 * @param _len Texture name length (if length is INT32_MAX, it's expected that _name is zero terminated string.
	 */
	public static final void setTextureName(TextureHandle _handle, String _name, int _len) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_TEXTURE_NAME).invokeExact(_handle.allocate(arena), cString(arena, _name), _len);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_TEXTURE_NAME = 83;
	private static final FunctionDescriptor FD_SET_TEXTURE_NAME = FunctionDescriptor.ofVoid(TextureHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Returns texture direct access pointer.
	 * <p>
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_TEXTURE_DIRECT_ACCESS}. This feature
	 *   is available on GPUs that have unified memory architecture (UMA) support.
	 * @param _handle Texture handle.
	 * @return Pointer to texture memory. If returned pointer is {@code NULL} direct access is not available for this texture. If pointer is {@code UINTPTR_MAX} sentinel value it means texture is pending creation. Pointer returned can be cached and it will be valid until texture is destroyed.
	 */
	public static final MemorySegment getDirectAccessPtr(TextureHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return (MemorySegment) downcallHandle(DC_GET_DIRECT_ACCESS_PTR).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_DIRECT_ACCESS_PTR = 84;
	private static final FunctionDescriptor FD_GET_DIRECT_ACCESS_PTR = FunctionDescriptor.of(ValueLayout.ADDRESS, TextureHandle.LAYOUT);
	/**
	 * Destroy texture.
	 * @param _handle Texture handle.
	 */
	public static final void destroyTexture(TextureHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_TEXTURE).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_TEXTURE = 85;
	private static final FunctionDescriptor FD_DESTROY_TEXTURE = FunctionDescriptor.ofVoid(TextureHandle.LAYOUT);
	/**
	 * Create frame buffer (simple).
	 * @param _width Texture width.
	 * @param _height Texture height.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 * @param _textureFlags Texture creation (see {@code BGFX_TEXTURE_*}.), and sampler (see {@code BGFX_SAMPLER_*}) flags. Default texture sampling mode is linear, and wrap mode is repeat. - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap   mode. - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic   sampling.
	 * @return Frame buffer handle.
	 */
	public static final FrameBufferHandle createFrameBuffer(short _width, short _height, TextureFormat _format, long _textureFlags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return FrameBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_FRAME_BUFFER).invokeExact((SegmentAllocator) arena, _width, _height, _format.ordinal(), _textureFlags));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_FRAME_BUFFER = 86;
	private static final FunctionDescriptor FD_CREATE_FRAME_BUFFER = FunctionDescriptor.of(FrameBufferHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
	/**
	 * Create frame buffer with size based on back-buffer ratio. Frame buffer will maintain ratio
	 * if back buffer resolution changes.
	 * @param _ratio Frame buffer size in respect to back-buffer size. See: {@code BackbufferRatio}.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 * @param _textureFlags Texture creation (see {@code BGFX_TEXTURE_*}.), and sampler (see {@code BGFX_SAMPLER_*}) flags. Default texture sampling mode is linear, and wrap mode is repeat. - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap   mode. - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic   sampling.
	 * @return Frame buffer handle.
	 */
	public static final FrameBufferHandle createFrameBufferScaled(BackbufferRatio _ratio, TextureFormat _format, long _textureFlags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return FrameBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_FRAME_BUFFER_SCALED).invokeExact((SegmentAllocator) arena, _ratio.ordinal(), _format.ordinal(), _textureFlags));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_FRAME_BUFFER_SCALED = 87;
	private static final FunctionDescriptor FD_CREATE_FRAME_BUFFER_SCALED = FunctionDescriptor.of(FrameBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
	/**
	 * Create MRT frame buffer from texture handles (simple).
	 * @param _num Number of texture handles.
	 * @param _handles Texture attachments.
	 * @param _destroyTexture If true, textures will be destroyed when frame buffer is destroyed.
	 * @return Frame buffer handle.
	 */
	public static final FrameBufferHandle createFrameBufferFromHandles(byte _num, MemorySegment _handles, boolean _destroyTexture) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return FrameBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_FRAME_BUFFER_FROM_HANDLES).invokeExact((SegmentAllocator) arena, _num, address(_handles), _destroyTexture));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_FRAME_BUFFER_FROM_HANDLES = 88;
	private static final FunctionDescriptor FD_CREATE_FRAME_BUFFER_FROM_HANDLES = FunctionDescriptor.of(FrameBufferHandle.LAYOUT, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Create MRT frame buffer from texture handles with specific layer and
	 * mip level.
	 * @param _num Number of attachments.
	 * @param _attachment Attachment texture info. See: {@code Attachment}.
	 * @param _destroyTexture If true, textures will be destroyed when frame buffer is destroyed.
	 * @return Frame buffer handle.
	 */
	public static final FrameBufferHandle createFrameBufferFromAttachment(byte _num, Attachment _attachment, boolean _destroyTexture) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return FrameBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_FRAME_BUFFER_FROM_ATTACHMENT).invokeExact((SegmentAllocator) arena, _num, address(_attachment), _destroyTexture));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_FRAME_BUFFER_FROM_ATTACHMENT = 89;
	private static final FunctionDescriptor FD_CREATE_FRAME_BUFFER_FROM_ATTACHMENT = FunctionDescriptor.of(FrameBufferHandle.LAYOUT, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Create frame buffer for multiple window rendering.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Frame buffer cannot be used for sampling.
	 * <p>
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_SWAP_CHAIN}.
	 * @param _nwh OS' target native window handle.
	 * @param _width Window back buffer width.
	 * @param _height Window back buffer height.
	 * @param _format Window back buffer color format.
	 * @param _depthFormat Window back buffer depth format.
	 * @return Frame buffer handle.
	 */
	public static final FrameBufferHandle createFrameBufferFromNwh(MemorySegment _nwh, short _width, short _height, TextureFormat _format, TextureFormat _depthFormat) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return FrameBufferHandle.read((MemorySegment) downcallHandle(DC_CREATE_FRAME_BUFFER_FROM_NWH).invokeExact((SegmentAllocator) arena, address(_nwh), _width, _height, _format.ordinal(), _depthFormat.ordinal()));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_FRAME_BUFFER_FROM_NWH = 90;
	private static final FunctionDescriptor FD_CREATE_FRAME_BUFFER_FROM_NWH = FunctionDescriptor.of(FrameBufferHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set frame buffer debug name.
	 * @param _handle Frame buffer handle.
	 * @param _name Frame buffer name.
	 * @param _len Frame buffer name length (if length is INT32_MAX, it's expected that _name is zero terminated string.
	 */
	public static final void setFrameBufferName(FrameBufferHandle _handle, String _name, int _len) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_FRAME_BUFFER_NAME).invokeExact(_handle.allocate(arena), cString(arena, _name), _len);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_FRAME_BUFFER_NAME = 91;
	private static final FunctionDescriptor FD_SET_FRAME_BUFFER_NAME = FunctionDescriptor.ofVoid(FrameBufferHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Obtain texture handle of frame buffer attachment.
	 * @param _handle Frame buffer handle.
	 * @param _attachment native function argument
	 * @return the native function result
	 */
	public static final TextureHandle getTexture(FrameBufferHandle _handle, byte _attachment) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return TextureHandle.read((MemorySegment) downcallHandle(DC_GET_TEXTURE).invokeExact((SegmentAllocator) arena, _handle.allocate(arena), _attachment));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_TEXTURE = 92;
	private static final FunctionDescriptor FD_GET_TEXTURE = FunctionDescriptor.of(TextureHandle.LAYOUT, FrameBufferHandle.LAYOUT, ValueLayout.JAVA_BYTE);
	/**
	 * Destroy frame buffer.
	 * @param _handle Frame buffer handle.
	 */
	public static final void destroyFrameBuffer(FrameBufferHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_FRAME_BUFFER).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_FRAME_BUFFER = 93;
	private static final FunctionDescriptor FD_DESTROY_FRAME_BUFFER = FunctionDescriptor.ofVoid(FrameBufferHandle.LAYOUT);
	/**
	 * Create shader uniform parameter.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   1. Uniform names are unique. It's valid to call {@code createUniform}
	 *      multiple times with the same uniform name. The library will always
	 *      return the same handle, but the handle reference count will be
	 *      incremented. This means that the same number of {@code destroyUniform}
	 *      must be called to properly destroy the uniform.
	 * <p>
	 *   2. Predefined uniforms (declared in {@code bgfx_shader.sh}):
	 *      - {@code u_viewRect vec4(x, y, width, height)} - view rectangle for current
	 *        view, in pixels.
	 *      - {@code u_viewTexel vec4(1.0/width, 1.0/height, undef, undef)} - inverse
	 *        width and height
	 *      - {@code u_view mat4} - view matrix
	 *      - {@code u_invView mat4} - inverted view matrix
	 *      - {@code u_proj mat4} - projection matrix
	 *      - {@code u_invProj mat4} - inverted projection matrix
	 *      - {@code u_viewProj mat4} - concatenated view projection matrix
	 *      - {@code u_invViewProj mat4} - concatenated inverted view projection matrix
	 *      - {@code u_model mat4[BGFX_CONFIG_MAX_BONES]} - array of model matrices.
	 *      - {@code u_modelView mat4} - concatenated model view matrix, only first
	 *        model matrix from array is used.
	 *      - {@code u_invModelView mat4} - inverted concatenated model view matrix.
	 *      - {@code u_modelViewProj mat4} - concatenated model view projection matrix.
	 *      - {@code u_alphaRef float} - alpha reference value for alpha test.
	 * @param _name Uniform name in shader.
	 * @param _type Type of uniform (See: {@code UniformType}).
	 * @param _num Number of elements in array.
	 * @return Handle to uniform object.
	 */
	public static final UniformHandle createUniform(String _name, UniformType _type, short _num) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return UniformHandle.read((MemorySegment) downcallHandle(DC_CREATE_UNIFORM).invokeExact((SegmentAllocator) arena, cString(arena, _name), _type.ordinal(), _num));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_UNIFORM = 94;
	private static final FunctionDescriptor FD_CREATE_UNIFORM = FunctionDescriptor.of(UniformHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT);
	/**
	 * Create shader uniform parameter.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   1. Uniform names are unique. It's valid to call {@code createUniform}
	 *      multiple times with the same uniform name. The library will always
	 *      return the same handle, but the handle reference count will be
	 *      incremented. This means that the same number of {@code destroyUniform}
	 *      must be called to properly destroy the uniform.
	 * <p>
	 *   2. Predefined uniforms (declared in {@code bgfx_shader.sh}):
	 *      - {@code u_viewRect vec4(x, y, width, height)} - view rectangle for current
	 *        view, in pixels.
	 *      - {@code u_viewTexel vec4(1.0/width, 1.0/height, undef, undef)} - inverse
	 *        width and height
	 *      - {@code u_view mat4} - view matrix
	 *      - {@code u_invView mat4} - inverted view matrix
	 *      - {@code u_proj mat4} - projection matrix
	 *      - {@code u_invProj mat4} - inverted projection matrix
	 *      - {@code u_viewProj mat4} - concatenated view projection matrix
	 *      - {@code u_invViewProj mat4} - concatenated inverted view projection matrix
	 *      - {@code u_model mat4[BGFX_CONFIG_MAX_BONES]} - array of model matrices.
	 *      - {@code u_modelView mat4} - concatenated model view matrix, only first
	 *        model matrix from array is used.
	 *      - {@code u_invModelView mat4} - inverted concatenated model view matrix.
	 *      - {@code u_modelViewProj mat4} - concatenated model view projection matrix.
	 *      - {@code u_alphaRef float} - alpha reference value for alpha test.
	 * @param _name Uniform name in shader.
	 * @param _freq Uniform change frequency (See: {@code UniformFreq}).
	 * @param _type Type of uniform (See: {@code UniformType}).
	 * @param _num Number of elements in array.
	 * @return Handle to uniform object.
	 */
	public static final UniformHandle createUniformWithFreq(String _name, UniformFreq _freq, UniformType _type, short _num) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return UniformHandle.read((MemorySegment) downcallHandle(DC_CREATE_UNIFORM_WITH_FREQ).invokeExact((SegmentAllocator) arena, cString(arena, _name), _freq.ordinal(), _type.ordinal(), _num));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_UNIFORM_WITH_FREQ = 95;
	private static final FunctionDescriptor FD_CREATE_UNIFORM_WITH_FREQ = FunctionDescriptor.of(UniformHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT);
	/**
	 * Retrieve uniform info.
	 * @param _handle Handle to uniform object.
	 * @param _info Uniform info.
	 */
	public static final void getUniformInfo(UniformHandle _handle, UniformInfo _info) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_GET_UNIFORM_INFO).invokeExact(_handle.allocate(arena), address(_info));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_UNIFORM_INFO = 96;
	private static final FunctionDescriptor FD_GET_UNIFORM_INFO = FunctionDescriptor.ofVoid(UniformHandle.LAYOUT, ValueLayout.ADDRESS);
	/**
	 * Destroy shader uniform parameter.
	 * @param _handle Handle to uniform object.
	 */
	public static final void destroyUniform(UniformHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_UNIFORM).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_UNIFORM = 97;
	private static final FunctionDescriptor FD_DESTROY_UNIFORM = FunctionDescriptor.ofVoid(UniformHandle.LAYOUT);
	/**
	 * Create occlusion query. Occlusion queries allow the GPU to determine
	 * if any pixels passed the depth test.
	 * @return Handle to occlusion query object.
	 */
	public static final OcclusionQueryHandle createOcclusionQuery() {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return OcclusionQueryHandle.read((MemorySegment) downcallHandle(DC_CREATE_OCCLUSION_QUERY).invokeExact((SegmentAllocator) arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_CREATE_OCCLUSION_QUERY = 98;
	private static final FunctionDescriptor FD_CREATE_OCCLUSION_QUERY = FunctionDescriptor.of(OcclusionQueryHandle.LAYOUT);
	/**
	 * Retrieve occlusion query result from previous frame.
	 * @param _handle Handle to occlusion query object.
	 * @param _result Number of pixels that passed test. This argument can be {@code NULL} if result of occlusion query is not needed.
	 * @return Occlusion query result.
	 */
	public static final OcclusionQueryResult getResult(OcclusionQueryHandle _handle, MemorySegment _result) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				return OcclusionQueryResult.fromValue((int) downcallHandle(DC_GET_RESULT).invokeExact(_handle.allocate(arena), address(_result)));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_RESULT = 99;
	private static final FunctionDescriptor FD_GET_RESULT = FunctionDescriptor.of(ValueLayout.JAVA_INT, OcclusionQueryHandle.LAYOUT, ValueLayout.ADDRESS);
	/**
	 * Destroy occlusion query.
	 * @param _handle Handle to occlusion query object.
	 */
	public static final void destroyOcclusionQuery(OcclusionQueryHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DESTROY_OCCLUSION_QUERY).invokeExact(_handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DESTROY_OCCLUSION_QUERY = 100;
	private static final FunctionDescriptor FD_DESTROY_OCCLUSION_QUERY = FunctionDescriptor.ofVoid(OcclusionQueryHandle.LAYOUT);
	/**
	 * Set palette color value.
	 * @param _index Index into palette.
	 * @param _rgba RGBA floating point values.
	 */
	public static final void setPaletteColor(byte _index, MemorySegment _rgba) {
		try {
			downcallHandle(DC_SET_PALETTE_COLOR).invokeExact(_index, address(_rgba));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_PALETTE_COLOR = 101;
	private static final FunctionDescriptor FD_SET_PALETTE_COLOR = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS);
	/**
	 * Set palette color value.
	 * @param _index Index into palette.
	 * @param _r Red value (RGBA floating point values)
	 * @param _g Green value (RGBA floating point values)
	 * @param _b Blue value (RGBA floating point values)
	 * @param _a Alpha value (RGBA floating point values)
	 */
	public static final void setPaletteColorRgba32f(byte _index, float _r, float _g, float _b, float _a) {
		try {
			downcallHandle(DC_SET_PALETTE_COLOR_RGBA32F).invokeExact(_index, _r, _g, _b, _a);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_PALETTE_COLOR_RGBA32F = 102;
	private static final FunctionDescriptor FD_SET_PALETTE_COLOR_RGBA32F = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT);
	/**
	 * Set palette color value.
	 * @param _index Index into palette.
	 * @param _rgba Packed 32-bit RGBA value.
	 */
	public static final void setPaletteColorRgba8(byte _index, int _rgba) {
		try {
			downcallHandle(DC_SET_PALETTE_COLOR_RGBA8).invokeExact(_index, _rgba);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_PALETTE_COLOR_RGBA8 = 103;
	private static final FunctionDescriptor FD_SET_PALETTE_COLOR_RGBA8 = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT);
	/**
	 * Set view name.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   This is debug only feature.
	 * <p>
	 *   In graphics debugger view name will appear as:
	 * <p>
	 *       "nnnc &lt;view name&gt;"
	 *        ^  ^ ^
	 *        |  +--- compute (C)
	 *        +------ view id
	 * @param _id View id.
	 * @param _name View name.
	 * @param _len View name length (if length is INT32_MAX, it's expected that _name is zero terminated string.
	 */
	public static final void setViewName(short _id, String _name, int _len) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_VIEW_NAME).invokeExact(_id, cString(arena, _name), _len);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_NAME = 104;
	private static final FunctionDescriptor FD_SET_VIEW_NAME = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Set view rectangle. Draw primitive outside view will be clipped.
	 * @param _id View id.
	 * @param _x Position x from the left corner of the window. Can be negative to place view origin outside of the window.
	 * @param _y Position y from the top corner of the window. Can be negative to place view origin outside of the window.
	 * @param _width Width of view port region.
	 * @param _height Height of view port region.
	 */
	public static final void setViewRect(short _id, short _x, short _y, short _width, short _height) {
		try {
			downcallHandle(DC_SET_VIEW_RECT).invokeExact(_id, _x, _y, _width, _height);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_RECT = 105;
	private static final FunctionDescriptor FD_SET_VIEW_RECT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT);
	/**
	 * Set view rectangle. Draw primitive outside view will be clipped.
	 * @param _id View id.
	 * @param _x Position x from the left corner of the window. Can be negative to place view origin outside of the window.
	 * @param _y Position y from the top corner of the window. Can be negative to place view origin outside of the window.
	 * @param _ratio Width and height will be set in respect to back-buffer size. See: {@code BackbufferRatio}.
	 */
	public static final void setViewRectRatio(short _id, short _x, short _y, BackbufferRatio _ratio) {
		try {
			downcallHandle(DC_SET_VIEW_RECT_RATIO).invokeExact(_id, _x, _y, _ratio.ordinal());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_RECT_RATIO = 106;
	private static final FunctionDescriptor FD_SET_VIEW_RECT_RATIO = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT);
	/**
	 * Set view scissor. Draw primitive outside view will be clipped. When
	 * _x, _y, _width and _height are set to 0, scissor will be disabled.
	 * @param _id View id.
	 * @param _x Position x from the left corner of the window.
	 * @param _y Position y from the top corner of the window.
	 * @param _width Width of view scissor region.
	 * @param _height Height of view scissor region.
	 */
	public static final void setViewScissor(short _id, short _x, short _y, short _width, short _height) {
		try {
			downcallHandle(DC_SET_VIEW_SCISSOR).invokeExact(_id, _x, _y, _width, _height);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_SCISSOR = 107;
	private static final FunctionDescriptor FD_SET_VIEW_SCISSOR = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT);
	/**
	 * Set view clear flags.
	 * @param _id View id.
	 * @param _flags Clear flags. Use {@code BGFX_CLEAR_NONE} to remove any clear operation. See: {@code BGFX_CLEAR_*}.
	 * @param _rgba Color clear value.
	 * @param _depth Depth clear value.
	 * @param _stencil Stencil clear value.
	 */
	public static final void setViewClear(short _id, short _flags, int _rgba, float _depth, byte _stencil) {
		try {
			downcallHandle(DC_SET_VIEW_CLEAR).invokeExact(_id, _flags, _rgba, _depth, _stencil);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_CLEAR = 108;
	private static final FunctionDescriptor FD_SET_VIEW_CLEAR = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_BYTE);
	/**
	 * Set view clear flags with different clear color for each
	 * frame buffer texture. {@code setPaletteColor} must be used to set up a
	 * clear color palette.
	 * @param _id View id.
	 * @param _flags Clear flags. Use {@code BGFX_CLEAR_NONE} to remove any clear operation. See: {@code BGFX_CLEAR_*}.
	 * @param _depth Depth clear value.
	 * @param _stencil Stencil clear value.
	 * @param _c0 Palette index for frame buffer attachment 0.
	 * @param _c1 Palette index for frame buffer attachment 1.
	 * @param _c2 Palette index for frame buffer attachment 2.
	 * @param _c3 Palette index for frame buffer attachment 3.
	 * @param _c4 Palette index for frame buffer attachment 4.
	 * @param _c5 Palette index for frame buffer attachment 5.
	 * @param _c6 Palette index for frame buffer attachment 6.
	 * @param _c7 Palette index for frame buffer attachment 7.
	 */
	public static final void setViewClearMrt(short _id, short _flags, float _depth, byte _stencil, byte _c0, byte _c1, byte _c2, byte _c3, byte _c4, byte _c5, byte _c6, byte _c7) {
		try {
			downcallHandle(DC_SET_VIEW_CLEAR_MRT).invokeExact(_id, _flags, _depth, _stencil, _c0, _c1, _c2, _c3, _c4, _c5, _c6, _c7);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_CLEAR_MRT = 109;
	private static final FunctionDescriptor FD_SET_VIEW_CLEAR_MRT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE);
	/**
	 * Set view sorting mode.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   View mode must be set prior calling {@code submit} for the view.
	 * @param _id View id.
	 * @param _mode View sort mode. See {@code ViewMode}.
	 */
	public static final void setViewMode(short _id, ViewMode _mode) {
		try {
			downcallHandle(DC_SET_VIEW_MODE).invokeExact(_id, _mode.ordinal());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_MODE = 110;
	private static final FunctionDescriptor FD_SET_VIEW_MODE = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT);
	/**
	 * Set view frame buffer.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Not persistent after {@code reset} call.
	 * @param _id View id.
	 * @param _handle Frame buffer handle. Passing {@code BGFX_INVALID_HANDLE} as frame buffer handle will draw primitives from this view into default back buffer.
	 */
	public static final void setViewFrameBuffer(short _id, FrameBufferHandle _handle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_VIEW_FRAME_BUFFER).invokeExact(_id, _handle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_FRAME_BUFFER = 111;
	private static final FunctionDescriptor FD_SET_VIEW_FRAME_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, FrameBufferHandle.LAYOUT);
	/**
	 * Set view's view matrix and projection matrix,
	 * all draw primitives in this view will use these two matrices.
	 * @param _id View id.
	 * @param _view View matrix.
	 * @param _proj Projection matrix.
	 */
	public static final void setViewTransform(short _id, MemorySegment _view, MemorySegment _proj) {
		try {
			downcallHandle(DC_SET_VIEW_TRANSFORM).invokeExact(_id, address(_view), address(_proj));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_TRANSFORM = 112;
	private static final FunctionDescriptor FD_SET_VIEW_TRANSFORM = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	/**
	 * Post submit view reordering.
	 * @param _id First view id.
	 * @param _num Number of views to remap.
	 * @param _order View remap id table. Passing {@code NULL} will reset view ids to default state.
	 */
	public static final void setViewOrder(short _id, short _num, MemorySegment _order) {
		try {
			downcallHandle(DC_SET_VIEW_ORDER).invokeExact(_id, _num, address(_order));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_ORDER = 113;
	private static final FunctionDescriptor FD_SET_VIEW_ORDER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS);
	/**
	 * Set view shading rate.
	 * <p>
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_VARIABLE_RATE_SHADING}.
	 * @param _id View id.
	 * @param _shadingRate Shading rate.
	 */
	public static final void setViewShadingRate(short _id, ShadingRate _shadingRate) {
		try {
			downcallHandle(DC_SET_VIEW_SHADING_RATE).invokeExact(_id, _shadingRate.ordinal());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_SHADING_RATE = 114;
	private static final FunctionDescriptor FD_SET_VIEW_SHADING_RATE = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT);
	/**
	 * Reset all view settings to default.
	 * @param _id _id View id.
	 */
	public static final void resetView(short _id) {
		try {
			downcallHandle(DC_RESET_VIEW).invokeExact(_id);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_RESET_VIEW = 115;
	private static final FunctionDescriptor FD_RESET_VIEW = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT);
	/**
	 * Begin submitting draw calls from thread. Obtains an encoder that can be
	 * used to submit draw calls, compute dispatches, and state changes.
	 * <p>
	 * In multithreaded mode ({@code BGFX_CONFIG_MULTITHREADED=1}), multiple threads
	 * can each obtain their own encoder and submit draw calls in parallel.
	 * Each encoder writes into its own uniform buffer, so there is no
	 * contention between threads. The maximum number of simultaneous encoders
	 * is configured via {@code Limits.maxEncoders} in {@code Init} (default: 8).
	 * <p>
	 * When called from the API thread (the thread that called {@code init})
	 * with {@code _forceNewEncoder} set to {@code false}, the default internal encoder
	 * (encoder 0) is returned. This is the same encoder used by the legacy
	 * non-encoder API ({@code setState}, {@code submit}, etc.). When called
	 * from a worker thread (or with {@code _forceNewEncoder} set to {@code true}), a new
	 * encoder is allocated from the encoder pool.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   The returned {@code Encoder} pointer is valid until {@code end} is called
	 *   with it. All encoders must be ended before {@code frame} is called.
	 *   If {@code frame} is called while encoders are still active, it will
	 *   wait for them to finish. Returns {@code NULL} if no encoder slots are
	 *   available (all {@code maxEncoders} slots are in use).
	 *   See also: {@code end}, {@code frame}.
	 * @param _forceNewEncoder Force allocation of a new encoder from the pool, even when called from the API thread.
	 * @return Encoder.
	 */
	public static final Encoder encoderBegin(boolean _forceNewEncoder) {
		try {
			return new Encoder((MemorySegment) downcallHandle(DC_ENCODER_BEGIN).invokeExact(_forceNewEncoder));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_ENCODER_BEGIN = 116;
	private static final FunctionDescriptor FD_ENCODER_BEGIN = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN);
	/**
	 * End submitting draw calls from thread. Returns the encoder obtained from
	 * {@code begin} back to the encoder pool.
	 * <p>
	 * After this call the {@code Encoder} pointer is no longer valid and must not
	 * be used. The encoder's recorded draw calls and state changes are finalized
	 * and will be included in the next frame when {@code frame} is called.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Must be called from the same thread that called {@code begin} for
	 *   this encoder. All encoders must be ended before {@code frame} is
	 *   called. The default encoder (encoder 0, used by the legacy API) is
	 *   managed internally and does not need to be passed to {@code end};
	 *   passing it is harmless but has no effect.
	 *   See also: {@code begin}, {@code frame}.
	 * @param _encoder Encoder.
	 */
	public static final void encoderEnd(Encoder _encoder) {
		try {
			downcallHandle(DC_ENCODER_END).invokeExact(address(_encoder));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_ENCODER_END = 117;
	private static final FunctionDescriptor FD_ENCODER_END = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
	static final int DC_ENCODER_SET_MARKER = 118;
	private static final FunctionDescriptor FD_ENCODER_SET_MARKER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_STATE = 119;
	private static final FunctionDescriptor FD_ENCODER_SET_STATE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_CONDITION = 120;
	private static final FunctionDescriptor FD_ENCODER_SET_CONDITION = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, OcclusionQueryHandle.LAYOUT, ValueLayout.JAVA_BOOLEAN);
	static final int DC_ENCODER_SET_STENCIL = 121;
	private static final FunctionDescriptor FD_ENCODER_SET_STENCIL = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_SCISSOR = 122;
	private static final FunctionDescriptor FD_ENCODER_SET_SCISSOR = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT);
	static final int DC_ENCODER_SET_SCISSOR_CACHED = 123;
	private static final FunctionDescriptor FD_ENCODER_SET_SCISSOR_CACHED = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	static final int DC_ENCODER_SET_TRANSFORM = 124;
	private static final FunctionDescriptor FD_ENCODER_SET_TRANSFORM = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	static final int DC_ENCODER_SET_TRANSFORM_CACHED = 125;
	private static final FunctionDescriptor FD_ENCODER_SET_TRANSFORM_CACHED = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT);
	static final int DC_ENCODER_ALLOC_TRANSFORM = 126;
	private static final FunctionDescriptor FD_ENCODER_ALLOC_TRANSFORM = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	static final int DC_ENCODER_SET_UNIFORM = 127;
	private static final FunctionDescriptor FD_ENCODER_SET_UNIFORM = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, UniformHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Set shader uniform parameter for view.
	 * <p>
	 * <strong>Attention:</strong> Uniform must be created with {@code UniformFreq.VIEW} argument.
	 * @param _id View id.
	 * @param _handle Uniform.
	 * @param _value Pointer to uniform data.
	 * @param _num Number of elements. Passing {@code UINT16_MAX} will use the _num passed on uniform creation.
	 */
	public static final void setViewUniform(short _id, UniformHandle _handle, MemorySegment _value, short _num) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_VIEW_UNIFORM).invokeExact(_id, _handle.allocate(arena), address(_value), _num);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VIEW_UNIFORM = 128;
	private static final FunctionDescriptor FD_SET_VIEW_UNIFORM = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, UniformHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Set shader uniform parameter for frame.
	 * <p>
	 * <strong>Attention:</strong> Uniform must be created with {@code UniformFreq.VIEW} argument.
	 * @param _handle Uniform.
	 * @param _value Pointer to uniform data.
	 * @param _num Number of elements. Passing {@code UINT16_MAX} will use the _num passed on uniform creation.
	 */
	public static final void setFrameUniform(UniformHandle _handle, MemorySegment _value, short _num) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_FRAME_UNIFORM).invokeExact(_handle.allocate(arena), address(_value), _num);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_FRAME_UNIFORM = 129;
	private static final FunctionDescriptor FD_SET_FRAME_UNIFORM = FunctionDescriptor.ofVoid(UniformHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	static final int DC_ENCODER_SET_INDEX_BUFFER = 130;
	private static final FunctionDescriptor FD_ENCODER_SET_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, IndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_DYNAMIC_INDEX_BUFFER = 131;
	private static final FunctionDescriptor FD_ENCODER_SET_DYNAMIC_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, DynamicIndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_TRANSIENT_INDEX_BUFFER = 132;
	private static final FunctionDescriptor FD_ENCODER_SET_TRANSIENT_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_VERTEX_BUFFER = 133;
	private static final FunctionDescriptor FD_ENCODER_SET_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_VERTEX_BUFFER_WITH_LAYOUT = 134;
	private static final FunctionDescriptor FD_ENCODER_SET_VERTEX_BUFFER_WITH_LAYOUT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, VertexLayoutHandle.LAYOUT);
	static final int DC_ENCODER_SET_DYNAMIC_VERTEX_BUFFER = 135;
	private static final FunctionDescriptor FD_ENCODER_SET_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT = 136;
	private static final FunctionDescriptor FD_ENCODER_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, VertexLayoutHandle.LAYOUT);
	static final int DC_ENCODER_SET_TRANSIENT_VERTEX_BUFFER = 137;
	private static final FunctionDescriptor FD_ENCODER_SET_TRANSIENT_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT = 138;
	private static final FunctionDescriptor FD_ENCODER_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, VertexLayoutHandle.LAYOUT);
	static final int DC_ENCODER_SET_VERTEX_COUNT = 139;
	private static final FunctionDescriptor FD_ENCODER_SET_VERTEX_COUNT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_INSTANCE_DATA_BUFFER = 140;
	private static final FunctionDescriptor FD_ENCODER_SET_INSTANCE_DATA_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER = 141;
	private static final FunctionDescriptor FD_ENCODER_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER = 142;
	private static final FunctionDescriptor FD_ENCODER_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_INSTANCE_COUNT = 143;
	private static final FunctionDescriptor FD_ENCODER_SET_INSTANCE_COUNT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_TEXTURE = 144;
	private static final FunctionDescriptor FD_ENCODER_SET_TEXTURE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, UniformHandle.LAYOUT, TextureHandle.LAYOUT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_TEXTURE_VIEW = 145;
	private static final FunctionDescriptor FD_ENCODER_SET_TEXTURE_VIEW = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, UniformHandle.LAYOUT, TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_TOUCH = 146;
	private static final FunctionDescriptor FD_ENCODER_TOUCH = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	static final int DC_ENCODER_SUBMIT = 147;
	private static final FunctionDescriptor FD_ENCODER_SUBMIT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	static final int DC_ENCODER_SUBMIT_OCCLUSION_QUERY = 148;
	private static final FunctionDescriptor FD_ENCODER_SUBMIT_OCCLUSION_QUERY = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, OcclusionQueryHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	static final int DC_ENCODER_SUBMIT_INDIRECT = 149;
	private static final FunctionDescriptor FD_ENCODER_SUBMIT_INDIRECT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	static final int DC_ENCODER_SUBMIT_INDIRECT_COUNT = 150;
	private static final FunctionDescriptor FD_ENCODER_SUBMIT_INDIRECT_COUNT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT, IndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	static final int DC_ENCODER_SET_COMPUTE_INDEX_BUFFER = 151;
	private static final FunctionDescriptor FD_ENCODER_SET_COMPUTE_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, IndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_COMPUTE_VERTEX_BUFFER = 152;
	private static final FunctionDescriptor FD_ENCODER_SET_COMPUTE_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_COMPUTE_DYNAMIC_INDEX_BUFFER = 153;
	private static final FunctionDescriptor FD_ENCODER_SET_COMPUTE_DYNAMIC_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, DynamicIndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER = 154;
	private static final FunctionDescriptor FD_ENCODER_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_COMPUTE_INDIRECT_BUFFER = 155;
	private static final FunctionDescriptor FD_ENCODER_SET_COMPUTE_INDIRECT_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_IMAGE = 156;
	private static final FunctionDescriptor FD_ENCODER_SET_IMAGE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, TextureHandle.LAYOUT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_SET_IMAGE_VIEW = 157;
	private static final FunctionDescriptor FD_ENCODER_SET_IMAGE_VIEW = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	static final int DC_ENCODER_DISPATCH = 158;
	private static final FunctionDescriptor FD_ENCODER_DISPATCH = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	static final int DC_ENCODER_DISPATCH_INDIRECT = 159;
	private static final FunctionDescriptor FD_ENCODER_DISPATCH_INDIRECT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	static final int DC_ENCODER_DISCARD = 160;
	private static final FunctionDescriptor FD_ENCODER_DISCARD = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE);
	static final int DC_ENCODER_BLIT = 161;
	private static final FunctionDescriptor FD_ENCODER_BLIT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	static final int DC_ENCODER_BLIT_BUFFER = 162;
	private static final FunctionDescriptor FD_ENCODER_BLIT_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	static final int DC_ENCODER_BLIT_TO_BUFFER = 163;
	private static final FunctionDescriptor FD_ENCODER_BLIT_TO_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	static final int DC_ENCODER_BLIT_FROM_BUFFER = 164;
	private static final FunctionDescriptor FD_ENCODER_BLIT_FROM_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	/**
	 * Request screen shot of window back buffer.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   {@code CallbackI.screenShot} must be implemented.
	 * <strong>Attention:</strong> Frame buffer handle must be created with OS' target native window handle.
	 * @param _handle Frame buffer handle. If handle is {@code BGFX_INVALID_HANDLE} request will be made for main window back buffer.
	 * @param _filePath Will be passed to {@code CallbackI.screenShot} callback.
	 */
	public static final void requestScreenShot(FrameBufferHandle _handle, String _filePath) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_REQUEST_SCREEN_SHOT).invokeExact(_handle.allocate(arena), cString(arena, _filePath));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_REQUEST_SCREEN_SHOT = 165;
	private static final FunctionDescriptor FD_REQUEST_SCREEN_SHOT = FunctionDescriptor.ofVoid(FrameBufferHandle.LAYOUT, ValueLayout.ADDRESS);
	/**
	 * Render frame. Executes the actual GPU rendering work for one frame.
	 * <p>
	 * In the default **multithreaded** configuration, {@code renderFrame} runs
	 * on the **render thread** while {@code frame} runs on the **API thread**.
	 * Their interaction is as follows:
	 * <p>
	 *   1. The render thread calls {@code renderFrame}, which blocks waiting
	 *      for the API thread to signal that a new frame is ready.
	 *   2. On the API thread, {@code frame} finishes building the frame,
	 *      swaps internal submit/render buffers, and signals the render thread.
	 *   3. {@code renderFrame} wakes up, executes pre-render commands,
	 *      submits GPU draw calls, executes post-render commands, flips the
	 *      back buffer, then signals back to the API thread that rendering
	 *      is complete.
	 *   4. The API thread's next {@code frame} call waits for this completion
	 *      signal before swapping buffers again.
	 * <p>
	 * This double-buffered semaphore handshake allows the API thread and
	 * render thread to run in parallel, overlapping CPU frame building with
	 * GPU rendering.
	 * <p>
	 * <strong>Attention:</strong> {@code renderFrame} is a blocking call. It waits for
	 *   {@code frame} to be called from the API thread to process the frame.
	 *   If a timeout value is passed, the call will return
	 *   {@code RenderFrame.TIMEOUT} even if {@code frame} has not been called.
	 *   A value of -1 (default) means wait indefinitely (up to
	 *   {@code BGFX_CONFIG_API_SEMAPHORE_TIMEOUT}).
	 * <p>
	 * <strong>Warning:</strong> This call should only be used on platforms that don't allow
	 *   creating a separate rendering thread. If it is called before
	 *   {@code init}, the internal render thread won't be created by the
	 *   {@code init} call, and the user is responsible for calling
	 *   {@code renderFrame} on the render thread each frame. If both
	 *   {@code renderFrame} and {@code init} are called from the same
	 *   thread, bgfx operates in single-threaded mode and {@code frame}
	 *   will internally invoke {@code renderFrame} automatically.
	 *   See also: {@code frame}.
	 * @param _msecs Timeout in milliseconds.
	 * @return Current renderer context state. See: {@code RenderFrame}.
	 */
	public static final RenderFrame renderFrame(int _msecs) {
		try {
			return RenderFrame.fromValue((int) downcallHandle(DC_RENDER_FRAME).invokeExact(_msecs));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_RENDER_FRAME = 166;
	private static final FunctionDescriptor FD_RENDER_FRAME = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set platform data.
	 * <p>
	 * <strong>Warning:</strong> Must be called before {@code init}.
	 * @param _data Platform data.
	 */
	public static final void setPlatformData(PlatformData _data) {
		try {
			downcallHandle(DC_SET_PLATFORM_DATA).invokeExact(address(_data));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_PLATFORM_DATA = 167;
	private static final FunctionDescriptor FD_SET_PLATFORM_DATA = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
	/**
	 * Get internal data for interop.
	 * <p>
	 * <strong>Attention:</strong> It's expected you understand some bgfx internals before you
	 *   use this call.
	 * <p>
	 * <strong>Warning:</strong> Must be called only on render thread.
	 * @return Internal data.
	 */
	public static final InternalData getInternalData() {
		try {
			return new InternalData((MemorySegment) downcallHandle(DC_GET_INTERNAL_DATA).invokeExact());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_GET_INTERNAL_DATA = 168;
	private static final FunctionDescriptor FD_GET_INTERNAL_DATA = FunctionDescriptor.of(ValueLayout.ADDRESS);
	/**
	 * Override internal texture with externally created texture. Previously
	 * created internal texture will released.
	 * <p>
	 * <strong>Attention:</strong> It's expected you understand some bgfx internals before you
	 *   use this call.
	 * <p>
	 * <strong>Warning:</strong> Must be called only on render thread.
	 * @param _handle Texture handle.
	 * @param _ptr Native API pointer to texture.
	 * @param _layerIndex Layer index for texture arrays (only implemented for D3D11).
	 * @return Native API pointer to texture. If result is 0, texture is not created yet from the main thread.
	 */
	public static final long overrideInternalTexturePtr(TextureHandle _handle, long _ptr, short _layerIndex) {
		try (Arena arena = Arena.ofConfined()) {
			return javaUintptr(invoke(DC_OVERRIDE_INTERNAL_TEXTURE_PTR, _handle.allocate(arena), nativeUintptr(_ptr), _layerIndex));
		}
	}

	static final int DC_OVERRIDE_INTERNAL_TEXTURE_PTR = 169;
	private static final FunctionDescriptor FD_OVERRIDE_INTERNAL_TEXTURE_PTR = FunctionDescriptor.of(C_UINTPTR_T, TextureHandle.LAYOUT, C_UINTPTR_T, ValueLayout.JAVA_SHORT);
	/**
	 * Override internal texture by creating new texture. Previously created
	 * internal texture will released.
	 * <p>
	 * <strong>Attention:</strong> It's expected you understand some bgfx internals before you
	 *   use this call.
	 * <p>
	 * <strong>Returns:</strong> Native API pointer to texture. If result is 0, texture is not created yet from the
	 *   main thread.
	 * <p>
	 * <strong>Warning:</strong> Must be called only on render thread.
	 * @param _handle Texture handle.
	 * @param _width Width.
	 * @param _height Height.
	 * @param _numMips Number of mip-maps.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 * @param _flags Texture creation (see {@code BGFX_TEXTURE_*}.), and sampler (see {@code BGFX_SAMPLER_*}) flags. Default texture sampling mode is linear, and wrap mode is repeat. - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap   mode. - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic   sampling.
	 * @return Native API pointer to texture. If result is 0, texture is not created yet from the main thread.
	 */
	public static final long overrideInternalTexture(TextureHandle _handle, short _width, short _height, byte _numMips, TextureFormat _format, long _flags) {
		try (Arena arena = Arena.ofConfined()) {
			return javaUintptr(invoke(DC_OVERRIDE_INTERNAL_TEXTURE, _handle.allocate(arena), _width, _height, _numMips, _format.ordinal(), _flags));
		}
	}

	static final int DC_OVERRIDE_INTERNAL_TEXTURE = 170;
	private static final FunctionDescriptor FD_OVERRIDE_INTERNAL_TEXTURE = FunctionDescriptor.of(C_UINTPTR_T, TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
	/**
	 * Sets a debug marker. This allows you to group graphics calls together for easy browsing in
	 * graphics debugging tools.
	 * @param _name Marker name.
	 * @param _len Marker name length (if length is INT32_MAX, it's expected that _name is zero terminated string.
	 */
	public static final void setMarker(String _name, int _len) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_MARKER).invokeExact(cString(arena, _name), _len);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_MARKER = 171;
	private static final FunctionDescriptor FD_SET_MARKER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
	/**
	 * Set render states for draw primitive.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   1. To set up more complex states use:
	 *      {@code BGFX_STATE_ALPHA_REF(_ref)},
	 *      {@code BGFX_STATE_POINT_SIZE(_size)},
	 *      {@code BGFX_STATE_BLEND_FUNC(_src, _dst)},
	 *      {@code BGFX_STATE_BLEND_FUNC_SEPARATE(_srcRGB, _dstRGB, _srcA, _dstA)},
	 *      {@code BGFX_STATE_BLEND_EQUATION(_equation)},
	 *      {@code BGFX_STATE_BLEND_EQUATION_SEPARATE(_equationRGB, _equationA)}
	 *   2. {@code BGFX_STATE_BLEND_EQUATION_ADD} is set when no other blend
	 *      equation is specified.
	 * @param _state State flags. Default state for primitive type is   triangles. See: {@code BGFX_STATE_DEFAULT}.   - {@code BGFX_STATE_DEPTH_TEST_*} - Depth test function.   - {@code BGFX_STATE_BLEND_*} - See remark 1 about BGFX_STATE_BLEND_FUNC.   - {@code BGFX_STATE_BLEND_EQUATION_*} - See remark 2.   - {@code BGFX_STATE_CULL_*} - Backface culling mode.   - {@code BGFX_STATE_WRITE_*} - Enable R, G, B, A or Z write.   - {@code BGFX_STATE_MSAA} - Enable hardware multisample antialiasing.   - {@code BGFX_STATE_PT_[TRISTRIP/LINES/POINTS]} - Primitive type.
	 * @param _rgba Sets blend factor used by {@code BGFX_STATE_BLEND_FACTOR} and   {@code BGFX_STATE_BLEND_INV_FACTOR} blend modes.
	 */
	public static final void setState(long _state, int _rgba) {
		try {
			downcallHandle(DC_SET_STATE).invokeExact(_state, _rgba);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_STATE = 172;
	private static final FunctionDescriptor FD_SET_STATE = FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT);
	/**
	 * Set condition for rendering.
	 * @param _handle Occlusion query handle.
	 * @param _visible Render if occlusion query is visible.
	 */
	public static final void setCondition(OcclusionQueryHandle _handle, boolean _visible) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_CONDITION).invokeExact(_handle.allocate(arena), _visible);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_CONDITION = 173;
	private static final FunctionDescriptor FD_SET_CONDITION = FunctionDescriptor.ofVoid(OcclusionQueryHandle.LAYOUT, ValueLayout.JAVA_BOOLEAN);
	/**
	 * Set stencil test state.
	 * @param _fstencil Front stencil state.
	 * @param _bstencil Back stencil state. If back is set to {@code BGFX_STENCIL_NONE} _fstencil is applied to both front and back facing primitives.
	 */
	public static final void setStencil(int _fstencil, int _bstencil) {
		try {
			downcallHandle(DC_SET_STENCIL).invokeExact(_fstencil, _bstencil);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_STENCIL = 174;
	private static final FunctionDescriptor FD_SET_STENCIL = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set scissor for draw primitive.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   To scissor for all primitives in view see {@code setViewScissor}.
	 * @param _x Position x from the left corner of the window.
	 * @param _y Position y from the top corner of the window.
	 * @param _width Width of view scissor region.
	 * @param _height Height of view scissor region.
	 * @return Scissor cache index.
	 */
	public static final short setScissor(short _x, short _y, short _width, short _height) {
		try {
			return (short) downcallHandle(DC_SET_SCISSOR).invokeExact(_x, _y, _width, _height);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_SCISSOR = 175;
	private static final FunctionDescriptor FD_SET_SCISSOR = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT);
	/**
	 * Set scissor from cache for draw primitive.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   To scissor for all primitives in view see {@code setViewScissor}.
	 * @param _cache Index in scissor cache.
	 */
	public static final void setScissorCached(short _cache) {
		try {
			downcallHandle(DC_SET_SCISSOR_CACHED).invokeExact(_cache);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_SCISSOR_CACHED = 176;
	private static final FunctionDescriptor FD_SET_SCISSOR_CACHED = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT);
	/**
	 * Set model matrix for draw primitive. If it is not called,
	 * the model will be rendered with an identity model matrix.
	 * @param _mtx Pointer to first matrix in array.
	 * @param _num Number of matrices in array.
	 * @return Index into matrix cache in case the same model matrix has to be used for other draw primitive call.
	 */
	public static final int setTransform(MemorySegment _mtx, short _num) {
		try {
			return (int) downcallHandle(DC_SET_TRANSFORM).invokeExact(address(_mtx), _num);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_TRANSFORM = 177;
	private static final FunctionDescriptor FD_SET_TRANSFORM = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 *  Set model matrix from matrix cache for draw primitive.
	 * @param _cache Index in matrix cache.
	 * @param _num Number of matrices from cache.
	 */
	public static final void setTransformCached(int _cache, short _num) {
		try {
			downcallHandle(DC_SET_TRANSFORM_CACHED).invokeExact(_cache, _num);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_TRANSFORM_CACHED = 178;
	private static final FunctionDescriptor FD_SET_TRANSFORM_CACHED = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT);
	/**
	 * Reserve matrices in internal matrix cache.
	 * <p>
	 * <strong>Attention:</strong> Pointer returned can be modified until {@code frame} is called.
	 * @param _transform Pointer to {@code Transform} structure.
	 * @param _num Number of matrices.
	 * @return Index in matrix cache.
	 */
	public static final int allocTransform(Transform _transform, short _num) {
		try {
			return (int) downcallHandle(DC_ALLOC_TRANSFORM).invokeExact(address(_transform), _num);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_ALLOC_TRANSFORM = 179;
	private static final FunctionDescriptor FD_ALLOC_TRANSFORM = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Set shader uniform parameter for draw primitive.
	 * @param _handle Uniform.
	 * @param _value Pointer to uniform data.
	 * @param _num Number of elements. Passing {@code UINT16_MAX} will use the _num passed on uniform creation.
	 */
	public static final void setUniform(UniformHandle _handle, MemorySegment _value, short _num) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_UNIFORM).invokeExact(_handle.allocate(arena), address(_value), _num);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_UNIFORM = 180;
	private static final FunctionDescriptor FD_SET_UNIFORM = FunctionDescriptor.ofVoid(UniformHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);
	/**
	 * Set index buffer for draw primitive.
	 * @param _handle Index buffer.
	 * @param _firstIndex First index to render.
	 * @param _numIndices Number of indices to render.
	 */
	public static final void setIndexBuffer(IndexBufferHandle _handle, int _firstIndex, int _numIndices) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_INDEX_BUFFER).invokeExact(_handle.allocate(arena), _firstIndex, _numIndices);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_INDEX_BUFFER = 181;
	private static final FunctionDescriptor FD_SET_INDEX_BUFFER = FunctionDescriptor.ofVoid(IndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set index buffer for draw primitive.
	 * @param _handle Dynamic index buffer.
	 * @param _firstIndex First index to render.
	 * @param _numIndices Number of indices to render.
	 */
	public static final void setDynamicIndexBuffer(DynamicIndexBufferHandle _handle, int _firstIndex, int _numIndices) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_DYNAMIC_INDEX_BUFFER).invokeExact(_handle.allocate(arena), _firstIndex, _numIndices);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_DYNAMIC_INDEX_BUFFER = 182;
	private static final FunctionDescriptor FD_SET_DYNAMIC_INDEX_BUFFER = FunctionDescriptor.ofVoid(DynamicIndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set index buffer for draw primitive.
	 * @param _tib Transient index buffer.
	 * @param _firstIndex First index to render.
	 * @param _numIndices Number of indices to render.
	 */
	public static final void setTransientIndexBuffer(TransientIndexBuffer _tib, int _firstIndex, int _numIndices) {
		try {
			downcallHandle(DC_SET_TRANSIENT_INDEX_BUFFER).invokeExact(address(_tib), _firstIndex, _numIndices);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_TRANSIENT_INDEX_BUFFER = 183;
	private static final FunctionDescriptor FD_SET_TRANSIENT_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set vertex buffer for draw primitive.
	 * @param _stream Vertex stream.
	 * @param _handle Vertex buffer.
	 * @param _startVertex First vertex to render.
	 * @param _numVertices Number of vertices to render.
	 */
	public static final void setVertexBuffer(byte _stream, VertexBufferHandle _handle, int _startVertex, int _numVertices) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_VERTEX_BUFFER).invokeExact(_stream, _handle.allocate(arena), _startVertex, _numVertices);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VERTEX_BUFFER = 184;
	private static final FunctionDescriptor FD_SET_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set vertex buffer for draw primitive.
	 * @param _stream Vertex stream.
	 * @param _handle Vertex buffer.
	 * @param _startVertex First vertex to render.
	 * @param _numVertices Number of vertices to render.
	 * @param _layoutHandle Vertex layout for aliasing vertex buffer. If invalid handle is used, vertex layout used for creation of vertex buffer will be used.
	 */
	public static final void setVertexBufferWithLayout(byte _stream, VertexBufferHandle _handle, int _startVertex, int _numVertices, VertexLayoutHandle _layoutHandle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_VERTEX_BUFFER_WITH_LAYOUT).invokeExact(_stream, _handle.allocate(arena), _startVertex, _numVertices, _layoutHandle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VERTEX_BUFFER_WITH_LAYOUT = 185;
	private static final FunctionDescriptor FD_SET_VERTEX_BUFFER_WITH_LAYOUT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, VertexLayoutHandle.LAYOUT);
	/**
	 * Set vertex buffer for draw primitive.
	 * @param _stream Vertex stream.
	 * @param _handle Dynamic vertex buffer.
	 * @param _startVertex First vertex to render.
	 * @param _numVertices Number of vertices to render.
	 */
	public static final void setDynamicVertexBuffer(byte _stream, DynamicVertexBufferHandle _handle, int _startVertex, int _numVertices) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_DYNAMIC_VERTEX_BUFFER).invokeExact(_stream, _handle.allocate(arena), _startVertex, _numVertices);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_DYNAMIC_VERTEX_BUFFER = 186;
	private static final FunctionDescriptor FD_SET_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set vertex buffer for draw primitive.
	 * @param _stream Vertex stream.
	 * @param _handle Dynamic vertex buffer.
	 * @param _startVertex First vertex to render.
	 * @param _numVertices Number of vertices to render.
	 * @param _layoutHandle Vertex layout for aliasing vertex buffer. If invalid handle is used, vertex layout used for creation of vertex buffer will be used.
	 */
	public static final void setDynamicVertexBufferWithLayout(byte _stream, DynamicVertexBufferHandle _handle, int _startVertex, int _numVertices, VertexLayoutHandle _layoutHandle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT).invokeExact(_stream, _handle.allocate(arena), _startVertex, _numVertices, _layoutHandle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT = 187;
	private static final FunctionDescriptor FD_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, VertexLayoutHandle.LAYOUT);
	/**
	 * Set vertex buffer for draw primitive.
	 * @param _stream Vertex stream.
	 * @param _tvb Transient vertex buffer.
	 * @param _startVertex First vertex to render.
	 * @param _numVertices Number of vertices to render.
	 */
	public static final void setTransientVertexBuffer(byte _stream, TransientVertexBuffer _tvb, int _startVertex, int _numVertices) {
		try {
			downcallHandle(DC_SET_TRANSIENT_VERTEX_BUFFER).invokeExact(_stream, address(_tvb), _startVertex, _numVertices);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_TRANSIENT_VERTEX_BUFFER = 188;
	private static final FunctionDescriptor FD_SET_TRANSIENT_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set vertex buffer for draw primitive.
	 * @param _stream Vertex stream.
	 * @param _tvb Transient vertex buffer.
	 * @param _startVertex First vertex to render.
	 * @param _numVertices Number of vertices to render.
	 * @param _layoutHandle Vertex layout for aliasing vertex buffer. If invalid handle is used, vertex layout used for creation of vertex buffer will be used.
	 */
	public static final void setTransientVertexBufferWithLayout(byte _stream, TransientVertexBuffer _tvb, int _startVertex, int _numVertices, VertexLayoutHandle _layoutHandle) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT).invokeExact(_stream, address(_tvb), _startVertex, _numVertices, _layoutHandle.allocate(arena));
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT = 189;
	private static final FunctionDescriptor FD_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, VertexLayoutHandle.LAYOUT);
	/**
	 * Set number of vertices for auto generated vertices use in conjunction
	 * with gl_VertexID.
	 * <p>
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_VERTEX_ID}.
	 * @param _numVertices Number of vertices.
	 */
	public static final void setVertexCount(int _numVertices) {
		try {
			downcallHandle(DC_SET_VERTEX_COUNT).invokeExact(_numVertices);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_VERTEX_COUNT = 190;
	private static final FunctionDescriptor FD_SET_VERTEX_COUNT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT);
	/**
	 * Set instance data buffer for draw primitive.
	 * @param _idb Transient instance data buffer.
	 * @param _start First instance data.
	 * @param _num Number of data instances.
	 */
	public static final void setInstanceDataBuffer(InstanceDataBuffer _idb, int _start, int _num) {
		try {
			downcallHandle(DC_SET_INSTANCE_DATA_BUFFER).invokeExact(address(_idb), _start, _num);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_INSTANCE_DATA_BUFFER = 191;
	private static final FunctionDescriptor FD_SET_INSTANCE_DATA_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set instance data buffer for draw primitive.
	 * @param _handle Vertex buffer.
	 * @param _startVertex First instance data.
	 * @param _num Number of data instances.
	 */
	public static final void setInstanceDataFromVertexBuffer(VertexBufferHandle _handle, int _startVertex, int _num) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER).invokeExact(_handle.allocate(arena), _startVertex, _num);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER = 192;
	private static final FunctionDescriptor FD_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER = FunctionDescriptor.ofVoid(VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set instance data buffer for draw primitive.
	 * @param _handle Dynamic vertex buffer.
	 * @param _startVertex First instance data.
	 * @param _num Number of data instances.
	 */
	public static final void setInstanceDataFromDynamicVertexBuffer(DynamicVertexBufferHandle _handle, int _startVertex, int _num) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER).invokeExact(_handle.allocate(arena), _startVertex, _num);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER = 193;
	private static final FunctionDescriptor FD_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set number of instances for auto generated instances use in conjunction
	 * with gl_InstanceID.
	 * <p>
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_VERTEX_ID}.
	 * @param _numInstances Number of instances.
	 */
	public static final void setInstanceCount(int _numInstances) {
		try {
			downcallHandle(DC_SET_INSTANCE_COUNT).invokeExact(_numInstances);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_INSTANCE_COUNT = 194;
	private static final FunctionDescriptor FD_SET_INSTANCE_COUNT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT);
	/**
	 * Set texture stage for draw primitive.
	 * @param _stage Texture unit.
	 * @param _sampler Program sampler.
	 * @param _handle Texture handle.
	 * @param _flags Texture sampling mode. Default value UINT32_MAX uses   texture sampling settings from the texture.   - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap     mode.   - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic     sampling.
	 */
	public static final void setTexture(byte _stage, UniformHandle _sampler, TextureHandle _handle, int _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_TEXTURE).invokeExact(_stage, _sampler.allocate(arena), _handle.allocate(arena), _flags);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_TEXTURE = 195;
	private static final FunctionDescriptor FD_SET_TEXTURE = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, UniformHandle.LAYOUT, TextureHandle.LAYOUT, ValueLayout.JAVA_INT);
	/**
	 * Set texture stage for draw primitive, selecting a sub-range of the
	 * texture's array layers and mip levels.
	 * @param _stage Texture unit.
	 * @param _sampler Program sampler.
	 * @param _handle Texture handle.
	 * @param _firstLayer First array layer.
	 * @param _numLayers Number of array layers.
	 * @param _firstMip First (most detailed) mip level.
	 * @param _numMips Number of mip levels.
	 * @param _flags Texture sampling mode. Default value UINT32_MAX uses   texture sampling settings from the texture.   - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap     mode.   - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic     sampling.
	 */
	public static final void setTextureView(byte _stage, UniformHandle _sampler, TextureHandle _handle, short _firstLayer, short _numLayers, byte _firstMip, byte _numMips, int _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_TEXTURE_VIEW).invokeExact(_stage, _sampler.allocate(arena), _handle.allocate(arena), _firstLayer, _numLayers, _firstMip, _numMips, _flags);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_TEXTURE_VIEW = 196;
	private static final FunctionDescriptor FD_SET_TEXTURE_VIEW = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, UniformHandle.LAYOUT, TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT);
	/**
	 * Submit an empty primitive for rendering. Uniforms and draw state
	 * will be applied but no geometry will be submitted.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   These empty draw calls will sort before ordinary draw calls.
	 * @param _id View id.
	 */
	public static final void touch(short _id) {
		try {
			downcallHandle(DC_TOUCH).invokeExact(_id);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_TOUCH = 197;
	private static final FunctionDescriptor FD_TOUCH = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT);
	/**
	 * Submit primitive for rendering.
	 * @param _id View id.
	 * @param _program Program.
	 * @param _depth Depth for sorting.
	 * @param _flags Which states to discard for next draw. See {@code BGFX_DISCARD_*}.
	 */
	public static final void submit(short _id, ProgramHandle _program, int _depth, byte _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SUBMIT).invokeExact(_id, _program.allocate(arena), _depth, _flags);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SUBMIT = 198;
	private static final FunctionDescriptor FD_SUBMIT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	/**
	 * Submit primitive with occlusion query for rendering.
	 * @param _id View id.
	 * @param _program Program.
	 * @param _occlusionQuery Occlusion query.
	 * @param _depth Depth for sorting.
	 * @param _flags Which states to discard for next draw. See {@code BGFX_DISCARD_*}.
	 */
	public static final void submitOcclusionQuery(short _id, ProgramHandle _program, OcclusionQueryHandle _occlusionQuery, int _depth, byte _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SUBMIT_OCCLUSION_QUERY).invokeExact(_id, _program.allocate(arena), _occlusionQuery.allocate(arena), _depth, _flags);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SUBMIT_OCCLUSION_QUERY = 199;
	private static final FunctionDescriptor FD_SUBMIT_OCCLUSION_QUERY = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, OcclusionQueryHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	/**
	 * Submit primitive for rendering with index and instance data info from
	 * indirect buffer.
	 * <p>
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_DRAW_INDIRECT}.
	 * @param _id View id.
	 * @param _program Program.
	 * @param _indirectHandle Indirect buffer.
	 * @param _start First element in indirect buffer.
	 * @param _num Number of draws.
	 * @param _depth Depth for sorting.
	 * @param _flags Which states to discard for next draw. See {@code BGFX_DISCARD_*}.
	 */
	public static final void submitIndirect(short _id, ProgramHandle _program, IndirectBufferHandle _indirectHandle, int _start, int _num, int _depth, byte _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SUBMIT_INDIRECT).invokeExact(_id, _program.allocate(arena), _indirectHandle.allocate(arena), _start, _num, _depth, _flags);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SUBMIT_INDIRECT = 200;
	private static final FunctionDescriptor FD_SUBMIT_INDIRECT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	/**
	 * Submit primitive for rendering with index and instance data info and
	 * draw count from indirect buffers.
	 * <p>
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_DRAW_INDIRECT_COUNT}.
	 * @param _id View id.
	 * @param _program Program.
	 * @param _indirectHandle Indirect buffer.
	 * @param _start First element in indirect buffer.
	 * @param _numHandle Buffer for number of draws. Must be   created with {@code BGFX_BUFFER_INDEX32} and {@code BGFX_BUFFER_DRAW_INDIRECT}.
	 * @param _numIndex Element in number buffer.
	 * @param _numMax Max number of draws.
	 * @param _depth Depth for sorting.
	 * @param _flags Which states to discard for next draw. See {@code BGFX_DISCARD_*}.
	 */
	public static final void submitIndirectCount(short _id, ProgramHandle _program, IndirectBufferHandle _indirectHandle, int _start, IndexBufferHandle _numHandle, int _numIndex, int _numMax, int _depth, byte _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SUBMIT_INDIRECT_COUNT).invokeExact(_id, _program.allocate(arena), _indirectHandle.allocate(arena), _start, _numHandle.allocate(arena), _numIndex, _numMax, _depth, _flags);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SUBMIT_INDIRECT_COUNT = 201;
	private static final FunctionDescriptor FD_SUBMIT_INDIRECT_COUNT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT, IndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	/**
	 * Set compute index buffer.
	 * @param _stage Compute stage.
	 * @param _handle Index buffer handle.
	 * @param _access Buffer access. See {@code Access}.
	 */
	public static final void setComputeIndexBuffer(byte _stage, IndexBufferHandle _handle, Access _access) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_COMPUTE_INDEX_BUFFER).invokeExact(_stage, _handle.allocate(arena), _access.ordinal());
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_COMPUTE_INDEX_BUFFER = 202;
	private static final FunctionDescriptor FD_SET_COMPUTE_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, IndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	/**
	 * Set compute vertex buffer.
	 * @param _stage Compute stage.
	 * @param _handle Vertex buffer handle.
	 * @param _access Buffer access. See {@code Access}.
	 */
	public static final void setComputeVertexBuffer(byte _stage, VertexBufferHandle _handle, Access _access) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_COMPUTE_VERTEX_BUFFER).invokeExact(_stage, _handle.allocate(arena), _access.ordinal());
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_COMPUTE_VERTEX_BUFFER = 203;
	private static final FunctionDescriptor FD_SET_COMPUTE_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	/**
	 * Set compute dynamic index buffer.
	 * @param _stage Compute stage.
	 * @param _handle Dynamic index buffer handle.
	 * @param _access Buffer access. See {@code Access}.
	 */
	public static final void setComputeDynamicIndexBuffer(byte _stage, DynamicIndexBufferHandle _handle, Access _access) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_COMPUTE_DYNAMIC_INDEX_BUFFER).invokeExact(_stage, _handle.allocate(arena), _access.ordinal());
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_COMPUTE_DYNAMIC_INDEX_BUFFER = 204;
	private static final FunctionDescriptor FD_SET_COMPUTE_DYNAMIC_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, DynamicIndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	/**
	 * Set compute dynamic vertex buffer.
	 * @param _stage Compute stage.
	 * @param _handle Dynamic vertex buffer handle.
	 * @param _access Buffer access. See {@code Access}.
	 */
	public static final void setComputeDynamicVertexBuffer(byte _stage, DynamicVertexBufferHandle _handle, Access _access) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER).invokeExact(_stage, _handle.allocate(arena), _access.ordinal());
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER = 205;
	private static final FunctionDescriptor FD_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	/**
	 * Set compute indirect buffer.
	 * @param _stage Compute stage.
	 * @param _handle Indirect buffer handle.
	 * @param _access Buffer access. See {@code Access}.
	 */
	public static final void setComputeIndirectBuffer(byte _stage, IndirectBufferHandle _handle, Access _access) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_COMPUTE_INDIRECT_BUFFER).invokeExact(_stage, _handle.allocate(arena), _access.ordinal());
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_COMPUTE_INDIRECT_BUFFER = 206;
	private static final FunctionDescriptor FD_SET_COMPUTE_INDIRECT_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT);
	/**
	 * Set compute image from texture.
	 * @param _stage Compute stage.
	 * @param _handle Texture handle.
	 * @param _mip Mip level.
	 * @param _access Image access. See {@code Access}.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 */
	public static final void setImage(byte _stage, TextureHandle _handle, byte _mip, Access _access, TextureFormat _format) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_IMAGE).invokeExact(_stage, _handle.allocate(arena), _mip, _access.ordinal(), _format.ordinal());
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_IMAGE = 207;
	private static final FunctionDescriptor FD_SET_IMAGE = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, TextureHandle.LAYOUT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Set compute image stage for draw primitive, selecting a sub-range of the
	 * texture's array layers and mip levels.
	 * @param _stage Compute stage.
	 * @param _handle Texture handle.
	 * @param _firstLayer First array layer.
	 * @param _numLayers Number of array layers.
	 * @param _mip Mip level.
	 * @param _access Image access. See {@code Access}.
	 * @param _format Texture format. See: {@code TextureFormat}.
	 */
	public static final void setImageView(byte _stage, TextureHandle _handle, short _firstLayer, short _numLayers, byte _mip, Access _access, TextureFormat _format) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_SET_IMAGE_VIEW).invokeExact(_stage, _handle.allocate(arena), _firstLayer, _numLayers, _mip, _access.ordinal(), _format.ordinal());
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_SET_IMAGE_VIEW = 208;
	private static final FunctionDescriptor FD_SET_IMAGE_VIEW = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE, TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
	/**
	 * Dispatch compute.
	 * @param _id View id.
	 * @param _program Compute program.
	 * @param _numX Number of groups X.
	 * @param _numY Number of groups Y.
	 * @param _numZ Number of groups Z.
	 * @param _flags Discard or preserve states. See {@code BGFX_DISCARD_*}.
	 */
	public static final void dispatch(short _id, ProgramHandle _program, int _numX, int _numY, int _numZ, byte _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DISPATCH).invokeExact(_id, _program.allocate(arena), _numX, _numY, _numZ, _flags);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DISPATCH = 209;
	private static final FunctionDescriptor FD_DISPATCH = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	/**
	 * Dispatch compute indirect.
	 * @param _id View id.
	 * @param _program Compute program.
	 * @param _indirectHandle Indirect buffer.
	 * @param _start First element in indirect buffer.
	 * @param _num Number of dispatches.
	 * @param _flags Discard or preserve states. See {@code BGFX_DISCARD_*}.
	 */
	public static final void dispatchIndirect(short _id, ProgramHandle _program, IndirectBufferHandle _indirectHandle, int _start, int _num, byte _flags) {
		try {
			try (Arena arena = Arena.ofConfined()) {
				downcallHandle(DC_DISPATCH_INDIRECT).invokeExact(_id, _program.allocate(arena), _indirectHandle.allocate(arena), _start, _num, _flags);
			}
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DISPATCH_INDIRECT = 210;
	private static final FunctionDescriptor FD_DISPATCH_INDIRECT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);
	/**
	 * Discard previously set state for draw or compute call.
	 * @param _flags Draw/compute states to discard.
	 */
	public static final void discard(byte _flags) {
		try {
			downcallHandle(DC_DISCARD).invokeExact(_flags);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_DISCARD = 211;
	private static final FunctionDescriptor FD_DISCARD = FunctionDescriptor.ofVoid(ValueLayout.JAVA_BYTE);
	/**
	 * Blit texture region between two textures.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   The copy covers the region the two sides have in common: each side gives
	 *   the origin it starts at, and the size is the smaller of the two extents.
	 *   A zero {@code width}, {@code height} or {@code depth} extends to the rest of that mip.
	 * <p>
	 *   Blit is performed on GPU, and it is ordered within the view. In views, all
	 *   draw commands are executed after blit and compute commands.
	 * <p>
	 * <strong>Attention:</strong> Destination texture must be created with {@code BGFX_TEXTURE_BLIT_DST} flag.
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_TEXTURE_BLIT}.
	 * @param _id View id.
	 * @param _dst Destination texture region.
	 * @param _src Source texture region.
	 */
	public static final void blit(short _id, TextureRegion _dst, TextureRegion _src) {
		try {
			downcallHandle(DC_BLIT).invokeExact(_id, address(_dst), address(_src));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_BLIT = 212;
	private static final FunctionDescriptor FD_BLIT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	/**
	 * Blit buffer region between two buffers.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   The source region gives the number of bytes copied, and the destination
	 *   region gives only the offset they land at. A zero {@code size} copies the rest of
	 *   the source buffer. {@code rowPitch} and {@code slicePitch} are unused.
	 * <p>
	 *   Buffer blit is performed on GPU, and it is ordered within the view, same as
	 *   texture blit. In views, all draw commands are executed after blit and compute
	 *   commands.
	 * <p>
	 * <strong>Attention:</strong> Source buffer must be created with one of {@code BGFX_BUFFER_COMPUTE_*}, or
	 *   {@code BGFX_BUFFER_DRAW_INDIRECT} flags.
	 * <strong>Attention:</strong> Destination buffer must be created with {@code BGFX_BUFFER_COMPUTE_WRITE}, or
	 *   {@code BGFX_BUFFER_DRAW_INDIRECT} flag.
	 * <strong>Attention:</strong> Source and destination buffer must be different.
	 * @param _id View id.
	 * @param _dst Destination buffer region.
	 * @param _src Source buffer region.
	 */
	public static final void blitBuffer(short _id, BufferRegion _dst, BufferRegion _src) {
		try {
			downcallHandle(DC_BLIT_BUFFER).invokeExact(_id, address(_dst), address(_src));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_BLIT_BUFFER = 213;
	private static final FunctionDescriptor FD_BLIT_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	/**
	 * Blit texture region into buffer.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   The texture region gives the size of the copy. {@code BufferRegion.rowPitch} and
	 *   {@code slicePitch} choose how the texels are laid out in the buffer, and 0 packs
	 *   them tightly. {@code BufferRegion.init} fills in the layout the backend copies
	 *   fastest, and bgfx repacks internally for any other layout.
	 * <p>
	 *   Blit is performed on GPU, and it is ordered within the view, same as texture
	 *   blit. In views, all draw commands are executed after blit and compute commands.
	 * <p>
	 * <strong>Attention:</strong> Destination buffer must be created with {@code BGFX_BUFFER_COMPUTE_WRITE}, or
	 *   {@code BGFX_BUFFER_DRAW_INDIRECT} flag.
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_TEXTURE_BLIT}.
	 * @param _id View id.
	 * @param _dst Destination buffer region.
	 * @param _src Source texture region.
	 */
	public static final void blitToBuffer(short _id, BufferRegion _dst, TextureRegion _src) {
		try {
			downcallHandle(DC_BLIT_TO_BUFFER).invokeExact(_id, address(_dst), address(_src));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_BLIT_TO_BUFFER = 214;
	private static final FunctionDescriptor FD_BLIT_TO_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
	/**
	 * Blit buffer contents into texture region.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   The texture region gives the size of the copy. {@code BufferRegion.rowPitch} and
	 *   {@code slicePitch} describe how the texels are laid out in the buffer, and 0 reads
	 *   them tightly packed. {@code BufferRegion.init} fills in the layout the backend
	 *   copies fastest, and bgfx repacks internally for any other layout.
	 * <p>
	 *   Blit is performed on GPU, and it is ordered within the view, same as texture
	 *   blit. In views, all draw commands are executed after blit and compute commands.
	 * <p>
	 * <strong>Attention:</strong> Source buffer must be created with one of {@code BGFX_BUFFER_COMPUTE_*}, or
	 *   {@code BGFX_BUFFER_DRAW_INDIRECT} flags.
	 * <strong>Attention:</strong> Destination texture must be created with {@code BGFX_TEXTURE_BLIT_DST} flag.
	 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_TEXTURE_BLIT}.
	 * @param _id View id.
	 * @param _dst Destination texture region.
	 * @param _src Source buffer region.
	 */
	public static final void blitFromBuffer(short _id, TextureRegion _dst, BufferRegion _src) {
		try {
			downcallHandle(DC_BLIT_FROM_BUFFER).invokeExact(_id, address(_dst), address(_src));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	static final int DC_BLIT_FROM_BUFFER = 215;
	private static final FunctionDescriptor FD_BLIT_FROM_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);

	private static MethodHandle[] linkAll(SymbolLookup lookup) {
		MethodHandle[] handles = new MethodHandle[216];
		handles[DC_TEXTURE_REGION_INIT] = downcall(lookup, "bgfx_texture_region_init", FD_TEXTURE_REGION_INIT);
		handles[DC_BUFFER_REGION_INIT_TEXTURE] = downcall(lookup, "bgfx_buffer_region_init_texture", FD_BUFFER_REGION_INIT_TEXTURE);
		handles[DC_BUFFER_REGION_INIT_BUFFER] = downcall(lookup, "bgfx_buffer_region_init_buffer", FD_BUFFER_REGION_INIT_BUFFER);
		handles[DC_ATTACHMENT_INIT] = downcall(lookup, "bgfx_attachment_init", FD_ATTACHMENT_INIT);
		handles[DC_VERTEX_LAYOUT_BEGIN] = downcall(lookup, "bgfx_vertex_layout_begin", FD_VERTEX_LAYOUT_BEGIN);
		handles[DC_VERTEX_LAYOUT_ADD] = downcall(lookup, "bgfx_vertex_layout_add", FD_VERTEX_LAYOUT_ADD);
		handles[DC_VERTEX_LAYOUT_DECODE] = downcall(lookup, "bgfx_vertex_layout_decode", FD_VERTEX_LAYOUT_DECODE);
		handles[DC_VERTEX_LAYOUT_HAS] = downcall(lookup, "bgfx_vertex_layout_has", FD_VERTEX_LAYOUT_HAS);
		handles[DC_VERTEX_LAYOUT_SKIP] = downcall(lookup, "bgfx_vertex_layout_skip", FD_VERTEX_LAYOUT_SKIP);
		handles[DC_VERTEX_LAYOUT_END] = downcall(lookup, "bgfx_vertex_layout_end", FD_VERTEX_LAYOUT_END);
		handles[DC_VERTEX_LAYOUT_GET_OFFSET] = downcall(lookup, "bgfx_vertex_layout_get_offset", FD_VERTEX_LAYOUT_GET_OFFSET);
		handles[DC_VERTEX_LAYOUT_GET_STRIDE] = downcall(lookup, "bgfx_vertex_layout_get_stride", FD_VERTEX_LAYOUT_GET_STRIDE);
		handles[DC_VERTEX_LAYOUT_GET_SIZE] = downcall(lookup, "bgfx_vertex_layout_get_size", FD_VERTEX_LAYOUT_GET_SIZE);
		handles[DC_VERTEX_PACK] = downcall(lookup, "bgfx_vertex_pack", FD_VERTEX_PACK);
		handles[DC_VERTEX_UNPACK] = downcall(lookup, "bgfx_vertex_unpack", FD_VERTEX_UNPACK);
		handles[DC_VERTEX_CONVERT] = downcall(lookup, "bgfx_vertex_convert", FD_VERTEX_CONVERT);
		handles[DC_TOPOLOGY_CONVERT] = downcall(lookup, "bgfx_topology_convert", FD_TOPOLOGY_CONVERT);
		handles[DC_TOPOLOGY_SORT_TRI_LIST] = downcall(lookup, "bgfx_topology_sort_tri_list", FD_TOPOLOGY_SORT_TRI_LIST);
		handles[DC_GET_SUPPORTED_RENDERERS] = downcall(lookup, "bgfx_get_supported_renderers", FD_GET_SUPPORTED_RENDERERS);
		handles[DC_GET_RENDERER_NAME] = downcall(lookup, "bgfx_get_renderer_name", FD_GET_RENDERER_NAME);
		handles[DC_INIT_CTOR] = downcall(lookup, "bgfx_init_ctor", FD_INIT_CTOR);
		handles[DC_INIT] = downcall(lookup, "bgfx_init", FD_INIT);
		handles[DC_SHUTDOWN] = downcall(lookup, "bgfx_shutdown", FD_SHUTDOWN);
		handles[DC_RESET] = downcall(lookup, "bgfx_reset", FD_RESET);
		handles[DC_FRAME] = downcall(lookup, "bgfx_frame", FD_FRAME);
		handles[DC_GET_RENDERER_TYPE] = downcall(lookup, "bgfx_get_renderer_type", FD_GET_RENDERER_TYPE);
		handles[DC_GET_CAPS] = downcall(lookup, "bgfx_get_caps", FD_GET_CAPS);
		handles[DC_GET_STATS] = downcall(lookup, "bgfx_get_stats", FD_GET_STATS);
		handles[DC_ALLOC] = downcall(lookup, "bgfx_alloc", FD_ALLOC);
		handles[DC_COPY] = downcall(lookup, "bgfx_copy", FD_COPY);
		handles[DC_MAKE_REF] = downcall(lookup, "bgfx_make_ref", FD_MAKE_REF);
		handles[DC_MAKE_REF_RELEASE] = downcall(lookup, "bgfx_make_ref_release", FD_MAKE_REF_RELEASE);
		handles[DC_SET_DEBUG] = downcall(lookup, "bgfx_set_debug", FD_SET_DEBUG);
		handles[DC_DBG_TEXT_CLEAR] = downcall(lookup, "bgfx_dbg_text_clear", FD_DBG_TEXT_CLEAR);
		handles[DC_DBG_TEXT_VPRINTF] = downcall(lookup, "bgfx_dbg_text_vprintf", FD_DBG_TEXT_VPRINTF);
		handles[DC_DBG_TEXT_IMAGE] = downcall(lookup, "bgfx_dbg_text_image", FD_DBG_TEXT_IMAGE);
		handles[DC_CREATE_INDEX_BUFFER] = downcall(lookup, "bgfx_create_index_buffer", FD_CREATE_INDEX_BUFFER);
		handles[DC_READ_BUFFER] = downcall(lookup, "bgfx_read_buffer", FD_READ_BUFFER);
		handles[DC_SET_INDEX_BUFFER_NAME] = downcall(lookup, "bgfx_set_index_buffer_name", FD_SET_INDEX_BUFFER_NAME);
		handles[DC_DESTROY_INDEX_BUFFER] = downcall(lookup, "bgfx_destroy_index_buffer", FD_DESTROY_INDEX_BUFFER);
		handles[DC_CREATE_VERTEX_LAYOUT] = downcall(lookup, "bgfx_create_vertex_layout", FD_CREATE_VERTEX_LAYOUT);
		handles[DC_DESTROY_VERTEX_LAYOUT] = downcall(lookup, "bgfx_destroy_vertex_layout", FD_DESTROY_VERTEX_LAYOUT);
		handles[DC_CREATE_VERTEX_BUFFER] = downcall(lookup, "bgfx_create_vertex_buffer", FD_CREATE_VERTEX_BUFFER);
		handles[DC_SET_VERTEX_BUFFER_NAME] = downcall(lookup, "bgfx_set_vertex_buffer_name", FD_SET_VERTEX_BUFFER_NAME);
		handles[DC_DESTROY_VERTEX_BUFFER] = downcall(lookup, "bgfx_destroy_vertex_buffer", FD_DESTROY_VERTEX_BUFFER);
		handles[DC_CREATE_DYNAMIC_INDEX_BUFFER] = downcall(lookup, "bgfx_create_dynamic_index_buffer", FD_CREATE_DYNAMIC_INDEX_BUFFER);
		handles[DC_CREATE_DYNAMIC_INDEX_BUFFER_MEM] = downcall(lookup, "bgfx_create_dynamic_index_buffer_mem", FD_CREATE_DYNAMIC_INDEX_BUFFER_MEM);
		handles[DC_UPDATE_DYNAMIC_INDEX_BUFFER] = downcall(lookup, "bgfx_update_dynamic_index_buffer", FD_UPDATE_DYNAMIC_INDEX_BUFFER);
		handles[DC_DESTROY_DYNAMIC_INDEX_BUFFER] = downcall(lookup, "bgfx_destroy_dynamic_index_buffer", FD_DESTROY_DYNAMIC_INDEX_BUFFER);
		handles[DC_CREATE_DYNAMIC_VERTEX_BUFFER] = downcall(lookup, "bgfx_create_dynamic_vertex_buffer", FD_CREATE_DYNAMIC_VERTEX_BUFFER);
		handles[DC_CREATE_DYNAMIC_VERTEX_BUFFER_MEM] = downcall(lookup, "bgfx_create_dynamic_vertex_buffer_mem", FD_CREATE_DYNAMIC_VERTEX_BUFFER_MEM);
		handles[DC_UPDATE_DYNAMIC_VERTEX_BUFFER] = downcall(lookup, "bgfx_update_dynamic_vertex_buffer", FD_UPDATE_DYNAMIC_VERTEX_BUFFER);
		handles[DC_DESTROY_DYNAMIC_VERTEX_BUFFER] = downcall(lookup, "bgfx_destroy_dynamic_vertex_buffer", FD_DESTROY_DYNAMIC_VERTEX_BUFFER);
		handles[DC_GET_AVAIL_TRANSIENT_INDEX_BUFFER] = downcall(lookup, "bgfx_get_avail_transient_index_buffer", FD_GET_AVAIL_TRANSIENT_INDEX_BUFFER);
		handles[DC_GET_AVAIL_TRANSIENT_VERTEX_BUFFER] = downcall(lookup, "bgfx_get_avail_transient_vertex_buffer", FD_GET_AVAIL_TRANSIENT_VERTEX_BUFFER);
		handles[DC_GET_AVAIL_INSTANCE_DATA_BUFFER] = downcall(lookup, "bgfx_get_avail_instance_data_buffer", FD_GET_AVAIL_INSTANCE_DATA_BUFFER);
		handles[DC_ALLOC_TRANSIENT_INDEX_BUFFER] = downcall(lookup, "bgfx_alloc_transient_index_buffer", FD_ALLOC_TRANSIENT_INDEX_BUFFER);
		handles[DC_ALLOC_TRANSIENT_VERTEX_BUFFER] = downcall(lookup, "bgfx_alloc_transient_vertex_buffer", FD_ALLOC_TRANSIENT_VERTEX_BUFFER);
		handles[DC_ALLOC_TRANSIENT_BUFFERS] = downcall(lookup, "bgfx_alloc_transient_buffers", FD_ALLOC_TRANSIENT_BUFFERS);
		handles[DC_ALLOC_INSTANCE_DATA_BUFFER] = downcall(lookup, "bgfx_alloc_instance_data_buffer", FD_ALLOC_INSTANCE_DATA_BUFFER);
		handles[DC_CREATE_INDIRECT_BUFFER] = downcall(lookup, "bgfx_create_indirect_buffer", FD_CREATE_INDIRECT_BUFFER);
		handles[DC_DESTROY_INDIRECT_BUFFER] = downcall(lookup, "bgfx_destroy_indirect_buffer", FD_DESTROY_INDIRECT_BUFFER);
		handles[DC_CREATE_SHADER] = downcall(lookup, "bgfx_create_shader", FD_CREATE_SHADER);
		handles[DC_GET_SHADER_UNIFORMS] = downcall(lookup, "bgfx_get_shader_uniforms", FD_GET_SHADER_UNIFORMS);
		handles[DC_SET_SHADER_NAME] = downcall(lookup, "bgfx_set_shader_name", FD_SET_SHADER_NAME);
		handles[DC_DESTROY_SHADER] = downcall(lookup, "bgfx_destroy_shader", FD_DESTROY_SHADER);
		handles[DC_CREATE_PROGRAM] = downcall(lookup, "bgfx_create_program", FD_CREATE_PROGRAM);
		handles[DC_CREATE_COMPUTE_PROGRAM] = downcall(lookup, "bgfx_create_compute_program", FD_CREATE_COMPUTE_PROGRAM);
		handles[DC_DESTROY_PROGRAM] = downcall(lookup, "bgfx_destroy_program", FD_DESTROY_PROGRAM);
		handles[DC_IS_TEXTURE_VALID] = downcall(lookup, "bgfx_is_texture_valid", FD_IS_TEXTURE_VALID);
		handles[DC_IS_VIDEO_CODEC_VALID] = downcall(lookup, "bgfx_is_video_codec_valid", FD_IS_VIDEO_CODEC_VALID);
		handles[DC_IS_FRAME_BUFFER_VALID] = downcall(lookup, "bgfx_is_frame_buffer_valid", FD_IS_FRAME_BUFFER_VALID);
		handles[DC_CALC_TEXTURE_SIZE] = downcall(lookup, "bgfx_calc_texture_size", FD_CALC_TEXTURE_SIZE);
		handles[DC_CREATE_TEXTURE] = downcall(lookup, "bgfx_create_texture", FD_CREATE_TEXTURE);
		handles[DC_CREATE_TEXTURE_2D] = downcall(lookup, "bgfx_create_texture_2d", FD_CREATE_TEXTURE_2D);
		handles[DC_CREATE_TEXTURE_2D_SCALED] = downcall(lookup, "bgfx_create_texture_2d_scaled", FD_CREATE_TEXTURE_2D_SCALED);
		handles[DC_CREATE_TEXTURE_3D] = downcall(lookup, "bgfx_create_texture_3d", FD_CREATE_TEXTURE_3D);
		handles[DC_CREATE_TEXTURE_CUBE] = downcall(lookup, "bgfx_create_texture_cube", FD_CREATE_TEXTURE_CUBE);
		handles[DC_UPDATE_TEXTURE_2D] = downcall(lookup, "bgfx_update_texture_2d", FD_UPDATE_TEXTURE_2D);
		handles[DC_UPDATE_TEXTURE_3D] = downcall(lookup, "bgfx_update_texture_3d", FD_UPDATE_TEXTURE_3D);
		handles[DC_UPDATE_TEXTURE_CUBE] = downcall(lookup, "bgfx_update_texture_cube", FD_UPDATE_TEXTURE_CUBE);
		handles[DC_CLEAR_TEXTURE] = downcall(lookup, "bgfx_clear_texture", FD_CLEAR_TEXTURE);
		handles[DC_READ_TEXTURE] = downcall(lookup, "bgfx_read_texture", FD_READ_TEXTURE);
		handles[DC_SET_TEXTURE_NAME] = downcall(lookup, "bgfx_set_texture_name", FD_SET_TEXTURE_NAME);
		handles[DC_GET_DIRECT_ACCESS_PTR] = downcall(lookup, "bgfx_get_direct_access_ptr", FD_GET_DIRECT_ACCESS_PTR);
		handles[DC_DESTROY_TEXTURE] = downcall(lookup, "bgfx_destroy_texture", FD_DESTROY_TEXTURE);
		handles[DC_CREATE_FRAME_BUFFER] = downcall(lookup, "bgfx_create_frame_buffer", FD_CREATE_FRAME_BUFFER);
		handles[DC_CREATE_FRAME_BUFFER_SCALED] = downcall(lookup, "bgfx_create_frame_buffer_scaled", FD_CREATE_FRAME_BUFFER_SCALED);
		handles[DC_CREATE_FRAME_BUFFER_FROM_HANDLES] = downcall(lookup, "bgfx_create_frame_buffer_from_handles", FD_CREATE_FRAME_BUFFER_FROM_HANDLES);
		handles[DC_CREATE_FRAME_BUFFER_FROM_ATTACHMENT] = downcall(lookup, "bgfx_create_frame_buffer_from_attachment", FD_CREATE_FRAME_BUFFER_FROM_ATTACHMENT);
		handles[DC_CREATE_FRAME_BUFFER_FROM_NWH] = downcall(lookup, "bgfx_create_frame_buffer_from_nwh", FD_CREATE_FRAME_BUFFER_FROM_NWH);
		handles[DC_SET_FRAME_BUFFER_NAME] = downcall(lookup, "bgfx_set_frame_buffer_name", FD_SET_FRAME_BUFFER_NAME);
		handles[DC_GET_TEXTURE] = downcall(lookup, "bgfx_get_texture", FD_GET_TEXTURE);
		handles[DC_DESTROY_FRAME_BUFFER] = downcall(lookup, "bgfx_destroy_frame_buffer", FD_DESTROY_FRAME_BUFFER);
		handles[DC_CREATE_UNIFORM] = downcall(lookup, "bgfx_create_uniform", FD_CREATE_UNIFORM);
		handles[DC_CREATE_UNIFORM_WITH_FREQ] = downcall(lookup, "bgfx_create_uniform_with_freq", FD_CREATE_UNIFORM_WITH_FREQ);
		handles[DC_GET_UNIFORM_INFO] = downcall(lookup, "bgfx_get_uniform_info", FD_GET_UNIFORM_INFO);
		handles[DC_DESTROY_UNIFORM] = downcall(lookup, "bgfx_destroy_uniform", FD_DESTROY_UNIFORM);
		handles[DC_CREATE_OCCLUSION_QUERY] = downcall(lookup, "bgfx_create_occlusion_query", FD_CREATE_OCCLUSION_QUERY);
		handles[DC_GET_RESULT] = downcall(lookup, "bgfx_get_result", FD_GET_RESULT);
		handles[DC_DESTROY_OCCLUSION_QUERY] = downcall(lookup, "bgfx_destroy_occlusion_query", FD_DESTROY_OCCLUSION_QUERY);
		handles[DC_SET_PALETTE_COLOR] = downcall(lookup, "bgfx_set_palette_color", FD_SET_PALETTE_COLOR);
		handles[DC_SET_PALETTE_COLOR_RGBA32F] = downcall(lookup, "bgfx_set_palette_color_rgba32f", FD_SET_PALETTE_COLOR_RGBA32F);
		handles[DC_SET_PALETTE_COLOR_RGBA8] = downcall(lookup, "bgfx_set_palette_color_rgba8", FD_SET_PALETTE_COLOR_RGBA8);
		handles[DC_SET_VIEW_NAME] = downcall(lookup, "bgfx_set_view_name", FD_SET_VIEW_NAME);
		handles[DC_SET_VIEW_RECT] = downcall(lookup, "bgfx_set_view_rect", FD_SET_VIEW_RECT);
		handles[DC_SET_VIEW_RECT_RATIO] = downcall(lookup, "bgfx_set_view_rect_ratio", FD_SET_VIEW_RECT_RATIO);
		handles[DC_SET_VIEW_SCISSOR] = downcall(lookup, "bgfx_set_view_scissor", FD_SET_VIEW_SCISSOR);
		handles[DC_SET_VIEW_CLEAR] = downcall(lookup, "bgfx_set_view_clear", FD_SET_VIEW_CLEAR);
		handles[DC_SET_VIEW_CLEAR_MRT] = downcall(lookup, "bgfx_set_view_clear_mrt", FD_SET_VIEW_CLEAR_MRT);
		handles[DC_SET_VIEW_MODE] = downcall(lookup, "bgfx_set_view_mode", FD_SET_VIEW_MODE);
		handles[DC_SET_VIEW_FRAME_BUFFER] = downcall(lookup, "bgfx_set_view_frame_buffer", FD_SET_VIEW_FRAME_BUFFER);
		handles[DC_SET_VIEW_TRANSFORM] = downcall(lookup, "bgfx_set_view_transform", FD_SET_VIEW_TRANSFORM);
		handles[DC_SET_VIEW_ORDER] = downcall(lookup, "bgfx_set_view_order", FD_SET_VIEW_ORDER);
		handles[DC_SET_VIEW_SHADING_RATE] = downcall(lookup, "bgfx_set_view_shading_rate", FD_SET_VIEW_SHADING_RATE);
		handles[DC_RESET_VIEW] = downcall(lookup, "bgfx_reset_view", FD_RESET_VIEW);
		handles[DC_ENCODER_BEGIN] = downcall(lookup, "bgfx_encoder_begin", FD_ENCODER_BEGIN);
		handles[DC_ENCODER_END] = downcall(lookup, "bgfx_encoder_end", FD_ENCODER_END);
		handles[DC_ENCODER_SET_MARKER] = downcall(lookup, "bgfx_encoder_set_marker", FD_ENCODER_SET_MARKER);
		handles[DC_ENCODER_SET_STATE] = downcall(lookup, "bgfx_encoder_set_state", FD_ENCODER_SET_STATE);
		handles[DC_ENCODER_SET_CONDITION] = downcall(lookup, "bgfx_encoder_set_condition", FD_ENCODER_SET_CONDITION);
		handles[DC_ENCODER_SET_STENCIL] = downcall(lookup, "bgfx_encoder_set_stencil", FD_ENCODER_SET_STENCIL);
		handles[DC_ENCODER_SET_SCISSOR] = downcall(lookup, "bgfx_encoder_set_scissor", FD_ENCODER_SET_SCISSOR);
		handles[DC_ENCODER_SET_SCISSOR_CACHED] = downcall(lookup, "bgfx_encoder_set_scissor_cached", FD_ENCODER_SET_SCISSOR_CACHED);
		handles[DC_ENCODER_SET_TRANSFORM] = downcall(lookup, "bgfx_encoder_set_transform", FD_ENCODER_SET_TRANSFORM);
		handles[DC_ENCODER_SET_TRANSFORM_CACHED] = downcall(lookup, "bgfx_encoder_set_transform_cached", FD_ENCODER_SET_TRANSFORM_CACHED);
		handles[DC_ENCODER_ALLOC_TRANSFORM] = downcall(lookup, "bgfx_encoder_alloc_transform", FD_ENCODER_ALLOC_TRANSFORM);
		handles[DC_ENCODER_SET_UNIFORM] = downcall(lookup, "bgfx_encoder_set_uniform", FD_ENCODER_SET_UNIFORM);
		handles[DC_SET_VIEW_UNIFORM] = downcall(lookup, "bgfx_set_view_uniform", FD_SET_VIEW_UNIFORM);
		handles[DC_SET_FRAME_UNIFORM] = downcall(lookup, "bgfx_set_frame_uniform", FD_SET_FRAME_UNIFORM);
		handles[DC_ENCODER_SET_INDEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_index_buffer", FD_ENCODER_SET_INDEX_BUFFER);
		handles[DC_ENCODER_SET_DYNAMIC_INDEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_dynamic_index_buffer", FD_ENCODER_SET_DYNAMIC_INDEX_BUFFER);
		handles[DC_ENCODER_SET_TRANSIENT_INDEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_transient_index_buffer", FD_ENCODER_SET_TRANSIENT_INDEX_BUFFER);
		handles[DC_ENCODER_SET_VERTEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_vertex_buffer", FD_ENCODER_SET_VERTEX_BUFFER);
		handles[DC_ENCODER_SET_VERTEX_BUFFER_WITH_LAYOUT] = downcall(lookup, "bgfx_encoder_set_vertex_buffer_with_layout", FD_ENCODER_SET_VERTEX_BUFFER_WITH_LAYOUT);
		handles[DC_ENCODER_SET_DYNAMIC_VERTEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_dynamic_vertex_buffer", FD_ENCODER_SET_DYNAMIC_VERTEX_BUFFER);
		handles[DC_ENCODER_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT] = downcall(lookup, "bgfx_encoder_set_dynamic_vertex_buffer_with_layout", FD_ENCODER_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT);
		handles[DC_ENCODER_SET_TRANSIENT_VERTEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_transient_vertex_buffer", FD_ENCODER_SET_TRANSIENT_VERTEX_BUFFER);
		handles[DC_ENCODER_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT] = downcall(lookup, "bgfx_encoder_set_transient_vertex_buffer_with_layout", FD_ENCODER_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT);
		handles[DC_ENCODER_SET_VERTEX_COUNT] = downcall(lookup, "bgfx_encoder_set_vertex_count", FD_ENCODER_SET_VERTEX_COUNT);
		handles[DC_ENCODER_SET_INSTANCE_DATA_BUFFER] = downcall(lookup, "bgfx_encoder_set_instance_data_buffer", FD_ENCODER_SET_INSTANCE_DATA_BUFFER);
		handles[DC_ENCODER_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_instance_data_from_vertex_buffer", FD_ENCODER_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER);
		handles[DC_ENCODER_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_instance_data_from_dynamic_vertex_buffer", FD_ENCODER_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER);
		handles[DC_ENCODER_SET_INSTANCE_COUNT] = downcall(lookup, "bgfx_encoder_set_instance_count", FD_ENCODER_SET_INSTANCE_COUNT);
		handles[DC_ENCODER_SET_TEXTURE] = downcall(lookup, "bgfx_encoder_set_texture", FD_ENCODER_SET_TEXTURE);
		handles[DC_ENCODER_SET_TEXTURE_VIEW] = downcall(lookup, "bgfx_encoder_set_texture_view", FD_ENCODER_SET_TEXTURE_VIEW);
		handles[DC_ENCODER_TOUCH] = downcall(lookup, "bgfx_encoder_touch", FD_ENCODER_TOUCH);
		handles[DC_ENCODER_SUBMIT] = downcall(lookup, "bgfx_encoder_submit", FD_ENCODER_SUBMIT);
		handles[DC_ENCODER_SUBMIT_OCCLUSION_QUERY] = downcall(lookup, "bgfx_encoder_submit_occlusion_query", FD_ENCODER_SUBMIT_OCCLUSION_QUERY);
		handles[DC_ENCODER_SUBMIT_INDIRECT] = downcall(lookup, "bgfx_encoder_submit_indirect", FD_ENCODER_SUBMIT_INDIRECT);
		handles[DC_ENCODER_SUBMIT_INDIRECT_COUNT] = downcall(lookup, "bgfx_encoder_submit_indirect_count", FD_ENCODER_SUBMIT_INDIRECT_COUNT);
		handles[DC_ENCODER_SET_COMPUTE_INDEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_compute_index_buffer", FD_ENCODER_SET_COMPUTE_INDEX_BUFFER);
		handles[DC_ENCODER_SET_COMPUTE_VERTEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_compute_vertex_buffer", FD_ENCODER_SET_COMPUTE_VERTEX_BUFFER);
		handles[DC_ENCODER_SET_COMPUTE_DYNAMIC_INDEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_compute_dynamic_index_buffer", FD_ENCODER_SET_COMPUTE_DYNAMIC_INDEX_BUFFER);
		handles[DC_ENCODER_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER] = downcall(lookup, "bgfx_encoder_set_compute_dynamic_vertex_buffer", FD_ENCODER_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER);
		handles[DC_ENCODER_SET_COMPUTE_INDIRECT_BUFFER] = downcall(lookup, "bgfx_encoder_set_compute_indirect_buffer", FD_ENCODER_SET_COMPUTE_INDIRECT_BUFFER);
		handles[DC_ENCODER_SET_IMAGE] = downcall(lookup, "bgfx_encoder_set_image", FD_ENCODER_SET_IMAGE);
		handles[DC_ENCODER_SET_IMAGE_VIEW] = downcall(lookup, "bgfx_encoder_set_image_view", FD_ENCODER_SET_IMAGE_VIEW);
		handles[DC_ENCODER_DISPATCH] = downcall(lookup, "bgfx_encoder_dispatch", FD_ENCODER_DISPATCH);
		handles[DC_ENCODER_DISPATCH_INDIRECT] = downcall(lookup, "bgfx_encoder_dispatch_indirect", FD_ENCODER_DISPATCH_INDIRECT);
		handles[DC_ENCODER_DISCARD] = downcall(lookup, "bgfx_encoder_discard", FD_ENCODER_DISCARD);
		handles[DC_ENCODER_BLIT] = downcall(lookup, "bgfx_encoder_blit", FD_ENCODER_BLIT);
		handles[DC_ENCODER_BLIT_BUFFER] = downcall(lookup, "bgfx_encoder_blit_buffer", FD_ENCODER_BLIT_BUFFER);
		handles[DC_ENCODER_BLIT_TO_BUFFER] = downcall(lookup, "bgfx_encoder_blit_to_buffer", FD_ENCODER_BLIT_TO_BUFFER);
		handles[DC_ENCODER_BLIT_FROM_BUFFER] = downcall(lookup, "bgfx_encoder_blit_from_buffer", FD_ENCODER_BLIT_FROM_BUFFER);
		handles[DC_REQUEST_SCREEN_SHOT] = downcall(lookup, "bgfx_request_screen_shot", FD_REQUEST_SCREEN_SHOT);
		handles[DC_RENDER_FRAME] = downcall(lookup, "bgfx_render_frame", FD_RENDER_FRAME);
		handles[DC_SET_PLATFORM_DATA] = downcall(lookup, "bgfx_set_platform_data", FD_SET_PLATFORM_DATA);
		handles[DC_GET_INTERNAL_DATA] = downcall(lookup, "bgfx_get_internal_data", FD_GET_INTERNAL_DATA);
		handles[DC_OVERRIDE_INTERNAL_TEXTURE_PTR] = downcall(lookup, "bgfx_override_internal_texture_ptr", FD_OVERRIDE_INTERNAL_TEXTURE_PTR);
		handles[DC_OVERRIDE_INTERNAL_TEXTURE] = downcall(lookup, "bgfx_override_internal_texture", FD_OVERRIDE_INTERNAL_TEXTURE);
		handles[DC_SET_MARKER] = downcall(lookup, "bgfx_set_marker", FD_SET_MARKER);
		handles[DC_SET_STATE] = downcall(lookup, "bgfx_set_state", FD_SET_STATE);
		handles[DC_SET_CONDITION] = downcall(lookup, "bgfx_set_condition", FD_SET_CONDITION);
		handles[DC_SET_STENCIL] = downcall(lookup, "bgfx_set_stencil", FD_SET_STENCIL);
		handles[DC_SET_SCISSOR] = downcall(lookup, "bgfx_set_scissor", FD_SET_SCISSOR);
		handles[DC_SET_SCISSOR_CACHED] = downcall(lookup, "bgfx_set_scissor_cached", FD_SET_SCISSOR_CACHED);
		handles[DC_SET_TRANSFORM] = downcall(lookup, "bgfx_set_transform", FD_SET_TRANSFORM);
		handles[DC_SET_TRANSFORM_CACHED] = downcall(lookup, "bgfx_set_transform_cached", FD_SET_TRANSFORM_CACHED);
		handles[DC_ALLOC_TRANSFORM] = downcall(lookup, "bgfx_alloc_transform", FD_ALLOC_TRANSFORM);
		handles[DC_SET_UNIFORM] = downcall(lookup, "bgfx_set_uniform", FD_SET_UNIFORM);
		handles[DC_SET_INDEX_BUFFER] = downcall(lookup, "bgfx_set_index_buffer", FD_SET_INDEX_BUFFER);
		handles[DC_SET_DYNAMIC_INDEX_BUFFER] = downcall(lookup, "bgfx_set_dynamic_index_buffer", FD_SET_DYNAMIC_INDEX_BUFFER);
		handles[DC_SET_TRANSIENT_INDEX_BUFFER] = downcall(lookup, "bgfx_set_transient_index_buffer", FD_SET_TRANSIENT_INDEX_BUFFER);
		handles[DC_SET_VERTEX_BUFFER] = downcall(lookup, "bgfx_set_vertex_buffer", FD_SET_VERTEX_BUFFER);
		handles[DC_SET_VERTEX_BUFFER_WITH_LAYOUT] = downcall(lookup, "bgfx_set_vertex_buffer_with_layout", FD_SET_VERTEX_BUFFER_WITH_LAYOUT);
		handles[DC_SET_DYNAMIC_VERTEX_BUFFER] = downcall(lookup, "bgfx_set_dynamic_vertex_buffer", FD_SET_DYNAMIC_VERTEX_BUFFER);
		handles[DC_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT] = downcall(lookup, "bgfx_set_dynamic_vertex_buffer_with_layout", FD_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT);
		handles[DC_SET_TRANSIENT_VERTEX_BUFFER] = downcall(lookup, "bgfx_set_transient_vertex_buffer", FD_SET_TRANSIENT_VERTEX_BUFFER);
		handles[DC_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT] = downcall(lookup, "bgfx_set_transient_vertex_buffer_with_layout", FD_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT);
		handles[DC_SET_VERTEX_COUNT] = downcall(lookup, "bgfx_set_vertex_count", FD_SET_VERTEX_COUNT);
		handles[DC_SET_INSTANCE_DATA_BUFFER] = downcall(lookup, "bgfx_set_instance_data_buffer", FD_SET_INSTANCE_DATA_BUFFER);
		handles[DC_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER] = downcall(lookup, "bgfx_set_instance_data_from_vertex_buffer", FD_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER);
		handles[DC_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER] = downcall(lookup, "bgfx_set_instance_data_from_dynamic_vertex_buffer", FD_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER);
		handles[DC_SET_INSTANCE_COUNT] = downcall(lookup, "bgfx_set_instance_count", FD_SET_INSTANCE_COUNT);
		handles[DC_SET_TEXTURE] = downcall(lookup, "bgfx_set_texture", FD_SET_TEXTURE);
		handles[DC_SET_TEXTURE_VIEW] = downcall(lookup, "bgfx_set_texture_view", FD_SET_TEXTURE_VIEW);
		handles[DC_TOUCH] = downcall(lookup, "bgfx_touch", FD_TOUCH);
		handles[DC_SUBMIT] = downcall(lookup, "bgfx_submit", FD_SUBMIT);
		handles[DC_SUBMIT_OCCLUSION_QUERY] = downcall(lookup, "bgfx_submit_occlusion_query", FD_SUBMIT_OCCLUSION_QUERY);
		handles[DC_SUBMIT_INDIRECT] = downcall(lookup, "bgfx_submit_indirect", FD_SUBMIT_INDIRECT);
		handles[DC_SUBMIT_INDIRECT_COUNT] = downcall(lookup, "bgfx_submit_indirect_count", FD_SUBMIT_INDIRECT_COUNT);
		handles[DC_SET_COMPUTE_INDEX_BUFFER] = downcall(lookup, "bgfx_set_compute_index_buffer", FD_SET_COMPUTE_INDEX_BUFFER);
		handles[DC_SET_COMPUTE_VERTEX_BUFFER] = downcall(lookup, "bgfx_set_compute_vertex_buffer", FD_SET_COMPUTE_VERTEX_BUFFER);
		handles[DC_SET_COMPUTE_DYNAMIC_INDEX_BUFFER] = downcall(lookup, "bgfx_set_compute_dynamic_index_buffer", FD_SET_COMPUTE_DYNAMIC_INDEX_BUFFER);
		handles[DC_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER] = downcall(lookup, "bgfx_set_compute_dynamic_vertex_buffer", FD_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER);
		handles[DC_SET_COMPUTE_INDIRECT_BUFFER] = downcall(lookup, "bgfx_set_compute_indirect_buffer", FD_SET_COMPUTE_INDIRECT_BUFFER);
		handles[DC_SET_IMAGE] = downcall(lookup, "bgfx_set_image", FD_SET_IMAGE);
		handles[DC_SET_IMAGE_VIEW] = downcall(lookup, "bgfx_set_image_view", FD_SET_IMAGE_VIEW);
		handles[DC_DISPATCH] = downcall(lookup, "bgfx_dispatch", FD_DISPATCH);
		handles[DC_DISPATCH_INDIRECT] = downcall(lookup, "bgfx_dispatch_indirect", FD_DISPATCH_INDIRECT);
		handles[DC_DISCARD] = downcall(lookup, "bgfx_discard", FD_DISCARD);
		handles[DC_BLIT] = downcall(lookup, "bgfx_blit", FD_BLIT);
		handles[DC_BLIT_BUFFER] = downcall(lookup, "bgfx_blit_buffer", FD_BLIT_BUFFER);
		handles[DC_BLIT_TO_BUFFER] = downcall(lookup, "bgfx_blit_to_buffer", FD_BLIT_TO_BUFFER);
		handles[DC_BLIT_FROM_BUFFER] = downcall(lookup, "bgfx_blit_from_buffer", FD_BLIT_FROM_BUFFER);
		return handles;
	}

	private static MemorySegment[] linkVariadicSymbols(SymbolLookup lookup) {
		MemorySegment[] addresses = new MemorySegment[1];
		addresses[VC_DBG_TEXT_PRINTF] = symbol(lookup, "bgfx_dbg_text_printf");
		return addresses;
	}

}
