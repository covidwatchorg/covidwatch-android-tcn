package org.covidwatch.android.presentation.menu

data class MenuItem(
    val title: String,
    val iconEnd: Int,
    val destination: Destination
)

sealed class Destination

object Settings : Destination()
object TestResults : Destination()
class Browser(val url: String) : Destination()