// Copyright 2011-2026 Branimir Karadzic. All rights reserved.
// License: https://github.com/bkaradzic/bgfx/blob/master/LICENSE

package io.github.bkaradzic.bgfx.util;

import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Shared Java FFM support used by the generated bgfx binding. */
@SuppressWarnings("restricted")
public final class FFMUtil {
	/** Native linker used for bgfx downcalls and callback upcalls. */
	public static final Linker LINKER = Linker.nativeLinker();

	/** Platform-native layout of C {@code uintptr_t}. */
	public static final ValueLayout C_UINTPTR_T =
		(ValueLayout) LINKER.canonicalLayouts().get("size_t");

	private FFMUtil() {
	}

	/**
	 * Resolves a virtual Java callback method for an upcall stub.
	 * @param owner callback interface
	 * @param name callback method name
	 * @param type callback method type
	 * @return the resolved callback target
	 */
	public static MethodHandle upcallTarget(Class<?> owner, String name, MethodType type) {
		try {
			return MethodHandles.lookup().findVirtual(owner, name, type);
		} catch (NoSuchMethodException | IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	/**
	 * Invokes a native handle with dynamically supplied arguments.
	 * @param handle native method handle
	 * @param args native arguments
	 * @return the native result, or {@code null} for {@code void}
	 */
	public static Object invoke(MethodHandle handle, Object... args) {
		try {
			return handle.invokeWithArguments(args);
		} catch (Throwable ex) {
			throw invocationFailure(ex);
		}
	}

	/**
	 * Converts an unexpected method-handle failure to an unchecked exception.
	 * @param exception invocation failure
	 * @return the unchecked failure
	 */
	public static RuntimeException invocationFailure(Throwable exception) {
		if (exception instanceof RuntimeException runtime) {
			return runtime;
		}
		if (exception instanceof Error error) {
			throw error;
		}
		throw new AssertionError("Unexpected native invocation failure", exception);
	}

	/**
	 * Invokes a layout slice handle at offset zero.
	 * @param handle layout slice handle
	 * @param segment containing segment
	 * @return the selected member segment
	 */
	public static MemorySegment slice(MethodHandle handle, MemorySegment segment) {
		try {
			return (MemorySegment) handle.invokeExact(segment, 0L);
		} catch (RuntimeException | Error ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new AssertionError("Unexpected layout slice failure", ex);
		}
	}

	/**
	 * Creates a C-compatible structure layout with explicit ABI padding.
	 * @param name native structure name
	 * @param members structure members in declaration order
	 * @return the padded structure layout
	 */
	public static StructLayout cStruct(String name, MemoryLayout... members) {
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

	/**
	 * Returns a segment view sized for the supplied native layout.
	 * @param segment source segment
	 * @param layout required native layout
	 * @return a segment view with the layout size
	 */
	public static MemorySegment view(MemorySegment segment, MemoryLayout layout) {
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

	/**
	 * Converts a nullable segment to a native address.
	 * @param segment nullable segment
	 * @return the segment or {@link MemorySegment#NULL}
	 */
	public static MemorySegment address(MemorySegment segment) {
		return segment == null ? MemorySegment.NULL : segment;
	}

	/**
	 * Converts a nullable native object to its native address.
	 * @param object nullable native object
	 * @return the object's segment or {@link MemorySegment#NULL}
	 */
	public static MemorySegment address(NativeObject object) {
		return object == null ? MemorySegment.NULL : object.segment();
	}

	/**
	 * Allocates a nullable UTF-8 C string.
	 * @param allocator destination allocator
	 * @param value nullable Java string
	 * @return the allocated C string or {@link MemorySegment#NULL}
	 */
	public static MemorySegment cString(SegmentAllocator allocator, String value) {
		return value == null ? MemorySegment.NULL : allocator.allocateFrom(value);
	}

	/**
	 * Reads a nullable UTF-8 C string.
	 * @param address nullable C string address
	 * @return the Java string, or {@code null}
	 */
	public static String readString(MemorySegment address) {
		return address.address() == 0 ? null : address.reinterpret(Long.MAX_VALUE).getString(0);
	}

	/**
	 * Converts a Java {@code long} to the platform-native {@code uintptr_t} carrier.
	 * @param value Java value
	 * @return the platform-native carrier value
	 */
	public static Object nativeUintptr(long value) {
		return C_UINTPTR_T.carrier() == long.class ? value : (int) value;
	}

	/**
	 * Converts the platform-native {@code uintptr_t} carrier to a Java {@code long}.
	 * @param value platform-native carrier value
	 * @return the Java value
	 */
	public static long javaUintptr(Object value) {
		return value instanceof Long val ? val : Integer.toUnsignedLong((Integer) value);
	}
}
