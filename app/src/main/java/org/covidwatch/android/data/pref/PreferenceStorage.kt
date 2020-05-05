package org.covidwatch.android.data.pref

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.covidwatch.android.data.CovidExposureSummary
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Storage for app and user preferences.
 */
interface PreferenceStorage {
    var lastFetchDate: Long
    var exposureSummary: CovidExposureSummary?
    val observableExposureSummary: LiveData<CovidExposureSummary?>
}

class SharedPreferenceStorage(context: Context) : PreferenceStorage {
    private val prefs = context.applicationContext.getSharedPreferences(NAME, MODE_PRIVATE)
    private val _exposureSummary = MutableLiveData<CovidExposureSummary>()

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            EXPOSURE_SUMMARY -> _exposureSummary.value = exposureSummary
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(changeListener)
    }

    override var lastFetchDate by Preference(prefs, LAST_FETCH_DATE, 0L)

    override var exposureSummary: CovidExposureSummary? by NullablePreference(
        prefs,
        EXPOSURE_SUMMARY,
        null
    )

    override val observableExposureSummary: LiveData<CovidExposureSummary?>
        get() = _exposureSummary.also { it.value = exposureSummary }

    companion object {
        private const val NAME = "ag_minimal_prefs"
        private const val LAST_FETCH_DATE = "last_fetch_date"
        private const val EXPOSURE_SUMMARY = "exposure_summary"
    }
}

open class NullablePreference<T>(
    private val preferences: SharedPreferences,
    private val name: String,
    private val defaultValue: T?
) : ReadWriteProperty<Any, T?> {

    private var value: T? = null

    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        return value ?: preferences.get(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        this.value = value
        preferences.put(name, value)
    }
}

class Preference<T>(
    preferences: SharedPreferences,
    name: String,
    defaultValue: T
) : NullablePreference<T>(preferences, name, defaultValue) {
    override fun getValue(thisRef: Any, property: KProperty<*>) =
        super.getValue(thisRef, property)!!
}

@Suppress("UNCHECKED_CAST")
fun <T> SharedPreferences.get(name: String, defaultValue: T? = null): T? {
    return when (defaultValue) {
        is Boolean -> getBoolean(name, defaultValue) as T
        is Float -> getFloat(name, defaultValue) as T
        is Long -> getLong(name, defaultValue) as T
        is Int -> getInt(name, defaultValue) as T
        is String -> getString(name, defaultValue) as T
        else -> defaultValue
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> SharedPreferences.put(name: String, value: T) {
    val editor = edit()

    when (value) {
        is Boolean -> editor.putBoolean(name, value)
        is Float -> editor.putFloat(name, value)
        is Long -> editor.putLong(name, value)
        is Int -> editor.putInt(name, value)
        is String -> editor.putString(name, value)
    }
    editor.apply()
}