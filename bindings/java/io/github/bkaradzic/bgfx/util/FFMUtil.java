// Copyright 2011-2026 Branimir Karadzic. All rights reserved.
// License: https://github.com/bkaradzic/bgfx/blob/master/LICENSE

package io.github.bkaradzic.bgfx.util;

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
import java.lang.invoke.MutableCallSite;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Shared Java FFM support used by the generated bgfx binding. */
@NullMarked
@SuppressWarnings("restricted")
public final class FFMUtil {
	/** Native linker used for bgfx downcalls and callback upcalls. */
	public static final Linker LINKER = Linker.nativeLinker();

	/** Platform-native layout of C {@code uintptr_t}. */
	public static final ValueLayout C_UINTPTR_T =
		(ValueLayout) LINKER.canonicalLayouts().get("size_t");

	private static final List<String> pendingDowncallNames = new ArrayList<>();
	private static final List<FunctionDescriptor> pendingDowncallDescriptors = new ArrayList<>();
	private static final List<MutableCallSite> pendingDowncallSites = new ArrayList<>();
	private static final List<String> pendingVariadicNames = new ArrayList<>();
	private static final List<MutableCallSite> pendingVariadicSites = new ArrayList<>();
	private static @Nullable SymbolLookup libraryLookup;
	private static @Nullable Arena libraryArena;

	private FFMUtil() {
	}

	/**
	 * Registers a fixed-arity native entry point for eager linking.
	 * @param name native symbol name
	 * @param descriptor native function descriptor
	 * @return a stable handle that becomes callable after linking
	 */
	public static synchronized MethodHandle downcall(
		String name, FunctionDescriptor descriptor) {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(descriptor, "descriptor");
		if (libraryLookup != null) {
			return LINKER.downcallHandle(symbol(libraryLookup, name), descriptor);
		}
		MethodType type = LINKER.downcallHandle(descriptor)
			.type()
			.dropParameterTypes(0, 1);
		MutableCallSite site = new MutableCallSite(type);
		site.setTarget(unlinkedTarget(type, name));
		pendingDowncallNames.add(name);
		pendingDowncallDescriptors.add(descriptor);
		pendingDowncallSites.add(site);
		return site.dynamicInvoker();
	}

	/**
	 * Registers a C variadic symbol for eager linking.
	 * @param name native symbol name
	 * @return a stable zero-argument handle returning the symbol address
	 */
	public static synchronized MethodHandle variadicSymbol(String name) {
		Objects.requireNonNull(name, "name");
		if (libraryLookup != null) {
			return MethodHandles.constant(
				MemorySegment.class, symbol(libraryLookup, name));
		}
		MethodType type = MethodType.methodType(MemorySegment.class);
		MutableCallSite site = new MutableCallSite(type);
		site.setTarget(unlinkedTarget(type, name));
		pendingVariadicNames.add(name);
		pendingVariadicSites.add(site);
		return site.dynamicInvoker();
	}

	private static MethodHandle unlinkedTarget(MethodType type, String name) {
		MethodHandle target = MethodHandles.throwException(
			type.returnType(), IllegalStateException.class);
		target = MethodHandles.insertArguments(target, 0,
			new IllegalStateException("Native symbol is not linked: " + name));
		return MethodHandles.dropArguments(target, 0, type.parameterList());
	}

