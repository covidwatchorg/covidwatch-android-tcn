package org.covidwatch.android.requirements.test_environment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentHostCallback
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelStore
import io.mockk.every
import io.mockk.mockk

class FragmentTestBed<T: Fragment>(val fragment: T) {
    init {
        val baseFragment = fragment as Fragment
        baseFragment["mHost"] = mockk<FragmentHostCallback<*>>(relaxed = false)

        val viewModelStore = ViewModelStore()

        val fragmentManagerMock = mockk<FragmentManager>(relaxed = false)
        every { fragmentManagerMock["isStateAtLeast"](1) } returns true
        every { fragmentManagerMock["getViewModelStore"](baseFragment) } returns viewModelStore

        baseFragment["mChildFragmentManager"] = fragmentManagerMock
        baseFragment["mFragmentManager"] = fragmentManagerMock

        fragment.onCreate(null)
    }
}