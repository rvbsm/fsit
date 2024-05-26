package dev.rvbsm.fsit.util

import java.util.*

/**
 * THIS IS FINE.
 * It is unique, assuming that UUIDs have the same standard version :)
 */
operator fun UUID.plus(other: UUID) =
    UUID(mostSignificantBits xor other.mostSignificantBits, leastSignificantBits xor other.leastSignificantBits)
