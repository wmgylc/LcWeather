package com.example.lcweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    //fragment是基于MainActivity的
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean reChoose = getIntent().getBooleanExtra("reChoose", false);
        if (reChoose == false) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (preferences.getString("weather", null) != null) {
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
                finish();
                /*Bug：会直接跳转到WeatherActivity而不会改变查询天气
                 *原因：再次选择城市的时候，weatherString不为null，因而在WeatherActivity中会直接进入情况二，
                 *然而此时的weatherString是原来的天气，而不是新选择的天气
                 *      Bug fixed.
                 *      直接在选择的时候将weatherString重置为null
                 */
            }
        }
    }

}
