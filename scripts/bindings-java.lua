local codegen = require "codegen"
local idl = codegen.idl "bgfx.idl"

local java_template = [[
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
$types

	// -------------------------------------------------------------------------
	// Generated native entry points. Descriptors and methods intentionally live
	// in this section rather than in per-function holder classes.
	// -------------------------------------------------------------------------
$funcs

}
]]

local function hasSuffix(str, suffix)
	return suffix == "" or str:sub(-#suffix) == suffix
end

local function gisub(s, pat, repl, n)
	pat = string.gsub(pat, '(%a)', function(v)
		return '[' .. string.upper(v) .. string.lower(v) .. ']'
	end)
	if n then
		return string.gsub(s, pat, repl, n)
	else
		return string.gsub(s, pat, repl)
	end
end

local primitive_types = {
	bool = { java = "boolean", layout = "ValueLayout.JAVA_BOOLEAN" },
	char = { java = "byte", layout = "ValueLayout.JAVA_BYTE" },
	float = { java = "float", layout = "ValueLayout.JAVA_FLOAT" },
	int8_t = { java = "byte", layout = "ValueLayout.JAVA_BYTE" },
	int16_t = { java = "short", layout = "ValueLayout.JAVA_SHORT" },
	int32_t = { java = "int", layout = "ValueLayout.JAVA_INT" },
	int64_t = { java = "long", layout = "ValueLayout.JAVA_LONG" },
	uint8_t = { java = "byte", layout = "ValueLayout.JAVA_BYTE" },
	uint16_t = { java = "short", layout = "ValueLayout.JAVA_SHORT" },
	uint32_t = { java = "int", layout = "ValueLayout.JAVA_INT" },
	uint64_t = { java = "long", layout = "ValueLayout.JAVA_LONG" },
	uintptr_t = { java = "long", layout = "C_UINTPTR_T", uintptr = true },
	bgfx_view_id_t = { java = "short", layout = "ValueLayout.JAVA_SHORT" },
	void = { java = "void" },
	va_list = { java = "MemorySegment", layout = "ValueLayout.ADDRESS", opaque = true },
}

local ctype_info = {}
local enum_counts = {}

local function java_type_name(typ)
	if typ.enum then
		return typ.typename
	elseif typ.namespace then
		return typ.namespace .. "." .. typ.name
	else
		return typ.name:gsub("::Enum$", "")
	end
end

for _, typ in ipairs(idl.types) do
	if typ.cname then
		local kind
		if typ.enum then
			kind = "enum"
			enum_counts[typ.typename] = #typ.enum
		elseif typ.handle then
			kind = "handle"
		elseif typ.struct then
			kind = "struct"
		elseif typ.args and typ.ret then
			kind = "funcptr"
		else
			kind = "opaque"
		end
		if kind ~= "opaque" or primitive_types[typ.cname] == nil then
			ctype_info[typ.cname] = {
				kind = kind,
				java = java_type_name(typ),
				typ = typ,
			}
		end
	end
end

local function normalize_ctype(ctype)
	ctype = ctype:gsub("%s+", " ")
	ctype = ctype:gsub("%s*%*%s*", "*")
	ctype = ctype:match("^%s*(.-)%s*$")
	local is_const = ctype:match("^const ") ~= nil
	ctype = ctype:gsub("^const%s+", "")

	local pointers = 0
	while hasSuffix(ctype, "*") do
		pointers = pointers + 1
		ctype = ctype:sub(1, -2)
	end

	return {
		base = ctype,
		pointers = pointers,
		is_const = is_const,
	}
end

local function type_details(arg, array_as_pointer)
	local result = normalize_ctype(arg.ctype)
	if arg.array and array_as_pointer then
		result.pointers = result.pointers + 1
	end
	result.info = ctype_info[result.base]
	result.primitive = primitive_types[result.base]
	return result
end

local function java_type(arg, context)
	if arg.ctype == "..." then
		return "VarArg..."
	end

	local details = type_details(arg, context ~= "member")
	if details.pointers > 0 then
		if context ~= "member" and details.base == "char" and details.pointers == 1 and details.is_const then
			return "String"
		elseif details.pointers == 1 and details.info and details.info.kind == "struct" then
			return details.info.java
		end
		return "MemorySegment"
	end

	if details.info then
		if details.info.kind == "funcptr" or details.info.kind == "opaque" then
			return "MemorySegment"
		end
		return details.info.java
	end
	return assert(details.primitive, "Unsupported C type: " .. arg.ctype).java
end

local function layout_type(arg, array_as_pointer)
	local details = type_details(arg, array_as_pointer)
	if details.pointers > 0 then
		return "ValueLayout.ADDRESS"
	end
	if details.info then
		if details.info.kind == "enum" then
			return "ValueLayout.JAVA_INT"
		elseif details.info.kind == "funcptr" or details.info.kind == "opaque" then
			return "ValueLayout.ADDRESS"
		end
		return details.info.java .. ".LAYOUT"
	end
	return assert(details.primitive, "Unsupported C layout type: " .. arg.ctype).layout
end

local function array_length(member)
	local number = member.array:match("^%[%s*(%d+)%s*%]$")
	if number then
		return tonumber(number)
	end

	local enum_name, enum_item = member.array:match("^%[%s*([%w_]+)::([%w_]+)%s*%]$")
	assert(enum_name and enum_item == "Count", "Unsupported array expression: " .. member.array)
	return assert(enum_counts[enum_name], "Unknown enum in array expression: " .. enum_name)
end

local function member_layout(member)
	local layout = layout_type(member, false)
	if member.array then
		layout = string.format("MemoryLayout.sequenceLayout(%d, %s)", array_length(member), layout)
	end
	return layout .. ".withName(\"" .. member.name .. "\")"
end

local function camel_name(cname)
	local name = cname:gsub("_(.)", cname.upper)
	return name:gsub("%dd", name.upper)
end

local function method_name(func)
	local name = gisub(camel_name(func.cname), func.this_type.type, "")
	return name:gsub("^%L", string.lower)
end

local converter = {}
local yield = coroutine.yield
local gen = {}

local combined
local lastCombinedFlag
local namespace
local downcall_entries
local variadic_entries
local emit_downcall_table

local function reset_generator_state()
	for _, name in ipairs(combined) do
		combined[name] = {}
	end
	lastCombinedFlag = nil
	namespace = ""
	downcall_entries = {}
	variadic_entries = {}
end

local function collect_methods()
	-- find the functions that have `this` first argument
	-- these belong to a type (struct) and we need to add them when converting structures
	local methods = {}
	for _, func in ipairs(idl["funcs"]) do
		if func.this ~= nil then
			local this_ctype = normalize_ctype(func.this_type.ctype).base
			if methods[this_ctype] == nil then
				methods[this_ctype] = {}
			end
			table.insert(methods[this_ctype], func)
		end
	end
	return methods
end

local function generate_section(what, methods)
	local tmp = {}
	for _, object in ipairs(idl[what]) do
		local co = coroutine.create(converter[what])
		local any
		-- we're pretty confident there are no types that have the same name with a func
		local funcs = methods[object.cname]
		while true do
			local ok, v = coroutine.resume(co, {
				obj = object,
				funcs = funcs
			})
			assert(ok, debug.traceback(co, v))
			if not v then
				break
			end
			table.insert(tmp, v)
			any = true
		end
		if any and tmp[#tmp] ~= "" then
			table.insert(tmp, "")
		end
	end
	if what == "funcs" then
		local co = coroutine.create(emit_downcall_table)
		while true do
			local ok, value = coroutine.resume(co)
			assert(ok, debug.traceback(co, value))
			if not value then
				break
			end
			table.insert(tmp, value)
		end
	end
	return table.concat(tmp, "\n")
end

-- Kept as independently generated sections so a future multi-file backend can
-- reuse the emitters without parsing a monolithic Java source file.
function gen.sections()
	reset_generator_state()
	local methods = collect_methods()
	return {
		types = generate_section("types", methods),
		funcs = generate_section("funcs", methods),
	}
end

function gen.gen()
	local sections = gen.sections()
	return (java_template:gsub("$(%l+)", sections))
end

combined = { "State", "Stencil", "Buffer", "Texture", "Sampler", "Reset" }

for _, v in ipairs(combined) do
	combined[v] = {}
end

local function javadoc_text(line)
	line = line or ""
	line = line:gsub("&", "&amp;")
	line = line:gsub("<", "&lt;")
	line = line:gsub(">", "&gt;")
	line = line:gsub("`([^`]*)`", "{@code %1}")
	line = line:gsub("bgfx::", "BGFX.")
	line = line:gsub("::", ".")
	line = line:gsub("([%w_%.]+)%.Enum", "%1")
	line = line:gsub("%*/", "*&#47;")
	line = line:gsub("^%s*@remarks?%s*", "<strong>Remarks:</strong> ")
	line = line:gsub("^%s*@attention%s*", "<strong>Attention:</strong> ")
	line = line:gsub("^%s*@warning%s*", "<strong>Warning:</strong> ")
	line = line:gsub("^%s*@note%s*", "<strong>Note:</strong> ")
	line = line:gsub("^%s*@returns?%s*", "<strong>Returns:</strong> ")
	return line
end

local function emit_javadoc(lines, doc_indent, params, returns)
	yield(doc_indent .. "/**")
	if lines and #lines > 0 then
		local emitted = false
		local paragraph = false
		for _, line in ipairs(lines) do
			local text = javadoc_text(line)
			if text == "" then
				paragraph = emitted
			else
				if paragraph then
					yield(doc_indent .. " * <p>")
				end
				yield(doc_indent .. " * " .. text)
				emitted = true
				paragraph = false
			end
		end
	end
	if params then
		for _, param in ipairs(params) do
			yield(doc_indent .. " * @param " .. param.name .. " " .. javadoc_text(param.text))
		end
	end
	if returns and returns ~= "" then
		yield(doc_indent .. " * @return " .. javadoc_text(returns))
	end
	yield(doc_indent .. " */")
end

local function FlagBlock(typ)
	local format = "0x%08x"
	local enumType = "int"
	if typ.bits == 64 then
		format = "0x%016xL"
		enumType = "long"
	elseif typ.bits == 16 then
		format = "(short) 0x%04x"
		enumType = "short"
	end

	local name = typ.name .. "Flags"
	emit_javadoc(typ.comments or { "Constants for " .. typ.name .. " flags." }, "\t")
	yield("\tpublic static final class " .. name .. " {")
	yield("\t\tprivate " .. name .. "() {")
	yield("\t\t}")

	for idx, flag in ipairs(typ.flag) do
		local flagName = flag.name:gsub("_", "")
		-- make 2d/3d upper case 2D/3D
		flagName = flagName:gsub("%dd", flagName.upper);
		if flag.comment ~= nil then
			if idx ~= 1 then
				yield("")
			end
			
			emit_javadoc(flag.comment, "\t\t")
		else
			yield("")
			emit_javadoc({ typ.name .. " flag value {@code " .. flagName .. "}." }, "\t\t")
		end
		yield("\t\tpublic static final " .. enumType .. " " .. flagName .. " = " .. string.format(flag.format or format, flag.value) .. ";")
	end

	if typ.shift then
		emit_javadoc({ "Bit shift for this flag group." }, "\t\t")
		yield("\t\tpublic static final " .. enumType .. " Shift = " .. typ.shift .. ";")
	end

	-- generate Mask
	if typ.mask then
		emit_javadoc({ "Bit mask for this flag group." }, "\t\t")
		yield("\t\tpublic static final " .. enumType .. " Mask = " .. string.format(format, typ.mask) .. ";")
	end

	yield("\t}")
end

local function lastCombinedFlagBlock()
	if lastCombinedFlag then
		local typ = combined[lastCombinedFlag]
		if typ then
			FlagBlock(combined[lastCombinedFlag])
			yield("")
		end
		lastCombinedFlag = nil
	end
end

local function should_emit_function(func)
	if func.cpponly then
		return false
	end
	return true
end

local function constant_name(name)
	return name:gsub("[^%w]", "_"):upper()
end

local function emit_comments(func, func_indent)
	local params = {}
	for _, arg in ipairs(func.args) do
		if arg.ctype ~= "..." then
			table.insert(params, {
				name = arg.name,
				text = arg.comment and table.concat(arg.comment, " ") or "native function argument",
			})
		else
			table.insert(params, { name = "_args", text = "promoted C variadic arguments" })
		end
	end
	local returns
	if func.ret and func.ret.comment then
		returns = table.concat(func.ret.comment, " ")
	elseif java_type(func.ret, "return") ~= "void" then
		returns = "the native function result"
	end
	emit_javadoc(func.comments or { "Calls {@code bgfx_" .. func.cname .. "}." }, func_indent, params, returns)
end

local function emit_enum(typ)
	emit_javadoc(typ.comments or { typ.typename .. " values." }, "\t")
	yield("\tpublic enum " .. typ.typename .. " {")
	for _, item in ipairs(typ.enum) do
		if item.comment then
			emit_javadoc(item.comment, "\t\t")
		else
			emit_javadoc({ typ.typename .. " value {@code " .. item.name .. "}." }, "\t\t")
		end
		yield("\t\t" .. item.name .. ",")
	end
	yield("")
	emit_javadoc({ "Number of native enum values." }, "\t\t")
	yield("\t\tCount;")
	yield("")
	emit_javadoc({ "Native C enum layout." }, "\t\t")
	yield("\t\tpublic static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;")
	yield("\t\tprivate static final " .. typ.typename .. "[] VALUES = values();")
	yield("")
	emit_javadoc({ "Returns the enum constant for a native C enum value." }, "\t\t",
		{ { name = "value", text = "the native enum value" } }, "the matching enum constant")
	yield("\t\tpublic static " .. typ.typename .. " fromValue(int value) {")
	yield("\t\t\tif (value >= 0 && value < VALUES.length) {")
	yield("\t\t\t\treturn VALUES[value];")
	yield("\t\t\t}")
	yield("\t\t\tthrow new IllegalArgumentException(\"Unknown " .. typ.typename .. " value: \" + value);")
	yield("\t\t}")
	yield("\t}")
end

local function emit_handle(typ)
	local tagged = typ.tagged ~= nil
	local fields = tagged and "short idx, short type" or "short idx"
	local record_params = { { name = "idx", text = "native handle index" } }
	if tagged then
		table.insert(record_params, { name = "type", text = "native buffer handle tag" })
	end
	emit_javadoc(typ.comments or { "Native bgfx handle." }, "\t", record_params)
	yield("\tpublic record " .. typ.name .. "(" .. fields .. ") {")
	emit_javadoc({ "Native by-value handle layout." }, "\t\t")
	yield("\t\tpublic static final StructLayout LAYOUT = cStruct(\"" .. typ.cname .. "\",")
	if tagged then
		yield("\t\t\tValueLayout.JAVA_SHORT.withName(\"idx\"),")
		yield("\t\t\tValueLayout.JAVA_SHORT.withName(\"type\"));")
	else
		yield("\t\t\tValueLayout.JAVA_SHORT.withName(\"idx\"));")
	end
	yield("\t\tprivate static final VarHandle VH_IDX = LAYOUT.varHandle(")
	yield("\t\t\tMemoryLayout.PathElement.groupElement(\"idx\"));")
	if tagged then
		yield("\t\tprivate static final VarHandle VH_TYPE = LAYOUT.varHandle(")
		yield("\t\t\tMemoryLayout.PathElement.groupElement(\"type\"));")
		emit_javadoc({ "Invalid handle sentinel." }, "\t\t")
		yield("\t\tpublic static final " .. typ.name .. " INVALID =")
		yield("\t\t\tnew " .. typ.name .. "((short) 0xffff, (short) 0xffff);")
	else
		emit_javadoc({ "Invalid handle sentinel." }, "\t\t")
		yield("\t\tpublic static final " .. typ.name .. " INVALID = new " .. typ.name .. "((short) 0xffff);")
	end
	if tagged then
		for index, source in ipairs(typ.tagged) do
			yield("")
			emit_javadoc({ "Creates a tagged buffer handle." }, "\t\t",
				{ { name = "handle", text = "the source " .. source } })
			yield("\t\tpublic " .. typ.name .. "(" .. source .. " handle) {")
			yield(string.format("\t\t\tthis(handle.idx(), (short) %d);", index - 1))
			yield("\t\t}")
		end
	end
	yield("")
	emit_javadoc({ "Returns whether this handle is valid." }, "\t\t", nil,
		"{@code true} when the handle index is not {@code UINT16_MAX}")
	yield("\t\tpublic boolean isValid() {")
	yield("\t\t\treturn idx != (short) 0xffff;")
	yield("\t\t}")
	yield("")
	emit_javadoc({ "Allocates and writes the native by-value handle representation." }, "\t\t",
		{ { name = "allocator", text = "the destination allocator" } }, "the allocated native segment")
	yield("\t\tpublic MemorySegment allocate(SegmentAllocator allocator) {")
	yield("\t\t\tMemorySegment segment = allocator.allocate(LAYOUT);")
	yield("\t\t\twrite(segment);")
	yield("\t\t\treturn segment;")
	yield("\t\t}")
	yield("")
	emit_javadoc({ "Writes this handle to an existing native segment." }, "\t\t",
		{ { name = "segment", text = "the destination segment" } })
	yield("\t\tpublic void write(MemorySegment segment) {")
	yield("\t\t\tsegment = view(segment, LAYOUT);")
	yield("\t\t\tVH_IDX.set(segment, 0L, idx);")
	if tagged then
		yield("\t\t\tVH_TYPE.set(segment, 0L, type);")
	end
	yield("\t\t}")
	yield("")
	emit_javadoc({ "Reads a by-value handle from native memory." }, "\t\t",
		{ { name = "segment", text = "the source segment" } }, "the decoded handle")
	yield("\t\tpublic static " .. typ.name .. " read(MemorySegment segment) {")
	yield("\t\t\tsegment = view(segment, LAYOUT);")
	if tagged then
		yield("\t\t\treturn new " .. typ.name .. "(")
		yield("\t\t\t\t(short) VH_IDX.get(segment, 0L),")
		yield("\t\t\t\t(short) VH_TYPE.get(segment, 0L));")
	else
		yield("\t\t\treturn new " .. typ.name .. "((short) VH_IDX.get(segment, 0L));")
	end
	yield("\t\t}")
	yield("\t}")
end

local function callback_carrier_type(arg)
	local details = type_details(arg, true)
	if details.pointers > 0 then
		return "MemorySegment"
	elseif details.info then
		if details.info.kind == "enum" then
			return "int"
		elseif details.info.kind == "handle" or details.info.kind == "struct"
			or details.info.kind == "funcptr" or details.info.kind == "opaque" then
			return "MemorySegment"
		end
	end
	return assert(details.primitive, "Unsupported callback type: " .. arg.ctype).java
end

local function java_class_literal(java)
	return java .. ".class"
end

local function descriptor_expression(ret, args)
	local layouts = {}
	for _, arg in ipairs(args) do
		if arg.ctype ~= "..." then
			table.insert(layouts, layout_type(arg, true))
		end
	end
	local suffix = #layouts == 0 and "" or ", " .. table.concat(layouts, ", ")
	if normalize_ctype(ret.ctype).base == "void" and normalize_ctype(ret.ctype).pointers == 0 then
		return "FunctionDescriptor.ofVoid(" .. table.concat(layouts, ", ") .. ")"
	end
	return "FunctionDescriptor.of(" .. layout_type(ret, false) .. suffix .. ")"
end

local function emit_funcptr(typ)
	local args = {}
	local arg_types = {}
	local method_classes = {}
	local params = {}
	for _, arg in ipairs(typ.args) do
		if arg.ctype ~= "..." then
			local name = arg.name ~= "" and arg.name or "_arg"
			table.insert(args, callback_carrier_type(arg) .. " " .. name)
			table.insert(arg_types, arg)
			table.insert(method_classes, java_class_literal(callback_carrier_type(arg)))
			table.insert(params, {
				name = name,
				text = arg.comment and table.concat(arg.comment, " ") or "native callback argument",
			})
		end
	end
	local ret = callback_carrier_type(typ.ret)
	emit_javadoc(typ.comments or { "Native callback." }, "\t")
	yield("\t@FunctionalInterface")
	yield("\tpublic interface " .. typ.name .. " {")
	emit_javadoc({ "Native callback function descriptor." }, "\t\t")
	yield("\t\tFunctionDescriptor DESCRIPTOR = " .. descriptor_expression(typ.ret, arg_types) .. ";")
	emit_javadoc({ "Bound callback target used to create upcall stubs." }, "\t\t")
	yield("\t\tMethodHandle TARGET = upcallTarget(")
	yield("\t\t\t" .. typ.name .. ".class, \"invoke\", MethodType.methodType(")
	local class_suffix = #method_classes == 0 and "" or ", " .. table.concat(method_classes, ", ")
	yield("\t\t\t\t" .. java_class_literal(ret) .. class_suffix .. "));")
	yield("")
	emit_javadoc({ "Invoked by native bgfx code. Implementations must not throw." }, "\t\t", params)
	yield("\t\t" .. ret .. " invoke(" .. table.concat(args, ", ") .. ");")
	yield("")
	emit_javadoc({
		"Creates an upcall stub for this callback.",
		"The arena must remain alive until bgfx can no longer invoke the callback.",
	}, "\t\t", { { name = "arena", text = "a caller-owned, long-lived arena" } }, "the native function pointer")
	yield("\t\tdefault MemorySegment upcall(Arena arena) {")
	yield("\t\t\tObjects.requireNonNull(arena, \"arena\");")
	yield("\t\t\treturn LINKER.upcallStub(TARGET.bindTo(this), DESCRIPTOR, arena);")
	yield("\t\t}")
	yield("\t}")
end

local function emit_member_accessor(member, body_indent)
	local details = type_details(member, false)
	local member_type = java_type(member, "member")
	local field_handle = (member.array
		or (details.pointers == 0 and details.info
			and (details.info.kind == "handle" or details.info.kind == "struct")))
		and "MH_" .. constant_name(member.name)
		or "VH_" .. constant_name(member.name)
	local comments = member.comment or { "Gets the native {@code " .. member.name .. "} field." }

	if member.array then
		emit_javadoc(comments, body_indent, nil, "a segment view of the inline array")
		yield(body_indent .. "public MemorySegment " .. member.name .. "() {")
		yield(body_indent .. "\treturn slice(" .. field_handle .. ", segment());")
		yield(body_indent .. "}")
		return
	end

	local getter
	local setter
	if details.pointers > 0 then
		if details.pointers == 1 and details.info and details.info.kind == "struct" then
			getter = "new " .. details.info.java .. "((MemorySegment) " .. field_handle .. ".get(segment(), 0L))"
			setter = field_handle .. ".set(segment(), 0L, address(value))"
		else
			getter = "(MemorySegment) " .. field_handle .. ".get(segment(), 0L)"
			setter = field_handle .. ".set(segment(), 0L, address(value))"
		end
	elseif details.info then
		if details.info.kind == "enum" then
			getter = details.info.java .. ".fromValue((int) " .. field_handle .. ".get(segment(), 0L))"
			setter = field_handle .. ".set(segment(), 0L, value.ordinal())"
		elseif details.info.kind == "handle" then
			getter = details.info.java .. ".read(slice(" .. field_handle .. ", segment()))"
			setter = "value.write(slice(" .. field_handle .. ", segment()))"
		elseif details.info.kind == "struct" then
			getter = "new " .. details.info.java .. "(slice(" .. field_handle .. ", segment()))"
			setter = "slice(" .. field_handle .. ", segment()).copyFrom(value.segment())"
		else
			getter = "(MemorySegment) " .. field_handle .. ".get(segment(), 0L)"
			setter = field_handle .. ".set(segment(), 0L, address(value))"
		end
	else
		local primitive = assert(details.primitive, "Unsupported member type: " .. member.ctype)
		assert(not primitive.uintptr, "uintptr_t struct fields require a carrier-specific accessor")
		getter = "(" .. primitive.java .. ") " .. field_handle .. ".get(segment(), 0L)"
		setter = field_handle .. ".set(segment(), 0L, value)"
	end

	emit_javadoc(comments, body_indent, nil, "the field value")
	yield(body_indent .. "public " .. member_type .. " " .. member.name .. "() {")
	yield(body_indent .. "\treturn " .. getter .. ";")
	yield(body_indent .. "}")
	yield("")
	emit_javadoc({ "Sets the native {@code " .. member.name .. "} field." }, body_indent,
		{ { name = "value", text = "the new field value" } })
	yield(body_indent .. "public void " .. member.name .. "(" .. member_type .. " value) {")
	yield(body_indent .. "\t" .. setter .. ";")
	yield(body_indent .. "}")
end

local concrete_structs = {}
for _, typ in ipairs(idl.types) do
	if typ.struct and not typ.namespace then
		concrete_structs[typ.name] = typ
	end
end

local function emit_struct_body(typ, funcs, body_indent)
	local opaque = typ.name == "Encoder" and #typ.struct == 0
	if not opaque then
		emit_javadoc({ "Native C structure layout." }, body_indent)
		yield(body_indent .. "public static final StructLayout LAYOUT = cStruct(\"" .. typ.cname .. "\",")
		for index, member in ipairs(typ.struct) do
			local suffix = index == #typ.struct and ");" or ","
			yield(body_indent .. "\t" .. member_layout(member) .. suffix)
		end
		for _, member in ipairs(typ.struct) do
			local details = type_details(member, false)
			local group = member.array or (details.pointers == 0 and details.info
				and (details.info.kind == "handle" or details.info.kind == "struct"))
			local handle_type = group and "MethodHandle" or "VarHandle"
			local handle_prefix = group and "MH_" or "VH_"
			local factory = group and "sliceHandle" or "varHandle"
			yield(body_indent .. "private static final " .. handle_type .. " "
				.. handle_prefix .. constant_name(member.name) .. " = LAYOUT." .. factory .. "(")
			yield(body_indent .. "\tMemoryLayout.PathElement.groupElement(\"" .. member.name .. "\"));")
		end
	end
	emit_javadoc({ opaque and "Wraps an opaque native pointer." or "Wraps an existing native structure." }, body_indent,
		{ { name = "segment", text = "native memory segment" } })
	yield(body_indent .. "public " .. typ.name .. "(MemorySegment segment) {")
	if opaque then
		yield(body_indent .. "\tsuper(segment);")
	else
		yield(body_indent .. "\tsuper(segment, LAYOUT);")
	end
	yield(body_indent .. "}")
	if not opaque then
		yield("")
		emit_javadoc({ "Allocates a native structure." }, body_indent,
			{ { name = "allocator", text = "destination allocator" } })
		yield(body_indent .. "public " .. typ.name .. "(SegmentAllocator allocator) {")
		yield(body_indent .. "\tsuper(allocator, LAYOUT);")
		yield(body_indent .. "}")
	end

	for _, member in ipairs(typ.struct) do
		yield("")
		emit_member_accessor(member, body_indent)
	end
	if funcs then
		for _, func in ipairs(funcs) do
			if should_emit_function(func) then
				yield("")
				converter.funcs({ obj = func, asMethod = true, methodIndent = body_indent })
			end
		end
	end
end

namespace = ""

function converter.types(params)
	local typ = params.obj
	local funcs = params.funcs
	if typ.args and typ.ret then
		emit_funcptr(typ)
	elseif typ.handle then
		lastCombinedFlagBlock()
		emit_handle(typ)
	elseif typ.enum then
		lastCombinedFlagBlock()
		emit_enum(typ)
	elseif typ.bits ~= nil then
		local prefix, name = typ.name:match "(%u%l+)(.*)"
		if prefix ~= lastCombinedFlag then
			lastCombinedFlagBlock()
			lastCombinedFlag = prefix
		end
		local combinedFlag = combined[prefix]
		if combinedFlag then
			combinedFlag.bits = typ.bits
			combinedFlag.name = prefix
			local flags = combinedFlag.flag or {}
			combinedFlag.flag = flags
			local lookup = combinedFlag.lookup or {}
			combinedFlag.lookup = lookup
			for _, flag in ipairs(typ.flag) do
				local flagName = name .. flag.name:gsub("_", "")
				local value = flag.value
				if value == nil then
					-- It's a combined flag
					value = 0
					for _, v in ipairs(flag) do
						value = value | assert(lookup[name .. v], v .. " is not defined for " .. flagName)
					end
				end
				lookup[flagName] = value
				table.insert(flags, {
					name = flagName,
					value = value,
					comment = flag.comment
				})
			end

			if typ.shift then
				table.insert(flags, {
					name = name .. "Shift",
					value = typ.shift,
					format = "%d",
					comment = typ.comment
				})
			end

			if typ.mask then
				-- generate Mask
				table.insert(flags, {
					name = name .. "Mask",
					value = typ.mask,
					comment = typ.comment
				})
				lookup[name .. "Mask"] = typ.mask
			end
		else
			FlagBlock(typ)
		end
	elseif typ.struct ~= nil then
		local skip = false
		local class_indent
		local body_indent
		if typ.namespace then
			if namespace ~= typ.namespace then
				local concrete = concrete_structs[typ.namespace]
				emit_javadoc(concrete and concrete.comments or { typ.namespace .. " structures." }, "\t")
				local extends = concrete and " extends NativeObject" or ""
				yield("\tpublic static final class " .. typ.namespace .. extends .. " {")
				namespace = typ.namespace
			end
			class_indent = "\t\t"
			body_indent = "\t\t\t"
		elseif namespace ~= "" then
			assert(typ.name == namespace, "Namespace " .. namespace .. " has no matching struct")
			namespace = ""
			skip = true
			class_indent = "\t"
			body_indent = "\t\t"
		else
			class_indent = "\t"
			body_indent = "\t\t"
		end

		if not skip then
			emit_javadoc(typ.comments or { typ.name .. " native structure." }, class_indent)
			yield(class_indent .. "public static final class " .. typ.name .. " extends NativeObject {")
		end
		emit_struct_body(typ, funcs, body_indent)
		yield(class_indent .. "}")
	end
end

local function is_string(arg)
	local details = type_details(arg, true)
	return details.base == "char" and details.pointers == 1 and details.is_const
end

local function is_group_value(arg)
	local details = type_details(arg, false)
	return details.pointers == 0 and details.info
		and (details.info.kind == "handle" or details.info.kind == "struct")
end

local function native_argument(arg, name)
	local details = type_details(arg, true)
	if details.pointers > 0 then
		if is_string(arg) then
			return "cString(arena, " .. name .. ")"
		end
		return "address(" .. name .. ")"
	elseif details.info then
		if details.info.kind == "enum" then
			return name .. ".ordinal()"
		elseif details.info.kind == "handle" then
			return name .. ".allocate(arena)"
		elseif details.info.kind == "struct" then
			return name .. ".segment()"
		end
		return "address(" .. name .. ")"
	end
	local primitive = assert(details.primitive, "Unsupported native argument: " .. arg.ctype)
	if primitive.uintptr then
		return "nativeUintptr(" .. name .. ")"
	elseif primitive.opaque then
		return "address(" .. name .. ")"
	end
	return name
end

local function function_needs_arena(func)
	if func.vararg or is_group_value(func.ret) then
		return true
	end
	for _, arg in ipairs(func.args) do
		if arg.ctype ~= "..." then
			local details = type_details(arg, true)
			if is_string(arg) or (details.pointers == 0 and details.info and details.info.kind == "handle") then
				return true
			end
		end
	end
	return false
end

local function native_call_arguments(func)
	local args = {}
	if is_group_value(func.ret) then
		table.insert(args, "(SegmentAllocator) arena")
	end
	if func.this then
		table.insert(args, "segment()")
	end
	for _, arg in ipairs(func.args) do
		if arg.ctype ~= "..." then
			table.insert(args, native_argument(arg, arg.name))
		end
	end
	return args
end

local function has_dynamic_carrier(arg, array_as_pointer)
	local details = type_details(arg, array_as_pointer)
	return details.pointers == 0 and details.primitive and details.primitive.uintptr
end

local function function_uses_exact_invoke(func)
	if has_dynamic_carrier(func.ret, false) then
		return false
	end
	for _, arg in ipairs(func.args) do
		if arg.ctype ~= "..." and has_dynamic_carrier(arg, true) then
			return false
		end
	end
	return true
end

local function native_call(func)
	local args = native_call_arguments(func)
	local suffix = #args == 0 and "" or ", " .. table.concat(args, ", ")
	if function_uses_exact_invoke(func) then
		return "downcallHandle(DC_" .. constant_name(func.cname) .. ").invokeExact("
			.. table.concat(args, ", ") .. ")"
	end
	return "invoke(DC_" .. constant_name(func.cname) .. suffix .. ")"
end

local function return_statement(func, call)
	local details = type_details(func.ret, false)
	if details.pointers > 0 then
		if details.base == "char" and details.pointers == 1 and details.is_const then
			return "return readString((MemorySegment) " .. call .. ");"
		elseif details.pointers == 1 and details.info and details.info.kind == "struct" then
			return "return new " .. details.info.java .. "((MemorySegment) " .. call .. ");"
		end
		return "return (MemorySegment) " .. call .. ";"
	elseif details.info then
		if details.info.kind == "enum" then
			return "return " .. details.info.java .. ".fromValue((int) " .. call .. ");"
		elseif details.info.kind == "handle" then
			return "return " .. details.info.java .. ".read((MemorySegment) " .. call .. ");"
		elseif details.info.kind == "struct" then
			return "return new " .. details.info.java .. "((MemorySegment) " .. call .. ");"
		end
		return "return (MemorySegment) " .. call .. ";"
	end
	local primitive = assert(details.primitive, "Unsupported return type: " .. func.ret.ctype)
	if primitive.java == "void" then
		return call .. ";"
	elseif primitive.uintptr then
		return "return javaUintptr(" .. call .. ");"
	elseif primitive.opaque then
		return "return (MemorySegment) " .. call .. ";"
	end
	return "return (" .. primitive.java .. ") " .. call .. ";"
end

local function public_parameters(func, variadic)
	local args = {}
	for _, arg in ipairs(func.args) do
		if arg.ctype ~= "..." then
			table.insert(args, java_type(arg, "arg") .. " " .. arg.name)
		end
	end
	if variadic then
		table.insert(args, "VarArg... _args")
	end
	return table.concat(args, ", ")
end

local function emit_regular_wrapper(func, func_indent, as_method)
	emit_comments(func, func_indent)
	local modifier = as_method and "public final " or "public static final "
	local name = as_method and method_name(func) or camel_name(func.cname)
	yield(func_indent .. modifier .. java_type(func.ret, "return") .. " " .. name
		.. "(" .. public_parameters(func, false) .. ") {")
	local body_indent = func_indent .. "\t"
	local exact = function_uses_exact_invoke(func)
	if exact then
		yield(body_indent .. "try {")
		body_indent = body_indent .. "\t"
	end
	if function_needs_arena(func) then
		yield(body_indent .. "try (Arena arena = Arena.ofConfined()) {")
		yield(body_indent .. "\t" .. return_statement(func, native_call(func)))
		yield(body_indent .. "}")
	else
		yield(body_indent .. return_statement(func, native_call(func)))
	end
	if exact then
		body_indent = body_indent:sub(1, -2)
		yield(body_indent .. "} catch (Throwable ex) {")
		yield(body_indent .. "\tthrow invocationFailure(ex);")
		yield(body_indent .. "}")
	end
	yield(func_indent .. "}")
end

local function emit_variadic_wrapper(func, func_indent)
	emit_comments(func, func_indent)
	yield(func_indent .. "public static final " .. java_type(func.ret, "return") .. " " .. camel_name(func.cname)
		.. "(" .. public_parameters(func, true) .. ") {")
	local body = func_indent .. "\t"
	yield(body .. "Objects.requireNonNull(_args, \"_args\");")
	yield(body .. "try (Arena arena = Arena.ofConfined()) {")
	yield(body .. "\tMemoryLayout[] layouts = new MemoryLayout[_args.length];")
	local fixed_count = 0
	for _, arg in ipairs(func.args) do
		if arg.ctype ~= "..." then
			fixed_count = fixed_count + 1
		end
	end
	yield(body .. "\tObject[] nativeArgs = new Object[" .. fixed_count .. " + _args.length];")
	local native_index = 0
	for _, arg in ipairs(func.args) do
		if arg.ctype ~= "..." then
			yield(body .. "\tnativeArgs[" .. native_index .. "] = " .. native_argument(arg, arg.name) .. ";")
			native_index = native_index + 1
		end
	end
	yield(body .. "\tfor (int index = 0; index < _args.length; ++index) {")
	yield(body .. "\t\tlayouts[index] = _args[index].layout();")
	yield(body .. "\t\tnativeArgs[" .. fixed_count .. " + index] = _args[index].value();")
	yield(body .. "\t}")
	local fixed_args = {}
	for _, arg in ipairs(func.args) do
		if arg.ctype ~= "..." then
			table.insert(fixed_args, arg)
		end
	end
	yield(body .. "\tFunctionDescriptor descriptor = " .. descriptor_expression(func.ret, fixed_args)
		.. ".appendArgumentLayouts(layouts);")
	yield(body .. "\tMethodHandle handle = LINKER.downcallHandle(")
	yield(body .. "\t\tvariadicSymbol(VC_" .. constant_name(func.cname) .. "),")
	yield(body .. "\t\tdescriptor, Linker.Option.firstVariadicArg(" .. fixed_count .. "));")
	yield(body .. "\t" .. return_statement(func, "invoke(handle, nativeArgs)"))
	yield(body .. "}")
	yield(func_indent .. "}")
end

local function function_descriptor(func)
	local args = {}
	if func.this then
		table.insert(args, func.this_type)
	end
	for _, arg in ipairs(func.args) do
		if arg.ctype ~= "..." then
			table.insert(args, arg)
		end
	end
	return descriptor_expression(func.ret, args)
end

local function emit_native_descriptor(func)
	if func.vararg then
		local index = #variadic_entries
		table.insert(variadic_entries, { func = func, index = index })
		yield("\tprivate static final int VC_" .. constant_name(func.cname) .. " = " .. index .. ";")
	else
		local index = #downcall_entries
		table.insert(downcall_entries, { func = func, index = index })
		yield("\tprivate static final int DC_" .. constant_name(func.cname) .. " = " .. index .. ";")
		yield("\tprivate static final FunctionDescriptor FD_" .. constant_name(func.cname)
			.. " = " .. function_descriptor(func) .. ";")
	end
end

emit_downcall_table = function()
	yield("")
	yield("\tprivate static MethodHandle[] linkAll(SymbolLookup lookup) {")
	yield("\t\tMethodHandle[] handles = new MethodHandle[" .. #downcall_entries .. "];")
	for _, entry in ipairs(downcall_entries) do
		local name = constant_name(entry.func.cname)
		yield("\t\thandles[DC_" .. name .. "] = downcall(lookup, \"bgfx_"
			.. entry.func.cname .. "\", FD_" .. name .. ");")
	end
	yield("\t\treturn handles;")
	yield("\t}")
	yield("")
	yield("\tprivate static MemorySegment[] linkVariadicSymbols(SymbolLookup lookup) {")
	yield("\t\tMemorySegment[] addresses = new MemorySegment[" .. #variadic_entries .. "];")
	for _, entry in ipairs(variadic_entries) do
		local name = constant_name(entry.func.cname)
		yield("\t\taddresses[VC_" .. name .. "] = symbol(lookup, \"bgfx_"
			.. entry.func.cname .. "\");")
	end
	yield("\t\treturn addresses;")
	yield("\t}")
end

function converter.funcs(params)
	local func = params.obj
	if not should_emit_function(func) then
		return
	end
	if params.asMethod then
		emit_regular_wrapper(func, params.methodIndent, true)
		return
	end

	if not func.this then
		if func.vararg then
			emit_variadic_wrapper(func, "\t")
		else
			emit_regular_wrapper(func, "\t", false)
		end
		yield("")
	end
	emit_native_descriptor(func)
end

function gen.write(codes, outputfile)
	local out = assert(io.open(outputfile, "wb"))
	out:write(codes)
	out:close()
	print("Generating: " .. outputfile)
end

if (...) == nil then
	-- run `lua bindings-java.lua` in command line
	print(gen.gen())
end

return gen
