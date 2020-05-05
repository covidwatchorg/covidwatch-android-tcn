package org.covidwatch.android.ui

import me.tatarka.bindingcollectionadapter2.ItemBinding
import org.covidwatch.android.BR
import org.covidwatch.android.R
import org.covidwatch.android.data.CovidExposureInformation

object ItemBindings {

    @JvmStatic
    fun bind(): ItemBinding<CovidExposureInformation> =
        ItemBinding.of(
            BR.item,
            R.layout.item_exposure_info
        )
}