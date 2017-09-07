package com.example.lcweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 2017/9/4.
 */

//对于复数的forecast只需要单体类，在集合声明数组即可
public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {

        public String max;

        public String min;
    }

    public class More {

        //????
        @SerializedName("txt_d")
        public String info;
    }
}
