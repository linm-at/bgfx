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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Modern Java FFM bindings for the bgfx C99 API.
 * <p>
 * Call {@link #load(Path)}, {@link #load(String)}, or {@link #link()} before
 * invoking a native method. Linking resolves every native entry point eagerly.
 */
@SuppressWarnings("restricted")
public final class BGFX {

	private static final Linker LINKER = Linker.nativeLinker();
	private static final ValueLayout C_UINTPTR_T =
		(ValueLayout) LINKER.canonicalLayouts().get("size_t");
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

	private static MethodHandle upcallTarget(
		Class<?> owner, String name, MethodType type) {
		try {
			return MethodHandles.lookup().findVirtual(owner, name, type);
		} catch (NoSuchMethodException | IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	private static MethodHandle downcallHandle(int index) {
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

	private static Object invoke(int index, Object... args) {
		return invoke(downcallHandle(index), args);
	}

	private static Object invoke(MethodHandle handle, Object... args) {
		try {
			return handle.invokeWithArguments(args);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static RuntimeException invocationFailure(Throwable exception) {
		if (exception instanceof RuntimeException runtime) {
			return runtime;
		}
		if (exception instanceof Error error) {
			throw error;
		}
		throw new AssertionError("Unexpected native invocation failure", exception);
	}

	private static MemorySegment slice(MethodHandle handle, MemorySegment segment) {
		try {
			return (MemorySegment) handle.invokeExact(segment, 0L);
		} catch (RuntimeException | Error ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new AssertionError("Unexpected layout slice failure", ex);
		}
	}

	private static StructLayout cStruct(String name, MemoryLayout... members) {
		List<MemoryLayout> elements = new ArrayList<>();
		long offset = 0;
		long alignment = 1;
		for (MemoryLayout member : members) {
			long memberAlignment = member.byteAlignment();
			long padding = (memberAlignment - offset % memberAlignment) % memberAlignment;
			if (padding != 0) {
				elements.add(MemoryLayout.paddingLayout(padding));
				offset += padding;
			}
			elements.add(member);
			offset += member.byteSize();
			alignment = Math.max(alignment, memberAlignment);
		}
		long padding = (alignment - offset % alignment) % alignment;
		if (padding != 0) {
			elements.add(MemoryLayout.paddingLayout(padding));
		}
		return MemoryLayout.structLayout(elements.toArray(MemoryLayout[]::new))
			.withByteAlignment(alignment)
			.withName(name);
	}

	private static MemorySegment view(MemorySegment segment, MemoryLayout layout) {
		Objects.requireNonNull(segment, "segment");
		if (segment.address() == 0) {
			return MemorySegment.NULL;
		}
		if (segment.byteSize() == 0) {
			return segment.reinterpret(layout.byteSize());
		}
		if (segment.byteSize() < layout.byteSize()) {
			throw new IllegalArgumentException("Segment is smaller than " + layout);
		}
		return segment.asSlice(0, layout.byteSize());
	}

	private static MemorySegment address(MemorySegment segment) {
		return segment == null ? MemorySegment.NULL : segment;
	}

	private static MemorySegment address(NativeObject object) {
		return object == null ? MemorySegment.NULL : object.segment();
	}

	private static MemorySegment cString(SegmentAllocator allocator, String value) {
		return value == null ? MemorySegment.NULL : allocator.allocateFrom(value);
	}

	private static String readString(MemorySegment address) {
		return address.address() == 0 ? null : address.reinterpret(Long.MAX_VALUE).getString(0);
	}

	private static Object nativeUintptr(long value) {
		if (C_UINTPTR_T.carrier() == long.class) {
			return value;
		}
		return (int) value;
	}

	private static long javaUintptr(Object value) {
		return value instanceof Long val ? val : Integer.toUnsignedLong((Integer) value);
	}

	/** Base class for native-backed bgfx structures and opaque objects. */
	public static abstract class NativeObject {
		private final MemorySegment segment;

		/**
		 * Wraps an opaque native address or an already-sized segment.
		 * @param segment native memory segment
		 */
		protected NativeObject(MemorySegment segment) {
			this.segment = Objects.requireNonNull(segment, "segment");
		}

		/**
		 * Wraps a native segment using the supplied structure layout.
		 * @param segment native memory segment
		 * @param layout native structure layout
		 */
		protected NativeObject(MemorySegment segment, MemoryLayout layout) {
			this(view(segment, layout));
		}

		/**
		 * Allocates a native structure using the supplied allocator.
		 * @param allocator destination allocator
		 * @param layout native structure layout
		 */
		protected NativeObject(SegmentAllocator allocator, MemoryLayout layout) {
			this(Objects.requireNonNull(allocator, "allocator").allocate(layout), layout);
		}

		/**
		 * Returns the wrapped native memory segment.
		 * @return the wrapped native memory segment
		 */
		public final MemorySegment segment() {
			return segment;
		}

		/**
		 * Reports whether this object wraps the null address.
		 * @return whether this object wraps the null address
		 */
		public final boolean isNull() {
			return segment().address() == 0;
		}
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
	// Generated API types. This is a deliberate output boundary so these types
	// can be emitted as separate source files without changing their emitters.
	// -------------------------------------------------------------------------
	/**
	 * Memory release callback.
	 */
	@FunctionalInterface
	public interface ReleaseFn {
		/**
		 * Native callback function descriptor.
		 */
		FunctionDescriptor DESCRIPTOR = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS);
		/**
		 * Bound callback target used to create upcall stubs.
		 */
		MethodHandle TARGET = upcallTarget(
			ReleaseFn.class, "invoke", MethodType.methodType(
				void.class, MemorySegment.class, MemorySegment.class));

		/**
		 * Invoked by native bgfx code. Implementations must not throw.
		 * @param _ptr Pointer to allocated data.
		 * @param _userData User defined data if needed.
		 */
		void invoke(MemorySegment _ptr, MemorySegment _userData);

		/**
		 * Creates an upcall stub for this callback.
		 * The arena must remain alive until bgfx can no longer invoke the callback.
		 * @param arena a caller-owned, long-lived arena
		 * @return the native function pointer
		 */
		default MemorySegment upcall(Arena arena) {
			Objects.requireNonNull(arena, "arena");
			return LINKER.upcallStub(TARGET.bindTo(this), DESCRIPTOR, arena);
		}
	}

	/**
	 * Constants for State flags.
	 */
	public static final class StateFlags {
		private StateFlags() {
		}
		/**
		 * Enable R write.
		 */
		public static final long WriteR = 0x0000000000000001L;

		/**
		 * Enable G write.
		 */
		public static final long WriteG = 0x0000000000000002L;

		/**
		 * Enable B write.
		 */
		public static final long WriteB = 0x0000000000000004L;

		/**
		 * Enable alpha write.
		 */
		public static final long WriteA = 0x0000000000000008L;

		/**
		 * Enable depth write.
		 */
		public static final long WriteZ = 0x0000004000000000L;

		/**
		 * Enable RGB write.
		 */
		public static final long WriteRgb = 0x0000000000000007L;

		/**
		 * Write all channels mask.
		 */
		public static final long WriteMask = 0x000000400000000fL;

		/**
		 * Enable depth test, less.
		 */
		public static final long DepthTestLess = 0x0000000000000010L;

		/**
		 * Enable depth test, less or equal.
		 */
		public static final long DepthTestLequal = 0x0000000000000020L;

		/**
		 * Enable depth test, equal.
		 */
		public static final long DepthTestEqual = 0x0000000000000030L;

		/**
		 * Enable depth test, greater or equal.
		 */
		public static final long DepthTestGequal = 0x0000000000000040L;

		/**
		 * Enable depth test, greater.
		 */
		public static final long DepthTestGreater = 0x0000000000000050L;

		/**
		 * Enable depth test, not equal.
		 */
		public static final long DepthTestNotequal = 0x0000000000000060L;

		/**
		 * Enable depth test, never.
		 */
		public static final long DepthTestNever = 0x0000000000000070L;

		/**
		 * Enable depth test, always.
		 */
		public static final long DepthTestAlways = 0x0000000000000080L;

		/**
		 * State flag value {@code DepthTestShift}.
		 */
		public static final long DepthTestShift = 4;

		/**
		 * State flag value {@code DepthTestMask}.
		 */
		public static final long DepthTestMask = 0x00000000000000f0L;

		/**
		 * 0, 0, 0, 0
		 */
		public static final long BlendZero = 0x0000000000001000L;

		/**
		 * 1, 1, 1, 1
		 */
		public static final long BlendOne = 0x0000000000002000L;

		/**
		 * Rs, Gs, Bs, As
		 */
		public static final long BlendSrcColor = 0x0000000000003000L;

		/**
		 * 1-Rs, 1-Gs, 1-Bs, 1-As
		 */
		public static final long BlendInvSrcColor = 0x0000000000004000L;

		/**
		 * As, As, As, As
		 */
		public static final long BlendSrcAlpha = 0x0000000000005000L;

		/**
		 * 1-As, 1-As, 1-As, 1-As
		 */
		public static final long BlendInvSrcAlpha = 0x0000000000006000L;

		/**
		 * Ad, Ad, Ad, Ad
		 */
		public static final long BlendDstAlpha = 0x0000000000007000L;

		/**
		 * 1-Ad, 1-Ad, 1-Ad ,1-Ad
		 */
		public static final long BlendInvDstAlpha = 0x0000000000008000L;

		/**
		 * Rd, Gd, Bd, Ad
		 */
		public static final long BlendDstColor = 0x0000000000009000L;

		/**
		 * 1-Rd, 1-Gd, 1-Bd, 1-Ad
		 */
		public static final long BlendInvDstColor = 0x000000000000a000L;

		/**
		 * f, f, f, 1; f = min(As, 1-Ad)
		 */
		public static final long BlendSrcAlphaSat = 0x000000000000b000L;

		/**
		 * Blend factor
		 */
		public static final long BlendFactor = 0x000000000000c000L;

		/**
		 * 1-Blend factor
		 */
		public static final long BlendInvFactor = 0x000000000000d000L;

		/**
		 * State flag value {@code BlendShift}.
		 */
		public static final long BlendShift = 12;

		/**
		 * State flag value {@code BlendMask}.
		 */
		public static final long BlendMask = 0x000000000ffff000L;

		/**
		 * Blend add: src + dst.
		 */
		public static final long BlendEquationAdd = 0x0000000000000000L;

		/**
		 * Blend subtract: src - dst.
		 */
		public static final long BlendEquationSub = 0x0000000010000000L;

		/**
		 * Blend reverse subtract: dst - src.
		 */
		public static final long BlendEquationRevsub = 0x0000000020000000L;

		/**
		 * Blend min: min(src, dst).
		 */
		public static final long BlendEquationMin = 0x0000000030000000L;

		/**
		 * Blend max: max(src, dst).
		 */
		public static final long BlendEquationMax = 0x0000000040000000L;

		/**
		 * State flag value {@code BlendEquationShift}.
		 */
		public static final long BlendEquationShift = 28;

		/**
		 * State flag value {@code BlendEquationMask}.
		 */
		public static final long BlendEquationMask = 0x00000003f0000000L;

		/**
		 * Cull clockwise triangles.
		 */
		public static final long CullCw = 0x0000001000000000L;

		/**
		 * Cull counter-clockwise triangles.
		 */
		public static final long CullCcw = 0x0000002000000000L;

		/**
		 * State flag value {@code CullShift}.
		 */
		public static final long CullShift = 36;

		/**
		 * State flag value {@code CullMask}.
		 */
		public static final long CullMask = 0x0000003000000000L;

		/**
		 * State flag value {@code AlphaRefShift}.
		 */
		public static final long AlphaRefShift = 40;

		/**
		 * State flag value {@code AlphaRefMask}.
		 */
		public static final long AlphaRefMask = 0x0000ff0000000000L;

		/**
		 * Tristrip.
		 */
		public static final long PtTristrip = 0x0001000000000000L;

		/**
		 * Lines.
		 */
		public static final long PtLines = 0x0002000000000000L;

		/**
		 * Line strip.
		 */
		public static final long PtLinestrip = 0x0003000000000000L;

		/**
		 * Points.
		 */
		public static final long PtPoints = 0x0004000000000000L;

		/**
		 * State flag value {@code PtShift}.
		 */
		public static final long PtShift = 48;

		/**
		 * State flag value {@code PtMask}.
		 */
		public static final long PtMask = 0x0007000000000000L;

		/**
		 * State flag value {@code PointSizeShift}.
		 */
		public static final long PointSizeShift = 52;

		/**
		 * State flag value {@code PointSizeMask}.
		 */
		public static final long PointSizeMask = 0x00f0000000000000L;

		/**
		 * Enable MSAA rasterization.
		 */
		public static final long Msaa = 0x0100000000000000L;

		/**
		 * Enable line AA rasterization.
		 */
		public static final long Lineaa = 0x0200000000000000L;

		/**
		 * Enable conservative rasterization.
		 */
		public static final long ConservativeRaster = 0x0400000000000000L;

		/**
		 * No state.
		 */
		public static final long None = 0x0000000000000000L;

		/**
		 * Front counter-clockwise (default is clockwise).
		 */
		public static final long FrontCcw = 0x0000008000000000L;

		/**
		 * Enable blend independent.
		 */
		public static final long BlendIndependent = 0x0000000400000000L;

		/**
		 * Enable alpha to coverage.
		 */
		public static final long BlendAlphaToCoverage = 0x0000000800000000L;

		/**
		 * Default state is write to RGB, alpha, and depth with depth test less enabled, with clockwise
		 * culling and MSAA (when writing into MSAA frame buffer, otherwise this flag is ignored).
		 */
		public static final long Default = 0x010000500000001fL;

		/**
		 * State flag value {@code Mask}.
		 */
		public static final long Mask = 0xffffffffffffffffL;

		/**
		 * State flag value {@code ReservedShift}.
		 */
		public static final long ReservedShift = 61;

		/**
		 * State flag value {@code ReservedMask}.
		 */
		public static final long ReservedMask = 0xe000000000000000L;
	}

	/**
	 * Constants for Stencil flags.
	 */
	public static final class StencilFlags {
		private StencilFlags() {
		}

		/**
		 * Stencil flag value {@code FuncRefShift}.
		 */
		public static final int FuncRefShift = 0;

		/**
		 * Stencil flag value {@code FuncRefMask}.
		 */
		public static final int FuncRefMask = 0x000000ff;

		/**
		 * Stencil flag value {@code FuncRmaskShift}.
		 */
		public static final int FuncRmaskShift = 8;

		/**
		 * Stencil flag value {@code FuncRmaskMask}.
		 */
		public static final int FuncRmaskMask = 0x0000ff00;

		/**
		 * No stencil test.
		 */
		public static final int None = 0x0000ff00;

		/**
		 * Stencil front or back mask.
		 */
		public static final int Mask = 0xffffffff;

		/**
		 * Enable stencil test, less.
		 */
		public static final int TestLess = 0x00010000;

		/**
		 * Enable stencil test, less or equal.
		 */
		public static final int TestLequal = 0x00020000;

		/**
		 * Enable stencil test, equal.
		 */
		public static final int TestEqual = 0x00030000;

		/**
		 * Enable stencil test, greater or equal.
		 */
		public static final int TestGequal = 0x00040000;

		/**
		 * Enable stencil test, greater.
		 */
		public static final int TestGreater = 0x00050000;

		/**
		 * Enable stencil test, not equal.
		 */
		public static final int TestNotequal = 0x00060000;

		/**
		 * Enable stencil test, never.
		 */
		public static final int TestNever = 0x00070000;

		/**
		 * Enable stencil test, always.
		 */
		public static final int TestAlways = 0x00080000;

		/**
		 * Stencil flag value {@code TestShift}.
		 */
		public static final int TestShift = 16;

		/**
		 * Stencil flag value {@code TestMask}.
		 */
		public static final int TestMask = 0x000f0000;

		/**
		 * Zero.
		 */
		public static final int OpFailSZero = 0x00000000;

		/**
		 * Keep.
		 */
		public static final int OpFailSKeep = 0x00100000;

		/**
		 * Replace.
		 */
		public static final int OpFailSReplace = 0x00200000;

		/**
		 * Increment and wrap.
		 */
		public static final int OpFailSIncr = 0x00300000;

		/**
		 * Increment and clamp.
		 */
		public static final int OpFailSIncrsat = 0x00400000;

		/**
		 * Decrement and wrap.
		 */
		public static final int OpFailSDecr = 0x00500000;

		/**
		 * Decrement and clamp.
		 */
		public static final int OpFailSDecrsat = 0x00600000;

		/**
		 * Invert.
		 */
		public static final int OpFailSInvert = 0x00700000;

		/**
		 * Stencil flag value {@code OpFailSShift}.
		 */
		public static final int OpFailSShift = 20;

		/**
		 * Stencil flag value {@code OpFailSMask}.
		 */
		public static final int OpFailSMask = 0x00f00000;

		/**
		 * Zero.
		 */
		public static final int OpFailZZero = 0x00000000;

		/**
		 * Keep.
		 */
		public static final int OpFailZKeep = 0x01000000;

		/**
		 * Replace.
		 */
		public static final int OpFailZReplace = 0x02000000;

		/**
		 * Increment and wrap.
		 */
		public static final int OpFailZIncr = 0x03000000;

		/**
		 * Increment and clamp.
		 */
		public static final int OpFailZIncrsat = 0x04000000;

		/**
		 * Decrement and wrap.
		 */
		public static final int OpFailZDecr = 0x05000000;

		/**
		 * Decrement and clamp.
		 */
		public static final int OpFailZDecrsat = 0x06000000;

		/**
		 * Invert.
		 */
		public static final int OpFailZInvert = 0x07000000;

		/**
		 * Stencil flag value {@code OpFailZShift}.
		 */
		public static final int OpFailZShift = 24;

		/**
		 * Stencil flag value {@code OpFailZMask}.
		 */
		public static final int OpFailZMask = 0x0f000000;

		/**
		 * Zero.
		 */
		public static final int OpPassZZero = 0x00000000;

		/**
		 * Keep.
		 */
		public static final int OpPassZKeep = 0x10000000;

		/**
		 * Replace.
		 */
		public static final int OpPassZReplace = 0x20000000;

		/**
		 * Increment and wrap.
		 */
		public static final int OpPassZIncr = 0x30000000;

		/**
		 * Increment and clamp.
		 */
		public static final int OpPassZIncrsat = 0x40000000;

		/**
		 * Decrement and wrap.
		 */
		public static final int OpPassZDecr = 0x50000000;

		/**
		 * Decrement and clamp.
		 */
		public static final int OpPassZDecrsat = 0x60000000;

		/**
		 * Invert.
		 */
		public static final int OpPassZInvert = 0x70000000;

		/**
		 * Stencil flag value {@code OpPassZShift}.
		 */
		public static final int OpPassZShift = 28;

		/**
		 * Stencil flag value {@code OpPassZMask}.
		 */
		public static final int OpPassZMask = 0xf0000000;
	}

	/**
	 * Constants for Clear flags.
	 */
	public static final class ClearFlags {
		private ClearFlags() {
		}
		/**
		 * No clear flags.
		 */
		public static final short None = (short) 0x0000;

		/**
		 * Clear color.
		 */
		public static final short Color = (short) 0x0001;

		/**
		 * Clear depth.
		 */
		public static final short Depth = (short) 0x0002;

		/**
		 * Clear stencil.
		 */
		public static final short Stencil = (short) 0x0004;

		/**
		 * Discard frame buffer attachment 0.
		 */
		public static final short DiscardColor0 = (short) 0x0008;

		/**
		 * Discard frame buffer attachment 1.
		 */
		public static final short DiscardColor1 = (short) 0x0010;

		/**
		 * Discard frame buffer attachment 2.
		 */
		public static final short DiscardColor2 = (short) 0x0020;

		/**
		 * Discard frame buffer attachment 3.
		 */
		public static final short DiscardColor3 = (short) 0x0040;

		/**
		 * Discard frame buffer attachment 4.
		 */
		public static final short DiscardColor4 = (short) 0x0080;

		/**
		 * Discard frame buffer attachment 5.
		 */
		public static final short DiscardColor5 = (short) 0x0100;

		/**
		 * Discard frame buffer attachment 6.
		 */
		public static final short DiscardColor6 = (short) 0x0200;

		/**
		 * Discard frame buffer attachment 7.
		 */
		public static final short DiscardColor7 = (short) 0x0400;

		/**
		 * Discard frame buffer depth attachment.
		 */
		public static final short DiscardDepth = (short) 0x0800;

		/**
		 * Discard frame buffer stencil attachment.
		 */
		public static final short DiscardStencil = (short) 0x1000;

		/**
		 * Clear flag value {@code DiscardColorMask}.
		 */
		public static final short DiscardColorMask = (short) 0x07f8;

		/**
		 * Clear flag value {@code DiscardMask}.
		 */
		public static final short DiscardMask = (short) 0x1ff8;
	}

	/**
	 * Rendering state discard. When state is preserved in submit, rendering states can be discarded
	 * on a finer grain.
	 */
	public static final class DiscardFlags {
		private DiscardFlags() {
		}
		/**
		 * Preserve everything.
		 */
		public static final int None = 0x00000000;

		/**
		 * Discard texture sampler and buffer bindings.
		 */
		public static final int Bindings = 0x00000001;

		/**
		 * Discard index buffer.
		 */
		public static final int IndexBuffer = 0x00000002;

		/**
		 * Discard instance data.
		 */
		public static final int InstanceData = 0x00000004;

		/**
		 * Discard state and uniform bindings.
		 */
		public static final int State = 0x00000008;

		/**
		 * Discard transform.
		 */
		public static final int Transform = 0x00000010;

		/**
		 * Discard vertex streams.
		 */
		public static final int VertexStreams = 0x00000020;

		/**
		 * Discard all states.
		 */
		public static final int All = 0x000000ff;
	}

	/**
	 * Constants for Debug flags.
	 */
	public static final class DebugFlags {
		private DebugFlags() {
		}
		/**
		 * No debug.
		 */
		public static final int None = 0x00000000;

		/**
		 * Enable wireframe for all primitives.
		 */
		public static final int Wireframe = 0x00000001;

		/**
		 * Enable infinitely fast hardware test. No draw calls will be submitted to driver.
		 * It's useful when profiling to quickly assess bottleneck between CPU and GPU.
		 */
		public static final int Ifh = 0x00000002;

		/**
		 * Enable statistics display.
		 */
		public static final int Stats = 0x00000004;

		/**
		 * Enable debug text display.
		 */
		public static final int Text = 0x00000008;

		/**
		 * Enable profiler. This causes per-view statistics to be collected, available through {@code BGFX.Stats.ViewStats}. This is unrelated to the profiler functions in {@code BGFX.CallbackI}.
		 */
		public static final int Profiler = 0x00000010;
	}

	/**
	 * Constants for Buffer flags.
	 */
	public static final class BufferFlags {
		private BufferFlags() {
		}

		/**
		 * Buffer flag value {@code None}.
		 */
		public static final short None = (short) 0x0000;

		/**
		 * Buffer will be read by shader.
		 */
		public static final short ComputeRead = (short) 0x0100;

		/**
		 * Buffer will be used for writing.
		 */
		public static final short ComputeWrite = (short) 0x0200;

		/**
		 * Buffer will be used for storing draw indirect commands.
		 */
		public static final short DrawIndirect = (short) 0x0400;

		/**
		 * Allow dynamic index/vertex buffer resize during update.
		 */
		public static final short AllowResize = (short) 0x0800;

		/**
		 * Index buffer contains 32-bit indices.
		 */
		public static final short Index32 = (short) 0x1000;

		/**
		 * Buffer flag value {@code ComputeReadWrite}.
		 */
		public static final short ComputeReadWrite = (short) 0x0300;
	}

	/**
	 * Constants for Texture flags.
	 */
	public static final class TextureFlags {
		private TextureFlags() {
		}

		/**
		 * Texture flag value {@code None}.
		 */
		public static final long None = 0x0000000000000000L;

		/**
		 * Texture will be used for MSAA sampling.
		 */
		public static final long MsaaSample = 0x0000000800000000L;

		/**
		 * Render target no MSAA.
		 */
		public static final long Rt = 0x0000001000000000L;

		/**
		 * Texture will be used for compute write.
		 */
		public static final long ComputeWrite = 0x0000100000000000L;

		/**
		 * Sample texture as sRGB.
		 */
		public static final long Srgb = 0x0000200000000000L;

		/**
		 * Texture will be used as blit destination.
		 */
		public static final long BlitDst = 0x0000400000000000L;

		/**
		 * Texture will be used for read back from GPU.
		 */
		public static final long ReadBack = 0x0000800000000000L;

		/**
		 * Texture is shared with other device or other process.
		 */
		public static final long ExternalShared = 0x0001000000000000L;

		/**
		 * Texture flag value {@code ReservedShift}.
		 */
		public static final long ReservedShift = 60;

		/**
		 * Texture flag value {@code ReservedMask}.
		 */
		public static final long ReservedMask = 0xf000000000000000L;

		/**
		 * Render target MSAAx2 mode.
		 */
		public static final long RtMsaaX2 = 0x0000002000000000L;

		/**
		 * Render target MSAAx4 mode.
		 */
		public static final long RtMsaaX4 = 0x0000003000000000L;

		/**
		 * Render target MSAAx8 mode.
		 */
		public static final long RtMsaaX8 = 0x0000004000000000L;

		/**
		 * Render target MSAAx16 mode.
		 */
		public static final long RtMsaaX16 = 0x0000005000000000L;

		/**
		 * Texture flag value {@code RtMsaaShift}.
		 */
		public static final long RtMsaaShift = 36;

		/**
		 * Texture flag value {@code RtMsaaMask}.
		 */
		public static final long RtMsaaMask = 0x0000007000000000L;

		/**
		 * Render target will be used for writing
		 */
		public static final long RtWriteOnly = 0x0000008000000000L;

		/**
		 * Texture flag value {@code RtShift}.
		 */
		public static final long RtShift = 36;

		/**
		 * Texture flag value {@code RtMask}.
		 */
		public static final long RtMask = 0x000000f000000000L;
	}

	/**
	 * Constants for Sampler flags.
	 */
	public static final class SamplerFlags {
		private SamplerFlags() {
		}
		/**
		 * Wrap U mode: Mirror
		 */
		public static final int UMirror = 0x00000001;

		/**
		 * Wrap U mode: Clamp
		 */
		public static final int UClamp = 0x00000002;

		/**
		 * Wrap U mode: Border
		 */
		public static final int UBorder = 0x00000003;

		/**
		 * Sampler flag value {@code UShift}.
		 */
		public static final int UShift = 0;

		/**
		 * Sampler flag value {@code UMask}.
		 */
		public static final int UMask = 0x00000003;

		/**
		 * Wrap V mode: Mirror
		 */
		public static final int VMirror = 0x00000004;

		/**
		 * Wrap V mode: Clamp
		 */
		public static final int VClamp = 0x00000008;

		/**
		 * Wrap V mode: Border
		 */
		public static final int VBorder = 0x0000000c;

		/**
		 * Sampler flag value {@code VShift}.
		 */
		public static final int VShift = 2;

		/**
		 * Sampler flag value {@code VMask}.
		 */
		public static final int VMask = 0x0000000c;

		/**
		 * Wrap W mode: Mirror
		 */
		public static final int WMirror = 0x00000010;

		/**
		 * Wrap W mode: Clamp
		 */
		public static final int WClamp = 0x00000020;

		/**
		 * Wrap W mode: Border
		 */
		public static final int WBorder = 0x00000030;

		/**
		 * Sampler flag value {@code WShift}.
		 */
		public static final int WShift = 4;

		/**
		 * Sampler flag value {@code WMask}.
		 */
		public static final int WMask = 0x00000030;

		/**
		 * Min sampling mode: Point
		 */
		public static final int MinPoint = 0x00000040;

		/**
		 * Min sampling mode: Anisotropic
		 */
		public static final int MinAnisotropic = 0x00000080;

		/**
		 * Sampler flag value {@code MinShift}.
		 */
		public static final int MinShift = 6;

		/**
		 * Sampler flag value {@code MinMask}.
		 */
		public static final int MinMask = 0x000000c0;

		/**
		 * Mag sampling mode: Point
		 */
		public static final int MagPoint = 0x00000100;

		/**
		 * Mag sampling mode: Anisotropic
		 */
		public static final int MagAnisotropic = 0x00000200;

		/**
		 * Sampler flag value {@code MagShift}.
		 */
		public static final int MagShift = 8;

		/**
		 * Sampler flag value {@code MagMask}.
		 */
		public static final int MagMask = 0x00000300;

		/**
		 * Mip sampling mode: Point
		 */
		public static final int MipPoint = 0x00000400;

		/**
		 * Sampler flag value {@code MipShift}.
		 */
		public static final int MipShift = 10;

		/**
		 * Sampler flag value {@code MipMask}.
		 */
		public static final int MipMask = 0x00000400;

		/**
		 * Compare when sampling depth texture: less.
		 */
		public static final int CompareLess = 0x00010000;

		/**
		 * Compare when sampling depth texture: less or equal.
		 */
		public static final int CompareLequal = 0x00020000;

		/**
		 * Compare when sampling depth texture: equal.
		 */
		public static final int CompareEqual = 0x00030000;

		/**
		 * Compare when sampling depth texture: greater or equal.
		 */
		public static final int CompareGequal = 0x00040000;

		/**
		 * Compare when sampling depth texture: greater.
		 */
		public static final int CompareGreater = 0x00050000;

		/**
		 * Compare when sampling depth texture: not equal.
		 */
		public static final int CompareNotequal = 0x00060000;

		/**
		 * Compare when sampling depth texture: never.
		 */
		public static final int CompareNever = 0x00070000;

		/**
		 * Compare when sampling depth texture: always.
		 */
		public static final int CompareAlways = 0x00080000;

		/**
		 * Sampler flag value {@code CompareShift}.
		 */
		public static final int CompareShift = 16;

		/**
		 * Sampler flag value {@code CompareMask}.
		 */
		public static final int CompareMask = 0x000f0000;

		/**
		 * Sampler flag value {@code BorderColorShift}.
		 */
		public static final int BorderColorShift = 24;

		/**
		 * Sampler flag value {@code BorderColorMask}.
		 */
		public static final int BorderColorMask = 0x0f000000;

		/**
		 * Sampler flag value {@code ReservedShift}.
		 */
		public static final int ReservedShift = 28;

		/**
		 * Sampler flag value {@code ReservedMask}.
		 */
		public static final int ReservedMask = 0xf0000000;

		/**
		 * Sampler flag value {@code None}.
		 */
		public static final int None = 0x00000000;

		/**
		 * Sample stencil instead of depth.
		 */
		public static final int SampleStencil = 0x00100000;

		/**
		 * Sampler flag value {@code Point}.
		 */
		public static final int Point = 0x00000540;

		/**
		 * Sampler flag value {@code UvwMirror}.
		 */
		public static final int UvwMirror = 0x00000015;

		/**
		 * Sampler flag value {@code UvwClamp}.
		 */
		public static final int UvwClamp = 0x0000002a;

		/**
		 * Sampler flag value {@code UvwBorder}.
		 */
		public static final int UvwBorder = 0x0000003f;

		/**
		 * Sampler flag value {@code BitsMask}.
		 */
		public static final int BitsMask = 0x000f07ff;
	}

	/**
	 * Constants for Reset flags.
	 */
	public static final class ResetFlags {
		private ResetFlags() {
		}
		/**
		 * Enable 2x MSAA.
		 */
		public static final int MsaaX2 = 0x00000010;

		/**
		 * Enable 4x MSAA.
		 */
		public static final int MsaaX4 = 0x00000020;

		/**
		 * Enable 8x MSAA.
		 */
		public static final int MsaaX8 = 0x00000030;

		/**
		 * Enable 16x MSAA.
		 */
		public static final int MsaaX16 = 0x00000040;

		/**
		 * Reset flag value {@code MsaaShift}.
		 */
		public static final int MsaaShift = 4;

		/**
		 * Reset flag value {@code MsaaMask}.
		 */
		public static final int MsaaMask = 0x00000070;

		/**
		 * No reset flags.
		 */
		public static final int None = 0x00000000;

		/**
		 * Not supported yet.
		 */
		public static final int Fullscreen = 0x00000001;

		/**
		 * Enable V-Sync.
		 */
		public static final int Vsync = 0x00000080;

		/**
		 * Turn on/off max anisotropy.
		 */
		public static final int Maxanisotropy = 0x00000100;

		/**
		 * Begin screen capture.
		 */
		public static final int Capture = 0x00000200;

		/**
		 * Flush rendering after submitting to GPU.
		 */
		public static final int FlushAfterRender = 0x00002000;

		/**
		 * This flag specifies where flip occurs. Default behaviour is that flip occurs
		 * before rendering new frame. This flag only has effect when {@code BGFX_CONFIG_MULTITHREADED=0}.
		 */
		public static final int FlipAfterRender = 0x00004000;

		/**
		 * Enable sRGB backbuffer.
		 */
		public static final int SrgbBackbuffer = 0x00008000;

		/**
		 * Enable HDR10 rendering.
		 */
		public static final int Hdr10 = 0x00010000;

		/**
		 * Enable HiDPI rendering.
		 */
		public static final int Hidpi = 0x00020000;

		/**
		 * Enable depth clamp.
		 */
		public static final int DepthClamp = 0x00040000;

		/**
		 * Suspend rendering.
		 */
		public static final int Suspend = 0x00080000;

		/**
		 * Transparent backbuffer. Availability depends on: {@code BGFX_CAPS_TRANSPARENT_BACKBUFFER}.
		 */
		public static final int TransparentBackbuffer = 0x00100000;

		/**
		 * Reset flag value {@code FullscreenShift}.
		 */
		public static final int FullscreenShift = 0;

		/**
		 * Reset flag value {@code FullscreenMask}.
		 */
		public static final int FullscreenMask = 0x00000001;

		/**
		 * Reset flag value {@code ReservedShift}.
		 */
		public static final int ReservedShift = 31;

		/**
		 * Reset flag value {@code ReservedMask}.
		 */
		public static final int ReservedMask = 0x80000000;
	}

	/**
	 * Constants for Caps flags.
	 */
	public static final class CapsFlags {
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

	/**
	 * Constants for CapsFormat flags.
	 */
	public static final class CapsFormatFlags {
		private CapsFormatFlags() {
		}
		/**
		 * Texture format is not supported.
		 */
		public static final int TextureNone = 0x00000000;

		/**
		 * Texture format is supported.
		 */
		public static final int Texture2D = 0x00000001;

		/**
		 * Texture as sRGB format is supported.
		 */
		public static final int Texture2DSrgb = 0x00000002;

		/**
		 * Texture format is emulated.
		 */
		public static final int Texture2DEmulated = 0x00000004;

		/**
		 * Texture format is supported.
		 */
		public static final int Texture3D = 0x00000008;

		/**
		 * Texture as sRGB format is supported.
		 */
		public static final int Texture3DSrgb = 0x00000010;

		/**
		 * Texture format is emulated.
		 */
		public static final int Texture3DEmulated = 0x00000020;

		/**
		 * Texture format is supported.
		 */
		public static final int TextureCube = 0x00000040;

		/**
		 * Texture as sRGB format is supported.
		 */
		public static final int TextureCubeSrgb = 0x00000080;

		/**
		 * Texture format is emulated.
		 */
		public static final int TextureCubeEmulated = 0x00000100;

		/**
		 * Texture format can be used from vertex shader.
		 */
		public static final int TextureVertex = 0x00000200;

		/**
		 * Texture format can be used as image and read from.
		 */
		public static final int TextureImageRead = 0x00000400;

		/**
		 * Texture format can be used as image and written to.
		 */
		public static final int TextureImageWrite = 0x00000800;

		/**
		 * Texture format can be used as frame buffer.
		 */
		public static final int TextureFramebuffer = 0x00001000;

		/**
		 * Texture format can be used as MSAA frame buffer.
		 */
		public static final int TextureFramebufferMsaa = 0x00002000;

		/**
		 * Texture can be sampled as MSAA.
		 */
		public static final int TextureMsaa = 0x00004000;

		/**
		 * Texture format supports auto-generated mips.
		 */
		public static final int TextureMipAutogen = 0x00008000;

		/**
		 * Texture format can be used as back buffer format.
		 */
		public static final int TextureBackbuffer = 0x00010000;

		/**
		 * Texture format can be used as video decode destination.
		 */
		public static final int TextureVideoDecodeDst = 0x00020000;
	}

	/**
	 * Constants for CapsVideoCodec flags.
	 */
	public static final class CapsVideoCodecFlags {
		private CapsVideoCodecFlags() {
		}
		/**
		 * Video codec is not supported.
		 */
		public static final int None = 0x00000000;

		/**
		 * 8-bit sample depth is supported.
		 */
		public static final int Bit8 = 0x00000001;

		/**
		 * 10-bit sample depth is supported.
		 */
		public static final int Bit10 = 0x00000002;

		/**
		 * 12-bit sample depth is supported.
		 */
		public static final int Bit12 = 0x00000004;

		/**
		 * 4:2:0 chroma subsampling is supported.
		 */
		public static final int Chroma420 = 0x00000008;

		/**
		 * 4:2:2 chroma subsampling is supported.
		 */
		public static final int Chroma422 = 0x00000010;

		/**
		 * 4:4:4 chroma subsampling is supported.
		 */
		public static final int Chroma444 = 0x00000020;
	}

	/**
	 * Video decoder lifetime flags (per {@code VideoDecoderInit.flags}).
	 */
	public static final class VideoDecoderInitFlags {
		private VideoDecoderInitFlags() {
		}
		/**
		 * No flags.
		 */
		public static final int None = 0x00000000;

		/**
		 * Cache submitted access units in driver-managed memory keyed by {@code ptsUs} so the
		 * presentation clock can revisit / loop without re-streaming. The cache is
		 * unbounded: the app picks the total cache size implicitly by choosing how
		 * many access units to submit. Without this flag access units are decoded once
		 * and dropped (streaming default).
		 */
		public static final int Retain = 0x00000001;
	}

	/**
	 * Video decoder per-frame submission flags (per {@code VideoDecoderFrame.flags}).
	 */
	public static final class VideoDecodeFrameFlags {
		private VideoDecodeFrameFlags() {
		}
		/**
		 * No flags.
		 */
		public static final int None = 0x00000000;

		/**
		 * First batch after a position change. The first access unit must be a clean IDR.
		 * Driver flushes its DPB, queued access units, and reorder pool before decoding;
		 * subsequent {@code presentationTimeUs} values may land anywhere (monotonicity is only
		 * required between non-{@code Set} ticks).
		 */
		public static final int Set = 0x00000001;

		/**
		 * Skip the picker dispatch for this call. Useful while bulk-loading access units
		 * so the displayed picture isn't churned mid-load.
		 */
		public static final int NoBlit = 0x00000002;

		/**
		 * Marks the last access unit of the clip; permits eager pre-decode in idle time
		 * and lets the picker emit the final frame without lookahead stalling.
		 */
		public static final int Final = 0x00000004;

		/**
		 * When {@code presentationTimeUs} runs past the highest cached {@code ptsUs}, the picker
		 * wraps modulo the cached pts range. Without this flag the picker freezes on
		 * the last displayable picture.
		 */
		public static final int Loop = 0x00000008;
	}

	/**
	 * Constants for Resolve flags.
	 */
	public static final class ResolveFlags {
		private ResolveFlags() {
		}
		/**
		 * No resolve flags.
		 */
		public static final int None = 0x00000000;

		/**
		 * Auto-generate mip maps on resolve.
		 */
		public static final int AutoGenMips = 0x00000001;
	}

	/**
	 * Constants for PciId flags.
	 */
	public static final class PciIdFlags {
		private PciIdFlags() {
		}
		/**
		 * Autoselect adapter.
		 */
		public static final short None = (short) 0x0000;

		/**
		 * Software rasterizer.
		 */
		public static final short SoftwareRasterizer = (short) 0x0001;

		/**
		 * AMD adapter.
		 */
		public static final short Amd = (short) 0x1002;

		/**
		 * Apple adapter.
		 */
		public static final short Apple = (short) 0x106b;

		/**
		 * Intel adapter.
		 */
		public static final short Intel = (short) 0x8086;

		/**
		 * nVidia adapter.
		 */
		public static final short Nvidia = (short) 0x10de;

		/**
		 * Microsoft adapter.
		 */
		public static final short Microsoft = (short) 0x1414;

		/**
		 * ARM adapter.
		 */
		public static final short Arm = (short) 0x13b5;
	}

	/**
	 * Constants for CubeMap flags.
	 */
	public static final class CubeMapFlags {
		private CubeMapFlags() {
		}
		/**
		 * Cubemap +x.
		 */
		public static final int PositiveX = 0x00000000;

		/**
		 * Cubemap -x.
		 */
		public static final int NegativeX = 0x00000001;

		/**
		 * Cubemap +y.
		 */
		public static final int PositiveY = 0x00000002;

		/**
		 * Cubemap -y.
		 */
		public static final int NegativeY = 0x00000003;

		/**
		 * Cubemap +z.
		 */
		public static final int PositiveZ = 0x00000004;

		/**
		 * Cubemap -z.
		 */
		public static final int NegativeZ = 0x00000005;
	}

	/**
	 * Constants for Frame flags.
	 */
	public static final class FrameFlags {
		private FrameFlags() {
		}
		/**
		 * No frame flags.
		 */
		public static final int None = 0x00000000;

		/**
		 * Capture frame with graphics debugger.
		 */
		public static final int DebugCapture = 0x00000001;

		/**
		 * Discard all draw calls.
		 */
		public static final int Discard = 0x00000002;

		/**
		 * Execute all rendering commands without presenting the backbuffer.
		 */
		public static final int Flush = 0x00000004;
	}

	/**
	 * Fatal error enum.
	 */
	public enum Fatal {
		/**
		 * Fatal value {@code DebugCheck}.
		 */
		DebugCheck,
		/**
		 * Fatal value {@code InvalidShader}.
		 */
		InvalidShader,
		/**
		 * Fatal value {@code UnableToInitialize}.
		 */
		UnableToInitialize,
		/**
		 * Fatal value {@code UnableToCreateTexture}.
		 */
		UnableToCreateTexture,
		/**
		 * Fatal value {@code DeviceLost}.
		 */
		DeviceLost,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final Fatal[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static Fatal fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown Fatal value: " + value);
		}
	}

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

	/**
	 * Access mode enum.
	 */
	public enum Access {
		/**
		 * Read.
		 */
		Read,
		/**
		 * Write.
		 */
		Write,
		/**
		 * Read and write.
		 */
		ReadWrite,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final Access[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static Access fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown Access value: " + value);
		}
	}

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

	/**
	 * Texture format enum.
	 * <p>
	 * Notation:
	 * <p>
	 *       RGBA16S
	 *       ^   ^ ^
	 *       |   | +-- [ ]Unorm
	 *       |   |     [F]loat
	 *       |   |     [S]norm
	 *       |   |     [I]nt
	 *       |   |     [U]int
	 *       |   +---- Number of bits per component
	 *       +-------- Components
	 * <p>
	 * <strong>Attention:</strong> Availability depends on Caps (see: formats).
	 */
	public enum TextureFormat {
		/**
		 * Block Compression 1. 5-bit R, 6-bit G, 5-bit B, 1-bit A. 4 BPP.
		 */
		BC1,
		/**
		 * Block Compression 2. 5-bit R, 6-bit G, 5-bit B, 4-bit explicit A. 8 BPP.
		 */
		BC2,
		/**
		 * Block Compression 3. 5-bit R, 6-bit G, 5-bit B, 8-bit interpolated A. 8 BPP.
		 */
		BC3,
		/**
		 * Block Compression 4. Single 8-bit red channel, unsigned normalized. 4 BPP.
		 */
		BC4,
		/**
		 * Block Compression 4. Single 8-bit red channel, signed normalized. 4 BPP.
		 */
		BC4S,
		/**
		 * Block Compression 5. Two 8-bit channels (RG), unsigned normalized. 8 BPP.
		 */
		BC5,
		/**
		 * Block Compression 5. Two 8-bit channels (RG), signed normalized. 8 BPP.
		 */
		BC5S,
		/**
		 * Block Compression 6H. Three 16-bit floating-point channels (RGB), HDR. 8 BPP.
		 */
		BC6H,
		/**
		 * Block Compression 6H. Three 16-bit unsigned floating-point channels (RGB), HDR. 8 BPP.
		 */
		BC6HU,
		/**
		 * RGB 4-7 bits per color channel, 0-8 bits alpha. Block Compression 7. High-quality RGBA, 4-7 bits per color, 0-8 bits alpha. 8 BPP.
		 */
		BC7,
		/**
		 * Ericsson Texture Compression 1. 8-bit per channel RGB. 4 BPP.
		 */
		ETC1,
		/**
		 * Ericsson Texture Compression 2. 8-bit per channel RGB. 4 BPP.
		 */
		ETC2,
		/**
		 * Ericsson Texture Compression 2 with full alpha. 8-bit per channel RGBA. 8 BPP.
		 */
		ETC2A,
		/**
		 * Ericsson Texture Compression 2 with 1-bit punch-through alpha. 4 BPP.
		 */
		ETC2A1,
		/**
		 * ETC2 Alpha Compression, single 11-bit red channel, unsigned normalized. 4 BPP.
		 */
		EACR11,
		/**
		 * ETC2 Alpha Compression, single 11-bit red channel, signed normalized. 4 BPP.
		 */
		EACR11S,
		/**
		 * ETC2 Alpha Compression, two 11-bit channels (RG), unsigned normalized. 8 BPP.
		 */
		EACRG11,
		/**
		 * ETC2 Alpha Compression, two 11-bit channels (RG), signed normalized. 8 BPP.
		 */
		EACRG11S,
		/**
		 * PowerVR Texture Compression v1. 3-channel RGB. 2 BPP.
		 */
		PTC12,
		/**
		 * PowerVR Texture Compression v1. 3-channel RGB. 4 BPP.
		 */
		PTC14,
		/**
		 * PowerVR Texture Compression v1. 4-channel RGBA. 2 BPP.
		 */
		PTC12A,
		/**
		 * PowerVR Texture Compression v1. 4-channel RGBA. 4 BPP.
		 */
		PTC14A,
		/**
		 * PowerVR Texture Compression v2. 4-channel RGBA. 2 BPP.
		 */
		PTC22,
		/**
		 * PowerVR Texture Compression v2. 4-channel RGBA. 4 BPP.
		 */
		PTC24,
		/**
		 * AMD Texture Compression. 3-channel RGB. 4 BPP.
		 */
		ATC,
		/**
		 * AMD Texture Compression with explicit alpha. 4-channel RGBA. 8 BPP.
		 */
		ATCE,
		/**
		 * AMD Texture Compression with interpolated alpha. 4-channel RGBA. 8 BPP.
		 */
		ATCI,
		/**
		 * Adaptive Scalable Texture Compression, 4x4 block, RGBA. 8.00 BPP.
		 */
		ASTC4x4,
		/**
		 * Adaptive Scalable Texture Compression, 5x4 block, RGBA. 6.40 BPP.
		 */
		ASTC5x4,
		/**
		 * Adaptive Scalable Texture Compression, 5x5 block, RGBA. 5.12 BPP.
		 */
		ASTC5x5,
		/**
		 * Adaptive Scalable Texture Compression, 6x5 block, RGBA. 4.27 BPP.
		 */
		ASTC6x5,
		/**
		 * Adaptive Scalable Texture Compression, 6x6 block, RGBA. 3.56 BPP.
		 */
		ASTC6x6,
		/**
		 * Adaptive Scalable Texture Compression, 8x5 block, RGBA. 3.20 BPP.
		 */
		ASTC8x5,
		/**
		 * Adaptive Scalable Texture Compression, 8x6 block, RGBA. 2.67 BPP.
		 */
		ASTC8x6,
		/**
		 * Adaptive Scalable Texture Compression, 8x8 block, RGBA. 2.00 BPP.
		 */
		ASTC8x8,
		/**
		 * Adaptive Scalable Texture Compression, 10x5 block, RGBA. 2.56 BPP.
		 */
		ASTC10x5,
		/**
		 * Adaptive Scalable Texture Compression, 10x6 block, RGBA. 2.13 BPP.
		 */
		ASTC10x6,
		/**
		 * Adaptive Scalable Texture Compression, 10x8 block, RGBA. 1.60 BPP.
		 */
		ASTC10x8,
		/**
		 * Adaptive Scalable Texture Compression, 10x10 block, RGBA. 1.28 BPP.
		 */
		ASTC10x10,
		/**
		 * Adaptive Scalable Texture Compression, 12x10 block, RGBA. 1.07 BPP.
		 */
		ASTC12x10,
		/**
		 * Adaptive Scalable Texture Compression, 12x12 block, RGBA. 0.89 BPP.
		 */
		ASTC12x12,
		/**
		 * Compressed formats above.
		 */
		Unknown,
		/**
		 * 1-bit single-channel red. Monochrome, 1-bit per pixel. 1 BPP.
		 */
		R1,
		/**
		 * 8-bit single-channel alpha, unsigned normalized. 8 BPP.
		 */
		A8,
		/**
		 * 8-bit single-channel red, unsigned normalized. 8 BPP.
		 */
		R8,
		/**
		 * 8-bit single-channel red, signed integer. 8 BPP.
		 */
		R8I,
		/**
		 * 8-bit single-channel red, unsigned integer. 8 BPP.
		 */
		R8U,
		/**
		 * 8-bit single-channel red, signed normalized. 8 BPP.
		 */
		R8S,
		/**
		 * 16-bit single-channel red, unsigned normalized. 16 BPP.
		 */
		R16,
		/**
		 * 16-bit single-channel red, signed integer. 16 BPP.
		 */
		R16I,
		/**
		 * 16-bit single-channel red, unsigned integer. 16 BPP.
		 */
		R16U,
		/**
		 * 16-bit single-channel red, half-precision floating point. 16 BPP.
		 */
		R16F,
		/**
		 * 16-bit single-channel red, signed normalized. 16 BPP.
		 */
		R16S,
		/**
		 * 32-bit single-channel red, signed integer. 32 BPP.
		 */
		R32I,
		/**
		 * 32-bit single-channel red, unsigned integer. 32 BPP.
		 */
		R32U,
		/**
		 * 32-bit single-channel red, full-precision floating point. 32 BPP.
		 */
		R32F,
		/**
		 * Two 8-bit channels (red, green), unsigned normalized. 16 BPP.
		 */
		RG8,
		/**
		 * Two 8-bit channels (red, green), signed integer. 16 BPP.
		 */
		RG8I,
		/**
		 * Two 8-bit channels (red, green), unsigned integer. 16 BPP.
		 */
		RG8U,
		/**
		 * Two 8-bit channels (red, green), signed normalized. 16 BPP.
		 */
		RG8S,
		/**
		 * Two 16-bit channels (red, green), unsigned normalized. 32 BPP.
		 */
		RG16,
		/**
		 * Two 16-bit channels (red, green), signed integer. 32 BPP.
		 */
		RG16I,
		/**
		 * Two 16-bit channels (red, green), unsigned integer. 32 BPP.
		 */
		RG16U,
		/**
		 * Two 16-bit channels (red, green), half-precision floating point. 32 BPP.
		 */
		RG16F,
		/**
		 * Two 16-bit channels (red, green), signed normalized. 32 BPP.
		 */
		RG16S,
		/**
		 * Two 32-bit channels (red, green), signed integer. 64 BPP.
		 */
		RG32I,
		/**
		 * Two 32-bit channels (red, green), unsigned integer. 64 BPP.
		 */
		RG32U,
		/**
		 * Two 32-bit channels (red, green), full-precision floating point. 64 BPP.
		 */
		RG32F,
		/**
		 * Three 8-bit channels (red, green, blue), unsigned normalized. 24 BPP.
		 */
		RGB8,
		/**
		 * Three 8-bit channels (red, green, blue), signed integer. 24 BPP.
		 */
		RGB8I,
		/**
		 * Three 8-bit channels (red, green, blue), unsigned integer. 24 BPP.
		 */
		RGB8U,
		/**
		 * Three 8-bit channels (red, green, blue), signed normalized. 24 BPP.
		 */
		RGB8S,
		/**
		 * Shared-exponent RGB. 9 bits per RGB channel with a shared 5-bit exponent, floating point. 32 BPP.
		 */
		RGB9E5F,
		/**
		 * Four 8-bit channels (blue, green, red, alpha), unsigned normalized. BGRA byte order. 32 BPP.
		 */
		BGRA8,
		/**
		 * Four 8-bit channels (red, green, blue, alpha), unsigned normalized. 32 BPP.
		 */
		RGBA8,
		/**
		 * Four 8-bit channels (red, green, blue, alpha), signed integer. 32 BPP.
		 */
		RGBA8I,
		/**
		 * Four 8-bit channels (red, green, blue, alpha), unsigned integer. 32 BPP.
		 */
		RGBA8U,
		/**
		 * Four 8-bit channels (red, green, blue, alpha), signed normalized. 32 BPP.
		 */
		RGBA8S,
		/**
		 * Four 16-bit channels (red, green, blue, alpha), unsigned normalized. 64 BPP.
		 */
		RGBA16,
		/**
		 * Four 16-bit channels (red, green, blue, alpha), signed integer. 64 BPP.
		 */
		RGBA16I,
		/**
		 * Four 16-bit channels (red, green, blue, alpha), unsigned integer. 64 BPP.
		 */
		RGBA16U,
		/**
		 * Four 16-bit channels (red, green, blue, alpha), half-precision floating point. 64 BPP.
		 */
		RGBA16F,
		/**
		 * Four 16-bit channels (red, green, blue, alpha), signed normalized. 64 BPP.
		 */
		RGBA16S,
		/**
		 * Four 32-bit channels (red, green, blue, alpha), signed integer. 128 BPP.
		 */
		RGBA32I,
		/**
		 * Four 32-bit channels (red, green, blue, alpha), unsigned integer. 128 BPP.
		 */
		RGBA32U,
		/**
		 * Four 32-bit channels (red, green, blue, alpha), full-precision floating point. 128 BPP.
		 */
		RGBA32F,
		/**
		 * Packed 16-bit, 5-bit blue, 6-bit green, 5-bit red. BGR byte order, unsigned normalized. 16 BPP.
		 */
		B5G6R5,
		/**
		 * Packed 16-bit, 5-bit red, 6-bit green, 5-bit blue. RGB byte order, unsigned normalized. 16 BPP.
		 */
		R5G6B5,
		/**
		 * Packed 16-bit, 4-bit per channel (blue, green, red, alpha). BGRA byte order, unsigned normalized. 16 BPP.
		 */
		BGRA4,
		/**
		 * Packed 16-bit, 4-bit per channel (red, green, blue, alpha), unsigned normalized. 16 BPP.
		 */
		RGBA4,
		/**
		 * Packed 16-bit, 5-bit blue, 5-bit green, 5-bit red, 1-bit alpha. BGRA byte order, unsigned normalized. 16 BPP.
		 */
		BGR5A1,
		/**
		 * Packed 16-bit, 5-bit red, 5-bit green, 5-bit blue, 1-bit alpha, unsigned normalized. 16 BPP.
		 */
		RGB5A1,
		/**
		 * Packed 32-bit, 10-bit red, 10-bit green, 10-bit blue, 2-bit alpha, unsigned normalized. 32 BPP.
		 */
		RGB10A2,
		/**
		 * Packed 32-bit, 10-bit red, 10-bit green, 10-bit blue, 2-bit alpha, unsigned integer. 32 BPP.
		 */
		RGB10A2U,
		/**
		 * Packed 32-bit, 11-bit red, 11-bit green, 10-bit blue, unsigned floating point. No alpha. 32 BPP.
		 */
		RG11B10F,
		/**
		 * Depth formats below.
		 */
		UnknownDepth,
		/**
		 * 16-bit depth, unsigned normalized. 16 BPP.
		 */
		D16,
		/**
		 * 24-bit depth, unsigned normalized (stored as 32-bit with 8 bits unused). 32 BPP.
		 */
		D24,
		/**
		 * 24-bit depth, unsigned normalized, with 8-bit stencil. 32 BPP.
		 */
		D24S8,
		/**
		 * 32-bit depth, unsigned normalized. 32 BPP.
		 */
		D32,
		/**
		 * 16-bit depth, floating point. 16 BPP.
		 */
		D16F,
		/**
		 * 24-bit depth, floating point (stored as 32-bit). 32 BPP.
		 */
		D24F,
		/**
		 * 32-bit depth, floating point. 32 BPP.
		 */
		D32F,
		/**
		 * 32-bit depth, floating point, with 8-bit stencil (stored as 64-bit). 64 BPP.
		 */
		D32FS8,
		/**
		 * 8-bit stencil only, no depth. 8 BPP.
		 */
		D0S8,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final TextureFormat[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static TextureFormat fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown TextureFormat value: " + value);
		}
	}

	/**
	 * Uniform type enum.
	 */
	public enum UniformType {
		/**
		 * Sampler.
		 */
		Sampler,
		/**
		 * Reserved, do not use.
		 */
		End,
		/**
		 * 4 floats vector.
		 */
		Vec4,
		/**
		 * 3x3 matrix.
		 */
		Mat3,
		/**
		 * 4x4 matrix.
		 */
		Mat4,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final UniformType[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static UniformType fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown UniformType value: " + value);
		}
	}

	/**
	 * Uniform frequency enum.
	 */
	public enum UniformFreq {
		/**
		 * Changing per draw call.
		 */
		Draw,
		/**
		 * Changing per view.
		 */
		View,
		/**
		 * Changing per frame.
		 */
		Frame,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final UniformFreq[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static UniformFreq fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown UniformFreq value: " + value);
		}
	}

	/**
	 * Backbuffer ratio enum.
	 */
	public enum BackbufferRatio {
		/**
		 * Equal to backbuffer.
		 */
		Equal,
		/**
		 * One half size of backbuffer.
		 */
		Half,
		/**
		 * One quarter size of backbuffer.
		 */
		Quarter,
		/**
		 * One eighth size of backbuffer.
		 */
		Eighth,
		/**
		 * One sixteenth size of backbuffer.
		 */
		Sixteenth,
		/**
		 * Double size of backbuffer.
		 */
		Double,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final BackbufferRatio[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static BackbufferRatio fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown BackbufferRatio value: " + value);
		}
	}

	/**
	 * Occlusion query result.
	 */
	public enum OcclusionQueryResult {
		/**
		 * Query failed test.
		 */
		Invisible,
		/**
		 * Query passed test.
		 */
		Visible,
		/**
		 * Query result is not available yet.
		 */
		NoResult,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final OcclusionQueryResult[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static OcclusionQueryResult fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown OcclusionQueryResult value: " + value);
		}
	}

	/**
	 * Video codec enum.
	 */
	public enum VideoCodec {
		/**
		 * H.264 / AVC.
		 */
		H264,
		/**
		 * H.265 / HEVC.
		 */
		H265,
		/**
		 * AV1.
		 */
		AV1,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final VideoCodec[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static VideoCodec fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown VideoCodec value: " + value);
		}
	}

	/**
	 * Primitive topology.
	 */
	public enum Topology {
		/**
		 * Triangle list.
		 */
		TriList,
		/**
		 * Triangle strip.
		 */
		TriStrip,
		/**
		 * Line list.
		 */
		LineList,
		/**
		 * Line strip.
		 */
		LineStrip,
		/**
		 * Point list.
		 */
		PointList,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final Topology[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static Topology fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown Topology value: " + value);
		}
	}

	/**
	 * Topology conversion function.
	 */
	public enum TopologyConvert {
		/**
		 * Flip winding order of triangle list.
		 */
		TriListFlipWinding,
		/**
		 * Flip winding order of triangle strip.
		 */
		TriStripFlipWinding,
		/**
		 * Convert triangle list to line list.
		 */
		TriListToLineList,
		/**
		 * Convert triangle strip to triangle list.
		 */
		TriStripToTriList,
		/**
		 * Convert line strip to line list.
		 */
		LineStripToLineList,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final TopologyConvert[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static TopologyConvert fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown TopologyConvert value: " + value);
		}
	}

	/**
	 * Topology sort order.
	 */
	public enum TopologySort {
		/**
		 * TopologySort value {@code DirectionFrontToBackMin}.
		 */
		DirectionFrontToBackMin,
		/**
		 * TopologySort value {@code DirectionFrontToBackAvg}.
		 */
		DirectionFrontToBackAvg,
		/**
		 * TopologySort value {@code DirectionFrontToBackMax}.
		 */
		DirectionFrontToBackMax,
		/**
		 * TopologySort value {@code DirectionBackToFrontMin}.
		 */
		DirectionBackToFrontMin,
		/**
		 * TopologySort value {@code DirectionBackToFrontAvg}.
		 */
		DirectionBackToFrontAvg,
		/**
		 * TopologySort value {@code DirectionBackToFrontMax}.
		 */
		DirectionBackToFrontMax,
		/**
		 * TopologySort value {@code DistanceFrontToBackMin}.
		 */
		DistanceFrontToBackMin,
		/**
		 * TopologySort value {@code DistanceFrontToBackAvg}.
		 */
		DistanceFrontToBackAvg,
		/**
		 * TopologySort value {@code DistanceFrontToBackMax}.
		 */
		DistanceFrontToBackMax,
		/**
		 * TopologySort value {@code DistanceBackToFrontMin}.
		 */
		DistanceBackToFrontMin,
		/**
		 * TopologySort value {@code DistanceBackToFrontAvg}.
		 */
		DistanceBackToFrontAvg,
		/**
		 * TopologySort value {@code DistanceBackToFrontMax}.
		 */
		DistanceBackToFrontMax,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final TopologySort[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static TopologySort fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown TopologySort value: " + value);
		}
	}

	/**
	 * View mode sets draw call sort order.
	 */
	public enum ViewMode {
		/**
		 * Default sort order.
		 */
		Default,
		/**
		 * Sort in the same order in which submit calls were called.
		 */
		Sequential,
		/**
		 * Sort draw call depth in ascending order.
		 */
		DepthAscending,
		/**
		 * Sort draw call depth in descending order.
		 */
		DepthDescending,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final ViewMode[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static ViewMode fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown ViewMode value: " + value);
		}
	}

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

	/**
	 * Native window handle type.
	 */
	public enum NativeWindowHandleType {
		/**
		 * Platform default handle type (X11 on Linux).
		 */
		Default,
		/**
		 * Wayland.
		 */
		Wayland,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final NativeWindowHandleType[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static NativeWindowHandleType fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown NativeWindowHandleType value: " + value);
		}
	}

	/**
	 * Render frame enum.
	 */
	public enum RenderFrame {
		/**
		 * Renderer context is not created yet.
		 */
		NoContext,
		/**
		 * Renderer context is created and rendering.
		 */
		Render,
		/**
		 * Renderer context wait for main thread signal timed out without rendering.
		 */
		Timeout,
		/**
		 * Renderer context is getting destroyed.
		 */
		Exiting,

		/**
		 * Number of native enum values.
		 */
		Count;

		/**
		 * Native C enum layout.
		 */
		public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;
		private static final RenderFrame[] VALUES = values();

		/**
		 * Returns the enum constant for a native C enum value.
		 * @param value the native enum value
		 * @return the matching enum constant
		 */
		public static RenderFrame fromValue(int value) {
			if (value >= 0 && value < VALUES.length) {
				return VALUES[value];
			}
			throw new IllegalArgumentException("Unknown RenderFrame value: " + value);
		}
	}

	/**
	 * Renderer capabilities.
	 */
	public static final class Caps extends NativeObject {
		/**
		 * GPU info.
		 */
		public static final class GPU extends NativeObject {
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

		/**
		 * Renderer runtime limits.
		 */
		public static final class Limits extends NativeObject {
			/**
			 * Native C structure layout.
			 */
			public static final StructLayout LAYOUT = cStruct("bgfx_caps_limits_t",
				ValueLayout.JAVA_INT.withName("maxDrawCalls"),
				ValueLayout.JAVA_INT.withName("maxBlits"),
				ValueLayout.JAVA_INT.withName("maxTextureSize"),
				ValueLayout.JAVA_INT.withName("maxTextureLayers"),
				ValueLayout.JAVA_INT.withName("maxViews"),
				ValueLayout.JAVA_INT.withName("maxFrameBuffers"),
				ValueLayout.JAVA_INT.withName("maxFBAttachments"),
				ValueLayout.JAVA_INT.withName("maxPrograms"),
				ValueLayout.JAVA_INT.withName("maxShaders"),
				ValueLayout.JAVA_INT.withName("maxTextures"),
				ValueLayout.JAVA_INT.withName("maxTextureSamplers"),
				ValueLayout.JAVA_INT.withName("maxComputeBindings"),
				ValueLayout.JAVA_INT.withName("maxVertexLayouts"),
				ValueLayout.JAVA_INT.withName("maxVertexStreams"),
				ValueLayout.JAVA_INT.withName("maxVertexAttributes"),
				ValueLayout.JAVA_INT.withName("maxInstanceData"),
				ValueLayout.JAVA_INT.withName("maxIndexBuffers"),
				ValueLayout.JAVA_INT.withName("maxVertexBuffers"),
				ValueLayout.JAVA_INT.withName("maxDynamicIndexBuffers"),
				ValueLayout.JAVA_INT.withName("maxDynamicVertexBuffers"),
				ValueLayout.JAVA_INT.withName("maxUniforms"),
				ValueLayout.JAVA_INT.withName("maxOcclusionQueries"),
				ValueLayout.JAVA_INT.withName("maxEncoders"),
				ValueLayout.JAVA_INT.withName("minResourceCbSize"),
				ValueLayout.JAVA_INT.withName("maxTransientVbSize"),
				ValueLayout.JAVA_INT.withName("maxTransientIbSize"),
				ValueLayout.JAVA_INT.withName("minUniformBufferSize"),
				ValueLayout.JAVA_INT.withName("blitRowPitchAlign"),
				ValueLayout.JAVA_INT.withName("blitOffsetAlign"));
			private static final VarHandle VH_MAXDRAWCALLS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxDrawCalls"));
			private static final VarHandle VH_MAXBLITS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxBlits"));
			private static final VarHandle VH_MAXTEXTURESIZE = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxTextureSize"));
			private static final VarHandle VH_MAXTEXTURELAYERS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxTextureLayers"));
			private static final VarHandle VH_MAXVIEWS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxViews"));
			private static final VarHandle VH_MAXFRAMEBUFFERS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxFrameBuffers"));
			private static final VarHandle VH_MAXFBATTACHMENTS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxFBAttachments"));
			private static final VarHandle VH_MAXPROGRAMS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxPrograms"));
			private static final VarHandle VH_MAXSHADERS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxShaders"));
			private static final VarHandle VH_MAXTEXTURES = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxTextures"));
			private static final VarHandle VH_MAXTEXTURESAMPLERS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxTextureSamplers"));
			private static final VarHandle VH_MAXCOMPUTEBINDINGS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxComputeBindings"));
			private static final VarHandle VH_MAXVERTEXLAYOUTS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxVertexLayouts"));
			private static final VarHandle VH_MAXVERTEXSTREAMS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxVertexStreams"));
			private static final VarHandle VH_MAXVERTEXATTRIBUTES = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxVertexAttributes"));
			private static final VarHandle VH_MAXINSTANCEDATA = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxInstanceData"));
			private static final VarHandle VH_MAXINDEXBUFFERS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxIndexBuffers"));
			private static final VarHandle VH_MAXVERTEXBUFFERS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxVertexBuffers"));
			private static final VarHandle VH_MAXDYNAMICINDEXBUFFERS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxDynamicIndexBuffers"));
			private static final VarHandle VH_MAXDYNAMICVERTEXBUFFERS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxDynamicVertexBuffers"));
			private static final VarHandle VH_MAXUNIFORMS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxUniforms"));
			private static final VarHandle VH_MAXOCCLUSIONQUERIES = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxOcclusionQueries"));
			private static final VarHandle VH_MAXENCODERS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxEncoders"));
			private static final VarHandle VH_MINRESOURCECBSIZE = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("minResourceCbSize"));
			private static final VarHandle VH_MAXTRANSIENTVBSIZE = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxTransientVbSize"));
			private static final VarHandle VH_MAXTRANSIENTIBSIZE = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxTransientIbSize"));
			private static final VarHandle VH_MINUNIFORMBUFFERSIZE = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("minUniformBufferSize"));
			private static final VarHandle VH_BLITROWPITCHALIGN = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("blitRowPitchAlign"));
			private static final VarHandle VH_BLITOFFSETALIGN = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("blitOffsetAlign"));
			/**
			 * Wraps an existing native structure.
			 * @param segment native memory segment
			 */
			public Limits(MemorySegment segment) {
				super(segment, LAYOUT);
			}

			/**
			 * Allocates a native structure.
			 * @param allocator destination allocator
			 */
			public Limits(SegmentAllocator allocator) {
				super(allocator, LAYOUT);
			}

			/**
			 * Maximum number of draw calls.
			 * @return the field value
			 */
			public int maxDrawCalls() {
				return (int) VH_MAXDRAWCALLS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxDrawCalls} field.
			 * @param value the new field value
			 */
			public void maxDrawCalls(int value) {
				VH_MAXDRAWCALLS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of blit calls.
			 * @return the field value
			 */
			public int maxBlits() {
				return (int) VH_MAXBLITS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxBlits} field.
			 * @param value the new field value
			 */
			public void maxBlits(int value) {
				VH_MAXBLITS.set(segment(), 0L, value);
			}

			/**
			 * Maximum texture size.
			 * @return the field value
			 */
			public int maxTextureSize() {
				return (int) VH_MAXTEXTURESIZE.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxTextureSize} field.
			 * @param value the new field value
			 */
			public void maxTextureSize(int value) {
				VH_MAXTEXTURESIZE.set(segment(), 0L, value);
			}

			/**
			 * Maximum texture layers.
			 * @return the field value
			 */
			public int maxTextureLayers() {
				return (int) VH_MAXTEXTURELAYERS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxTextureLayers} field.
			 * @param value the new field value
			 */
			public void maxTextureLayers(int value) {
				VH_MAXTEXTURELAYERS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of views.
			 * @return the field value
			 */
			public int maxViews() {
				return (int) VH_MAXVIEWS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxViews} field.
			 * @param value the new field value
			 */
			public void maxViews(int value) {
				VH_MAXVIEWS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of frame buffer handles.
			 * @return the field value
			 */
			public int maxFrameBuffers() {
				return (int) VH_MAXFRAMEBUFFERS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxFrameBuffers} field.
			 * @param value the new field value
			 */
			public void maxFrameBuffers(int value) {
				VH_MAXFRAMEBUFFERS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of frame buffer attachments.
			 * @return the field value
			 */
			public int maxFBAttachments() {
				return (int) VH_MAXFBATTACHMENTS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxFBAttachments} field.
			 * @param value the new field value
			 */
			public void maxFBAttachments(int value) {
				VH_MAXFBATTACHMENTS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of program handles.
			 * @return the field value
			 */
			public int maxPrograms() {
				return (int) VH_MAXPROGRAMS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxPrograms} field.
			 * @param value the new field value
			 */
			public void maxPrograms(int value) {
				VH_MAXPROGRAMS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of shader handles.
			 * @return the field value
			 */
			public int maxShaders() {
				return (int) VH_MAXSHADERS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxShaders} field.
			 * @param value the new field value
			 */
			public void maxShaders(int value) {
				VH_MAXSHADERS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of texture handles.
			 * @return the field value
			 */
			public int maxTextures() {
				return (int) VH_MAXTEXTURES.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxTextures} field.
			 * @param value the new field value
			 */
			public void maxTextures(int value) {
				VH_MAXTEXTURES.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of texture samplers.
			 * @return the field value
			 */
			public int maxTextureSamplers() {
				return (int) VH_MAXTEXTURESAMPLERS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxTextureSamplers} field.
			 * @param value the new field value
			 */
			public void maxTextureSamplers(int value) {
				VH_MAXTEXTURESAMPLERS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of compute bindings.
			 * @return the field value
			 */
			public int maxComputeBindings() {
				return (int) VH_MAXCOMPUTEBINDINGS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxComputeBindings} field.
			 * @param value the new field value
			 */
			public void maxComputeBindings(int value) {
				VH_MAXCOMPUTEBINDINGS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of vertex format layouts.
			 * @return the field value
			 */
			public int maxVertexLayouts() {
				return (int) VH_MAXVERTEXLAYOUTS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxVertexLayouts} field.
			 * @param value the new field value
			 */
			public void maxVertexLayouts(int value) {
				VH_MAXVERTEXLAYOUTS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of vertex streams.
			 * @return the field value
			 */
			public int maxVertexStreams() {
				return (int) VH_MAXVERTEXSTREAMS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxVertexStreams} field.
			 * @param value the new field value
			 */
			public void maxVertexStreams(int value) {
				VH_MAXVERTEXSTREAMS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of vertex attributes.
			 * @return the field value
			 */
			public int maxVertexAttributes() {
				return (int) VH_MAXVERTEXATTRIBUTES.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxVertexAttributes} field.
			 * @param value the new field value
			 */
			public void maxVertexAttributes(int value) {
				VH_MAXVERTEXATTRIBUTES.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of instance data slots.
			 * @return the field value
			 */
			public int maxInstanceData() {
				return (int) VH_MAXINSTANCEDATA.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxInstanceData} field.
			 * @param value the new field value
			 */
			public void maxInstanceData(int value) {
				VH_MAXINSTANCEDATA.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of index buffer handles.
			 * @return the field value
			 */
			public int maxIndexBuffers() {
				return (int) VH_MAXINDEXBUFFERS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxIndexBuffers} field.
			 * @param value the new field value
			 */
			public void maxIndexBuffers(int value) {
				VH_MAXINDEXBUFFERS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of vertex buffer handles.
			 * @return the field value
			 */
			public int maxVertexBuffers() {
				return (int) VH_MAXVERTEXBUFFERS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxVertexBuffers} field.
			 * @param value the new field value
			 */
			public void maxVertexBuffers(int value) {
				VH_MAXVERTEXBUFFERS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of dynamic index buffer handles.
			 * @return the field value
			 */
			public int maxDynamicIndexBuffers() {
				return (int) VH_MAXDYNAMICINDEXBUFFERS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxDynamicIndexBuffers} field.
			 * @param value the new field value
			 */
			public void maxDynamicIndexBuffers(int value) {
				VH_MAXDYNAMICINDEXBUFFERS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of dynamic vertex buffer handles.
			 * @return the field value
			 */
			public int maxDynamicVertexBuffers() {
				return (int) VH_MAXDYNAMICVERTEXBUFFERS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxDynamicVertexBuffers} field.
			 * @param value the new field value
			 */
			public void maxDynamicVertexBuffers(int value) {
				VH_MAXDYNAMICVERTEXBUFFERS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of uniform handles.
			 * @return the field value
			 */
			public int maxUniforms() {
				return (int) VH_MAXUNIFORMS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxUniforms} field.
			 * @param value the new field value
			 */
			public void maxUniforms(int value) {
				VH_MAXUNIFORMS.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of occlusion query handles.
			 * @return the field value
			 */
			public int maxOcclusionQueries() {
				return (int) VH_MAXOCCLUSIONQUERIES.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxOcclusionQueries} field.
			 * @param value the new field value
			 */
			public void maxOcclusionQueries(int value) {
				VH_MAXOCCLUSIONQUERIES.set(segment(), 0L, value);
			}

			/**
			 * Maximum number of encoder threads.
			 * @return the field value
			 */
			public int maxEncoders() {
				return (int) VH_MAXENCODERS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxEncoders} field.
			 * @param value the new field value
			 */
			public void maxEncoders(int value) {
				VH_MAXENCODERS.set(segment(), 0L, value);
			}

			/**
			 * Minimum resource command buffer size.
			 * @return the field value
			 */
			public int minResourceCbSize() {
				return (int) VH_MINRESOURCECBSIZE.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code minResourceCbSize} field.
			 * @param value the new field value
			 */
			public void minResourceCbSize(int value) {
				VH_MINRESOURCECBSIZE.set(segment(), 0L, value);
			}

			/**
			 * Maximum transient vertex buffer size.
			 * @return the field value
			 */
			public int maxTransientVbSize() {
				return (int) VH_MAXTRANSIENTVBSIZE.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxTransientVbSize} field.
			 * @param value the new field value
			 */
			public void maxTransientVbSize(int value) {
				VH_MAXTRANSIENTVBSIZE.set(segment(), 0L, value);
			}

			/**
			 * Maximum transient index buffer size.
			 * @return the field value
			 */
			public int maxTransientIbSize() {
				return (int) VH_MAXTRANSIENTIBSIZE.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxTransientIbSize} field.
			 * @param value the new field value
			 */
			public void maxTransientIbSize(int value) {
				VH_MAXTRANSIENTIBSIZE.set(segment(), 0L, value);
			}

			/**
			 * Mimimum uniform buffer size.
			 * @return the field value
			 */
			public int minUniformBufferSize() {
				return (int) VH_MINUNIFORMBUFFERSIZE.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code minUniformBufferSize} field.
			 * @param value the new field value
			 */
			public void minUniformBufferSize(int value) {
				VH_MINUNIFORMBUFFERSIZE.set(segment(), 0L, value);
			}

			/**
			 * Row pitch alignment, in bytes, that buffer to texture blit copies
			 * natively. Any other {@code BufferRegion.rowPitch} is repacked internally.
			 * @return the field value
			 */
			public int blitRowPitchAlign() {
				return (int) VH_BLITROWPITCHALIGN.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code blitRowPitchAlign} field.
			 * @param value the new field value
			 */
			public void blitRowPitchAlign(int value) {
				VH_BLITROWPITCHALIGN.set(segment(), 0L, value);
			}

			/**
			 * Offset alignment, in bytes, that buffer to texture blit copies
			 * natively. Any other {@code BufferRegion.offset} is repacked internally.
			 * @return the field value
			 */
			public int blitOffsetAlign() {
				return (int) VH_BLITOFFSETALIGN.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code blitOffsetAlign} field.
			 * @param value the new field value
			 */
			public void blitOffsetAlign(int value) {
				VH_BLITOFFSETALIGN.set(segment(), 0L, value);
			}
		}

		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_caps_t",
			ValueLayout.JAVA_INT.withName("rendererType"),
			ValueLayout.JAVA_LONG.withName("supported"),
			ValueLayout.JAVA_SHORT.withName("vendorId"),
			ValueLayout.JAVA_SHORT.withName("deviceId"),
			ValueLayout.JAVA_BOOLEAN.withName("homogeneousDepth"),
			ValueLayout.JAVA_BOOLEAN.withName("originBottomLeft"),
			ValueLayout.JAVA_BYTE.withName("numGPUs"),
			MemoryLayout.sequenceLayout(4, Caps.GPU.LAYOUT).withName("gpu"),
			Caps.Limits.LAYOUT.withName("limits"),
			MemoryLayout.sequenceLayout(105, ValueLayout.JAVA_INT).withName("formats"),
			MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT).withName("codecs"));
		private static final VarHandle VH_RENDERERTYPE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("rendererType"));
		private static final VarHandle VH_SUPPORTED = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("supported"));
		private static final VarHandle VH_VENDORID = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("vendorId"));
		private static final VarHandle VH_DEVICEID = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("deviceId"));
		private static final VarHandle VH_HOMOGENEOUSDEPTH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("homogeneousDepth"));
		private static final VarHandle VH_ORIGINBOTTOMLEFT = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("originBottomLeft"));
		private static final VarHandle VH_NUMGPUS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numGPUs"));
		private static final MethodHandle MH_GPU = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("gpu"));
		private static final MethodHandle MH_LIMITS = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("limits"));
		private static final MethodHandle MH_FORMATS = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("formats"));
		private static final MethodHandle MH_CODECS = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("codecs"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public Caps(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public Caps(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Renderer backend type. See: {@code BGFX.RendererType}
		 * @return the field value
		 */
		public RendererType rendererType() {
			return RendererType.fromValue((int) VH_RENDERERTYPE.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code rendererType} field.
		 * @param value the new field value
		 */
		public void rendererType(RendererType value) {
			VH_RENDERERTYPE.set(segment(), 0L, value.ordinal());
		}

		/**
		 * Supported functionality.
		 * <strong>Attention:</strong> See {@code BGFX_CAPS_*} flags at https://bkaradzic.github.io/bgfx/bgfx.html#available-caps
		 * @return the field value
		 */
		public long supported() {
			return (long) VH_SUPPORTED.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code supported} field.
		 * @param value the new field value
		 */
		public void supported(long value) {
			VH_SUPPORTED.set(segment(), 0L, value);
		}

		/**
		 * Selected GPU vendor PCI id.
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
		 * Selected GPU device id.
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
		 * True when NDC depth is in [-1, 1] range, otherwise its [0, 1].
		 * @return the field value
		 */
		public boolean homogeneousDepth() {
			return (boolean) VH_HOMOGENEOUSDEPTH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code homogeneousDepth} field.
		 * @param value the new field value
		 */
		public void homogeneousDepth(boolean value) {
			VH_HOMOGENEOUSDEPTH.set(segment(), 0L, value);
		}

		/**
		 * True when NDC origin is at bottom left.
		 * @return the field value
		 */
		public boolean originBottomLeft() {
			return (boolean) VH_ORIGINBOTTOMLEFT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code originBottomLeft} field.
		 * @param value the new field value
		 */
		public void originBottomLeft(boolean value) {
			VH_ORIGINBOTTOMLEFT.set(segment(), 0L, value);
		}

		/**
		 * Number of enumerated GPUs.
		 * @return the field value
		 */
		public byte numGPUs() {
			return (byte) VH_NUMGPUS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numGPUs} field.
		 * @param value the new field value
		 */
		public void numGPUs(byte value) {
			VH_NUMGPUS.set(segment(), 0L, value);
		}

		/**
		 * Enumerated GPUs.
		 * @return a segment view of the inline array
		 */
		public MemorySegment gpu() {
			return slice(MH_GPU, segment());
		}

		/**
		 * Renderer runtime limits.
		 * @return the field value
		 */
		public Caps.Limits limits() {
			return new Caps.Limits(slice(MH_LIMITS, segment()));
		}

		/**
		 * Sets the native {@code limits} field.
		 * @param value the new field value
		 */
		public void limits(Caps.Limits value) {
			slice(MH_LIMITS, segment()).copyFrom(value.segment());
		}

		/**
		 * Supported texture format capabilities flags:
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_NONE} - Texture format is not supported.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_2D} - Texture format is supported.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_2D_SRGB} - Texture as sRGB format is supported.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_2D_EMULATED} - Texture format is emulated.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_3D} - Texture format is supported.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_3D_SRGB} - Texture as sRGB format is supported.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_3D_EMULATED} - Texture format is emulated.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_CUBE} - Texture format is supported.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_CUBE_SRGB} - Texture as sRGB format is supported.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_CUBE_EMULATED} - Texture format is emulated.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_VERTEX} - Texture format can be used from vertex shader.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_IMAGE_READ} - Texture format can be used as image
		 *     and read from.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_IMAGE_WRITE} - Texture format can be used as image
		 *     and written to.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_FRAMEBUFFER} - Texture format can be used as frame
		 *     buffer.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_FRAMEBUFFER_MSAA} - Texture format can be used as MSAA
		 *     frame buffer.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_MSAA} - Texture can be sampled as MSAA.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_MIP_AUTOGEN} - Texture format supports auto-generated
		 *     mips.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_BACKBUFFER} - Texture format can be used as back buffer format.
		 *   - {@code BGFX_CAPS_FORMAT_TEXTURE_VIDEO_DECODE_DST} - Texture format can be used as video
		 *     decode destination.
		 * @return a segment view of the inline array
		 */
		public MemorySegment formats() {
			return slice(MH_FORMATS, segment());
		}

		/**
		 * Supported video codec capabilities flags. A non-zero entry means the codec is
		 * supported for hardware decode; bits describe sample depths and chroma
		 * subsamplings:
		 *   - {@code BGFX_CAPS_VIDEO_CODEC_NONE} - Video codec is not supported.
		 *   - {@code BGFX_CAPS_VIDEO_CODEC_BIT_8} - 8-bit sample depth is supported.
		 *   - {@code BGFX_CAPS_VIDEO_CODEC_BIT_10} - 10-bit sample depth is supported.
		 *   - {@code BGFX_CAPS_VIDEO_CODEC_BIT_12} - 12-bit sample depth is supported.
		 *   - {@code BGFX_CAPS_VIDEO_CODEC_CHROMA_420} - 4:2:0 chroma subsampling is supported.
		 *   - {@code BGFX_CAPS_VIDEO_CODEC_CHROMA_422} - 4:2:2 chroma subsampling is supported.
		 *   - {@code BGFX_CAPS_VIDEO_CODEC_CHROMA_444} - 4:4:4 chroma subsampling is supported.
		 * @return a segment view of the inline array
		 */
		public MemorySegment codecs() {
			return slice(MH_CODECS, segment());
		}
	}

	/**
	 * Internal data.
	 */
	public static final class InternalData extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_internal_data_t",
			ValueLayout.ADDRESS.withName("caps"),
			ValueLayout.ADDRESS.withName("context"));
		private static final VarHandle VH_CAPS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("caps"));
		private static final VarHandle VH_CONTEXT = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("context"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public InternalData(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public InternalData(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Renderer capabilities.
		 * @return the field value
		 */
		public Caps caps() {
			return new Caps((MemorySegment) VH_CAPS.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code caps} field.
		 * @param value the new field value
		 */
		public void caps(Caps value) {
			VH_CAPS.set(segment(), 0L, address(value));
		}

		/**
		 * GL context, or D3D device.
		 * @return the field value
		 */
		public MemorySegment context() {
			return (MemorySegment) VH_CONTEXT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code context} field.
		 * @param value the new field value
		 */
		public void context(MemorySegment value) {
			VH_CONTEXT.set(segment(), 0L, address(value));
		}
	}

	/**
	 * Platform data.
	 */
	public static final class PlatformData extends NativeObject {
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
			return (MemorySegment) VH_NDT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code ndt} field.
		 * @param value the new field value
		 */
		public void ndt(MemorySegment value) {
			VH_NDT.set(segment(), 0L, address(value));
		}

		/**
		 * Native window handle. If {@code NULL}, bgfx will create a headless
		 * context/device, provided the rendering API supports it.
		 * @return the field value
		 */
		public MemorySegment nwh() {
			return (MemorySegment) VH_NWH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code nwh} field.
		 * @param value the new field value
		 */
		public void nwh(MemorySegment value) {
			VH_NWH.set(segment(), 0L, address(value));
		}

		/**
		 * GL context, D3D device, or Vulkan device. If {@code NULL}, bgfx
		 * will create context/device.
		 * @return the field value
		 */
		public MemorySegment context() {
			return (MemorySegment) VH_CONTEXT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code context} field.
		 * @param value the new field value
		 */
		public void context(MemorySegment value) {
			VH_CONTEXT.set(segment(), 0L, address(value));
		}

		/**
		 * D3D12 Queue. If {@code NULL} bgfx will create queue.
		 * @return the field value
		 */
		public MemorySegment queue() {
			return (MemorySegment) VH_QUEUE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code queue} field.
		 * @param value the new field value
		 */
		public void queue(MemorySegment value) {
			VH_QUEUE.set(segment(), 0L, address(value));
		}

		/**
		 * GL back-buffer, or D3D render target view. If {@code NULL} bgfx will
		 * create back-buffer color surface.
		 * @return the field value
		 */
		public MemorySegment backBuffer() {
			return (MemorySegment) VH_BACKBUFFER.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code backBuffer} field.
		 * @param value the new field value
		 */
		public void backBuffer(MemorySegment value) {
			VH_BACKBUFFER.set(segment(), 0L, address(value));
		}

		/**
		 * Backbuffer depth/stencil. If {@code NULL}, bgfx will create a back-buffer
		 * depth/stencil surface.
		 * @return the field value
		 */
		public MemorySegment backBufferDS() {
			return (MemorySegment) VH_BACKBUFFERDS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code backBufferDS} field.
		 * @param value the new field value
		 */
		public void backBufferDS(MemorySegment value) {
			VH_BACKBUFFERDS.set(segment(), 0L, address(value));
		}

		/**
		 * Handle type. Needed for platforms having more than one option.
		 * @return the field value
		 */
		public NativeWindowHandleType type() {
			return NativeWindowHandleType.fromValue((int) VH_TYPE.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code type} field.
		 * @param value the new field value
		 */
		public void type(NativeWindowHandleType value) {
			VH_TYPE.set(segment(), 0L, value.ordinal());
		}
	}

	/**
	 * Backbuffer resolution and reset parameters.
	 */
	public static final class Resolution extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_resolution_t",
			ValueLayout.JAVA_INT.withName("formatColor"),
			ValueLayout.JAVA_INT.withName("formatDepthStencil"),
			ValueLayout.JAVA_INT.withName("width"),
			ValueLayout.JAVA_INT.withName("height"),
			ValueLayout.JAVA_INT.withName("reset"),
			ValueLayout.JAVA_BYTE.withName("numBackBuffers"),
			ValueLayout.JAVA_BYTE.withName("maxFrameLatency"),
			ValueLayout.JAVA_BYTE.withName("debugTextScale"));
		private static final VarHandle VH_FORMATCOLOR = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("formatColor"));
		private static final VarHandle VH_FORMATDEPTHSTENCIL = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("formatDepthStencil"));
		private static final VarHandle VH_WIDTH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("width"));
		private static final VarHandle VH_HEIGHT = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("height"));
		private static final VarHandle VH_RESET = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("reset"));
		private static final VarHandle VH_NUMBACKBUFFERS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numBackBuffers"));
		private static final VarHandle VH_MAXFRAMELATENCY = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("maxFrameLatency"));
		private static final VarHandle VH_DEBUGTEXTSCALE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("debugTextScale"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public Resolution(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public Resolution(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Backbuffer color format.
		 * @return the field value
		 */
		public TextureFormat formatColor() {
			return TextureFormat.fromValue((int) VH_FORMATCOLOR.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code formatColor} field.
		 * @param value the new field value
		 */
		public void formatColor(TextureFormat value) {
			VH_FORMATCOLOR.set(segment(), 0L, value.ordinal());
		}

		/**
		 * Backbuffer depth/stencil format.
		 * @return the field value
		 */
		public TextureFormat formatDepthStencil() {
			return TextureFormat.fromValue((int) VH_FORMATDEPTHSTENCIL.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code formatDepthStencil} field.
		 * @param value the new field value
		 */
		public void formatDepthStencil(TextureFormat value) {
			VH_FORMATDEPTHSTENCIL.set(segment(), 0L, value.ordinal());
		}

		/**
		 * Backbuffer width.
		 * @return the field value
		 */
		public int width() {
			return (int) VH_WIDTH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code width} field.
		 * @param value the new field value
		 */
		public void width(int value) {
			VH_WIDTH.set(segment(), 0L, value);
		}

		/**
		 * Backbuffer height.
		 * @return the field value
		 */
		public int height() {
			return (int) VH_HEIGHT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code height} field.
		 * @param value the new field value
		 */
		public void height(int value) {
			VH_HEIGHT.set(segment(), 0L, value);
		}

		/**
		 * Reset parameters.
		 * @return the field value
		 */
		public int reset() {
			return (int) VH_RESET.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code reset} field.
		 * @param value the new field value
		 */
		public void reset(int value) {
			VH_RESET.set(segment(), 0L, value);
		}

		/**
		 * Number of back buffers.
		 * @return the field value
		 */
		public byte numBackBuffers() {
			return (byte) VH_NUMBACKBUFFERS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numBackBuffers} field.
		 * @param value the new field value
		 */
		public void numBackBuffers(byte value) {
			VH_NUMBACKBUFFERS.set(segment(), 0L, value);
		}

		/**
		 * Maximum frame latency.
		 * @return the field value
		 */
		public byte maxFrameLatency() {
			return (byte) VH_MAXFRAMELATENCY.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code maxFrameLatency} field.
		 * @param value the new field value
		 */
		public void maxFrameLatency(byte value) {
			VH_MAXFRAMELATENCY.set(segment(), 0L, value);
		}

		/**
		 * Scale factor for debug text.
		 * @return the field value
		 */
		public byte debugTextScale() {
			return (byte) VH_DEBUGTEXTSCALE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code debugTextScale} field.
		 * @param value the new field value
		 */
		public void debugTextScale(byte value) {
			VH_DEBUGTEXTSCALE.set(segment(), 0L, value);
		}
	}

	/**
	 * Initialization parameters used by {@code BGFX.init}.
	 */
	public static final class Init extends NativeObject {
		/**
		 * Configurable runtime limits parameters.
		 */
		public static final class Limits extends NativeObject {
			/**
			 * Native C structure layout.
			 */
			public static final StructLayout LAYOUT = cStruct("bgfx_init_limits_t",
				ValueLayout.JAVA_SHORT.withName("maxEncoders"),
				ValueLayout.JAVA_INT.withName("numDrawCalls"),
				ValueLayout.JAVA_INT.withName("numDrawCallPeakFrames"),
				ValueLayout.JAVA_INT.withName("minResourceCbSize"),
				ValueLayout.JAVA_INT.withName("maxTransientVbSize"),
				ValueLayout.JAVA_INT.withName("maxTransientIbSize"),
				ValueLayout.JAVA_INT.withName("minUniformBufferSize"));
			private static final VarHandle VH_MAXENCODERS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxEncoders"));
			private static final VarHandle VH_NUMDRAWCALLS = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("numDrawCalls"));
			private static final VarHandle VH_NUMDRAWCALLPEAKFRAMES = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("numDrawCallPeakFrames"));
			private static final VarHandle VH_MINRESOURCECBSIZE = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("minResourceCbSize"));
			private static final VarHandle VH_MAXTRANSIENTVBSIZE = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxTransientVbSize"));
			private static final VarHandle VH_MAXTRANSIENTIBSIZE = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("maxTransientIbSize"));
			private static final VarHandle VH_MINUNIFORMBUFFERSIZE = LAYOUT.varHandle(
				MemoryLayout.PathElement.groupElement("minUniformBufferSize"));
			/**
			 * Wraps an existing native structure.
			 * @param segment native memory segment
			 */
			public Limits(MemorySegment segment) {
				super(segment, LAYOUT);
			}

			/**
			 * Allocates a native structure.
			 * @param allocator destination allocator
			 */
			public Limits(SegmentAllocator allocator) {
				super(allocator, LAYOUT);
			}

			/**
			 * Maximum number of encoder threads.
			 * @return the field value
			 */
			public short maxEncoders() {
				return (short) VH_MAXENCODERS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxEncoders} field.
			 * @param value the new field value
			 */
			public void maxEncoders(short value) {
				VH_MAXENCODERS.set(segment(), 0L, value);
			}

			/**
			 * Number of draw calls per frame to reserve storage for. Rounded
			 * up to a multiple of {@code BGFX_CONFIG_DRAW_CALL_BLOCK}, which is also
			 * the minimum. This is a reservation, not a limit: submitting more
			 * than this grows the storage during the frame, up to
			 * {@code BGFX_CONFIG_MAX_DRAW_CALLS}. With
			 * {@code BGFX_CONFIG_DYNAMIC_FRAME_STORAGE} disabled nothing grows, and
			 * this is a hard limit that {@code Caps.Limits.maxDrawCalls} reports
			 * back; submissions past it are dropped. See
			 * {@code Stats.numDrawCallsPeak} to size it.
			 * @return the field value
			 */
			public int numDrawCalls() {
				return (int) VH_NUMDRAWCALLS.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code numDrawCalls} field.
			 * @param value the new field value
			 */
			public void numDrawCalls(int value) {
				VH_NUMDRAWCALLS.set(segment(), 0L, value);
			}

			/**
			 * Number of frames the draw-call peak (high-water mark) is observed
			 * before unused storage is released. Set to 0 to keep whatever has
			 * been allocated for the lifetime of the context. With
			 * {@code BGFX_CONFIG_DYNAMIC_FRAME_STORAGE} disabled nothing per frame is
			 * resized at all, and this only releases unused uniform buffer space.
			 * @return the field value
			 */
			public int numDrawCallPeakFrames() {
				return (int) VH_NUMDRAWCALLPEAKFRAMES.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code numDrawCallPeakFrames} field.
			 * @param value the new field value
			 */
			public void numDrawCallPeakFrames(int value) {
				VH_NUMDRAWCALLPEAKFRAMES.set(segment(), 0L, value);
			}

			/**
			 * Minimum resource command buffer size.
			 * @return the field value
			 */
			public int minResourceCbSize() {
				return (int) VH_MINRESOURCECBSIZE.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code minResourceCbSize} field.
			 * @param value the new field value
			 */
			public void minResourceCbSize(int value) {
				VH_MINRESOURCECBSIZE.set(segment(), 0L, value);
			}

			/**
			 * Maximum transient vertex buffer size.
			 * @return the field value
			 */
			public int maxTransientVbSize() {
				return (int) VH_MAXTRANSIENTVBSIZE.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxTransientVbSize} field.
			 * @param value the new field value
			 */
			public void maxTransientVbSize(int value) {
				VH_MAXTRANSIENTVBSIZE.set(segment(), 0L, value);
			}

			/**
			 * Maximum transient index buffer size.
			 * @return the field value
			 */
			public int maxTransientIbSize() {
				return (int) VH_MAXTRANSIENTIBSIZE.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code maxTransientIbSize} field.
			 * @param value the new field value
			 */
			public void maxTransientIbSize(int value) {
				VH_MAXTRANSIENTIBSIZE.set(segment(), 0L, value);
			}

			/**
			 * Mimimum uniform buffer size.
			 * @return the field value
			 */
			public int minUniformBufferSize() {
				return (int) VH_MINUNIFORMBUFFERSIZE.get(segment(), 0L);
			}

			/**
			 * Sets the native {@code minUniformBufferSize} field.
			 * @param value the new field value
			 */
			public void minUniformBufferSize(int value) {
				VH_MINUNIFORMBUFFERSIZE.set(segment(), 0L, value);
			}
		}

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
			Init.Limits.LAYOUT.withName("limits"),
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
		 * Select rendering backend. When set to RendererType.Count
		 * a default rendering backend will be selected appropriate to the platform.
		 * See: {@code BGFX.RendererType}
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
		 * Backbuffer resolution and reset parameters. See: {@code BGFX.Resolution}.
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
		public Init.Limits limits() {
			return new Init.Limits(slice(MH_LIMITS, segment()));
		}

		/**
		 * Sets the native {@code limits} field.
		 * @param value the new field value
		 */
		public void limits(Init.Limits value) {
			slice(MH_LIMITS, segment()).copyFrom(value.segment());
		}

		/**
		 * Provide application specific callback interface.
		 * See: {@code BGFX.CallbackI}
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

	/**
	 * Memory must be obtained by calling {@code BGFX.alloc}, {@code BGFX.copy}, or {@code BGFX.makeRef}.
	 * <p>
	 * <strong>Attention:</strong> It is illegal to create this structure on stack and pass it to any bgfx API.
	 */
	public static final class Memory extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_memory_t",
			ValueLayout.ADDRESS.withName("data"),
			ValueLayout.JAVA_INT.withName("size"));
		private static final VarHandle VH_DATA = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("data"));
		private static final VarHandle VH_SIZE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("size"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public Memory(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public Memory(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Pointer to data.
		 * @return the field value
		 */
		public MemorySegment data() {
			return (MemorySegment) VH_DATA.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code data} field.
		 * @param value the new field value
		 */
		public void data(MemorySegment value) {
			VH_DATA.set(segment(), 0L, address(value));
		}

		/**
		 * Data size.
		 * @return the field value
		 */
		public int size() {
			return (int) VH_SIZE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code size} field.
		 * @param value the new field value
		 */
		public void size(int value) {
			VH_SIZE.set(segment(), 0L, value);
		}
	}

	/**
	 * Transient index buffer.
	 */
	public static final class TransientIndexBuffer extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_transient_index_buffer_t",
			ValueLayout.ADDRESS.withName("data"),
			ValueLayout.JAVA_INT.withName("size"),
			ValueLayout.JAVA_INT.withName("startIndex"),
			IndexBufferHandle.LAYOUT.withName("handle"),
			ValueLayout.JAVA_BOOLEAN.withName("isIndex16"));
		private static final VarHandle VH_DATA = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("data"));
		private static final VarHandle VH_SIZE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("size"));
		private static final VarHandle VH_STARTINDEX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("startIndex"));
		private static final MethodHandle MH_HANDLE = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("handle"));
		private static final VarHandle VH_ISINDEX16 = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("isIndex16"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public TransientIndexBuffer(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public TransientIndexBuffer(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Pointer to data.
		 * @return the field value
		 */
		public MemorySegment data() {
			return (MemorySegment) VH_DATA.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code data} field.
		 * @param value the new field value
		 */
		public void data(MemorySegment value) {
			VH_DATA.set(segment(), 0L, address(value));
		}

		/**
		 * Data size.
		 * @return the field value
		 */
		public int size() {
			return (int) VH_SIZE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code size} field.
		 * @param value the new field value
		 */
		public void size(int value) {
			VH_SIZE.set(segment(), 0L, value);
		}

		/**
		 * First index.
		 * @return the field value
		 */
		public int startIndex() {
			return (int) VH_STARTINDEX.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code startIndex} field.
		 * @param value the new field value
		 */
		public void startIndex(int value) {
			VH_STARTINDEX.set(segment(), 0L, value);
		}

		/**
		 * Index buffer handle.
		 * @return the field value
		 */
		public IndexBufferHandle handle() {
			return IndexBufferHandle.read(slice(MH_HANDLE, segment()));
		}

		/**
		 * Sets the native {@code handle} field.
		 * @param value the new field value
		 */
		public void handle(IndexBufferHandle value) {
			value.write(slice(MH_HANDLE, segment()));
		}

		/**
		 * Index buffer format is 16-bits if true, otherwise it is 32-bit.
		 * @return the field value
		 */
		public boolean isIndex16() {
			return (boolean) VH_ISINDEX16.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code isIndex16} field.
		 * @param value the new field value
		 */
		public void isIndex16(boolean value) {
			VH_ISINDEX16.set(segment(), 0L, value);
		}
	}

	/**
	 * Transient vertex buffer.
	 */
	public static final class TransientVertexBuffer extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_transient_vertex_buffer_t",
			ValueLayout.ADDRESS.withName("data"),
			ValueLayout.JAVA_INT.withName("size"),
			ValueLayout.JAVA_INT.withName("startVertex"),
			ValueLayout.JAVA_SHORT.withName("stride"),
			VertexBufferHandle.LAYOUT.withName("handle"),
			VertexLayoutHandle.LAYOUT.withName("layoutHandle"));
		private static final VarHandle VH_DATA = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("data"));
		private static final VarHandle VH_SIZE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("size"));
		private static final VarHandle VH_STARTVERTEX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("startVertex"));
		private static final VarHandle VH_STRIDE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("stride"));
		private static final MethodHandle MH_HANDLE = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("handle"));
		private static final MethodHandle MH_LAYOUTHANDLE = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("layoutHandle"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public TransientVertexBuffer(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public TransientVertexBuffer(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Pointer to data.
		 * @return the field value
		 */
		public MemorySegment data() {
			return (MemorySegment) VH_DATA.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code data} field.
		 * @param value the new field value
		 */
		public void data(MemorySegment value) {
			VH_DATA.set(segment(), 0L, address(value));
		}

		/**
		 * Data size.
		 * @return the field value
		 */
		public int size() {
			return (int) VH_SIZE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code size} field.
		 * @param value the new field value
		 */
		public void size(int value) {
			VH_SIZE.set(segment(), 0L, value);
		}

		/**
		 * First vertex.
		 * @return the field value
		 */
		public int startVertex() {
			return (int) VH_STARTVERTEX.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code startVertex} field.
		 * @param value the new field value
		 */
		public void startVertex(int value) {
			VH_STARTVERTEX.set(segment(), 0L, value);
		}

		/**
		 * Vertex stride.
		 * @return the field value
		 */
		public short stride() {
			return (short) VH_STRIDE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code stride} field.
		 * @param value the new field value
		 */
		public void stride(short value) {
			VH_STRIDE.set(segment(), 0L, value);
		}

		/**
		 * Vertex buffer handle.
		 * @return the field value
		 */
		public VertexBufferHandle handle() {
			return VertexBufferHandle.read(slice(MH_HANDLE, segment()));
		}

		/**
		 * Sets the native {@code handle} field.
		 * @param value the new field value
		 */
		public void handle(VertexBufferHandle value) {
			value.write(slice(MH_HANDLE, segment()));
		}

		/**
		 * Vertex layout handle.
		 * @return the field value
		 */
		public VertexLayoutHandle layoutHandle() {
			return VertexLayoutHandle.read(slice(MH_LAYOUTHANDLE, segment()));
		}

		/**
		 * Sets the native {@code layoutHandle} field.
		 * @param value the new field value
		 */
		public void layoutHandle(VertexLayoutHandle value) {
			value.write(slice(MH_LAYOUTHANDLE, segment()));
		}
	}

	/**
	 * Instance data buffer info.
	 */
	public static final class InstanceDataBuffer extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_instance_data_buffer_t",
			ValueLayout.ADDRESS.withName("data"),
			ValueLayout.JAVA_INT.withName("size"),
			ValueLayout.JAVA_INT.withName("offset"),
			ValueLayout.JAVA_INT.withName("num"),
			ValueLayout.JAVA_SHORT.withName("stride"),
			VertexBufferHandle.LAYOUT.withName("handle"));
		private static final VarHandle VH_DATA = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("data"));
		private static final VarHandle VH_SIZE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("size"));
		private static final VarHandle VH_OFFSET = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("offset"));
		private static final VarHandle VH_NUM = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("num"));
		private static final VarHandle VH_STRIDE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("stride"));
		private static final MethodHandle MH_HANDLE = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("handle"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public InstanceDataBuffer(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public InstanceDataBuffer(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Pointer to data.
		 * @return the field value
		 */
		public MemorySegment data() {
			return (MemorySegment) VH_DATA.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code data} field.
		 * @param value the new field value
		 */
		public void data(MemorySegment value) {
			VH_DATA.set(segment(), 0L, address(value));
		}

		/**
		 * Data size.
		 * @return the field value
		 */
		public int size() {
			return (int) VH_SIZE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code size} field.
		 * @param value the new field value
		 */
		public void size(int value) {
			VH_SIZE.set(segment(), 0L, value);
		}

		/**
		 * Offset in vertex buffer.
		 * @return the field value
		 */
		public int offset() {
			return (int) VH_OFFSET.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code offset} field.
		 * @param value the new field value
		 */
		public void offset(int value) {
			VH_OFFSET.set(segment(), 0L, value);
		}

		/**
		 * Number of instances.
		 * @return the field value
		 */
		public int num() {
			return (int) VH_NUM.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code num} field.
		 * @param value the new field value
		 */
		public void num(int value) {
			VH_NUM.set(segment(), 0L, value);
		}

		/**
		 * Vertex buffer stride.
		 * @return the field value
		 */
		public short stride() {
			return (short) VH_STRIDE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code stride} field.
		 * @param value the new field value
		 */
		public void stride(short value) {
			VH_STRIDE.set(segment(), 0L, value);
		}

		/**
		 * Vertex buffer object handle.
		 * @return the field value
		 */
		public VertexBufferHandle handle() {
			return VertexBufferHandle.read(slice(MH_HANDLE, segment()));
		}

		/**
		 * Sets the native {@code handle} field.
		 * @param value the new field value
		 */
		public void handle(VertexBufferHandle value) {
			value.write(slice(MH_HANDLE, segment()));
		}
	}

	/**
	 * Region of a texture, used as the source or destination of a blit, or as
	 * the region handed to {@code BGFX.read}.
	 * <p>
	 * Every field defaults to zero, and zero always means "the natural whole".
	 * {@code { .handle = tex }} therefore addresses all of mip 0.
	 */
	public static final class TextureRegion extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_texture_region_t",
			TextureHandle.LAYOUT.withName("handle"),
			ValueLayout.JAVA_BYTE.withName("mip"),
			ValueLayout.JAVA_SHORT.withName("x"),
			ValueLayout.JAVA_SHORT.withName("y"),
			ValueLayout.JAVA_SHORT.withName("z"),
			ValueLayout.JAVA_SHORT.withName("width"),
			ValueLayout.JAVA_SHORT.withName("height"),
			ValueLayout.JAVA_SHORT.withName("depth"));
		private static final MethodHandle MH_HANDLE = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("handle"));
		private static final VarHandle VH_MIP = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("mip"));
		private static final VarHandle VH_X = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("x"));
		private static final VarHandle VH_Y = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("y"));
		private static final VarHandle VH_Z = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("z"));
		private static final VarHandle VH_WIDTH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("width"));
		private static final VarHandle VH_HEIGHT = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("height"));
		private static final VarHandle VH_DEPTH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("depth"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public TextureRegion(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public TextureRegion(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Texture handle.
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
		public byte mip() {
			return (byte) VH_MIP.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code mip} field.
		 * @param value the new field value
		 */
		public void mip(byte value) {
			VH_MIP.set(segment(), 0L, value);
		}

		/**
		 * X position of the region.
		 * @return the field value
		 */
		public short x() {
			return (short) VH_X.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code x} field.
		 * @param value the new field value
		 */
		public void x(short value) {
			VH_X.set(segment(), 0L, value);
		}

		/**
		 * Y position of the region.
		 * @return the field value
		 */
		public short y() {
			return (short) VH_Y.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code y} field.
		 * @param value the new field value
		 */
		public void y(short value) {
			VH_Y.set(segment(), 0L, value);
		}

		/**
		 * If texture is 2D this should be 0. If the texture is a cube map
		 * this is the cube face, for a 2D array it is the layer, and for a
		 * 3D texture it is the Z position.
		 * @return the field value
		 */
		public short z() {
			return (short) VH_Z.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code z} field.
		 * @param value the new field value
		 */
		public void z(short value) {
			VH_Z.set(segment(), 0L, value);
		}

		/**
		 * Width of the region. 0 uses the rest of the mip from {@code x}.
		 * @return the field value
		 */
		public short width() {
			return (short) VH_WIDTH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code width} field.
		 * @param value the new field value
		 */
		public void width(short value) {
			VH_WIDTH.set(segment(), 0L, value);
		}

		/**
		 * Height of the region. 0 uses the rest of the mip from {@code y}.
		 * @return the field value
		 */
		public short height() {
			return (short) VH_HEIGHT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code height} field.
		 * @param value the new field value
		 */
		public void height(short value) {
			VH_HEIGHT.set(segment(), 0L, value);
		}

		/**
		 * Depth of the region for a 3D texture, or the number of layers or
		 * cube faces otherwise. 0 uses the rest from {@code z}.
		 * @return the field value
		 */
		public short depth() {
			return (short) VH_DEPTH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code depth} field.
		 * @param value the new field value
		 */
		public void depth(short value) {
			VH_DEPTH.set(segment(), 0L, value);
		}

		/**
		 * Fill in the region of a plain 2D texture. {@code mip}, {@code z} and {@code depth} are left
		 * at zero, which addresses mip 0 of the only slice a 2D texture has.
		 * @param _handle Texture handle.
		 * @param _x X position of the region.
		 * @param _y Y position of the region.
		 * @param _width Width of the region. 0 uses the rest of the mip from {@code _x}.
		 * @param _height Height of the region. 0 uses the rest of the mip from {@code _y}.
		 */
		public final void init(TextureHandle _handle, short _x, short _y, short _width, short _height) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_TEXTURE_REGION_INIT).invokeExact(segment(), _handle.allocate(arena), _x, _y, _width, _height);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}
	}

	/**
	 * Region of a buffer, used as the source or destination of a blit, or as the
	 * region handed to {@code BGFX.read}.
	 * <p>
	 * {@code rowPitch} and {@code slicePitch} describe how texture data is laid out in the
	 * buffer, and are ignored when the other end of the blit is also a buffer.
	 * Both are in bytes, and 0 selects the tightly packed layout: a row pitch of
	 * the region width in blocks multiplied by the block size, and a slice pitch
	 * of that row pitch multiplied by the region height in blocks.
	 * <p>
	 * A pitch the backend cannot copy natively is repacked by bgfx, which costs
	 * an extra pass over the data. {@code Caps.Limits.blitRowPitchAlign} and
	 * {@code blitOffsetAlign} report what the backend copies directly, and
	 * {@code BufferRegion.init} fills in a layout that matches them.
	 */
	public static final class BufferRegion extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_buffer_region_t",
			BufferHandle.LAYOUT.withName("handle"),
			ValueLayout.JAVA_INT.withName("offset"),
			ValueLayout.JAVA_INT.withName("size"),
			ValueLayout.JAVA_INT.withName("rowPitch"),
			ValueLayout.JAVA_INT.withName("slicePitch"));
		private static final MethodHandle MH_HANDLE = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("handle"));
		private static final VarHandle VH_OFFSET = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("offset"));
		private static final VarHandle VH_SIZE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("size"));
		private static final VarHandle VH_ROWPITCH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("rowPitch"));
		private static final VarHandle VH_SLICEPITCH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("slicePitch"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public BufferRegion(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public BufferRegion(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Buffer handle.
		 * @return the field value
		 */
		public BufferHandle handle() {
			return BufferHandle.read(slice(MH_HANDLE, segment()));
		}

		/**
		 * Sets the native {@code handle} field.
		 * @param value the new field value
		 */
		public void handle(BufferHandle value) {
			value.write(slice(MH_HANDLE, segment()));
		}

		/**
		 * Byte offset into the buffer.
		 * @return the field value
		 */
		public int offset() {
			return (int) VH_OFFSET.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code offset} field.
		 * @param value the new field value
		 */
		public void offset(int value) {
			VH_OFFSET.set(segment(), 0L, value);
		}

		/**
		 * Number of bytes. Only used when both ends of a blit are
		 * buffers, or by {@code BGFX.read}. 0 uses the rest of the buffer.
		 * @return the field value
		 */
		public int size() {
			return (int) VH_SIZE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code size} field.
		 * @param value the new field value
		 */
		public void size(int value) {
			VH_SIZE.set(segment(), 0L, value);
		}

		/**
		 * Distance in bytes between the start of two consecutive rows
		 * of blocks. 0 is tightly packed.
		 * @return the field value
		 */
		public int rowPitch() {
			return (int) VH_ROWPITCH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code rowPitch} field.
		 * @param value the new field value
		 */
		public void rowPitch(int value) {
			VH_ROWPITCH.set(segment(), 0L, value);
		}

		/**
		 * Distance in bytes between the start of two consecutive
		 * slices, layers or cube faces. 0 is tightly packed.
		 * @return the field value
		 */
		public int slicePitch() {
			return (int) VH_SLICEPITCH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code slicePitch} field.
		 * @param value the new field value
		 */
		public void slicePitch(int value) {
			VH_SLICEPITCH.set(segment(), 0L, value);
		}

		/**
		 * Fill {@code rowPitch}, {@code slicePitch} and {@code size} with the layout the backend copies
		 * fastest for {@code _texture}, and round {@code offset} up to {@code Caps.Limits.blitOffsetAlign}.
		 * {@code handle} is left untouched, so {@code size} can be used to create the buffer the
		 * region will point at.
		 * @param _texture Texture region the buffer is copied to or from.
		 */
		public final void initTexture(TextureRegion _texture) {
			try {
				downcallHandle(DC_BUFFER_REGION_INIT_TEXTURE).invokeExact(segment(), address(_texture));
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Fill in the region a blit between two buffers copies. {@code rowPitch} and
		 * {@code slicePitch} are left at zero, since neither end of such a blit is a
		 * texture.
		 * @param _handle Buffer handle.
		 * @param _offset Byte offset into the buffer.
		 * @param _size Number of bytes. 0 uses the rest of the buffer.
		 */
		public final void initBuffer(BufferHandle _handle, int _offset, int _size) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_BUFFER_REGION_INIT_BUFFER).invokeExact(segment(), _handle.allocate(arena), _offset, _size);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}
	}

	/**
	 * Texture info.
	 */
	public static final class TextureInfo extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_texture_info_t",
			ValueLayout.JAVA_INT.withName("format"),
			ValueLayout.JAVA_INT.withName("storageSize"),
			ValueLayout.JAVA_SHORT.withName("width"),
			ValueLayout.JAVA_SHORT.withName("height"),
			ValueLayout.JAVA_SHORT.withName("depth"),
			ValueLayout.JAVA_SHORT.withName("numLayers"),
			ValueLayout.JAVA_BYTE.withName("numMips"),
			ValueLayout.JAVA_BYTE.withName("bitsPerPixel"),
			ValueLayout.JAVA_BOOLEAN.withName("cubeMap"));
		private static final VarHandle VH_FORMAT = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("format"));
		private static final VarHandle VH_STORAGESIZE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("storageSize"));
		private static final VarHandle VH_WIDTH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("width"));
		private static final VarHandle VH_HEIGHT = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("height"));
		private static final VarHandle VH_DEPTH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("depth"));
		private static final VarHandle VH_NUMLAYERS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numLayers"));
		private static final VarHandle VH_NUMMIPS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numMips"));
		private static final VarHandle VH_BITSPERPIXEL = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("bitsPerPixel"));
		private static final VarHandle VH_CUBEMAP = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("cubeMap"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public TextureInfo(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public TextureInfo(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Texture format.
		 * @return the field value
		 */
		public TextureFormat format() {
			return TextureFormat.fromValue((int) VH_FORMAT.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code format} field.
		 * @param value the new field value
		 */
		public void format(TextureFormat value) {
			VH_FORMAT.set(segment(), 0L, value.ordinal());
		}

		/**
		 * Total amount of bytes required to store texture.
		 * @return the field value
		 */
		public int storageSize() {
			return (int) VH_STORAGESIZE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code storageSize} field.
		 * @param value the new field value
		 */
		public void storageSize(int value) {
			VH_STORAGESIZE.set(segment(), 0L, value);
		}

		/**
		 * Texture width.
		 * @return the field value
		 */
		public short width() {
			return (short) VH_WIDTH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code width} field.
		 * @param value the new field value
		 */
		public void width(short value) {
			VH_WIDTH.set(segment(), 0L, value);
		}

		/**
		 * Texture height.
		 * @return the field value
		 */
		public short height() {
			return (short) VH_HEIGHT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code height} field.
		 * @param value the new field value
		 */
		public void height(short value) {
			VH_HEIGHT.set(segment(), 0L, value);
		}

		/**
		 * Texture depth.
		 * @return the field value
		 */
		public short depth() {
			return (short) VH_DEPTH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code depth} field.
		 * @param value the new field value
		 */
		public void depth(short value) {
			VH_DEPTH.set(segment(), 0L, value);
		}

		/**
		 * Number of layers in texture array.
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
		 * Number of MIP maps.
		 * @return the field value
		 */
		public byte numMips() {
			return (byte) VH_NUMMIPS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numMips} field.
		 * @param value the new field value
		 */
		public void numMips(byte value) {
			VH_NUMMIPS.set(segment(), 0L, value);
		}

		/**
		 * Format bits per pixel.
		 * @return the field value
		 */
		public byte bitsPerPixel() {
			return (byte) VH_BITSPERPIXEL.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code bitsPerPixel} field.
		 * @param value the new field value
		 */
		public void bitsPerPixel(byte value) {
			VH_BITSPERPIXEL.set(segment(), 0L, value);
		}

		/**
		 * Texture is cubemap.
		 * @return the field value
		 */
		public boolean cubeMap() {
			return (boolean) VH_CUBEMAP.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code cubeMap} field.
		 * @param value the new field value
		 */
		public void cubeMap(boolean value) {
			VH_CUBEMAP.set(segment(), 0L, value);
		}
	}

	/**
	 * Video decoder initialization. Serialized into the Memory passed to
	 * {@code createTexture2D}. When the memory blob begins with {@code magic}, bgfx
	 * infers the texture is a video decode destination (the caller need not set
	 * any extra texture flag). Everything else the renderer needs about the
	 * stream (chroma format, bit depth, profile, level, coded dimensions, DPB
	 * layout, color metadata) is parsed out of the codec parameter sets at
	 * create time.
	 */
	public static final class VideoDecoderInit extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_video_decoder_init_t",
			ValueLayout.JAVA_INT.withName("magic"),
			ValueLayout.JAVA_INT.withName("codec"),
			ValueLayout.ADDRESS.withName("parameterSets"),
			ValueLayout.JAVA_INT.withName("parameterSetsSize"),
			ValueLayout.JAVA_INT.withName("cachedAuBytes"),
			ValueLayout.JAVA_BYTE.withName("flags"));
		private static final VarHandle VH_MAGIC = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("magic"));
		private static final VarHandle VH_CODEC = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("codec"));
		private static final VarHandle VH_PARAMETERSETS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("parameterSets"));
		private static final VarHandle VH_PARAMETERSETSSIZE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("parameterSetsSize"));
		private static final VarHandle VH_CACHEDAUBYTES = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("cachedAuBytes"));
		private static final VarHandle VH_FLAGS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("flags"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public VideoDecoderInit(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public VideoDecoderInit(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Structure magic. Must be {@code BX_MAKEFOURCC('V', 'D', 'I', 0x0)}.
		 * @return the field value
		 */
		public int magic() {
			return (int) VH_MAGIC.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code magic} field.
		 * @param value the new field value
		 */
		public void magic(int value) {
			VH_MAGIC.set(segment(), 0L, value);
		}

		/**
		 * Video codec. See: {@code VideoCodec}.
		 * @return the field value
		 */
		public VideoCodec codec() {
			return VideoCodec.fromValue((int) VH_CODEC.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code codec} field.
		 * @param value the new field value
		 */
		public void codec(VideoCodec value) {
			VH_CODEC.set(segment(), 0L, value.ordinal());
		}

		/**
		 * Codec parameter sets (Annex B for H.264/H.265, OBUs for AV1).
		 * @return the field value
		 */
		public MemorySegment parameterSets() {
			return (MemorySegment) VH_PARAMETERSETS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code parameterSets} field.
		 * @param value the new field value
		 */
		public void parameterSets(MemorySegment value) {
			VH_PARAMETERSETS.set(segment(), 0L, address(value));
		}

		/**
		 * Parameter sets size in bytes.
		 * @return the field value
		 */
		public int parameterSetsSize() {
			return (int) VH_PARAMETERSETSSIZE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code parameterSetsSize} field.
		 * @param value the new field value
		 */
		public void parameterSetsSize(int value) {
			VH_PARAMETERSETSSIZE.set(segment(), 0L, value);
		}

		/**
		 * Soft cap (in bytes) on the streaming access-unit FIFO (when
		 * {@code BGFX_VIDEO_DECODER_INIT_RETAIN} is NOT set). 0 selects the
		 * default. Ignored in RETAIN mode (the retain cache is unbounded).
		 * @return the field value
		 */
		public int cachedAuBytes() {
			return (int) VH_CACHEDAUBYTES.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code cachedAuBytes} field.
		 * @param value the new field value
		 */
		public void cachedAuBytes(int value) {
			VH_CACHEDAUBYTES.set(segment(), 0L, value);
		}

		/**
		 * Decoder lifetime flags. See: {@code BGFX_VIDEO_DECODER_INIT_*}.
		 * @return the field value
		 */
		public byte flags() {
			return (byte) VH_FLAGS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code flags} field.
		 * @param value the new field value
		 */
		public void flags(byte value) {
			VH_FLAGS.set(segment(), 0L, value);
		}
	}

	/**
	 * One access unit entry inside a {@code VideoDecoderFrame} batch. The bitstream
	 * for the AU lives at offset {@code Σ aus[0..ii].size} inside the frame's
	 * {@code bitstream} buffer (access units are stored back-to-back in decode /
	 * submission order).
	 */
	public static final class VideoDecoderAu extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_video_decoder_au_t",
			ValueLayout.JAVA_INT.withName("size"),
			ValueLayout.JAVA_LONG.withName("ptsUs"));
		private static final VarHandle VH_SIZE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("size"));
		private static final VarHandle VH_PTSUS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("ptsUs"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public VideoDecoderAu(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public VideoDecoderAu(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Access unit size in bytes.
		 * @return the field value
		 */
		public int size() {
			return (int) VH_SIZE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code size} field.
		 * @param value the new field value
		 */
		public void size(int value) {
			VH_SIZE.set(segment(), 0L, value);
		}

		/**
		 * Presentation timestamp in microseconds for this access unit (container-provided).
		 * @return the field value
		 */
		public long ptsUs() {
			return (long) VH_PTSUS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code ptsUs} field.
		 * @param value the new field value
		 */
		public void ptsUs(long value) {
			VH_PTSUS.set(segment(), 0L, value);
		}
	}

	/**
	 * Video decoder per-frame submission. Serialized into the Memory passed
	 * to {@code updateTexture2D} for a video decode destination texture. The
	 * renderer parses the slice / tile-group header out of the bitstream and
	 * translates it to the backend-specific decoder arguments.
	 * <p>
	 * A single call may submit a batch of access units: {@code bitstream} is the
	 * back-to-back concatenation of {@code numAus} access units, and {@code aus[ii]}
	 * holds the size and PTS of each. AUs are enqueued in array order
	 * (which is the codec's decode order). Set {@code numAus == 0} (and
	 * {@code bitstream == NULL}) for a presentation-only tick that only advances
	 * the playback clock.
	 * <p>
	 * The {@code bitstream} and {@code aus} pointers must remain valid until bgfx has
	 * consumed the submission ({@code BGFX.copy} only deep-copies the
	 * {@code VideoDecoderFrame} struct itself, not the buffers it references).
	 */
	public static final class VideoDecoderFrame extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_video_decoder_frame_t",
			ValueLayout.JAVA_INT.withName("magic"),
			ValueLayout.ADDRESS.withName("bitstream"),
			ValueLayout.ADDRESS.withName("aus"),
			ValueLayout.JAVA_INT.withName("numAus"),
			ValueLayout.JAVA_LONG.withName("presentationTimeUs"),
			ValueLayout.JAVA_BYTE.withName("flags"));
		private static final VarHandle VH_MAGIC = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("magic"));
		private static final VarHandle VH_BITSTREAM = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("bitstream"));
		private static final VarHandle VH_AUS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("aus"));
		private static final VarHandle VH_NUMAUS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numAus"));
		private static final VarHandle VH_PRESENTATIONTIMEUS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("presentationTimeUs"));
		private static final VarHandle VH_FLAGS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("flags"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public VideoDecoderFrame(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public VideoDecoderFrame(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Structure magic. Must be {@code BX_MAKEFOURCC('V', 'D', 'F', 0x0)}.
		 * @return the field value
		 */
		public int magic() {
			return (int) VH_MAGIC.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code magic} field.
		 * @param value the new field value
		 */
		public void magic(int value) {
			VH_MAGIC.set(segment(), 0L, value);
		}

		/**
		 * Concatenated access-unit bitstream (decode order). NULL for presentation-only ticks.
		 * @return the field value
		 */
		public MemorySegment bitstream() {
			return (MemorySegment) VH_BITSTREAM.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code bitstream} field.
		 * @param value the new field value
		 */
		public void bitstream(MemorySegment value) {
			VH_BITSTREAM.set(segment(), 0L, address(value));
		}

		/**
		 * Per-AU size and PTS array. NULL when {@code numAus == 0}.
		 * @return the field value
		 */
		public VideoDecoderAu aus() {
			return new VideoDecoderAu((MemorySegment) VH_AUS.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code aus} field.
		 * @param value the new field value
		 */
		public void aus(VideoDecoderAu value) {
			VH_AUS.set(segment(), 0L, address(value));
		}

		/**
		 * Number of access units in this batch. 0 for presentation-only ticks.
		 * @return the field value
		 */
		public int numAus() {
			return (int) VH_NUMAUS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numAus} field.
		 * @param value the new field value
		 */
		public void numAus(int value) {
			VH_NUMAUS.set(segment(), 0L, value);
		}

		/**
		 * Current playback wall-clock time. Driver dispatches the picture whose {@code ptsUs}
		 * best matches. Must be monotonically non-decreasing between non-{@code SET} calls.
		 * @return the field value
		 */
		public long presentationTimeUs() {
			return (long) VH_PRESENTATIONTIMEUS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code presentationTimeUs} field.
		 * @param value the new field value
		 */
		public void presentationTimeUs(long value) {
			VH_PRESENTATIONTIMEUS.set(segment(), 0L, value);
		}

		/**
		 * Per-frame submission flags. See: {@code BGFX_VIDEO_DECODE_FRAME_*}.
		 * @return the field value
		 */
		public byte flags() {
			return (byte) VH_FLAGS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code flags} field.
		 * @param value the new field value
		 */
		public void flags(byte value) {
			VH_FLAGS.set(segment(), 0L, value);
		}
	}

	/**
	 * Uniform info.
	 */
	public static final class UniformInfo extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_uniform_info_t",
			MemoryLayout.sequenceLayout(256, ValueLayout.JAVA_BYTE).withName("name"),
			ValueLayout.JAVA_INT.withName("type"),
			ValueLayout.JAVA_SHORT.withName("num"));
		private static final MethodHandle MH_NAME = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("name"));
		private static final VarHandle VH_TYPE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("type"));
		private static final VarHandle VH_NUM = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("num"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public UniformInfo(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public UniformInfo(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Uniform name.
		 * @return a segment view of the inline array
		 */
		public MemorySegment name() {
			return slice(MH_NAME, segment());
		}

		/**
		 * Uniform type.
		 * @return the field value
		 */
		public UniformType type() {
			return UniformType.fromValue((int) VH_TYPE.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code type} field.
		 * @param value the new field value
		 */
		public void type(UniformType value) {
			VH_TYPE.set(segment(), 0L, value.ordinal());
		}

		/**
		 * Number of elements in array.
		 * @return the field value
		 */
		public short num() {
			return (short) VH_NUM.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code num} field.
		 * @param value the new field value
		 */
		public void num(short value) {
			VH_NUM.set(segment(), 0L, value);
		}
	}

	/**
	 * Frame buffer texture attachment info.
	 */
	public static final class Attachment extends NativeObject {
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
					downcallHandle(DC_ATTACHMENT_INIT).invokeExact(segment(), _handle.allocate(arena), _access.ordinal(), _layer, _numLayers, _mip, _resolve);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}
	}

	/**
	 * Transform data.
	 */
	public static final class Transform extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_transform_t",
			ValueLayout.ADDRESS.withName("data"),
			ValueLayout.JAVA_SHORT.withName("num"));
		private static final VarHandle VH_DATA = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("data"));
		private static final VarHandle VH_NUM = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("num"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public Transform(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public Transform(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Pointer to first 4x4 matrix.
		 * @return the field value
		 */
		public MemorySegment data() {
			return (MemorySegment) VH_DATA.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code data} field.
		 * @param value the new field value
		 */
		public void data(MemorySegment value) {
			VH_DATA.set(segment(), 0L, address(value));
		}

		/**
		 * Number of matrices.
		 * @return the field value
		 */
		public short num() {
			return (short) VH_NUM.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code num} field.
		 * @param value the new field value
		 */
		public void num(short value) {
			VH_NUM.set(segment(), 0L, value);
		}
	}

	/**
	 * View stats.
	 */
	public static final class ViewStats extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_view_stats_t",
			MemoryLayout.sequenceLayout(256, ValueLayout.JAVA_BYTE).withName("name"),
			ValueLayout.JAVA_SHORT.withName("view"),
			ValueLayout.JAVA_LONG.withName("cpuTimeBegin"),
			ValueLayout.JAVA_LONG.withName("cpuTimeEnd"),
			ValueLayout.JAVA_LONG.withName("gpuTimeBegin"),
			ValueLayout.JAVA_LONG.withName("gpuTimeEnd"),
			ValueLayout.JAVA_INT.withName("gpuFrameNum"));
		private static final MethodHandle MH_NAME = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("name"));
		private static final VarHandle VH_VIEW = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("view"));
		private static final VarHandle VH_CPUTIMEBEGIN = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("cpuTimeBegin"));
		private static final VarHandle VH_CPUTIMEEND = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("cpuTimeEnd"));
		private static final VarHandle VH_GPUTIMEBEGIN = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("gpuTimeBegin"));
		private static final VarHandle VH_GPUTIMEEND = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("gpuTimeEnd"));
		private static final VarHandle VH_GPUFRAMENUM = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("gpuFrameNum"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public ViewStats(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public ViewStats(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * View name.
		 * @return a segment view of the inline array
		 */
		public MemorySegment name() {
			return slice(MH_NAME, segment());
		}

		/**
		 * View id.
		 * @return the field value
		 */
		public short view() {
			return (short) VH_VIEW.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code view} field.
		 * @param value the new field value
		 */
		public void view(short value) {
			VH_VIEW.set(segment(), 0L, value);
		}

		/**
		 * CPU (submit) begin time.
		 * @return the field value
		 */
		public long cpuTimeBegin() {
			return (long) VH_CPUTIMEBEGIN.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code cpuTimeBegin} field.
		 * @param value the new field value
		 */
		public void cpuTimeBegin(long value) {
			VH_CPUTIMEBEGIN.set(segment(), 0L, value);
		}

		/**
		 * CPU (submit) end time.
		 * @return the field value
		 */
		public long cpuTimeEnd() {
			return (long) VH_CPUTIMEEND.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code cpuTimeEnd} field.
		 * @param value the new field value
		 */
		public void cpuTimeEnd(long value) {
			VH_CPUTIMEEND.set(segment(), 0L, value);
		}

		/**
		 * GPU begin time.
		 * @return the field value
		 */
		public long gpuTimeBegin() {
			return (long) VH_GPUTIMEBEGIN.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code gpuTimeBegin} field.
		 * @param value the new field value
		 */
		public void gpuTimeBegin(long value) {
			VH_GPUTIMEBEGIN.set(segment(), 0L, value);
		}

		/**
		 * GPU end time.
		 * @return the field value
		 */
		public long gpuTimeEnd() {
			return (long) VH_GPUTIMEEND.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code gpuTimeEnd} field.
		 * @param value the new field value
		 */
		public void gpuTimeEnd(long value) {
			VH_GPUTIMEEND.set(segment(), 0L, value);
		}

		/**
		 * Frame which generated gpuTimeBegin, gpuTimeEnd.
		 * @return the field value
		 */
		public int gpuFrameNum() {
			return (int) VH_GPUFRAMENUM.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code gpuFrameNum} field.
		 * @param value the new field value
		 */
		public void gpuFrameNum(int value) {
			VH_GPUFRAMENUM.set(segment(), 0L, value);
		}
	}

	/**
	 * Encoder stats.
	 */
	public static final class EncoderStats extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_encoder_stats_t",
			ValueLayout.JAVA_LONG.withName("cpuTimeBegin"),
			ValueLayout.JAVA_LONG.withName("cpuTimeEnd"));
		private static final VarHandle VH_CPUTIMEBEGIN = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("cpuTimeBegin"));
		private static final VarHandle VH_CPUTIMEEND = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("cpuTimeEnd"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public EncoderStats(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public EncoderStats(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Encoder thread CPU submit begin time.
		 * @return the field value
		 */
		public long cpuTimeBegin() {
			return (long) VH_CPUTIMEBEGIN.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code cpuTimeBegin} field.
		 * @param value the new field value
		 */
		public void cpuTimeBegin(long value) {
			VH_CPUTIMEBEGIN.set(segment(), 0L, value);
		}

		/**
		 * Encoder thread CPU submit end time.
		 * @return the field value
		 */
		public long cpuTimeEnd() {
			return (long) VH_CPUTIMEEND.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code cpuTimeEnd} field.
		 * @param value the new field value
		 */
		public void cpuTimeEnd(long value) {
			VH_CPUTIMEEND.set(segment(), 0L, value);
		}
	}

	/**
	 * Renderer statistics data.
	 * <p>
	 * <strong>Remarks:</strong> All time values are high-resolution timestamps, while
	 * time frequencies define timestamps-per-second for that hardware.
	 */
	public static final class Stats extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_stats_t",
			ValueLayout.JAVA_LONG.withName("cpuTimeFrame"),
			ValueLayout.JAVA_LONG.withName("cpuTimeBegin"),
			ValueLayout.JAVA_LONG.withName("cpuTimeEnd"),
			ValueLayout.JAVA_LONG.withName("cpuTimerFreq"),
			ValueLayout.JAVA_LONG.withName("gpuTimeBegin"),
			ValueLayout.JAVA_LONG.withName("gpuTimeEnd"),
			ValueLayout.JAVA_LONG.withName("gpuTimerFreq"),
			ValueLayout.JAVA_LONG.withName("waitRender"),
			ValueLayout.JAVA_LONG.withName("waitSubmit"),
			ValueLayout.JAVA_INT.withName("numDraw"),
			ValueLayout.JAVA_INT.withName("numCompute"),
			ValueLayout.JAVA_INT.withName("numBlit"),
			ValueLayout.JAVA_INT.withName("numBlitRepack"),
			ValueLayout.JAVA_INT.withName("numDrawCallsPeak"),
			ValueLayout.JAVA_INT.withName("maxGpuLatency"),
			ValueLayout.JAVA_INT.withName("gpuFrameNum"),
			ValueLayout.JAVA_SHORT.withName("numDynamicIndexBuffers"),
			ValueLayout.JAVA_SHORT.withName("numDynamicVertexBuffers"),
			ValueLayout.JAVA_SHORT.withName("numFrameBuffers"),
			ValueLayout.JAVA_SHORT.withName("numIndexBuffers"),
			ValueLayout.JAVA_SHORT.withName("numOcclusionQueries"),
			ValueLayout.JAVA_SHORT.withName("numPrograms"),
			ValueLayout.JAVA_SHORT.withName("numShaders"),
			ValueLayout.JAVA_SHORT.withName("numTextures"),
			ValueLayout.JAVA_SHORT.withName("numUniforms"),
			ValueLayout.JAVA_SHORT.withName("numVertexBuffers"),
			ValueLayout.JAVA_SHORT.withName("numVertexLayouts"),
			ValueLayout.JAVA_LONG.withName("textureMemoryUsed"),
			ValueLayout.JAVA_LONG.withName("rtMemoryUsed"),
			ValueLayout.JAVA_INT.withName("transientVbUsed"),
			ValueLayout.JAVA_INT.withName("transientIbUsed"),
			MemoryLayout.sequenceLayout(5, ValueLayout.JAVA_INT).withName("numPrims"),
			ValueLayout.JAVA_LONG.withName("gpuMemoryMax"),
			ValueLayout.JAVA_LONG.withName("gpuMemoryUsed"),
			ValueLayout.JAVA_SHORT.withName("width"),
			ValueLayout.JAVA_SHORT.withName("height"),
			ValueLayout.JAVA_SHORT.withName("textWidth"),
			ValueLayout.JAVA_SHORT.withName("textHeight"),
			ValueLayout.JAVA_SHORT.withName("numViews"),
			ValueLayout.ADDRESS.withName("viewStats"),
			ValueLayout.JAVA_BYTE.withName("numEncoders"),
			ValueLayout.ADDRESS.withName("encoderStats"));
		private static final VarHandle VH_CPUTIMEFRAME = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("cpuTimeFrame"));
		private static final VarHandle VH_CPUTIMEBEGIN = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("cpuTimeBegin"));
		private static final VarHandle VH_CPUTIMEEND = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("cpuTimeEnd"));
		private static final VarHandle VH_CPUTIMERFREQ = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("cpuTimerFreq"));
		private static final VarHandle VH_GPUTIMEBEGIN = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("gpuTimeBegin"));
		private static final VarHandle VH_GPUTIMEEND = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("gpuTimeEnd"));
		private static final VarHandle VH_GPUTIMERFREQ = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("gpuTimerFreq"));
		private static final VarHandle VH_WAITRENDER = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("waitRender"));
		private static final VarHandle VH_WAITSUBMIT = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("waitSubmit"));
		private static final VarHandle VH_NUMDRAW = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numDraw"));
		private static final VarHandle VH_NUMCOMPUTE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numCompute"));
		private static final VarHandle VH_NUMBLIT = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numBlit"));
		private static final VarHandle VH_NUMBLITREPACK = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numBlitRepack"));
		private static final VarHandle VH_NUMDRAWCALLSPEAK = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numDrawCallsPeak"));
		private static final VarHandle VH_MAXGPULATENCY = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("maxGpuLatency"));
		private static final VarHandle VH_GPUFRAMENUM = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("gpuFrameNum"));
		private static final VarHandle VH_NUMDYNAMICINDEXBUFFERS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numDynamicIndexBuffers"));
		private static final VarHandle VH_NUMDYNAMICVERTEXBUFFERS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numDynamicVertexBuffers"));
		private static final VarHandle VH_NUMFRAMEBUFFERS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numFrameBuffers"));
		private static final VarHandle VH_NUMINDEXBUFFERS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numIndexBuffers"));
		private static final VarHandle VH_NUMOCCLUSIONQUERIES = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numOcclusionQueries"));
		private static final VarHandle VH_NUMPROGRAMS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numPrograms"));
		private static final VarHandle VH_NUMSHADERS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numShaders"));
		private static final VarHandle VH_NUMTEXTURES = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numTextures"));
		private static final VarHandle VH_NUMUNIFORMS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numUniforms"));
		private static final VarHandle VH_NUMVERTEXBUFFERS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numVertexBuffers"));
		private static final VarHandle VH_NUMVERTEXLAYOUTS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numVertexLayouts"));
		private static final VarHandle VH_TEXTUREMEMORYUSED = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("textureMemoryUsed"));
		private static final VarHandle VH_RTMEMORYUSED = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("rtMemoryUsed"));
		private static final VarHandle VH_TRANSIENTVBUSED = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("transientVbUsed"));
		private static final VarHandle VH_TRANSIENTIBUSED = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("transientIbUsed"));
		private static final MethodHandle MH_NUMPRIMS = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("numPrims"));
		private static final VarHandle VH_GPUMEMORYMAX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("gpuMemoryMax"));
		private static final VarHandle VH_GPUMEMORYUSED = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("gpuMemoryUsed"));
		private static final VarHandle VH_WIDTH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("width"));
		private static final VarHandle VH_HEIGHT = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("height"));
		private static final VarHandle VH_TEXTWIDTH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("textWidth"));
		private static final VarHandle VH_TEXTHEIGHT = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("textHeight"));
		private static final VarHandle VH_NUMVIEWS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numViews"));
		private static final VarHandle VH_VIEWSTATS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("viewStats"));
		private static final VarHandle VH_NUMENCODERS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("numEncoders"));
		private static final VarHandle VH_ENCODERSTATS = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("encoderStats"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public Stats(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public Stats(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * CPU time between two {@code BGFX.frame} calls.
		 * @return the field value
		 */
		public long cpuTimeFrame() {
			return (long) VH_CPUTIMEFRAME.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code cpuTimeFrame} field.
		 * @param value the new field value
		 */
		public void cpuTimeFrame(long value) {
			VH_CPUTIMEFRAME.set(segment(), 0L, value);
		}

		/**
		 * Render thread CPU submit begin time.
		 * @return the field value
		 */
		public long cpuTimeBegin() {
			return (long) VH_CPUTIMEBEGIN.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code cpuTimeBegin} field.
		 * @param value the new field value
		 */
		public void cpuTimeBegin(long value) {
			VH_CPUTIMEBEGIN.set(segment(), 0L, value);
		}

		/**
		 * Render thread CPU submit end time.
		 * @return the field value
		 */
		public long cpuTimeEnd() {
			return (long) VH_CPUTIMEEND.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code cpuTimeEnd} field.
		 * @param value the new field value
		 */
		public void cpuTimeEnd(long value) {
			VH_CPUTIMEEND.set(segment(), 0L, value);
		}

		/**
		 * CPU timer frequency. Timestamps-per-second
		 * @return the field value
		 */
		public long cpuTimerFreq() {
			return (long) VH_CPUTIMERFREQ.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code cpuTimerFreq} field.
		 * @param value the new field value
		 */
		public void cpuTimerFreq(long value) {
			VH_CPUTIMERFREQ.set(segment(), 0L, value);
		}

		/**
		 * GPU frame begin time.
		 * @return the field value
		 */
		public long gpuTimeBegin() {
			return (long) VH_GPUTIMEBEGIN.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code gpuTimeBegin} field.
		 * @param value the new field value
		 */
		public void gpuTimeBegin(long value) {
			VH_GPUTIMEBEGIN.set(segment(), 0L, value);
		}

		/**
		 * GPU frame end time.
		 * @return the field value
		 */
		public long gpuTimeEnd() {
			return (long) VH_GPUTIMEEND.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code gpuTimeEnd} field.
		 * @param value the new field value
		 */
		public void gpuTimeEnd(long value) {
			VH_GPUTIMEEND.set(segment(), 0L, value);
		}

		/**
		 * GPU timer frequency.
		 * @return the field value
		 */
		public long gpuTimerFreq() {
			return (long) VH_GPUTIMERFREQ.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code gpuTimerFreq} field.
		 * @param value the new field value
		 */
		public void gpuTimerFreq(long value) {
			VH_GPUTIMERFREQ.set(segment(), 0L, value);
		}

		/**
		 * Time spent waiting for render backend thread to finish issuing draw commands to underlying graphics API.
		 * @return the field value
		 */
		public long waitRender() {
			return (long) VH_WAITRENDER.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code waitRender} field.
		 * @param value the new field value
		 */
		public void waitRender(long value) {
			VH_WAITRENDER.set(segment(), 0L, value);
		}

		/**
		 * Time spent waiting for submit thread to advance to next frame.
		 * @return the field value
		 */
		public long waitSubmit() {
			return (long) VH_WAITSUBMIT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code waitSubmit} field.
		 * @param value the new field value
		 */
		public void waitSubmit(long value) {
			VH_WAITSUBMIT.set(segment(), 0L, value);
		}

		/**
		 * Number of draw calls submitted.
		 * @return the field value
		 */
		public int numDraw() {
			return (int) VH_NUMDRAW.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numDraw} field.
		 * @param value the new field value
		 */
		public void numDraw(int value) {
			VH_NUMDRAW.set(segment(), 0L, value);
		}

		/**
		 * Number of compute calls submitted.
		 * @return the field value
		 */
		public int numCompute() {
			return (int) VH_NUMCOMPUTE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numCompute} field.
		 * @param value the new field value
		 */
		public void numCompute(int value) {
			VH_NUMCOMPUTE.set(segment(), 0L, value);
		}

		/**
		 * Number of blit calls submitted.
		 * @return the field value
		 */
		public int numBlit() {
			return (int) VH_NUMBLIT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numBlit} field.
		 * @param value the new field value
		 */
		public void numBlit(int value) {
			VH_NUMBLIT.set(segment(), 0L, value);
		}

		/**
		 * Number of buffer to texture blit calls that had to be repacked,
		 * because {@code BufferRegion.rowPitch} or {@code offset} didn't match
		 * {@code Caps.Limits.blitRowPitchAlign} or {@code blitOffsetAlign}.
		 * @return the field value
		 */
		public int numBlitRepack() {
			return (int) VH_NUMBLITREPACK.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numBlitRepack} field.
		 * @param value the new field value
		 */
		public void numBlitRepack(int value) {
			VH_NUMBLITREPACK.set(segment(), 0L, value);
		}

		/**
		 * Highest number of draw+compute calls requested in a single
		 * frame so far (peak demand, before any were dropped). Useful
		 * to tune {@code Init.Limits.numDrawCalls}.
		 * @return the field value
		 */
		public int numDrawCallsPeak() {
			return (int) VH_NUMDRAWCALLSPEAK.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numDrawCallsPeak} field.
		 * @param value the new field value
		 */
		public void numDrawCallsPeak(int value) {
			VH_NUMDRAWCALLSPEAK.set(segment(), 0L, value);
		}

		/**
		 * GPU driver latency.
		 * @return the field value
		 */
		public int maxGpuLatency() {
			return (int) VH_MAXGPULATENCY.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code maxGpuLatency} field.
		 * @param value the new field value
		 */
		public void maxGpuLatency(int value) {
			VH_MAXGPULATENCY.set(segment(), 0L, value);
		}

		/**
		 * Frame which generated gpuTimeBegin, gpuTimeEnd.
		 * @return the field value
		 */
		public int gpuFrameNum() {
			return (int) VH_GPUFRAMENUM.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code gpuFrameNum} field.
		 * @param value the new field value
		 */
		public void gpuFrameNum(int value) {
			VH_GPUFRAMENUM.set(segment(), 0L, value);
		}

		/**
		 * Number of used dynamic index buffers.
		 * @return the field value
		 */
		public short numDynamicIndexBuffers() {
			return (short) VH_NUMDYNAMICINDEXBUFFERS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numDynamicIndexBuffers} field.
		 * @param value the new field value
		 */
		public void numDynamicIndexBuffers(short value) {
			VH_NUMDYNAMICINDEXBUFFERS.set(segment(), 0L, value);
		}

		/**
		 * Number of used dynamic vertex buffers.
		 * @return the field value
		 */
		public short numDynamicVertexBuffers() {
			return (short) VH_NUMDYNAMICVERTEXBUFFERS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numDynamicVertexBuffers} field.
		 * @param value the new field value
		 */
		public void numDynamicVertexBuffers(short value) {
			VH_NUMDYNAMICVERTEXBUFFERS.set(segment(), 0L, value);
		}

		/**
		 * Number of used frame buffers.
		 * @return the field value
		 */
		public short numFrameBuffers() {
			return (short) VH_NUMFRAMEBUFFERS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numFrameBuffers} field.
		 * @param value the new field value
		 */
		public void numFrameBuffers(short value) {
			VH_NUMFRAMEBUFFERS.set(segment(), 0L, value);
		}

		/**
		 * Number of used index buffers.
		 * @return the field value
		 */
		public short numIndexBuffers() {
			return (short) VH_NUMINDEXBUFFERS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numIndexBuffers} field.
		 * @param value the new field value
		 */
		public void numIndexBuffers(short value) {
			VH_NUMINDEXBUFFERS.set(segment(), 0L, value);
		}

		/**
		 * Number of used occlusion queries.
		 * @return the field value
		 */
		public short numOcclusionQueries() {
			return (short) VH_NUMOCCLUSIONQUERIES.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numOcclusionQueries} field.
		 * @param value the new field value
		 */
		public void numOcclusionQueries(short value) {
			VH_NUMOCCLUSIONQUERIES.set(segment(), 0L, value);
		}

		/**
		 * Number of used programs.
		 * @return the field value
		 */
		public short numPrograms() {
			return (short) VH_NUMPROGRAMS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numPrograms} field.
		 * @param value the new field value
		 */
		public void numPrograms(short value) {
			VH_NUMPROGRAMS.set(segment(), 0L, value);
		}

		/**
		 * Number of used shaders.
		 * @return the field value
		 */
		public short numShaders() {
			return (short) VH_NUMSHADERS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numShaders} field.
		 * @param value the new field value
		 */
		public void numShaders(short value) {
			VH_NUMSHADERS.set(segment(), 0L, value);
		}

		/**
		 * Number of used textures.
		 * @return the field value
		 */
		public short numTextures() {
			return (short) VH_NUMTEXTURES.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numTextures} field.
		 * @param value the new field value
		 */
		public void numTextures(short value) {
			VH_NUMTEXTURES.set(segment(), 0L, value);
		}

		/**
		 * Number of used uniforms.
		 * @return the field value
		 */
		public short numUniforms() {
			return (short) VH_NUMUNIFORMS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numUniforms} field.
		 * @param value the new field value
		 */
		public void numUniforms(short value) {
			VH_NUMUNIFORMS.set(segment(), 0L, value);
		}

		/**
		 * Number of used vertex buffers.
		 * @return the field value
		 */
		public short numVertexBuffers() {
			return (short) VH_NUMVERTEXBUFFERS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numVertexBuffers} field.
		 * @param value the new field value
		 */
		public void numVertexBuffers(short value) {
			VH_NUMVERTEXBUFFERS.set(segment(), 0L, value);
		}

		/**
		 * Number of used vertex layouts.
		 * @return the field value
		 */
		public short numVertexLayouts() {
			return (short) VH_NUMVERTEXLAYOUTS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numVertexLayouts} field.
		 * @param value the new field value
		 */
		public void numVertexLayouts(short value) {
			VH_NUMVERTEXLAYOUTS.set(segment(), 0L, value);
		}

		/**
		 * Estimate of texture memory used.
		 * @return the field value
		 */
		public long textureMemoryUsed() {
			return (long) VH_TEXTUREMEMORYUSED.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code textureMemoryUsed} field.
		 * @param value the new field value
		 */
		public void textureMemoryUsed(long value) {
			VH_TEXTUREMEMORYUSED.set(segment(), 0L, value);
		}

		/**
		 * Estimate of render target memory used.
		 * @return the field value
		 */
		public long rtMemoryUsed() {
			return (long) VH_RTMEMORYUSED.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code rtMemoryUsed} field.
		 * @param value the new field value
		 */
		public void rtMemoryUsed(long value) {
			VH_RTMEMORYUSED.set(segment(), 0L, value);
		}

		/**
		 * Amount of transient vertex buffer used.
		 * @return the field value
		 */
		public int transientVbUsed() {
			return (int) VH_TRANSIENTVBUSED.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code transientVbUsed} field.
		 * @param value the new field value
		 */
		public void transientVbUsed(int value) {
			VH_TRANSIENTVBUSED.set(segment(), 0L, value);
		}

		/**
		 * Amount of transient index buffer used.
		 * @return the field value
		 */
		public int transientIbUsed() {
			return (int) VH_TRANSIENTIBUSED.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code transientIbUsed} field.
		 * @param value the new field value
		 */
		public void transientIbUsed(int value) {
			VH_TRANSIENTIBUSED.set(segment(), 0L, value);
		}

		/**
		 * Number of primitives rendered.
		 * @return a segment view of the inline array
		 */
		public MemorySegment numPrims() {
			return slice(MH_NUMPRIMS, segment());
		}

		/**
		 * Maximum available GPU memory for application.
		 * @return the field value
		 */
		public long gpuMemoryMax() {
			return (long) VH_GPUMEMORYMAX.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code gpuMemoryMax} field.
		 * @param value the new field value
		 */
		public void gpuMemoryMax(long value) {
			VH_GPUMEMORYMAX.set(segment(), 0L, value);
		}

		/**
		 * Amount of GPU memory used by the application.
		 * @return the field value
		 */
		public long gpuMemoryUsed() {
			return (long) VH_GPUMEMORYUSED.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code gpuMemoryUsed} field.
		 * @param value the new field value
		 */
		public void gpuMemoryUsed(long value) {
			VH_GPUMEMORYUSED.set(segment(), 0L, value);
		}

		/**
		 * Backbuffer width in pixels.
		 * @return the field value
		 */
		public short width() {
			return (short) VH_WIDTH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code width} field.
		 * @param value the new field value
		 */
		public void width(short value) {
			VH_WIDTH.set(segment(), 0L, value);
		}

		/**
		 * Backbuffer height in pixels.
		 * @return the field value
		 */
		public short height() {
			return (short) VH_HEIGHT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code height} field.
		 * @param value the new field value
		 */
		public void height(short value) {
			VH_HEIGHT.set(segment(), 0L, value);
		}

		/**
		 * Debug text width in characters.
		 * @return the field value
		 */
		public short textWidth() {
			return (short) VH_TEXTWIDTH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code textWidth} field.
		 * @param value the new field value
		 */
		public void textWidth(short value) {
			VH_TEXTWIDTH.set(segment(), 0L, value);
		}

		/**
		 * Debug text height in characters.
		 * @return the field value
		 */
		public short textHeight() {
			return (short) VH_TEXTHEIGHT.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code textHeight} field.
		 * @param value the new field value
		 */
		public void textHeight(short value) {
			VH_TEXTHEIGHT.set(segment(), 0L, value);
		}

		/**
		 * Number of view stats.
		 * @return the field value
		 */
		public short numViews() {
			return (short) VH_NUMVIEWS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numViews} field.
		 * @param value the new field value
		 */
		public void numViews(short value) {
			VH_NUMVIEWS.set(segment(), 0L, value);
		}

		/**
		 * Array of View stats.
		 * @return the field value
		 */
		public ViewStats viewStats() {
			return new ViewStats((MemorySegment) VH_VIEWSTATS.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code viewStats} field.
		 * @param value the new field value
		 */
		public void viewStats(ViewStats value) {
			VH_VIEWSTATS.set(segment(), 0L, address(value));
		}

		/**
		 * Number of encoders used during frame.
		 * @return the field value
		 */
		public byte numEncoders() {
			return (byte) VH_NUMENCODERS.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code numEncoders} field.
		 * @param value the new field value
		 */
		public void numEncoders(byte value) {
			VH_NUMENCODERS.set(segment(), 0L, value);
		}

		/**
		 * Array of encoder stats.
		 * @return the field value
		 */
		public EncoderStats encoderStats() {
			return new EncoderStats((MemorySegment) VH_ENCODERSTATS.get(segment(), 0L));
		}

		/**
		 * Sets the native {@code encoderStats} field.
		 * @param value the new field value
		 */
		public void encoderStats(EncoderStats value) {
			VH_ENCODERSTATS.set(segment(), 0L, address(value));
		}
	}

	/**
	 * Vertex layout.
	 */
	public static final class VertexLayout extends NativeObject {
		/**
		 * Native C structure layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_vertex_layout_t",
			ValueLayout.JAVA_INT.withName("hash"),
			ValueLayout.JAVA_SHORT.withName("stride"),
			MemoryLayout.sequenceLayout(26, ValueLayout.JAVA_SHORT).withName("offset"),
			MemoryLayout.sequenceLayout(26, ValueLayout.JAVA_SHORT).withName("attributes"));
		private static final VarHandle VH_HASH = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("hash"));
		private static final VarHandle VH_STRIDE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("stride"));
		private static final MethodHandle MH_OFFSET = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("offset"));
		private static final MethodHandle MH_ATTRIBUTES = LAYOUT.sliceHandle(
			MemoryLayout.PathElement.groupElement("attributes"));
		/**
		 * Wraps an existing native structure.
		 * @param segment native memory segment
		 */
		public VertexLayout(MemorySegment segment) {
			super(segment, LAYOUT);
		}

		/**
		 * Allocates a native structure.
		 * @param allocator destination allocator
		 */
		public VertexLayout(SegmentAllocator allocator) {
			super(allocator, LAYOUT);
		}

		/**
		 * Hash.
		 * @return the field value
		 */
		public int hash() {
			return (int) VH_HASH.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code hash} field.
		 * @param value the new field value
		 */
		public void hash(int value) {
			VH_HASH.set(segment(), 0L, value);
		}

		/**
		 * Stride.
		 * @return the field value
		 */
		public short stride() {
			return (short) VH_STRIDE.get(segment(), 0L);
		}

		/**
		 * Sets the native {@code stride} field.
		 * @param value the new field value
		 */
		public void stride(short value) {
			VH_STRIDE.set(segment(), 0L, value);
		}

		/**
		 * Attribute offsets.
		 * @return a segment view of the inline array
		 */
		public MemorySegment offset() {
			return slice(MH_OFFSET, segment());
		}

		/**
		 * Used attributes.
		 * @return a segment view of the inline array
		 */
		public MemorySegment attributes() {
			return slice(MH_ATTRIBUTES, segment());
		}

		/**
		 * Start VertexLayout.
		 * @param _rendererType Renderer backend type. See: {@code BGFX.RendererType}
		 * @return Returns itself.
		 */
		public final VertexLayout begin(RendererType _rendererType) {
			try {
				return new VertexLayout((MemorySegment) downcallHandle(DC_VERTEX_LAYOUT_BEGIN).invokeExact(segment(), _rendererType.ordinal()));
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Add attribute to VertexLayout.
		 * <p>
		 * <strong>Remarks:</strong> Must be called between begin/end.
		 * @param _attrib Attribute semantics. See: {@code BGFX.Attrib}
		 * @param _num Number of elements 1, 2, 3 or 4.
		 * @param _type Element type.
		 * @param _normalized When using fixed point AttribType (f.e. Uint8) value will be normalized for vertex shader usage. When normalized is set to true, AttribType.Uint8 value in range 0-255 will be in range 0.0-1.0 in vertex shader.
		 * @param _asInt Packaging rule for vertexPack, vertexUnpack, and vertexConvert for AttribType.Uint8 and AttribType.Int16. Unpacking code must be implemented inside vertex shader.
		 * @return Returns itself.
		 */
		public final VertexLayout add(Attrib _attrib, byte _num, AttribType _type, boolean _normalized, boolean _asInt) {
			try {
				return new VertexLayout((MemorySegment) downcallHandle(DC_VERTEX_LAYOUT_ADD).invokeExact(segment(), _attrib.ordinal(), _num, _type.ordinal(), _normalized, _asInt));
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Decode attribute.
		 * @param _attrib Attribute semantics. See: {@code BGFX.Attrib}
		 * @param _num Number of elements.
		 * @param _type Element type.
		 * @param _normalized Attribute is normalized.
		 * @param _asInt Attribute is packed as int.
		 */
		public final void decode(Attrib _attrib, MemorySegment _num, MemorySegment _type, MemorySegment _normalized, MemorySegment _asInt) {
			try {
				downcallHandle(DC_VERTEX_LAYOUT_DECODE).invokeExact(segment(), _attrib.ordinal(), address(_num), address(_type), address(_normalized), address(_asInt));
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Returns {@code true} if VertexLayout contains attribute.
		 * @param _attrib Attribute semantics. See: {@code BGFX.Attrib}
		 * @return True if VertexLayout contains attribute.
		 */
		public final boolean has(Attrib _attrib) {
			try {
				return (boolean) downcallHandle(DC_VERTEX_LAYOUT_HAS).invokeExact(segment(), _attrib.ordinal());
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Skip {@code _num} bytes in vertex stream.
		 * @param _num Number of bytes to skip.
		 * @return Returns itself.
		 */
		public final VertexLayout skip(byte _num) {
			try {
				return new VertexLayout((MemorySegment) downcallHandle(DC_VERTEX_LAYOUT_SKIP).invokeExact(segment(), _num));
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * End VertexLayout.
		 */
		public final void end() {
			try {
				downcallHandle(DC_VERTEX_LAYOUT_END).invokeExact(segment());
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Returns relative attribute offset from the vertex.
		 * @param _attrib Attribute semantics. See: {@code BGFX.Attrib}
		 * @return Relative attribute offset from the vertex.
		 */
		public final short getOffset(Attrib _attrib) {
			try {
				return (short) downcallHandle(DC_VERTEX_LAYOUT_GET_OFFSET).invokeExact(segment(), _attrib.ordinal());
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Returns vertex stride.
		 * @return Vertex stride.
		 */
		public final short getStride() {
			try {
				return (short) downcallHandle(DC_VERTEX_LAYOUT_GET_STRIDE).invokeExact(segment());
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Returns size of vertex buffer for number of vertices.
		 * @param _num Number of vertices.
		 * @return Size of vertex buffer for number of vertices.
		 */
		public final int getSize(int _num) {
			try {
				return (int) downcallHandle(DC_VERTEX_LAYOUT_GET_SIZE).invokeExact(segment(), _num);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}
	}

	/**
	 * Encoders are used for submitting draw calls from multiple threads. Only one encoder
	 * per thread should be used. Use {@code BGFX.begin()} to obtain an encoder for a thread.
	 */
	public static final class Encoder extends NativeObject {
		/**
		 * Wraps an opaque native pointer.
		 * @param segment native memory segment
		 */
		public Encoder(MemorySegment segment) {
			super(segment);
		}

		/**
		 * Sets a debug marker. This allows you to group graphics calls together for easy browsing in
		 * graphics debugging tools.
		 * @param _name Marker name.
		 * @param _len Marker name length (if length is INT32_MAX, it's expected that _name is zero terminated string.
		 */
		public final void setMarker(String _name, int _len) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_MARKER).invokeExact(segment(), cString(arena, _name), _len);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

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
		public final void setState(long _state, int _rgba) {
			try {
				downcallHandle(DC_ENCODER_SET_STATE).invokeExact(segment(), _state, _rgba);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set condition for rendering.
		 * @param _handle Occlusion query handle.
		 * @param _visible Render if occlusion query is visible.
		 */
		public final void setCondition(OcclusionQueryHandle _handle, boolean _visible) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_CONDITION).invokeExact(segment(), _handle.allocate(arena), _visible);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set stencil test state.
		 * @param _fstencil Front stencil state.
		 * @param _bstencil Back stencil state. If back is set to {@code BGFX_STENCIL_NONE} _fstencil is applied to both front and back facing primitives.
		 */
		public final void setStencil(int _fstencil, int _bstencil) {
			try {
				downcallHandle(DC_ENCODER_SET_STENCIL).invokeExact(segment(), _fstencil, _bstencil);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set scissor for draw primitive.
		 * <p>
		 * <strong>Remarks:</strong> 
		 *   To scissor for all primitives in view see {@code BGFX.setViewScissor}.
		 * @param _x Position x from the left corner of the window.
		 * @param _y Position y from the top corner of the window.
		 * @param _width Width of view scissor region.
		 * @param _height Height of view scissor region.
		 * @return Scissor cache index.
		 */
		public final short setScissor(short _x, short _y, short _width, short _height) {
			try {
				return (short) downcallHandle(DC_ENCODER_SET_SCISSOR).invokeExact(segment(), _x, _y, _width, _height);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set scissor from cache for draw primitive.
		 * <p>
		 * <strong>Remarks:</strong> 
		 *   To scissor for all primitives in view see {@code BGFX.setViewScissor}.
		 * @param _cache Index in scissor cache.
		 */
		public final void setScissorCached(short _cache) {
			try {
				downcallHandle(DC_ENCODER_SET_SCISSOR_CACHED).invokeExact(segment(), _cache);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set model matrix for draw primitive. If it is not called,
		 * the model will be rendered with an identity model matrix.
		 * @param _mtx Pointer to first matrix in array.
		 * @param _num Number of matrices in array.
		 * @return Index into matrix cache in case the same model matrix has to be used for other draw primitive call.
		 */
		public final int setTransform(MemorySegment _mtx, short _num) {
			try {
				return (int) downcallHandle(DC_ENCODER_SET_TRANSFORM).invokeExact(segment(), address(_mtx), _num);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 *  Set model matrix from matrix cache for draw primitive.
		 * @param _cache Index in matrix cache.
		 * @param _num Number of matrices from cache.
		 */
		public final void setTransformCached(int _cache, short _num) {
			try {
				downcallHandle(DC_ENCODER_SET_TRANSFORM_CACHED).invokeExact(segment(), _cache, _num);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Reserve matrices in internal matrix cache.
		 * <p>
		 * <strong>Attention:</strong> Pointer returned can be modified until {@code BGFX.frame} is called.
		 * @param _transform Pointer to {@code Transform} structure.
		 * @param _num Number of matrices.
		 * @return Index in matrix cache.
		 */
		public final int allocTransform(Transform _transform, short _num) {
			try {
				return (int) downcallHandle(DC_ENCODER_ALLOC_TRANSFORM).invokeExact(segment(), address(_transform), _num);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set shader uniform parameter for draw primitive.
		 * @param _handle Uniform.
		 * @param _value Pointer to uniform data.
		 * @param _num Number of elements. Passing {@code UINT16_MAX} will use the _num passed on uniform creation.
		 */
		public final void setUniform(UniformHandle _handle, MemorySegment _value, short _num) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_UNIFORM).invokeExact(segment(), _handle.allocate(arena), address(_value), _num);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set index buffer for draw primitive.
		 * @param _handle Index buffer.
		 * @param _firstIndex First index to render.
		 * @param _numIndices Number of indices to render.
		 */
		public final void setIndexBuffer(IndexBufferHandle _handle, int _firstIndex, int _numIndices) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_INDEX_BUFFER).invokeExact(segment(), _handle.allocate(arena), _firstIndex, _numIndices);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set index buffer for draw primitive.
		 * @param _handle Dynamic index buffer.
		 * @param _firstIndex First index to render.
		 * @param _numIndices Number of indices to render.
		 */
		public final void setDynamicIndexBuffer(DynamicIndexBufferHandle _handle, int _firstIndex, int _numIndices) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_DYNAMIC_INDEX_BUFFER).invokeExact(segment(), _handle.allocate(arena), _firstIndex, _numIndices);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set index buffer for draw primitive.
		 * @param _tib Transient index buffer.
		 * @param _firstIndex First index to render.
		 * @param _numIndices Number of indices to render.
		 */
		public final void setTransientIndexBuffer(TransientIndexBuffer _tib, int _firstIndex, int _numIndices) {
			try {
				downcallHandle(DC_ENCODER_SET_TRANSIENT_INDEX_BUFFER).invokeExact(segment(), address(_tib), _firstIndex, _numIndices);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set vertex buffer for draw primitive.
		 * @param _stream Vertex stream.
		 * @param _handle Vertex buffer.
		 * @param _startVertex First vertex to render.
		 * @param _numVertices Number of vertices to render.
		 */
		public final void setVertexBuffer(byte _stream, VertexBufferHandle _handle, int _startVertex, int _numVertices) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_VERTEX_BUFFER).invokeExact(segment(), _stream, _handle.allocate(arena), _startVertex, _numVertices);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set vertex buffer for draw primitive.
		 * @param _stream Vertex stream.
		 * @param _handle Vertex buffer.
		 * @param _startVertex First vertex to render.
		 * @param _numVertices Number of vertices to render.
		 * @param _layoutHandle Vertex layout for aliasing vertex buffer. If invalid handle is used, vertex layout used for creation of vertex buffer will be used.
		 */
		public final void setVertexBufferWithLayout(byte _stream, VertexBufferHandle _handle, int _startVertex, int _numVertices, VertexLayoutHandle _layoutHandle) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_VERTEX_BUFFER_WITH_LAYOUT).invokeExact(segment(), _stream, _handle.allocate(arena), _startVertex, _numVertices, _layoutHandle.allocate(arena));
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set vertex buffer for draw primitive.
		 * @param _stream Vertex stream.
		 * @param _handle Dynamic vertex buffer.
		 * @param _startVertex First vertex to render.
		 * @param _numVertices Number of vertices to render.
		 */
		public final void setDynamicVertexBuffer(byte _stream, DynamicVertexBufferHandle _handle, int _startVertex, int _numVertices) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_DYNAMIC_VERTEX_BUFFER).invokeExact(segment(), _stream, _handle.allocate(arena), _startVertex, _numVertices);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set vertex buffer for draw primitive.
		 * @param _stream Vertex stream.
		 * @param _handle Dynamic vertex buffer.
		 * @param _startVertex First vertex to render.
		 * @param _numVertices Number of vertices to render.
		 * @param _layoutHandle Vertex layout for aliasing vertex buffer. If invalid handle is used, vertex layout used for creation of vertex buffer will be used.
		 */
		public final void setDynamicVertexBufferWithLayout(byte _stream, DynamicVertexBufferHandle _handle, int _startVertex, int _numVertices, VertexLayoutHandle _layoutHandle) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT).invokeExact(segment(), _stream, _handle.allocate(arena), _startVertex, _numVertices, _layoutHandle.allocate(arena));
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set vertex buffer for draw primitive.
		 * @param _stream Vertex stream.
		 * @param _tvb Transient vertex buffer.
		 * @param _startVertex First vertex to render.
		 * @param _numVertices Number of vertices to render.
		 */
		public final void setTransientVertexBuffer(byte _stream, TransientVertexBuffer _tvb, int _startVertex, int _numVertices) {
			try {
				downcallHandle(DC_ENCODER_SET_TRANSIENT_VERTEX_BUFFER).invokeExact(segment(), _stream, address(_tvb), _startVertex, _numVertices);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set vertex buffer for draw primitive.
		 * @param _stream Vertex stream.
		 * @param _tvb Transient vertex buffer.
		 * @param _startVertex First vertex to render.
		 * @param _numVertices Number of vertices to render.
		 * @param _layoutHandle Vertex layout for aliasing vertex buffer. If invalid handle is used, vertex layout used for creation of vertex buffer will be used.
		 */
		public final void setTransientVertexBufferWithLayout(byte _stream, TransientVertexBuffer _tvb, int _startVertex, int _numVertices, VertexLayoutHandle _layoutHandle) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT).invokeExact(segment(), _stream, address(_tvb), _startVertex, _numVertices, _layoutHandle.allocate(arena));
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set number of vertices for auto generated vertices use in conjunction
		 * with gl_VertexID.
		 * <p>
		 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_VERTEX_ID}.
		 * @param _numVertices Number of vertices.
		 */
		public final void setVertexCount(int _numVertices) {
			try {
				downcallHandle(DC_ENCODER_SET_VERTEX_COUNT).invokeExact(segment(), _numVertices);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set instance data buffer for draw primitive.
		 * @param _idb Transient instance data buffer.
		 * @param _start First instance data.
		 * @param _num Number of data instances.
		 */
		public final void setInstanceDataBuffer(InstanceDataBuffer _idb, int _start, int _num) {
			try {
				downcallHandle(DC_ENCODER_SET_INSTANCE_DATA_BUFFER).invokeExact(segment(), address(_idb), _start, _num);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set instance data buffer for draw primitive.
		 * @param _handle Vertex buffer.
		 * @param _startVertex First instance data.
		 * @param _num Number of data instances.
		 */
		public final void setInstanceDataFromVertexBuffer(VertexBufferHandle _handle, int _startVertex, int _num) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER).invokeExact(segment(), _handle.allocate(arena), _startVertex, _num);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set instance data buffer for draw primitive.
		 * @param _handle Dynamic vertex buffer.
		 * @param _startVertex First instance data.
		 * @param _num Number of data instances.
		 */
		public final void setInstanceDataFromDynamicVertexBuffer(DynamicVertexBufferHandle _handle, int _startVertex, int _num) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER).invokeExact(segment(), _handle.allocate(arena), _startVertex, _num);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set number of instances for auto generated instances use in conjunction
		 * with gl_InstanceID.
		 * <p>
		 * <strong>Attention:</strong> Availability depends on: {@code BGFX_CAPS_VERTEX_ID}.
		 * @param _numInstances Number of instances.
		 */
		public final void setInstanceCount(int _numInstances) {
			try {
				downcallHandle(DC_ENCODER_SET_INSTANCE_COUNT).invokeExact(segment(), _numInstances);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set texture stage for draw primitive.
		 * @param _stage Texture unit.
		 * @param _sampler Program sampler.
		 * @param _handle Texture handle.
		 * @param _flags Texture sampling mode. Default value UINT32_MAX uses   texture sampling settings from the texture.   - {@code BGFX_SAMPLER_[U/V/W]_[MIRROR/CLAMP]} - Mirror or clamp to edge wrap     mode.   - {@code BGFX_SAMPLER_[MIN/MAG/MIP]_[POINT/ANISOTROPIC]} - Point or anisotropic     sampling.
		 */
		public final void setTexture(byte _stage, UniformHandle _sampler, TextureHandle _handle, int _flags) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_TEXTURE).invokeExact(segment(), _stage, _sampler.allocate(arena), _handle.allocate(arena), _flags);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

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
		public final void setTextureView(byte _stage, UniformHandle _sampler, TextureHandle _handle, short _firstLayer, short _numLayers, byte _firstMip, byte _numMips, int _flags) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_TEXTURE_VIEW).invokeExact(segment(), _stage, _sampler.allocate(arena), _handle.allocate(arena), _firstLayer, _numLayers, _firstMip, _numMips, _flags);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Submit an empty primitive for rendering. Uniforms and draw state
		 * will be applied but no geometry will be submitted. Useful in cases
		 * when no other draw/compute primitive is submitted to view, but it's
		 * desired to execute clear view.
		 * <p>
		 * <strong>Remarks:</strong> 
		 *   These empty draw calls will sort before ordinary draw calls.
		 * @param _id View id.
		 */
		public final void touch(short _id) {
			try {
				downcallHandle(DC_ENCODER_TOUCH).invokeExact(segment(), _id);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Submit primitive for rendering.
		 * @param _id View id.
		 * @param _program Program.
		 * @param _depth Depth for sorting.
		 * @param _flags Discard or preserve states. See {@code BGFX_DISCARD_*}.
		 */
		public final void submit(short _id, ProgramHandle _program, int _depth, byte _flags) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SUBMIT).invokeExact(segment(), _id, _program.allocate(arena), _depth, _flags);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Submit primitive with occlusion query for rendering.
		 * @param _id View id.
		 * @param _program Program.
		 * @param _occlusionQuery Occlusion query.
		 * @param _depth Depth for sorting.
		 * @param _flags Discard or preserve states. See {@code BGFX_DISCARD_*}.
		 */
		public final void submitOcclusionQuery(short _id, ProgramHandle _program, OcclusionQueryHandle _occlusionQuery, int _depth, byte _flags) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SUBMIT_OCCLUSION_QUERY).invokeExact(segment(), _id, _program.allocate(arena), _occlusionQuery.allocate(arena), _depth, _flags);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

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
		 * @param _flags Discard or preserve states. See {@code BGFX_DISCARD_*}.
		 */
		public final void submitIndirect(short _id, ProgramHandle _program, IndirectBufferHandle _indirectHandle, int _start, int _num, int _depth, byte _flags) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SUBMIT_INDIRECT).invokeExact(segment(), _id, _program.allocate(arena), _indirectHandle.allocate(arena), _start, _num, _depth, _flags);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

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
		 * @param _flags Discard or preserve states. See {@code BGFX_DISCARD_*}.
		 */
		public final void submitIndirectCount(short _id, ProgramHandle _program, IndirectBufferHandle _indirectHandle, int _start, IndexBufferHandle _numHandle, int _numIndex, int _numMax, int _depth, byte _flags) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SUBMIT_INDIRECT_COUNT).invokeExact(segment(), _id, _program.allocate(arena), _indirectHandle.allocate(arena), _start, _numHandle.allocate(arena), _numIndex, _numMax, _depth, _flags);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set compute index buffer.
		 * @param _stage Compute stage.
		 * @param _handle Index buffer handle.
		 * @param _access Buffer access. See {@code Access}.
		 */
		public final void setComputeIndexBuffer(byte _stage, IndexBufferHandle _handle, Access _access) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_COMPUTE_INDEX_BUFFER).invokeExact(segment(), _stage, _handle.allocate(arena), _access.ordinal());
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set compute vertex buffer.
		 * @param _stage Compute stage.
		 * @param _handle Vertex buffer handle.
		 * @param _access Buffer access. See {@code Access}.
		 */
		public final void setComputeVertexBuffer(byte _stage, VertexBufferHandle _handle, Access _access) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_COMPUTE_VERTEX_BUFFER).invokeExact(segment(), _stage, _handle.allocate(arena), _access.ordinal());
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set compute dynamic index buffer.
		 * @param _stage Compute stage.
		 * @param _handle Dynamic index buffer handle.
		 * @param _access Buffer access. See {@code Access}.
		 */
		public final void setComputeDynamicIndexBuffer(byte _stage, DynamicIndexBufferHandle _handle, Access _access) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_COMPUTE_DYNAMIC_INDEX_BUFFER).invokeExact(segment(), _stage, _handle.allocate(arena), _access.ordinal());
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set compute dynamic vertex buffer.
		 * @param _stage Compute stage.
		 * @param _handle Dynamic vertex buffer handle.
		 * @param _access Buffer access. See {@code Access}.
		 */
		public final void setComputeDynamicVertexBuffer(byte _stage, DynamicVertexBufferHandle _handle, Access _access) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER).invokeExact(segment(), _stage, _handle.allocate(arena), _access.ordinal());
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set compute indirect buffer.
		 * @param _stage Compute stage.
		 * @param _handle Indirect buffer handle.
		 * @param _access Buffer access. See {@code Access}.
		 */
		public final void setComputeIndirectBuffer(byte _stage, IndirectBufferHandle _handle, Access _access) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_COMPUTE_INDIRECT_BUFFER).invokeExact(segment(), _stage, _handle.allocate(arena), _access.ordinal());
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Set compute image from texture.
		 * @param _stage Compute stage.
		 * @param _handle Texture handle.
		 * @param _mip Mip level.
		 * @param _access Image access. See {@code Access}.
		 * @param _format Texture format. See: {@code TextureFormat}.
		 */
		public final void setImage(byte _stage, TextureHandle _handle, byte _mip, Access _access, TextureFormat _format) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_IMAGE).invokeExact(segment(), _stage, _handle.allocate(arena), _mip, _access.ordinal(), _format.ordinal());
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

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
		public final void setImageView(byte _stage, TextureHandle _handle, short _firstLayer, short _numLayers, byte _mip, Access _access, TextureFormat _format) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_SET_IMAGE_VIEW).invokeExact(segment(), _stage, _handle.allocate(arena), _firstLayer, _numLayers, _mip, _access.ordinal(), _format.ordinal());
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Dispatch compute.
		 * @param _id View id.
		 * @param _program Compute program.
		 * @param _numX Number of groups X.
		 * @param _numY Number of groups Y.
		 * @param _numZ Number of groups Z.
		 * @param _flags Discard or preserve states. See {@code BGFX_DISCARD_*}.
		 */
		public final void dispatch(short _id, ProgramHandle _program, int _numX, int _numY, int _numZ, byte _flags) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_DISPATCH).invokeExact(segment(), _id, _program.allocate(arena), _numX, _numY, _numZ, _flags);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Dispatch compute indirect.
		 * @param _id View id.
		 * @param _program Compute program.
		 * @param _indirectHandle Indirect buffer.
		 * @param _start First element in indirect buffer.
		 * @param _num Number of dispatches.
		 * @param _flags Discard or preserve states. See {@code BGFX_DISCARD_*}.
		 */
		public final void dispatchIndirect(short _id, ProgramHandle _program, IndirectBufferHandle _indirectHandle, int _start, int _num, byte _flags) {
			try {
				try (Arena arena = Arena.ofConfined()) {
					downcallHandle(DC_ENCODER_DISPATCH_INDIRECT).invokeExact(segment(), _id, _program.allocate(arena), _indirectHandle.allocate(arena), _start, _num, _flags);
				}
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

		/**
		 * Discard previously set state for draw or compute call.
		 * @param _flags Discard or preserve states. See {@code BGFX_DISCARD_*}.
		 */
		public final void discard(byte _flags) {
			try {
				downcallHandle(DC_ENCODER_DISCARD).invokeExact(segment(), _flags);
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

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
		public final void blit(short _id, TextureRegion _dst, TextureRegion _src) {
			try {
				downcallHandle(DC_ENCODER_BLIT).invokeExact(segment(), _id, address(_dst), address(_src));
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

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
		public final void blitBuffer(short _id, BufferRegion _dst, BufferRegion _src) {
			try {
				downcallHandle(DC_ENCODER_BLIT_BUFFER).invokeExact(segment(), _id, address(_dst), address(_src));
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

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
		public final void blitToBuffer(short _id, BufferRegion _dst, TextureRegion _src) {
			try {
				downcallHandle(DC_ENCODER_BLIT_TO_BUFFER).invokeExact(segment(), _id, address(_dst), address(_src));
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}

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
		public final void blitFromBuffer(short _id, TextureRegion _dst, BufferRegion _src) {
			try {
				downcallHandle(DC_ENCODER_BLIT_FROM_BUFFER).invokeExact(segment(), _id, address(_dst), address(_src));
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record DynamicIndexBufferHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_dynamic_index_buffer_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final DynamicIndexBufferHandle INVALID = new DynamicIndexBufferHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static DynamicIndexBufferHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new DynamicIndexBufferHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record DynamicVertexBufferHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_dynamic_vertex_buffer_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final DynamicVertexBufferHandle INVALID = new DynamicVertexBufferHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static DynamicVertexBufferHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new DynamicVertexBufferHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record FrameBufferHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_frame_buffer_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final FrameBufferHandle INVALID = new FrameBufferHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static FrameBufferHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new FrameBufferHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record IndexBufferHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_index_buffer_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final IndexBufferHandle INVALID = new IndexBufferHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static IndexBufferHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new IndexBufferHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record IndirectBufferHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_indirect_buffer_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final IndirectBufferHandle INVALID = new IndirectBufferHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static IndirectBufferHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new IndirectBufferHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record OcclusionQueryHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_occlusion_query_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final OcclusionQueryHandle INVALID = new OcclusionQueryHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static OcclusionQueryHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new OcclusionQueryHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record ProgramHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_program_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final ProgramHandle INVALID = new ProgramHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static ProgramHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new ProgramHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record ShaderHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_shader_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final ShaderHandle INVALID = new ShaderHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static ShaderHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new ShaderHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record TextureHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_texture_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final TextureHandle INVALID = new TextureHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static TextureHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new TextureHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record UniformHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_uniform_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final UniformHandle INVALID = new UniformHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static UniformHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new UniformHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record VertexBufferHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_vertex_buffer_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final VertexBufferHandle INVALID = new VertexBufferHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static VertexBufferHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new VertexBufferHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Native bgfx handle.
	 * @param idx native handle index
	 */
	public record VertexLayoutHandle(short idx) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_vertex_layout_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final VertexLayoutHandle INVALID = new VertexLayoutHandle((short) 0xffff);

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static VertexLayoutHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new VertexLayoutHandle((short) VH_IDX.get(segment, 0L));
		}
	}

	/**
	 * Tagged buffer handle. All buffer handle types implicitly convert to it, and the tag
	 * keeps track of which type the handle originally was.
	 * @param idx native handle index
	 * @param type native buffer handle tag
	 */
	public record BufferHandle(short idx, short type) {
		/**
		 * Native by-value handle layout.
		 */
		public static final StructLayout LAYOUT = cStruct("bgfx_buffer_handle_t",
			ValueLayout.JAVA_SHORT.withName("idx"),
			ValueLayout.JAVA_SHORT.withName("type"));
		private static final VarHandle VH_IDX = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("idx"));
		private static final VarHandle VH_TYPE = LAYOUT.varHandle(
			MemoryLayout.PathElement.groupElement("type"));
		/**
		 * Invalid handle sentinel.
		 */
		public static final BufferHandle INVALID =
			new BufferHandle((short) 0xffff, (short) 0xffff);

		/**
		 * Creates a tagged buffer handle.
		 * @param handle the source DynamicIndexBufferHandle
		 */
		public BufferHandle(DynamicIndexBufferHandle handle) {
			this(handle.idx(), (short) 0);
		}

		/**
		 * Creates a tagged buffer handle.
		 * @param handle the source DynamicVertexBufferHandle
		 */
		public BufferHandle(DynamicVertexBufferHandle handle) {
			this(handle.idx(), (short) 1);
		}

		/**
		 * Creates a tagged buffer handle.
		 * @param handle the source IndexBufferHandle
		 */
		public BufferHandle(IndexBufferHandle handle) {
			this(handle.idx(), (short) 2);
		}

		/**
		 * Creates a tagged buffer handle.
		 * @param handle the source IndirectBufferHandle
		 */
		public BufferHandle(IndirectBufferHandle handle) {
			this(handle.idx(), (short) 3);
		}

		/**
		 * Creates a tagged buffer handle.
		 * @param handle the source VertexBufferHandle
		 */
		public BufferHandle(VertexBufferHandle handle) {
			this(handle.idx(), (short) 4);
		}

		/**
		 * Returns whether this handle is valid.
		 * @return {@code true} when the handle index is not {@code UINT16_MAX}
		 */
		public boolean isValid() {
			return idx != (short) 0xffff;
		}

		/**
		 * Allocates and writes the native by-value handle representation.
		 * @param allocator the destination allocator
		 * @return the allocated native segment
		 */
		public MemorySegment allocate(SegmentAllocator allocator) {
			MemorySegment segment = allocator.allocate(LAYOUT);
			write(segment);
			return segment;
		}

		/**
		 * Writes this handle to an existing native segment.
		 * @param segment the destination segment
		 */
		public void write(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			VH_IDX.set(segment, 0L, idx);
			VH_TYPE.set(segment, 0L, type);
		}

		/**
		 * Reads a by-value handle from native memory.
		 * @param segment the source segment
		 * @return the decoded handle
		 */
		public static BufferHandle read(MemorySegment segment) {
			segment = view(segment, LAYOUT);
			return new BufferHandle(
				(short) VH_IDX.get(segment, 0L),
				(short) VH_TYPE.get(segment, 0L));
		}
	}


	// -------------------------------------------------------------------------
	// Generated native entry points. Descriptors and methods intentionally live
	// in this section rather than in per-function holder classes.
	// -------------------------------------------------------------------------
	private static final int DC_TEXTURE_REGION_INIT = 0;
	private static final FunctionDescriptor FD_TEXTURE_REGION_INIT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT);

	private static final int DC_BUFFER_REGION_INIT_TEXTURE = 1;
	private static final FunctionDescriptor FD_BUFFER_REGION_INIT_TEXTURE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS);

	private static final int DC_BUFFER_REGION_INIT_BUFFER = 2;
	private static final FunctionDescriptor FD_BUFFER_REGION_INIT_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, BufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ATTACHMENT_INIT = 3;
	private static final FunctionDescriptor FD_ATTACHMENT_INIT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, TextureHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE);

	private static final int DC_VERTEX_LAYOUT_BEGIN = 4;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_BEGIN = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

	private static final int DC_VERTEX_LAYOUT_ADD = 5;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_ADD = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_BOOLEAN);

	private static final int DC_VERTEX_LAYOUT_DECODE = 6;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_DECODE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);

	private static final int DC_VERTEX_LAYOUT_HAS = 7;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_HAS = FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

	private static final int DC_VERTEX_LAYOUT_SKIP = 8;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_SKIP = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE);

	private static final int DC_VERTEX_LAYOUT_END = 9;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_END = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);

	private static final int DC_VERTEX_LAYOUT_GET_OFFSET = 10;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_GET_OFFSET = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

	private static final int DC_VERTEX_LAYOUT_GET_STRIDE = 11;
	private static final FunctionDescriptor FD_VERTEX_LAYOUT_GET_STRIDE = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS);

	private static final int DC_VERTEX_LAYOUT_GET_SIZE = 12;
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

	private static final int DC_VERTEX_PACK = 13;
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

	private static final int DC_VERTEX_UNPACK = 14;
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

	private static final int DC_VERTEX_CONVERT = 15;
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

	private static final int DC_TOPOLOGY_CONVERT = 16;
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

	private static final int DC_TOPOLOGY_SORT_TRI_LIST = 17;
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

	private static final int DC_GET_SUPPORTED_RENDERERS = 18;
	private static final FunctionDescriptor FD_GET_SUPPORTED_RENDERERS = FunctionDescriptor.of(ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS);

	/**
	 * Returns name of renderer.
	 * @param _type Renderer backend type. See: {@code BGFX.RendererType}
	 * @return Name of renderer.
	 */
	public static final String getRendererName(RendererType _type) {
		try {
			return readString((MemorySegment) downcallHandle(DC_GET_RENDERER_NAME).invokeExact(_type.ordinal()));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_GET_RENDERER_NAME = 19;
	private static final FunctionDescriptor FD_GET_RENDERER_NAME = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

	/**
	 * Fill BGFX.Init struct with default values, before using it to initialize the library.
	 * @param _init Pointer to structure to be initialized. See: {@code BGFX.Init} for more info.
	 */
	public static final void initCtor(Init _init) {
		try {
			downcallHandle(DC_INIT_CTOR).invokeExact(address(_init));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_INIT_CTOR = 20;
	private static final FunctionDescriptor FD_INIT_CTOR = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);

	/**
	 * Initialize the bgfx library.
	 * @param _init Initialization parameters. See: {@code BGFX.Init} for more info.
	 * @return {@code true} if initialization was successful.
	 */
	public static final boolean init(Init _init) {
		try {
			return (boolean) downcallHandle(DC_INIT).invokeExact(address(_init));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_INIT = 21;
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

	private static final int DC_SHUTDOWN = 22;
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

	private static final int DC_RESET = 23;
	private static final FunctionDescriptor FD_RESET = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	/**
	 * Advance to next frame. This is the main frame-advancement call on the
	 * API thread (the thread from which {@code BGFX.init} was called).
	 * <p>
	 * **Multithreaded renderer** ({@code BGFX_CONFIG_MULTITHREADED=1}, default):
	 * This call waits for the render thread to finish processing the previous
	 * frame, then swaps internal submit/render buffers, signals the render
	 * thread to begin processing the new frame via {@code BGFX.renderFrame}, and
	 * returns immediately. The render thread and API thread then run in
	 * parallel: the API thread builds the next frame while the render thread
	 * executes GPU commands for the current frame.
	 * <p>
	 * **Single-threaded renderer** ({@code BGFX_CONFIG_MULTITHREADED=0}, or when
	 * {@code BGFX.renderFrame} and {@code BGFX.init} are called from the same thread):
	 * This call swaps internal buffers and performs frame rendering inline
	 * (internally calls {@code BGFX.renderFrame}), then returns.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Must be called from the API thread (the thread that called
	 *   {@code BGFX.init}). In multithreaded mode, this call synchronizes with
	 *   {@code BGFX.renderFrame} running on the render thread via semaphores:
	 *   {@code BGFX.frame} waits for the render thread to finish, then posts a
	 *   signal that {@code BGFX.renderFrame} waits on to begin the next frame.
	 *   See also: {@code BGFX.renderFrame}.
	 * @param _flags Frame flags. See: {@code BGFX_FRAME_*} for more info.   - {@code BGFX_FRAME_NONE} - No frame flag.   - {@code BGFX_FRAME_DEBUG_CAPTURE} - Capture frame with graphics debugger.   - {@code BGFX_FRAME_DISCARD} - Discard all draw calls.   - {@code BGFX_FRAME_FLUSH} - Execute all rendering commands     without presenting the backbuffer.
	 * @return Current frame number. This might be used in conjunction with double/multi buffering data outside the library and passing it to library via {@code BGFX.makeRef} calls.
	 */
	public static final int frame(byte _flags) {
		try {
			return (int) downcallHandle(DC_FRAME).invokeExact(_flags);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_FRAME = 24;
	private static final FunctionDescriptor FD_FRAME = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);

	/**
	 * Returns current renderer backend API type.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Library must be initialized.
	 * @return Renderer backend type. See: {@code BGFX.RendererType}
	 */
	public static final RendererType getRendererType() {
		try {
			return RendererType.fromValue((int) downcallHandle(DC_GET_RENDERER_TYPE).invokeExact());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_GET_RENDERER_TYPE = 25;
	private static final FunctionDescriptor FD_GET_RENDERER_TYPE = FunctionDescriptor.of(ValueLayout.JAVA_INT);

	/**
	 * Returns renderer capabilities.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Library must be initialized.
	 * @return Pointer to static {@code BGFX.Caps} structure.
	 */
	public static final Caps getCaps() {
		try {
			return new Caps((MemorySegment) downcallHandle(DC_GET_CAPS).invokeExact());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_GET_CAPS = 26;
	private static final FunctionDescriptor FD_GET_CAPS = FunctionDescriptor.of(ValueLayout.ADDRESS);

	/**
	 * Returns performance counters.
	 * <p>
	 * <strong>Attention:</strong> Pointer returned is valid until {@code BGFX.frame} is called.
	 * @return Performance counters.
	 */
	public static final Stats getStats() {
		try {
			return new Stats((MemorySegment) downcallHandle(DC_GET_STATS).invokeExact());
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_GET_STATS = 27;
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

	private static final int DC_ALLOC = 28;
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

	private static final int DC_COPY = 29;
	private static final FunctionDescriptor FD_COPY = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

	/**
	 * Make reference to data to pass to bgfx. Unlike {@code BGFX.alloc}, this call
	 * doesn't allocate memory for data. It just copies the _data pointer. You
	 * can pass {@code ReleaseFn} function pointer to release this memory after it's
	 * consumed, otherwise you must make sure _data is available for at least 2
	 * {@code BGFX.frame} calls. {@code ReleaseFn} function must be able to be called
	 * from any thread.
	 * <p>
	 * <strong>Attention:</strong> Data passed must be available for at least 2 {@code BGFX.frame} calls.
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

	private static final int DC_MAKE_REF = 30;
	private static final FunctionDescriptor FD_MAKE_REF = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

	/**
	 * Make reference to data to pass to bgfx. Unlike {@code BGFX.alloc}, this call
	 * doesn't allocate memory for data. It just copies the _data pointer. You
	 * can pass {@code ReleaseFn} function pointer to release this memory after it's
	 * consumed, otherwise you must make sure _data is available for at least 2
	 * {@code BGFX.frame} calls. {@code ReleaseFn} function must be able to be called
	 * from any thread.
	 * <p>
	 * <strong>Attention:</strong> Data passed must be available for at least 2 {@code BGFX.frame} calls.
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

	private static final int DC_MAKE_REF_RELEASE = 31;
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

	private static final int DC_SET_DEBUG = 32;
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

	private static final int DC_DBG_TEXT_CLEAR = 33;
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
			invoke(handle, nativeArgs);
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

	private static final int DC_DBG_TEXT_VPRINTF = 34;
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

	private static final int DC_DBG_TEXT_IMAGE = 35;
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

	private static final int DC_CREATE_INDEX_BUFFER = 36;
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
	 * @return Frame number when the result will be available. See: {@code BGFX.frame}.
	 */
	public static final int readBuffer(BufferRegion _src, MemorySegment _data) {
		try {
			return (int) downcallHandle(DC_READ_BUFFER).invokeExact(address(_src), address(_data));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_READ_BUFFER = 37;
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

	private static final int DC_SET_INDEX_BUFFER_NAME = 38;
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

	private static final int DC_DESTROY_INDEX_BUFFER = 39;
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

	private static final int DC_CREATE_VERTEX_LAYOUT = 40;
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

	private static final int DC_DESTROY_VERTEX_LAYOUT = 41;
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

	private static final int DC_CREATE_VERTEX_BUFFER = 42;
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

	private static final int DC_SET_VERTEX_BUFFER_NAME = 43;
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

	private static final int DC_DESTROY_VERTEX_BUFFER = 44;
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

	private static final int DC_CREATE_DYNAMIC_INDEX_BUFFER = 45;
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

	private static final int DC_CREATE_DYNAMIC_INDEX_BUFFER_MEM = 46;
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

	private static final int DC_UPDATE_DYNAMIC_INDEX_BUFFER = 47;
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

	private static final int DC_DESTROY_DYNAMIC_INDEX_BUFFER = 48;
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

	private static final int DC_CREATE_DYNAMIC_VERTEX_BUFFER = 49;
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

	private static final int DC_CREATE_DYNAMIC_VERTEX_BUFFER_MEM = 50;
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

	private static final int DC_UPDATE_DYNAMIC_VERTEX_BUFFER = 51;
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

	private static final int DC_DESTROY_DYNAMIC_VERTEX_BUFFER = 52;
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

	private static final int DC_GET_AVAIL_TRANSIENT_INDEX_BUFFER = 53;
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

	private static final int DC_GET_AVAIL_TRANSIENT_VERTEX_BUFFER = 54;
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

	private static final int DC_GET_AVAIL_INSTANCE_DATA_BUFFER = 55;
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

	private static final int DC_ALLOC_TRANSIENT_INDEX_BUFFER = 56;
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

	private static final int DC_ALLOC_TRANSIENT_VERTEX_BUFFER = 57;
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

	private static final int DC_ALLOC_TRANSIENT_BUFFERS = 58;
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

	private static final int DC_ALLOC_INSTANCE_DATA_BUFFER = 59;
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

	private static final int DC_CREATE_INDIRECT_BUFFER = 60;
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

	private static final int DC_DESTROY_INDIRECT_BUFFER = 61;
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

	private static final int DC_CREATE_SHADER = 62;
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

	private static final int DC_GET_SHADER_UNIFORMS = 63;
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

	private static final int DC_SET_SHADER_NAME = 64;
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

	private static final int DC_DESTROY_SHADER = 65;
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

	private static final int DC_CREATE_PROGRAM = 66;
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

	private static final int DC_CREATE_COMPUTE_PROGRAM = 67;
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

	private static final int DC_DESTROY_PROGRAM = 68;
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

	private static final int DC_IS_TEXTURE_VALID = 69;
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

	private static final int DC_IS_VIDEO_CODEC_VALID = 70;
	private static final FunctionDescriptor FD_IS_VIDEO_CODEC_VALID = FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE);

	/**
	 * Validate frame buffer parameters.
	 * @param _num Number of attachments.
	 * @param _attachment Attachment texture info. See: {@code BGFX.Attachment}.
	 * @return True if a frame buffer with the same parameters can be created.
	 */
	public static final boolean isFrameBufferValid(byte _num, Attachment _attachment) {
		try {
			return (boolean) downcallHandle(DC_IS_FRAME_BUFFER_VALID).invokeExact(_num, address(_attachment));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_IS_FRAME_BUFFER_VALID = 71;
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

	private static final int DC_CALC_TEXTURE_SIZE = 72;
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

	private static final int DC_CREATE_TEXTURE = 73;
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

	private static final int DC_CREATE_TEXTURE_2D = 74;
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

	private static final int DC_CREATE_TEXTURE_2D_SCALED = 75;
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

	private static final int DC_CREATE_TEXTURE_3D = 76;
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

	private static final int DC_CREATE_TEXTURE_CUBE = 77;
	private static final FunctionDescriptor FD_CREATE_TEXTURE_CUBE = FunctionDescriptor.of(TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BOOLEAN, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG);

	/**
	 * Update 2D texture.
	 * <p>
	 * <strong>Attention:</strong> It's valid to update only mutable texture. See {@code BGFX.createTexture2D} for more info.
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

	private static final int DC_UPDATE_TEXTURE_2D = 78;
	private static final FunctionDescriptor FD_UPDATE_TEXTURE_2D = FunctionDescriptor.ofVoid(TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);

	/**
	 * Update 3D texture.
	 * <p>
	 * <strong>Attention:</strong> It's valid to update only mutable texture. See {@code BGFX.createTexture3D} for more info.
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

	private static final int DC_UPDATE_TEXTURE_3D = 79;
	private static final FunctionDescriptor FD_UPDATE_TEXTURE_3D = FunctionDescriptor.ofVoid(TextureHandle.LAYOUT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS);

	/**
	 * Update Cube texture.
	 * <p>
	 * <strong>Attention:</strong> It's valid to update only mutable texture. See {@code BGFX.createTextureCube} for more info.
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

	private static final int DC_UPDATE_TEXTURE_CUBE = 80;
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

	private static final int DC_CLEAR_TEXTURE = 81;
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
	 * @return Frame number when the result will be available. See: {@code BGFX.frame}.
	 */
	public static final int readTexture(TextureRegion _src, MemorySegment _data) {
		try {
			return (int) downcallHandle(DC_READ_TEXTURE).invokeExact(address(_src), address(_data));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_READ_TEXTURE = 82;
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

	private static final int DC_SET_TEXTURE_NAME = 83;
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

	private static final int DC_GET_DIRECT_ACCESS_PTR = 84;
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

	private static final int DC_DESTROY_TEXTURE = 85;
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

	private static final int DC_CREATE_FRAME_BUFFER = 86;
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

	private static final int DC_CREATE_FRAME_BUFFER_SCALED = 87;
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

	private static final int DC_CREATE_FRAME_BUFFER_FROM_HANDLES = 88;
	private static final FunctionDescriptor FD_CREATE_FRAME_BUFFER_FROM_HANDLES = FunctionDescriptor.of(FrameBufferHandle.LAYOUT, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN);

	/**
	 * Create MRT frame buffer from texture handles with specific layer and
	 * mip level.
	 * @param _num Number of attachments.
	 * @param _attachment Attachment texture info. See: {@code BGFX.Attachment}.
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

	private static final int DC_CREATE_FRAME_BUFFER_FROM_ATTACHMENT = 89;
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

	private static final int DC_CREATE_FRAME_BUFFER_FROM_NWH = 90;
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

	private static final int DC_SET_FRAME_BUFFER_NAME = 91;
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

	private static final int DC_GET_TEXTURE = 92;
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

	private static final int DC_DESTROY_FRAME_BUFFER = 93;
	private static final FunctionDescriptor FD_DESTROY_FRAME_BUFFER = FunctionDescriptor.ofVoid(FrameBufferHandle.LAYOUT);

	/**
	 * Create shader uniform parameter.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   1. Uniform names are unique. It's valid to call {@code BGFX.createUniform}
	 *      multiple times with the same uniform name. The library will always
	 *      return the same handle, but the handle reference count will be
	 *      incremented. This means that the same number of {@code BGFX.destroyUniform}
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
	 * @param _type Type of uniform (See: {@code BGFX.UniformType}).
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

	private static final int DC_CREATE_UNIFORM = 94;
	private static final FunctionDescriptor FD_CREATE_UNIFORM = FunctionDescriptor.of(UniformHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT);

	/**
	 * Create shader uniform parameter.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   1. Uniform names are unique. It's valid to call {@code BGFX.createUniform}
	 *      multiple times with the same uniform name. The library will always
	 *      return the same handle, but the handle reference count will be
	 *      incremented. This means that the same number of {@code BGFX.destroyUniform}
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
	 * @param _freq Uniform change frequency (See: {@code BGFX.UniformFreq}).
	 * @param _type Type of uniform (See: {@code BGFX.UniformType}).
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

	private static final int DC_CREATE_UNIFORM_WITH_FREQ = 95;
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

	private static final int DC_GET_UNIFORM_INFO = 96;
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

	private static final int DC_DESTROY_UNIFORM = 97;
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

	private static final int DC_CREATE_OCCLUSION_QUERY = 98;
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

	private static final int DC_GET_RESULT = 99;
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

	private static final int DC_DESTROY_OCCLUSION_QUERY = 100;
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

	private static final int DC_SET_PALETTE_COLOR = 101;
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

	private static final int DC_SET_PALETTE_COLOR_RGBA32F = 102;
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

	private static final int DC_SET_PALETTE_COLOR_RGBA8 = 103;
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

	private static final int DC_SET_VIEW_NAME = 104;
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

	private static final int DC_SET_VIEW_RECT = 105;
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

	private static final int DC_SET_VIEW_RECT_RATIO = 106;
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

	private static final int DC_SET_VIEW_SCISSOR = 107;
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

	private static final int DC_SET_VIEW_CLEAR = 108;
	private static final FunctionDescriptor FD_SET_VIEW_CLEAR = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_BYTE);

	/**
	 * Set view clear flags with different clear color for each
	 * frame buffer texture. {@code BGFX.setPaletteColor} must be used to set up a
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

	private static final int DC_SET_VIEW_CLEAR_MRT = 109;
	private static final FunctionDescriptor FD_SET_VIEW_CLEAR_MRT = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE);

	/**
	 * Set view sorting mode.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   View mode must be set prior calling {@code BGFX.submit} for the view.
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

	private static final int DC_SET_VIEW_MODE = 110;
	private static final FunctionDescriptor FD_SET_VIEW_MODE = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_INT);

	/**
	 * Set view frame buffer.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Not persistent after {@code BGFX.reset} call.
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

	private static final int DC_SET_VIEW_FRAME_BUFFER = 111;
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

	private static final int DC_SET_VIEW_TRANSFORM = 112;
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

	private static final int DC_SET_VIEW_ORDER = 113;
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

	private static final int DC_SET_VIEW_SHADING_RATE = 114;
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

	private static final int DC_RESET_VIEW = 115;
	private static final FunctionDescriptor FD_RESET_VIEW = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT);

	/**
	 * Begin submitting draw calls from thread. Obtains an encoder that can be
	 * used to submit draw calls, compute dispatches, and state changes.
	 * <p>
	 * In multithreaded mode ({@code BGFX_CONFIG_MULTITHREADED=1}), multiple threads
	 * can each obtain their own encoder and submit draw calls in parallel.
	 * Each encoder writes into its own uniform buffer, so there is no
	 * contention between threads. The maximum number of simultaneous encoders
	 * is configured via {@code Limits.maxEncoders} in {@code BGFX.Init} (default: 8).
	 * <p>
	 * When called from the API thread (the thread that called {@code BGFX.init})
	 * with {@code _forceNewEncoder} set to {@code false}, the default internal encoder
	 * (encoder 0) is returned. This is the same encoder used by the legacy
	 * non-encoder API ({@code BGFX.setState}, {@code BGFX.submit}, etc.). When called
	 * from a worker thread (or with {@code _forceNewEncoder} set to {@code true}), a new
	 * encoder is allocated from the encoder pool.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   The returned {@code Encoder} pointer is valid until {@code BGFX.end} is called
	 *   with it. All encoders must be ended before {@code BGFX.frame} is called.
	 *   If {@code BGFX.frame} is called while encoders are still active, it will
	 *   wait for them to finish. Returns {@code NULL} if no encoder slots are
	 *   available (all {@code maxEncoders} slots are in use).
	 *   See also: {@code BGFX.end}, {@code BGFX.frame}.
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

	private static final int DC_ENCODER_BEGIN = 116;
	private static final FunctionDescriptor FD_ENCODER_BEGIN = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN);

	/**
	 * End submitting draw calls from thread. Returns the encoder obtained from
	 * {@code BGFX.begin} back to the encoder pool.
	 * <p>
	 * After this call the {@code Encoder} pointer is no longer valid and must not
	 * be used. The encoder's recorded draw calls and state changes are finalized
	 * and will be included in the next frame when {@code BGFX.frame} is called.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   Must be called from the same thread that called {@code BGFX.begin} for
	 *   this encoder. All encoders must be ended before {@code BGFX.frame} is
	 *   called. The default encoder (encoder 0, used by the legacy API) is
	 *   managed internally and does not need to be passed to {@code BGFX.end};
	 *   passing it is harmless but has no effect.
	 *   See also: {@code BGFX.begin}, {@code BGFX.frame}.
	 * @param _encoder Encoder.
	 */
	public static final void encoderEnd(Encoder _encoder) {
		try {
			downcallHandle(DC_ENCODER_END).invokeExact(address(_encoder));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_ENCODER_END = 117;
	private static final FunctionDescriptor FD_ENCODER_END = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);

	private static final int DC_ENCODER_SET_MARKER = 118;
	private static final FunctionDescriptor FD_ENCODER_SET_MARKER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_STATE = 119;
	private static final FunctionDescriptor FD_ENCODER_SET_STATE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_CONDITION = 120;
	private static final FunctionDescriptor FD_ENCODER_SET_CONDITION = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, OcclusionQueryHandle.LAYOUT, ValueLayout.JAVA_BOOLEAN);

	private static final int DC_ENCODER_SET_STENCIL = 121;
	private static final FunctionDescriptor FD_ENCODER_SET_STENCIL = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_SCISSOR = 122;
	private static final FunctionDescriptor FD_ENCODER_SET_SCISSOR = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT);

	private static final int DC_ENCODER_SET_SCISSOR_CACHED = 123;
	private static final FunctionDescriptor FD_ENCODER_SET_SCISSOR_CACHED = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);

	private static final int DC_ENCODER_SET_TRANSFORM = 124;
	private static final FunctionDescriptor FD_ENCODER_SET_TRANSFORM = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);

	private static final int DC_ENCODER_SET_TRANSFORM_CACHED = 125;
	private static final FunctionDescriptor FD_ENCODER_SET_TRANSFORM_CACHED = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT);

	private static final int DC_ENCODER_ALLOC_TRANSFORM = 126;
	private static final FunctionDescriptor FD_ENCODER_ALLOC_TRANSFORM = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);

	private static final int DC_ENCODER_SET_UNIFORM = 127;
	private static final FunctionDescriptor FD_ENCODER_SET_UNIFORM = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, UniformHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);

	/**
	 * Set shader uniform parameter for view.
	 * <p>
	 * <strong>Attention:</strong> Uniform must be created with {@code BGFX.UniformFreq.View} argument.
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

	private static final int DC_SET_VIEW_UNIFORM = 128;
	private static final FunctionDescriptor FD_SET_VIEW_UNIFORM = FunctionDescriptor.ofVoid(ValueLayout.JAVA_SHORT, UniformHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);

	/**
	 * Set shader uniform parameter for frame.
	 * <p>
	 * <strong>Attention:</strong> Uniform must be created with {@code BGFX.UniformFreq.View} argument.
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

	private static final int DC_SET_FRAME_UNIFORM = 129;
	private static final FunctionDescriptor FD_SET_FRAME_UNIFORM = FunctionDescriptor.ofVoid(UniformHandle.LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);

	private static final int DC_ENCODER_SET_INDEX_BUFFER = 130;
	private static final FunctionDescriptor FD_ENCODER_SET_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, IndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_DYNAMIC_INDEX_BUFFER = 131;
	private static final FunctionDescriptor FD_ENCODER_SET_DYNAMIC_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, DynamicIndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_TRANSIENT_INDEX_BUFFER = 132;
	private static final FunctionDescriptor FD_ENCODER_SET_TRANSIENT_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_VERTEX_BUFFER = 133;
	private static final FunctionDescriptor FD_ENCODER_SET_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_VERTEX_BUFFER_WITH_LAYOUT = 134;
	private static final FunctionDescriptor FD_ENCODER_SET_VERTEX_BUFFER_WITH_LAYOUT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, VertexLayoutHandle.LAYOUT);

	private static final int DC_ENCODER_SET_DYNAMIC_VERTEX_BUFFER = 135;
	private static final FunctionDescriptor FD_ENCODER_SET_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT = 136;
	private static final FunctionDescriptor FD_ENCODER_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, VertexLayoutHandle.LAYOUT);

	private static final int DC_ENCODER_SET_TRANSIENT_VERTEX_BUFFER = 137;
	private static final FunctionDescriptor FD_ENCODER_SET_TRANSIENT_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT = 138;
	private static final FunctionDescriptor FD_ENCODER_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, VertexLayoutHandle.LAYOUT);

	private static final int DC_ENCODER_SET_VERTEX_COUNT = 139;
	private static final FunctionDescriptor FD_ENCODER_SET_VERTEX_COUNT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_INSTANCE_DATA_BUFFER = 140;
	private static final FunctionDescriptor FD_ENCODER_SET_INSTANCE_DATA_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER = 141;
	private static final FunctionDescriptor FD_ENCODER_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER = 142;
	private static final FunctionDescriptor FD_ENCODER_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_INSTANCE_COUNT = 143;
	private static final FunctionDescriptor FD_ENCODER_SET_INSTANCE_COUNT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_TEXTURE = 144;
	private static final FunctionDescriptor FD_ENCODER_SET_TEXTURE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, UniformHandle.LAYOUT, TextureHandle.LAYOUT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_TEXTURE_VIEW = 145;
	private static final FunctionDescriptor FD_ENCODER_SET_TEXTURE_VIEW = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, UniformHandle.LAYOUT, TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_TOUCH = 146;
	private static final FunctionDescriptor FD_ENCODER_TOUCH = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT);

	private static final int DC_ENCODER_SUBMIT = 147;
	private static final FunctionDescriptor FD_ENCODER_SUBMIT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);

	private static final int DC_ENCODER_SUBMIT_OCCLUSION_QUERY = 148;
	private static final FunctionDescriptor FD_ENCODER_SUBMIT_OCCLUSION_QUERY = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, OcclusionQueryHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);

	private static final int DC_ENCODER_SUBMIT_INDIRECT = 149;
	private static final FunctionDescriptor FD_ENCODER_SUBMIT_INDIRECT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);

	private static final int DC_ENCODER_SUBMIT_INDIRECT_COUNT = 150;
	private static final FunctionDescriptor FD_ENCODER_SUBMIT_INDIRECT_COUNT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT, IndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);

	private static final int DC_ENCODER_SET_COMPUTE_INDEX_BUFFER = 151;
	private static final FunctionDescriptor FD_ENCODER_SET_COMPUTE_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, IndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_COMPUTE_VERTEX_BUFFER = 152;
	private static final FunctionDescriptor FD_ENCODER_SET_COMPUTE_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, VertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_COMPUTE_DYNAMIC_INDEX_BUFFER = 153;
	private static final FunctionDescriptor FD_ENCODER_SET_COMPUTE_DYNAMIC_INDEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, DynamicIndexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER = 154;
	private static final FunctionDescriptor FD_ENCODER_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, DynamicVertexBufferHandle.LAYOUT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_COMPUTE_INDIRECT_BUFFER = 155;
	private static final FunctionDescriptor FD_ENCODER_SET_COMPUTE_INDIRECT_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_IMAGE = 156;
	private static final FunctionDescriptor FD_ENCODER_SET_IMAGE = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, TextureHandle.LAYOUT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_SET_IMAGE_VIEW = 157;
	private static final FunctionDescriptor FD_ENCODER_SET_IMAGE_VIEW = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, TextureHandle.LAYOUT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	private static final int DC_ENCODER_DISPATCH = 158;
	private static final FunctionDescriptor FD_ENCODER_DISPATCH = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);

	private static final int DC_ENCODER_DISPATCH_INDIRECT = 159;
	private static final FunctionDescriptor FD_ENCODER_DISPATCH_INDIRECT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ProgramHandle.LAYOUT, IndirectBufferHandle.LAYOUT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_BYTE);

	private static final int DC_ENCODER_DISCARD = 160;
	private static final FunctionDescriptor FD_ENCODER_DISCARD = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE);

	private static final int DC_ENCODER_BLIT = 161;
	private static final FunctionDescriptor FD_ENCODER_BLIT = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);

	private static final int DC_ENCODER_BLIT_BUFFER = 162;
	private static final FunctionDescriptor FD_ENCODER_BLIT_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);

	private static final int DC_ENCODER_BLIT_TO_BUFFER = 163;
	private static final FunctionDescriptor FD_ENCODER_BLIT_TO_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);

	private static final int DC_ENCODER_BLIT_FROM_BUFFER = 164;
	private static final FunctionDescriptor FD_ENCODER_BLIT_FROM_BUFFER = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_SHORT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);

	/**
	 * Request screen shot of window back buffer.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   {@code BGFX.CallbackI.screenShot} must be implemented.
	 * <strong>Attention:</strong> Frame buffer handle must be created with OS' target native window handle.
	 * @param _handle Frame buffer handle. If handle is {@code BGFX_INVALID_HANDLE} request will be made for main window back buffer.
	 * @param _filePath Will be passed to {@code BGFX.CallbackI.screenShot} callback.
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

	private static final int DC_REQUEST_SCREEN_SHOT = 165;
	private static final FunctionDescriptor FD_REQUEST_SCREEN_SHOT = FunctionDescriptor.ofVoid(FrameBufferHandle.LAYOUT, ValueLayout.ADDRESS);

	/**
	 * Render frame. Executes the actual GPU rendering work for one frame.
	 * <p>
	 * In the default **multithreaded** configuration, {@code BGFX.renderFrame} runs
	 * on the **render thread** while {@code BGFX.frame} runs on the **API thread**.
	 * Their interaction is as follows:
	 * <p>
	 *   1. The render thread calls {@code BGFX.renderFrame}, which blocks waiting
	 *      for the API thread to signal that a new frame is ready.
	 *   2. On the API thread, {@code BGFX.frame} finishes building the frame,
	 *      swaps internal submit/render buffers, and signals the render thread.
	 *   3. {@code BGFX.renderFrame} wakes up, executes pre-render commands,
	 *      submits GPU draw calls, executes post-render commands, flips the
	 *      back buffer, then signals back to the API thread that rendering
	 *      is complete.
	 *   4. The API thread's next {@code BGFX.frame} call waits for this completion
	 *      signal before swapping buffers again.
	 * <p>
	 * This double-buffered semaphore handshake allows the API thread and
	 * render thread to run in parallel, overlapping CPU frame building with
	 * GPU rendering.
	 * <p>
	 * <strong>Attention:</strong> {@code BGFX.renderFrame} is a blocking call. It waits for
	 *   {@code BGFX.frame} to be called from the API thread to process the frame.
	 *   If a timeout value is passed, the call will return
	 *   {@code RenderFrame.Timeout} even if {@code BGFX.frame} has not been called.
	 *   A value of -1 (default) means wait indefinitely (up to
	 *   {@code BGFX_CONFIG_API_SEMAPHORE_TIMEOUT}).
	 * <p>
	 * <strong>Warning:</strong> This call should only be used on platforms that don't allow
	 *   creating a separate rendering thread. If it is called before
	 *   {@code BGFX.init}, the internal render thread won't be created by the
	 *   {@code BGFX.init} call, and the user is responsible for calling
	 *   {@code BGFX.renderFrame} on the render thread each frame. If both
	 *   {@code BGFX.renderFrame} and {@code BGFX.init} are called from the same
	 *   thread, bgfx operates in single-threaded mode and {@code BGFX.frame}
	 *   will internally invoke {@code BGFX.renderFrame} automatically.
	 *   See also: {@code BGFX.frame}.
	 * @param _msecs Timeout in milliseconds.
	 * @return Current renderer context state. See: {@code BGFX.RenderFrame}.
	 */
	public static final RenderFrame renderFrame(int _msecs) {
		try {
			return RenderFrame.fromValue((int) downcallHandle(DC_RENDER_FRAME).invokeExact(_msecs));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_RENDER_FRAME = 166;
	private static final FunctionDescriptor FD_RENDER_FRAME = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	/**
	 * Set platform data.
	 * <p>
	 * <strong>Warning:</strong> Must be called before {@code BGFX.init}.
	 * @param _data Platform data.
	 */
	public static final void setPlatformData(PlatformData _data) {
		try {
			downcallHandle(DC_SET_PLATFORM_DATA).invokeExact(address(_data));
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_SET_PLATFORM_DATA = 167;
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

	private static final int DC_GET_INTERNAL_DATA = 168;
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

	private static final int DC_OVERRIDE_INTERNAL_TEXTURE_PTR = 169;
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

	private static final int DC_OVERRIDE_INTERNAL_TEXTURE = 170;
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

	private static final int DC_SET_MARKER = 171;
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

	private static final int DC_SET_STATE = 172;
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

	private static final int DC_SET_CONDITION = 173;
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

	private static final int DC_SET_STENCIL = 174;
	private static final FunctionDescriptor FD_SET_STENCIL = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

	/**
	 * Set scissor for draw primitive.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   To scissor for all primitives in view see {@code BGFX.setViewScissor}.
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

	private static final int DC_SET_SCISSOR = 175;
	private static final FunctionDescriptor FD_SET_SCISSOR = FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT, ValueLayout.JAVA_SHORT);

	/**
	 * Set scissor from cache for draw primitive.
	 * <p>
	 * <strong>Remarks:</strong> 
	 *   To scissor for all primitives in view see {@code BGFX.setViewScissor}.
	 * @param _cache Index in scissor cache.
	 */
	public static final void setScissorCached(short _cache) {
		try {
			downcallHandle(DC_SET_SCISSOR_CACHED).invokeExact(_cache);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	private static final int DC_SET_SCISSOR_CACHED = 176;
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

	private static final int DC_SET_TRANSFORM = 177;
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

	private static final int DC_SET_TRANSFORM_CACHED = 178;
	private static final FunctionDescriptor FD_SET_TRANSFORM_CACHED = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_SHORT);

	/**
	 * Reserve matrices in internal matrix cache.
	 * <p>
	 * <strong>Attention:</strong> Pointer returned can be modified until {@code BGFX.frame} is called.
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

	private static final int DC_ALLOC_TRANSFORM = 179;
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

	private static final int DC_SET_UNIFORM = 180;
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

	private static final int DC_SET_INDEX_BUFFER = 181;
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

	private static final int DC_SET_DYNAMIC_INDEX_BUFFER = 182;
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

	private static final int DC_SET_TRANSIENT_INDEX_BUFFER = 183;
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

	private static final int DC_SET_VERTEX_BUFFER = 184;
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

	private static final int DC_SET_VERTEX_BUFFER_WITH_LAYOUT = 185;
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

	private static final int DC_SET_DYNAMIC_VERTEX_BUFFER = 186;
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

	private static final int DC_SET_DYNAMIC_VERTEX_BUFFER_WITH_LAYOUT = 187;
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

	private static final int DC_SET_TRANSIENT_VERTEX_BUFFER = 188;
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

	private static final int DC_SET_TRANSIENT_VERTEX_BUFFER_WITH_LAYOUT = 189;
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

	private static final int DC_SET_VERTEX_COUNT = 190;
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

	private static final int DC_SET_INSTANCE_DATA_BUFFER = 191;
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

	private static final int DC_SET_INSTANCE_DATA_FROM_VERTEX_BUFFER = 192;
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

	private static final int DC_SET_INSTANCE_DATA_FROM_DYNAMIC_VERTEX_BUFFER = 193;
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

	private static final int DC_SET_INSTANCE_COUNT = 194;
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

	private static final int DC_SET_TEXTURE = 195;
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

	private static final int DC_SET_TEXTURE_VIEW = 196;
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

	private static final int DC_TOUCH = 197;
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

	private static final int DC_SUBMIT = 198;
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

	private static final int DC_SUBMIT_OCCLUSION_QUERY = 199;
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

	private static final int DC_SUBMIT_INDIRECT = 200;
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

	private static final int DC_SUBMIT_INDIRECT_COUNT = 201;
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

	private static final int DC_SET_COMPUTE_INDEX_BUFFER = 202;
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

	private static final int DC_SET_COMPUTE_VERTEX_BUFFER = 203;
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

	private static final int DC_SET_COMPUTE_DYNAMIC_INDEX_BUFFER = 204;
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

	private static final int DC_SET_COMPUTE_DYNAMIC_VERTEX_BUFFER = 205;
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

	private static final int DC_SET_COMPUTE_INDIRECT_BUFFER = 206;
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

	private static final int DC_SET_IMAGE = 207;
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

	private static final int DC_SET_IMAGE_VIEW = 208;
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

	private static final int DC_DISPATCH = 209;
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

	private static final int DC_DISPATCH_INDIRECT = 210;
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

	private static final int DC_DISCARD = 211;
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

	private static final int DC_BLIT = 212;
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

	private static final int DC_BLIT_BUFFER = 213;
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

	private static final int DC_BLIT_TO_BUFFER = 214;
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

	private static final int DC_BLIT_FROM_BUFFER = 215;
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
