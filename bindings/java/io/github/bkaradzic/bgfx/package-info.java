// Copyright 2011-2026 Branimir Karadzic. All rights reserved.
// License: https://github.com/bkaradzic/bgfx/blob/master/LICENSE

/**
 * Modern Java Foreign Function and Memory API bindings for the bgfx C99 API.
 * <p>
 * Native entry points are exposed as static methods on {@link io.github.bkaradzic.bgfx.BGFX}.
 * Call {@link io.github.bkaradzic.bgfx.BGFX#load(java.nio.file.Path)},
 * {@link io.github.bkaradzic.bgfx.BGFX#load(String)}, or
 * {@link io.github.bkaradzic.bgfx.BGFX#link()} before invoking them.
 * <p>
 * Native structures can be allocated with
 * constructors accepting a {@link java.lang.foreign.SegmentAllocator}. Their
 * lifetime is governed by the allocator used to create them. Handles are
 * immutable Java records.
 * <p>
 * This package is null-marked. Java {@code null} is accepted only where a
 * type use is explicitly annotated with
 * {@link org.jspecify.annotations.Nullable}; a native null pointer represented
 * by {@link java.lang.foreign.MemorySegment#NULL} is still a non-null Java value.
 */
@NullMarked
package io.github.bkaradzic.bgfx;

import org.jspecify.annotations.NullMarked;
