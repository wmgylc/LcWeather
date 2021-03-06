package com.example.lcweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by admin on 2017/9/4.
 */

public class Weather {

    public String status;

    public Basic basic;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

}
