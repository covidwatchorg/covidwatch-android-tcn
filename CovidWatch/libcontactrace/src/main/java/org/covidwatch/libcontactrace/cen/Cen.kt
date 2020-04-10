package org.covidwatch.libcontactrace.cen

/**
 * A CEN or a Contact Event Number holds the arbitrary data that will be
 * exchanged in an interaction between peer-phones using this library, or
 * iPhones/iPads running the iOS counterpart.
 */
interface Cen {
    val data: ByteArray
}
