package com.abdallah.prayertimequran.ui.fragments.prayer

import android.app.Application
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.abdallah.prayertimequran.common.Constants
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class PrayerViewModel(app: Application) : AndroidViewModel(app) {
    val job: Job = Job()
    val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(getApplication())
    val allPrayerLiveData: MutableLiveData<Map<String, String?>> = MutableLiveData()
    val allPrayerLiveDataByLong: MutableLiveData<Map<String, Long>> = MutableLiveData()
    private var allPrayerLiveDataByLongCallBack: ((Map<String, Long> )-> Unit) ? = null
    private val mapOfPrayers = mutableMapOf<String, String?>()
    private val mapOfPrayersInLong = mutableMapOf<String, Long>()

    val remainingTime: MutableLiveData<Map<String, Long>> = MutableLiveData()
    private val prayersInLong: MutableLiveData<ArrayList<Long>> = MutableLiveData()
    private var prayersInLongCallBack: ((ArrayList<Long> )-> Unit) ? = null


    init {
        getAllPrayerInLongFromShared()

    }

    fun setPrayerTimesInShared(prayerName: String, prayerTime: String) =
        coroutineScope.launch {
            with(sharedPreferences.edit()) {
                putString(prayerName, prayerTime)
                Log.d("test", "the value of  $prayerName is : $prayerTime  ")
                commit() // may error here
            }

        }

    fun checkPermissionsIsToke(): Boolean =
        sharedPreferences.getBoolean(Constants.LOCATION_TOKE, false)


    fun checkTheRangeOfPrayerTimes(lifecycle: LifecycleOwner) {
        prayersInLongCallBack = { prayerTimes ->
            val date = Date().time
            var dateDifference = -1L
            var PrayerName = ""
            if (prayerTimes[0] < date && date < prayerTimes[1]) {
                Log.d("test", "in 1 Range")
                dateDifference = prayerTimes[1] - date
                PrayerName = "الشروق"
            } else if (prayerTimes[1] < date && date < prayerTimes[2]) {
                Log.d("test", "in 2 Range")
                dateDifference = prayerTimes[2] - date
                PrayerName = "الظهر"
            } else if (prayerTimes[2] < date && date < prayerTimes[3]) {
                Log.d("test", "in 3 Range")
                dateDifference = prayerTimes[3] - date
                PrayerName = "العصر"
            } else if (prayerTimes[3] < date && date < prayerTimes[4]) {
                Log.d("test", "in 4 Range")
                dateDifference = prayerTimes[4] - date
                PrayerName = "المغرب"
            } else if (prayerTimes[4] < date && date < prayerTimes[5]) {
                Log.d("test", "in 5 Range")
                dateDifference = prayerTimes[5] - date
                PrayerName = "العشاء"
            }

            // before 12 AM
            else if ((prayerTimes[5]) < date && date < prayerTimes[0] + 86400000) {
                Log.d("test", "in 6 Range")
                dateDifference = date - (prayerTimes[0] + 86400000)
                PrayerName = "الفجر"
            }
            //after 12 AM
            else if ((prayerTimes[5] - 86400000) < date && date < prayerTimes[0]) {
                Log.d("test", "in 6 Range")
                dateDifference = date- prayerTimes[0]
                PrayerName = "الفجر"
            } else {
                Log.d("test", "didnt found range")

            }

            if (dateDifference < 0) {
                dateDifference *= -1
            }


            remainingTime.value = mapOf(PrayerName to dateDifference)

        }
    }


    // new fun with long values by the day in the date formatting

    fun checkTimeToStartTimerByLongValues(fragmentActivity: FragmentActivity) {
//        Log.d("test", "Start checkTimeToStartTimer")

        allPrayerLiveDataByLongCallBack= { map ->
            val fjr = map[Constants.FJR]
            val sunrise = map[Constants.SUNRISE]
            val douhr = map[Constants.DOHR]
            val asr = map[Constants.ASR]
            val maghreb = map[Constants.MAGHREB]
            val isha = map[Constants.ISHA]
//            Log.d("test", "checkTimeToStartTimer preyerViewModel.class")

//            prayersInLong.value = arrayListOf(fjr!!, sunrise!!, douhr!!, asr!!, maghreb!!, isha!!)

            prayersInLongCallBack?.let { it(arrayListOf(fjr!!, sunrise!!, douhr!!, asr!!, maghreb!!, isha!!)) }
        }

    }

    fun getAllPrayerInLongFromShared() =
        coroutineScope.launch {
//            Log.d("test" , "getAllPrayerInLongFromShared PrayerViewModel.class")
            val fajr = getLongFromShared(Constants.FJR)
            val sunrise = getLongFromShared(Constants.SUNRISE)
            val duhr = getLongFromShared(Constants.DOHR)
            val asr = getLongFromShared(Constants.ASR)
            val maghreb = getLongFromShared(Constants.MAGHREB)
            val isha = getLongFromShared(Constants.ISHA)
            mapOfPrayersInLong[Constants.FJR] = fajr
            mapOfPrayersInLong[Constants.SUNRISE] = sunrise
            mapOfPrayersInLong[Constants.DOHR] = duhr
            mapOfPrayersInLong[Constants.ASR] = asr
            mapOfPrayersInLong[Constants.MAGHREB] = maghreb
            mapOfPrayersInLong[Constants.ISHA] = isha
            allPrayerLiveDataByLong.postValue(mapOfPrayersInLong)
            withContext(Dispatchers.Main) {


                allPrayerLiveDataByLongCallBack?.let { it(mapOfPrayersInLong) }
            }

        }

    fun getPrayerTimeFromShared(prayerName: String, defultValue: String = "00:00 AM"): String? =
        sharedPreferences.getString(prayerName, defultValue)

    fun getLongFromShared(varName: String, defaultValue: Long = 0L): Long =
        sharedPreferences.getLong(varName, defaultValue)

}

