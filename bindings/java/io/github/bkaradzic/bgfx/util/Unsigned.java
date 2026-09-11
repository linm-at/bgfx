package io.github.bkaradzic.bgfx.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated integral value is to be interpreted as unsigned.
 *
 * <p>Java has no unsigned {@code byte}, {@code short}, {@code int}, or
 * {@code long} primitive types. The underlying bit representation is stored
 * in the corresponding signed Java primitive and interpreted as unsigned
 * where appropriate.
 *
 * @see Byte#toUnsignedInt(byte)
 * @see Short#toUnsignedInt(short)
 * @see Integer#toUnsignedLong(int)
 * @see Integer#compareUnsigned(int, int)
 * @see Long#compareUnsigned(long, long)
 */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.CLASS)
public @interface Unsigned {}
