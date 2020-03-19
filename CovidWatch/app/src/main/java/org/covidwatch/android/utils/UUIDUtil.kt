package org.covidwatch.android.utils

import java.nio.ByteBuffer
import java.util.*

fun UUID.toBytes(): ByteArray {
    val b = ByteBuffer.wrap(ByteArray(16))
    b.putLong(mostSignificantBits)
    b.putLong(leastSignificantBits)
    return b.array()
}

fun ByteArray.toUUID(): UUID? {
    val byteBuffer = ByteBuffer.wrap(this)
    val high = byteBuffer.long
    val low = byteBuffer.long
    return UUID(high, low)
}