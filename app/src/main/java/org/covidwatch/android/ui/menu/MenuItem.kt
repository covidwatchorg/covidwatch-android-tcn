package org.covidwatch.android.ui.menu

data class MenuItem(
    val title: Int,
    val iconEnd: Int,
    val destination: Destination
)

sealed class Destination

object Settings : Destination()
object TestResults : Destination()
class Browser(val url: String) : Destination()