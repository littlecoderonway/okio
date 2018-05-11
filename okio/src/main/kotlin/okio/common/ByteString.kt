/*
 * Copyright (C) 2018 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package okio.common

import okio.ByteString
import okio.and
import okio.arrayRangeEquals
import okio.arraycopy
import okio.hashCode

// TODO Kotlin's expect classes can't have default implementations, so platform implementations
// have to call these functions. Remove all this nonsense when expect class allow actual code.

internal fun ByteString.commonToAsciiLowercase(): ByteString {
  // Search for an uppercase character. If we don't find one, return this.
  var i = 0
  while (i < data.size) {
    var c = data[i]
    if (c < 'A'.toByte() || c > 'Z'.toByte()) {
      i++
      continue
    }

    // This string is needs to be lowercased. Create and return a new byte string.
    val lowercase = data.copyOf()
    lowercase[i++] = (c - ('A' - 'a')).toByte()
    while (i < lowercase.size) {
      c = lowercase[i]
      if (c < 'A'.toByte() || c > 'Z'.toByte()) {
        i++
        continue
      }
      lowercase[i] = (c - ('A' - 'a')).toByte()
      i++
    }
    return ByteString(lowercase)
  }
  return this
}

internal fun ByteString.commonToAsciiUppercase(): ByteString {
  // Search for an lowercase character. If we don't find one, return this.
  var i = 0
  while (i < data.size) {
    var c = data[i]
    if (c < 'a'.toByte() || c > 'z'.toByte()) {
      i++
      continue
    }

    // This string is needs to be uppercased. Create and return a new byte string.
    val lowercase = data.copyOf()
    lowercase[i++] = (c - ('a' - 'A')).toByte()
    while (i < lowercase.size) {
      c = lowercase[i]
      if (c < 'a'.toByte() || c > 'z'.toByte()) {
        i++
        continue
      }
      lowercase[i] = (c - ('a' - 'A')).toByte()
      i++
    }
    return ByteString(lowercase)
  }
  return this
}

internal fun ByteString.commonSubstring(beginIndex: Int, endIndex: Int): ByteString {
  require(beginIndex >= 0) { "beginIndex < 0" }
  require(endIndex <= data.size) { "endIndex > length(${data.size})" }

  val subLen = endIndex - beginIndex
  require(subLen >= 0) { "endIndex < beginIndex" }

  if (beginIndex == 0 && endIndex == data.size) {
    return this
  }

  val copy = ByteArray(subLen)
  arraycopy(data, beginIndex, copy, 0, subLen)
  return ByteString(copy)
}

internal fun ByteString.commonGetByte(pos: Int) = data[pos]

internal fun ByteString.commonGetSize() = data.size

internal fun ByteString.commonToByteArray() = data.copyOf()

internal fun ByteString.commonInternalArray() = data

internal fun ByteString.commonRangeEquals(
  offset: Int,
  other: ByteString,
  otherOffset: Int,
  byteCount: Int
): Boolean = other.rangeEquals(otherOffset, this.data, offset, byteCount)

internal fun ByteString.commonRangeEquals(
  offset: Int,
  other: ByteArray,
  otherOffset: Int,
  byteCount: Int
): Boolean {
  return (offset >= 0 && offset <= data.size - byteCount
      && otherOffset >= 0 && otherOffset <= other.size - byteCount
      && arrayRangeEquals(data, offset, other, otherOffset, byteCount))
}

internal fun ByteString.commonStartsWith(prefix: ByteString) =
    rangeEquals(0, prefix, 0, prefix.size)

internal fun ByteString.commonStartsWith(prefix: ByteArray) =
    rangeEquals(0, prefix, 0, prefix.size)

internal fun ByteString.commonEndsWith(suffix: ByteString) =
    rangeEquals(size - suffix.size, suffix, 0, suffix.size)

internal fun ByteString.commonEndsWith(suffix: ByteArray) =
    rangeEquals(size - suffix.size, suffix, 0, suffix.size)

internal fun ByteString.commonEquals(other: Any?): Boolean {
  return when {
    other === this -> true
    other is ByteString -> other.size == data.size && other.rangeEquals(0, data, 0, data.size)
    else -> false
  }
}

internal fun ByteString.commonHashCode(): Int {
  val result = hashCode
  if (result != 0) return result
  hashCode = hashCode(data)
  return hashCode
}

internal fun ByteString.commonCompareTo(other: ByteString): Int {
  val sizeA = size
  val sizeB = other.size
  var i = 0
  val size = minOf(sizeA, sizeB)
  while (i < size) {
    val byteA = this[i] and 0xff
    val byteB = other[i] and 0xff
    if (byteA == byteB) {
      i++
      continue
    }
    return if (byteA < byteB) -1 else 1
  }
  if (sizeA == sizeB) return 0
  return if (sizeA < sizeB) -1 else 1
}

internal val COMMON_HEX_DIGITS =
    charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

internal val COMMON_EMPTY = ByteString.of()

internal fun commonOf(vararg data: Byte) = ByteString(data.copyOf())

internal fun String.commonDecodeHex(): ByteString {
  require(length % 2 == 0) { "Unexpected hex string: ${this}" }

  val result = ByteArray(length / 2)
  for (i in result.indices) {
    val d1 = decodeHexDigit(this[i * 2]) shl 4
    val d2 = decodeHexDigit(this[i * 2 + 1])
    result[i] = (d1 + d2).toByte()
  }
  return ByteString(result)
}

private fun decodeHexDigit(c: Char): Int {
  return when (c) {
    in '0'..'9' -> c - '0'
    in 'a'..'f' -> c - 'a' + 10
    in 'A'..'F' -> c - 'A' + 10
    else -> throw IllegalArgumentException("Unexpected hex digit: $c")
  }
}
