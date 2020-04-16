package org.covidwatch.android.data.contactevent

/**
 * Enumeration of all possible infection states of a person
 */
enum class InfectionState(val isPotentiallyInfectious: Boolean) {
    PotentiallyInfectious(true),
    Healthy(false)
}