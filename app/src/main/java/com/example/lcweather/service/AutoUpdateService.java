package com.example.lcweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaActionSound;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.lcweather.WeatherActivity;
import com.example.lcweather.gson.Weather;
import com.example.lcweather.util.HttpUtil;
import com.example.lcweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //int anHour = 1 * 60 * 60 * 1000;
        int anHour = 5 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent1, 0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String weatherId = prefs.getString("weather_id", null);
        String Url = "http://guolin.tech/api/weather?city=" +
                weatherId + "&key=d2aef8bddce74235b61a5bc06ca1b61b";

        HttpUtil.sendOkHttpRequest(Url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(AutoUpdateService.this, "failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && weather.status.equals("ok")) {
                    /*只是更新了editor数据，这样只有在weatherActivity onCreate的时候才会
                     *调用editor的数据更新天气，然而应该在onResume的时候更新天气
                     */
                    Toast.makeText(AutoUpdateService.this, "succeed", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(AutoUpdateService.this)
                            .edit();
                    Log.d("WeatherActivity", responseText);
                    editor.putString("weather", responseText);
                    editor.putBoolean("weatherChange", true);
                    editor.apply();
                }
            }
        });
    }

}
