package org.covidwatch.libcontactrace.cen

/**
 * CENVisitor
 *
 * The visitor that will be accepted on every GeneratedCEN and ObservedCEN.
 * Custom routines to run on ObservedCENs or GeneratedCENs must be implemented
 * in a class that implements this visitor.
 *
 */
interface CenVisitor {
    fun visit(cen: GeneratedCen)
    fun visit(cen: ObservedCen)
}