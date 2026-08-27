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
 * Renderer statistics data.
 * <p>
 * <strong>Remarks:</strong> All time values are high-resolution timestamps, while
 * time frequencies define timestamps-per-second for that hardware.
 */
@NullMarked
public final class Stats extends NativeObject {
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
	 * CPU time between two {@code frame} calls.
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
