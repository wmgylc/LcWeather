package com.example.lcweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 2017/9/4.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;

    }
}
