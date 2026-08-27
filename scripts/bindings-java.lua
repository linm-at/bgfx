local codegen = require "codegen"
local idl = codegen.idl "bgfx.idl"

-- TODO: Add JSpecify annoations/vendor JSpecify

local java_package = "io.github.bkaradzic.bgfx"

local java_header = [[
// Copyright 2011-2026 Branimir Karadzic. All rights reserved.
// License: https://github.com/bkaradzic/bgfx/blob/master/LICENSE


//
// AUTO GENERATED! DO NOT EDIT!
//
]]

local java_template = java_header .. [[

package ]] .. java_package .. [[;

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

import ]] .. java_package .. [[.util.FFMUtil;

import static ]] .. java_package .. [[.util.FFMUtil.*;


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
		return java_package .. "." .. typ.namespace:lower() .. "." .. typ.name
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
local downcall_entries
local variadic_entries
local emit_downcall_table

local function reset_generator_state()
	for _, name in ipairs(combined) do
		combined[name] = {}
	end
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

local function generate_object(what, object, funcs)
	local tmp = {}
	local co = coroutine.create(converter[what])
	while true do
		local ok, value = coroutine.resume(co, {
			obj = object,
			funcs = funcs,
			topLevel = what == "types",
		})
		assert(ok, debug.traceback(co, value))
		if not value then
			break
		end
		table.insert(tmp, value)
	end
	return table.concat(tmp, "\n")
end

local function generate_function_section()
	local tmp = {}
	for _, object in ipairs(idl.funcs) do
		local body = generate_object("funcs", object)
		if body ~= "" then
			table.insert(tmp, body)
		end
	end
	local co = coroutine.create(emit_downcall_table)
	while true do
		local ok, value = coroutine.resume(co)
		assert(ok, debug.traceback(co, value))
		if not value then
			break
		end
		table.insert(tmp, value)
	end
	return table.concat(tmp, "\n")
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
	line = line:gsub("bgfx::", "")
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

local function FlagBlock(typ, root_indent)
	root_indent = root_indent or ""
	local body_indent = root_indent .. "\t"
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
	emit_javadoc(typ.comments or { "Constants for " .. typ.name .. " flags." }, root_indent)
	yield(root_indent .. "public final class " .. name .. " {")
	yield(body_indent .. "private " .. name .. "() {")
	yield(body_indent .. "}")

	for idx, flag in ipairs(typ.flag) do
		local flagName = flag.name:gsub("_", "")
		-- make 2d/3d upper case 2D/3D
		flagName = flagName:gsub("%dd", flagName.upper);
		if flag.comment ~= nil then
			if idx ~= 1 then
				yield("")
			end
			
			emit_javadoc(flag.comment, body_indent)
		else
			yield("")
			emit_javadoc({ typ.name .. " flag value {@code " .. flagName .. "}." }, body_indent)
		end
		yield(body_indent .. "public static final " .. enumType .. " " .. flagName .. " = " .. string.format(flag.format or format, flag.value) .. ";")
	end

	if typ.shift then
		emit_javadoc({ "Bit shift for this flag group." }, body_indent)
		yield(body_indent .. "public static final " .. enumType .. " Shift = " .. typ.shift .. ";")
	end

	-- generate Mask
	if typ.mask then
		emit_javadoc({ "Bit mask for this flag group." }, body_indent)
		yield(body_indent .. "public static final " .. enumType .. " Mask = " .. string.format(format, typ.mask) .. ";")
	end

	yield(root_indent .. "}")
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

local function emit_enum(typ, root_indent)
	root_indent = root_indent or ""
	local body_indent = root_indent .. "\t"
	emit_javadoc(typ.comments or { typ.typename .. " values." }, root_indent)
	yield(root_indent .. "public enum " .. typ.typename .. " {")
	for _, item in ipairs(typ.enum) do
		if item.comment then
			emit_javadoc(item.comment, body_indent)
		else
			emit_javadoc({ typ.typename .. " value {@code " .. item.name .. "}." }, body_indent)
		end
		yield(body_indent .. item.name .. ",")
	end
	yield("")
	emit_javadoc({ "Number of native enum values." }, body_indent)
	yield(body_indent .. "Count;")
	yield("")
	emit_javadoc({ "Native C enum layout." }, body_indent)
	yield(body_indent .. "public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT;")
	yield(body_indent .. "private static final " .. typ.typename .. "[] VALUES = values();")
	yield("")
	emit_javadoc({ "Returns the enum constant for a native C enum value." }, body_indent,
		{ { name = "value", text = "the native enum value" } }, "the matching enum constant")
	yield(body_indent .. "public static " .. typ.typename .. " fromValue(int value) {")
	yield(body_indent .. "\tif (value >= 0 && value < VALUES.length) {")
	yield(body_indent .. "\t\treturn VALUES[value];")
	yield(body_indent .. "\t}")
	yield(body_indent .. "\tthrow new IllegalArgumentException(\"Unknown " .. typ.typename .. " value: \" + value);")
	yield(body_indent .. "}")
	yield(root_indent .. "}")
end

local function emit_handle(typ, root_indent)
	root_indent = root_indent or ""
	local body_indent = root_indent .. "\t"
	local tagged = typ.tagged ~= nil
	local fields = tagged and "short idx, short type" or "short idx"
	local record_params = { { name = "idx", text = "native handle index" } }
	if tagged then
		table.insert(record_params, { name = "type", text = "native buffer handle tag" })
	end
	emit_javadoc(typ.comments or { "Native bgfx handle." }, root_indent, record_params)
	yield(root_indent .. "public record " .. typ.name .. "(" .. fields .. ") {")
	emit_javadoc({ "Native by-value handle layout." }, body_indent)
	yield(body_indent .. "public static final StructLayout LAYOUT = cStruct(\"" .. typ.cname .. "\",")
	if tagged then
		yield(body_indent .. "\tValueLayout.JAVA_SHORT.withName(\"idx\"),")
		yield(body_indent .. "\tValueLayout.JAVA_SHORT.withName(\"type\"));")
	else
		yield(body_indent .. "\tValueLayout.JAVA_SHORT.withName(\"idx\"));")
	end
	yield(body_indent .. "private static final VarHandle VH_IDX = LAYOUT.varHandle(")
	yield(body_indent .. "\tMemoryLayout.PathElement.groupElement(\"idx\"));")
	if tagged then
		yield(body_indent .. "private static final VarHandle VH_TYPE = LAYOUT.varHandle(")
		yield(body_indent .. "\tMemoryLayout.PathElement.groupElement(\"type\"));")
		emit_javadoc({ "Invalid handle sentinel." }, body_indent)
		yield(body_indent .. "public static final " .. typ.name .. " INVALID =")
		yield(body_indent .. "\tnew " .. typ.name .. "((short) 0xffff, (short) 0xffff);")
	else
		emit_javadoc({ "Invalid handle sentinel." }, body_indent)
		yield(body_indent .. "public static final " .. typ.name .. " INVALID = new " .. typ.name .. "((short) 0xffff);")
	end
	if tagged then
		for index, source in ipairs(typ.tagged) do
			yield("")
			emit_javadoc({ "Creates a tagged buffer handle." }, body_indent,
				{ { name = "handle", text = "the source " .. source } })
			yield(body_indent .. "public " .. typ.name .. "(" .. source .. " handle) {")
			yield(string.format(body_indent .. "\tthis(handle.idx(), (short) %d);", index - 1))
			yield(body_indent .. "}")
		end
	end
	yield("")
	emit_javadoc({ "Returns whether this handle is valid." }, body_indent, nil,
		"{@code true} when the handle index is not {@code UINT16_MAX}")
	yield(body_indent .. "public boolean isValid() {")
	yield(body_indent .. "\treturn idx != (short) 0xffff;")
	yield(body_indent .. "}")
	yield("")
	emit_javadoc({ "Allocates and writes the native by-value handle representation." }, body_indent,
		{ { name = "allocator", text = "the destination allocator" } }, "the allocated native segment")
	yield(body_indent .. "public MemorySegment allocate(SegmentAllocator allocator) {")
	yield(body_indent .. "\tMemorySegment segment = allocator.allocate(LAYOUT);")
	yield(body_indent .. "\twrite(segment);")
	yield(body_indent .. "\treturn segment;")
	yield(body_indent .. "}")
	yield("")
	emit_javadoc({ "Writes this handle to an existing native segment." }, body_indent,
		{ { name = "segment", text = "the destination segment" } })
	yield(body_indent .. "public void write(MemorySegment segment) {")
	yield(body_indent .. "\tsegment = view(segment, LAYOUT);")
	yield(body_indent .. "\tVH_IDX.set(segment, 0L, idx);")
	if tagged then
		yield(body_indent .. "\tVH_TYPE.set(segment, 0L, type);")
	end
	yield(body_indent .. "}")
	yield("")
	emit_javadoc({ "Reads a by-value handle from native memory." }, body_indent,
		{ { name = "segment", text = "the source segment" } }, "the decoded handle")
	yield(body_indent .. "public static " .. typ.name .. " read(MemorySegment segment) {")
	yield(body_indent .. "\tsegment = view(segment, LAYOUT);")
	if tagged then
		yield(body_indent .. "\treturn new " .. typ.name .. "(")
		yield(body_indent .. "\t\t(short) VH_IDX.get(segment, 0L),")
		yield(body_indent .. "\t\t(short) VH_TYPE.get(segment, 0L));")
	else
		yield(body_indent .. "\treturn new " .. typ.name .. "((short) VH_IDX.get(segment, 0L));")
	end
	yield(body_indent .. "}")
	yield(root_indent .. "}")
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

local function emit_funcptr(typ, root_indent)
	root_indent = root_indent or ""
	local body_indent = root_indent .. "\t"
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
	emit_javadoc(typ.comments or { "Native callback." }, root_indent)
	yield(root_indent .. "@FunctionalInterface")
	yield(root_indent .. "@SuppressWarnings(\"restricted\")")
	yield(root_indent .. "public interface " .. typ.name .. " {")
	emit_javadoc({ "Native callback function descriptor." }, body_indent)
	yield(body_indent .. "FunctionDescriptor DESCRIPTOR = " .. descriptor_expression(typ.ret, arg_types) .. ";")
	emit_javadoc({ "Bound callback target used to create upcall stubs." }, body_indent)
	yield(body_indent .. "MethodHandle TARGET = upcallTarget(")
	yield(body_indent .. "\t" .. typ.name .. ".class, \"invoke\", MethodType.methodType(")
	local class_suffix = #method_classes == 0 and "" or ", " .. table.concat(method_classes, ", ")
	yield(body_indent .. "\t\t" .. java_class_literal(ret) .. class_suffix .. "));")
	yield("")
	emit_javadoc({ "Invoked by native bgfx code. Implementations must not throw." }, body_indent, params)
	yield(body_indent .. ret .. " invoke(" .. table.concat(args, ", ") .. ");")
	yield("")
	emit_javadoc({
		"Creates an upcall stub for this callback.",
		"The arena must remain alive until bgfx can no longer invoke the callback.",
	}, body_indent, { { name = "arena", text = "a caller-owned, long-lived arena" } }, "the native function pointer")
	yield(body_indent .. "default MemorySegment upcall(Arena arena) {")
	yield(body_indent .. "\tObjects.requireNonNull(arena, \"arena\");")
	yield(body_indent .. "\treturn LINKER.upcallStub(TARGET.bindTo(this), DESCRIPTOR, arena);")
	yield(body_indent .. "}")
	yield(root_indent .. "}")
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

function converter.types(params)
	local typ = params.obj
	local funcs = params.funcs
	local root_indent = params.topLevel and "" or "\t"
	if typ.args and typ.ret then
		emit_funcptr(typ, root_indent)
	elseif typ.handle then
		emit_handle(typ, root_indent)
	elseif typ.enum then
		emit_enum(typ, root_indent)
	elseif typ.bits ~= nil then
		FlagBlock(typ, root_indent)
	elseif typ.struct ~= nil then
		emit_javadoc(typ.comments or { typ.name .. " native structure." }, root_indent)
		yield(root_indent .. "public final class " .. typ.name .. " extends NativeObject {")
		emit_struct_body(typ, funcs, root_indent .. "\t")
		yield(root_indent .. "}")
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
	yield(body .. "\t" .. return_statement(func, "FFMUtil.invoke(handle, nativeArgs)"))
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
		yield("\tstatic final int DC_" .. constant_name(func.cname) .. " = " .. index .. ";")
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

local function combine_flag_type(target, typ, name)
	target.bits = typ.bits
	target.name = typ.name:match("(%u%l+)")
	local flags = target.flag or {}
	target.flag = flags
	local lookup = target.lookup or {}
	target.lookup = lookup
	for _, flag in ipairs(typ.flag) do
		local flag_name = name .. flag.name:gsub("_", "")
		local value = flag.value
		if value == nil then
			value = 0
			for _, part in ipairs(flag) do
				value = value | assert(lookup[name .. part], part .. " is not defined for " .. flag_name)
			end
		end
		lookup[flag_name] = value
		table.insert(flags, {
			name = flag_name,
			value = value,
			comment = flag.comment,
		})
	end
	if typ.shift then
		table.insert(flags, {
			name = name .. "Shift",
			value = typ.shift,
			format = "%d",
			comment = typ.comment,
		})
	end
	if typ.mask then
		table.insert(flags, {
			name = name .. "Mask",
			value = typ.mask,
			comment = typ.comment,
		})
		lookup[name .. "Mask"] = typ.mask
	end
end

local function prepared_types()
	for _, typ in ipairs(idl.types) do
		if typ.bits ~= nil then
			local prefix, name = typ.name:match("(%u%l+)(.*)")
			if combined[prefix] then
				combine_flag_type(combined[prefix], typ, name)
			end
		end
	end

	local result = {}
	local emitted = {}
	for _, typ in ipairs(idl.types) do
		local output = typ
		if typ.bits ~= nil then
			local prefix = typ.name:match("(%u%l+)")
			if combined[prefix] then
				if emitted[prefix] then
					output = nil
				else
					emitted[prefix] = true
					output = combined[prefix]
				end
			end
		end
		if output then
			table.insert(result, output)
		end
	end
	return result
end

local function generated_type_name(typ)
	if typ.args and typ.ret then
		return typ.name
	elseif typ.handle then
		return typ.name
	elseif typ.enum then
		return typ.typename
	elseif typ.bits ~= nil then
		return typ.name .. "Flags"
	elseif typ.struct ~= nil then
		return typ.name
	end
end

local function type_source(typ, body)
	local package_name = java_package
	if typ.namespace then
		package_name = package_name .. "." .. typ.namespace:lower()
	end
	local imports = [[

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

import ]] .. java_package .. [[.util.NativeObject;

import static ]] .. java_package .. [[.BGFX.*;
import static ]] .. java_package .. [[.util.FFMUtil.*;
]]
	if typ.namespace then
		imports = imports .. "import " .. java_package .. ".*;\n"
	end
	return java_header .. "\npackage " .. package_name .. ";\n" .. imports .. "\n" .. body .. "\n"
end

function gen.files()
	reset_generator_state()
	local methods = collect_methods()
	local files = {}

	for _, typ in ipairs(prepared_types()) do
		local name = generated_type_name(typ)
		if name then
			local body = generate_object("types", typ, methods[typ.cname])
			if body ~= "" then
				local path = (typ.namespace and typ.namespace:lower() .. "/" or "") .. name .. ".java"
				assert(files[path] == nil, "Duplicate Java output: " .. path)
				files[path] = type_source(typ, body)
			end
		end
	end

	local sections = { funcs = generate_function_section() }
	files["Bgfx.java"] = (java_template:gsub("$(%l+)", sections))
	return files
end

function gen.gen()
	return gen.files()
end

local function ensure_directory(path)
	if os.mkdir then
		os.mkdir(path)
		return
	end
	local separator = package.config:sub(1, 1)
	local command
	if separator == "\\" then
		command = 'mkdir "' .. path .. '" >NUL 2>NUL'
	else
		command = 'mkdir -p "' .. path .. '"'
	end
	local ok, _, code = os.execute(command)
	assert(ok or code == 0, "Unable to create directory: " .. path)
end

local function clear_generated_output(outputdir)
	local normalized = outputdir:gsub("\\", "/"):gsub("/+$", "")
	assert(normalized:match("io/github/bkaradzic/bgfx$"),
		"Java output directory must end with io/github/bkaradzic/bgfx: " .. outputdir)

	local separator = package.config:sub(1, 1)
	local command
	if separator == "\\" then
		local escaped = outputdir:gsub("'", "''")
		command = "powershell -NoProfile -Command \"Get-ChildItem -LiteralPath '"
			.. escaped .. "' | Where-Object Name -ne 'util' | Remove-Item -Recurse -Force\""
	else
		command = 'find "' .. outputdir
			.. '" -mindepth 1 -maxdepth 1 ! -name util -exec rm -rf -- {} +'
	end
	local ok, _, code = os.execute(command)
	assert(ok or code == 0, "Unable to clear generated Java output: " .. outputdir)
end

function gen.write(files, outputdir)
	print("Generating: " .. outputdir .. "/**.java")
	ensure_directory(outputdir)
	clear_generated_output(outputdir)
	local names = {}
	for name in pairs(files) do
		table.insert(names, name)
	end
	table.sort(names)
	for _, name in ipairs(names) do
		local directory = name:match("^(.*)/")
		if directory then
			ensure_directory(outputdir .. "/" .. directory)
		end
		local outputfile = outputdir .. "/" .. name
		local out = assert(io.open(outputfile, "wb"))
		out:write(files[name])
		out:close()
	end
end

if (...) == nil then
	-- run `lua bindings-java.lua` in command line
	local files = gen.gen()
	local names = {}
	for name in pairs(files) do
		table.insert(names, name)
	end
	table.sort(names)
	for _, name in ipairs(names) do
		print("// " .. name)
		print(files[name])
	end
end

return gen
