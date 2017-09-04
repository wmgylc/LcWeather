package com.example.lcweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by admin on 2017/9/4.
 */

public class County extends DataSupport {

    private int id;

    private String countyName;

    private int countyCode;

    private int cityId;

    public int getId() {
        return id;
    }

    public String getCountyName() {
        return countyName;
    }

    public int getCountyCode() {
        return countyCode;
    }

    public int getCityId() {
        return cityId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCountyName(String cityName) {
        this.countyName = cityName;
    }

    public void setCountyCode(int cityCode) {
        this.countyCode = cityCode;
    }

    public void setCityId(int provinceId) {
        this.cityId = provinceId;
    }
}