	/**
	 * Loads a shared library and eagerly links every registered native entry point.
	 * @param library shared-library path
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
	 * Loads a shared library by platform-dependent name and eagerly links every
	 * registered native entry point.
	 * @param library platform-dependent shared-library name
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
	 * Eagerly links every registered native entry point from libraries already
	 * visible to the process.
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
		if (libraryLookup != null || libraryArena != null) {
			throw new IllegalStateException("Native calls are already linked");
		}
	}

	private static void installLibrary(SymbolLookup library, @Nullable Arena arena) {
		SymbolLookup lookup = library
			.or(SymbolLookup.loaderLookup())
			.or(LINKER.defaultLookup());
		MethodHandle[] handles = new MethodHandle[pendingDowncallSites.size()];
		for (int index = 0; index < handles.length; ++index) {
			handles[index] = LINKER.downcallHandle(
				symbol(lookup, pendingDowncallNames.get(index)),
				pendingDowncallDescriptors.get(index));
		}
		MemorySegment[] variadics = new MemorySegment[pendingVariadicSites.size()];
		for (int index = 0; index < variadics.length; ++index) {
			variadics[index] = symbol(lookup, pendingVariadicNames.get(index));
		}
		for (int index = 0; index < handles.length; ++index) {
			pendingDowncallSites.get(index).setTarget(handles[index]);
		}
		for (int index = 0; index < variadics.length; ++index) {
			pendingVariadicSites.get(index).setTarget(
				MethodHandles.constant(MemorySegment.class, variadics[index]));
		}
		MutableCallSite.syncAll(pendingDowncallSites.toArray(MutableCallSite[]::new));
		MutableCallSite.syncAll(pendingVariadicSites.toArray(MutableCallSite[]::new));
		pendingDowncallNames.clear();
		pendingDowncallDescriptors.clear();
		pendingDowncallSites.clear();
		pendingVariadicNames.clear();
		pendingVariadicSites.clear();
		libraryArena = arena;
		libraryLookup = lookup;
	}

	private static MemorySegment symbol(SymbolLookup lookup, String name) {
		return lookup.find(name).orElseThrow(
			() -> new UnsatisfiedLinkError("Unable to find native symbol " + name));
	}

	/**
	 * Invokes a C variadic entry point, inferring each variadic native layout
	 * from its Java value and applying C default argument promotions.
	 * Byte, short, character, boolean, integer, long, float, double, string,
	 * memory segment, native object, and enum values are supported.
	 * @param symbolHandle linked zero-argument symbol-address handle
	 * @param descriptor descriptor of the fixed arguments and return value
	 * @param fixedArgs converted fixed native arguments
	 * @param variadicArgs Java values to convert to promoted variadic arguments
	 * @return the native result, or {@code null} for {@code void}
	 */
	public static @Nullable Object invokeVariadic(
		MethodHandle symbolHandle,
		FunctionDescriptor descriptor,
		Object[] fixedArgs,
		Object[] variadicArgs) {
		Objects.requireNonNull(descriptor, "descriptor");
		Objects.requireNonNull(symbolHandle, "symbolHandle");
		Objects.requireNonNull(fixedArgs, "fixedArgs");
		Objects.requireNonNull(variadicArgs, "variadicArgs");
		MemoryLayout[] layouts = new MemoryLayout[variadicArgs.length];
		Object[] nativeArgs = Arrays.copyOf(
			fixedArgs, fixedArgs.length + variadicArgs.length);
		try (Arena arena = Arena.ofConfined()) {
			for (int index = 0; index < variadicArgs.length; ++index) {
				Object argument = Objects.requireNonNull(
					variadicArgs[index], "variadicArgs[" + index + "]");
				Object value;
				if (argument instanceof Byte number) {
					layouts[index] = ValueLayout.JAVA_INT;
					value = number.intValue();
				} else if (argument instanceof Short number) {
					layouts[index] = ValueLayout.JAVA_INT;
					value = number.intValue();
				} else if (argument instanceof Character character) {
					layouts[index] = ValueLayout.JAVA_INT;
					value = (int) character;
				} else if (argument instanceof Boolean bool) {
					layouts[index] = ValueLayout.JAVA_INT;
					value = bool ? 1 : 0;
				} else if (argument instanceof Integer) {
					layouts[index] = ValueLayout.JAVA_INT;
					value = argument;
				} else if (argument instanceof Long) {
					layouts[index] = ValueLayout.JAVA_LONG;
					value = argument;
				} else if (argument instanceof Float number) {
					layouts[index] = ValueLayout.JAVA_DOUBLE;
					value = number.doubleValue();
				} else if (argument instanceof Double) {
					layouts[index] = ValueLayout.JAVA_DOUBLE;
					value = argument;
				} else if (argument instanceof String string) {
					layouts[index] = ValueLayout.ADDRESS;
					value = cString(arena, string);
				} else if (argument instanceof MemorySegment segment) {
					layouts[index] = ValueLayout.ADDRESS;
					value = address(segment);
				} else if (argument instanceof NativeObject object) {
					layouts[index] = ValueLayout.ADDRESS;
					value = address(object);
				} else if (argument instanceof Enum<?> enumValue) {
					layouts[index] = ValueLayout.JAVA_INT;
					value = enumValue.ordinal();
				} else {
					throw new IllegalArgumentException(
						"Unsupported C variadic argument type: "
							+ argument.getClass().getName());
				}
				nativeArgs[fixedArgs.length + index] = value;
			}
			FunctionDescriptor variadicDescriptor =
				descriptor.appendArgumentLayouts(layouts);
			MemorySegment symbol;
			try {
				symbol = (MemorySegment) symbolHandle.invokeExact();
			} catch (Throwable ex) {
				throw invocationFailure(ex);
			}
			MethodHandle handle = LINKER.downcallHandle(
				symbol,
				variadicDescriptor,
				Linker.Option.firstVariadicArg(fixedArgs.length));
			return invoke(handle, nativeArgs);
		}
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
	public static @Nullable Object invoke(MethodHandle handle, Object... args) {
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
	 * Converts a nullable or zero-address segment to a canonical native address.
	 * @param segment nullable segment
	 * @return the segment, or {@link MemorySegment#NULL} when its address is zero
	 */
	public static MemorySegment address(@Nullable MemorySegment segment) {
		return segment == null || segment.address() == 0
			? MemorySegment.NULL
			: segment;
	}

	/**
	 * Converts a nullable or zero-address native object to a canonical address.
	 * @param object nullable native object
	 * @return the object's segment, or {@link MemorySegment#NULL} when its address is zero
	 */
	public static MemorySegment address(@Nullable NativeObject object) {
		return object == null ? MemorySegment.NULL : address(object.segment());
	}

	/**
	 * Allocates a nullable UTF-8 C string.
	 * @param allocator destination allocator
	 * @param value nullable Java string
	 * @return the allocated C string or {@link MemorySegment#NULL}
	 */
	public static MemorySegment cString(SegmentAllocator allocator, @Nullable String value) {
		return value == null ? MemorySegment.NULL : allocator.allocateFrom(value);
	}

	/**
	 * Reads a nullable UTF-8 C string.
	 * @param address nullable C string address
	 * @return the Java string, or {@code null}
	 */
	public static @Nullable String readString(MemorySegment address) {
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
