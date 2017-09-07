package com.example.lcweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lcweather.db.City;
import com.example.lcweather.db.County;
import com.example.lcweather.db.Province;
import com.example.lcweather.util.HttpUtil;
import com.example.lcweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by admin on 2017/9/4.
 * 用于遍历各省市县的数据
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    //province
    private List<Province> provinceList;

    //city
    private List<City> cityList;

    //county
    private List<County> countyList;

    private Province selectedProvince;

    private City selectedCity;

    private int currentLevel;

    //获取控件的实例
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        //目前的datalist是空的？
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //默认情况下就是加载省级资源
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    //获取当前的省份信息，然后加载市级信息
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    //~
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String name = countyList.get(position).getCountyName();
                    Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //如果当前是县，就会返回到市，那么就加载市级信息
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "back", Toast.LENGTH_SHORT).show();
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });

        queryProvinces();
    }

    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear(); //清除之前留下的数据
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            //通知修改listview的数据流
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            //如果本地没有数据
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        //从特定id的city入手
        cityList = DataSupport
                .where("provinceid = ?", String.valueOf(selectedProvince.getId()))
                .find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for(City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        //从特定id的city入手
        countyList = DataSupport
                .where("cityid = ?", String.valueOf(selectedCity.getId()))
                .find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        //注意网络服务都是运行在子线程，需要更新ui则需要回到主线程
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败",  Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if (type.equals("province")) {
                    //查询成功一定要返回true，否则第一次永远不会query数据
                    result = Utility.handleProvinceResponse(responseText);
                } else if (type.equals("city")) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if (type.equals("county")) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) { //获取成功
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //关闭提示窗，同时更新listview数据，涉及ui操作，必须放在主线程
                            closeProgressDialog();
                            if (type.equals("province")) {
                                queryProvinces();
                            } else if (type.equals("city")) {
                                queryCities();
                            } else if (type.equals("county")) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

//package com.example.lcweather;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.lcweather.db.City;
//import com.example.lcweather.db.County;
//import com.example.lcweather.db.Province;
//import com.example.lcweather.util.HttpUtil;
//import com.example.lcweather.util.Utility;
//
//import org.litepal.crud.DataSupport;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.Response;
//
//public class ChooseAreaFragment extends Fragment {
//
//    private static final String TAG = "ChooseAreaFragment";
//
//    public static final int LEVEL_PROVINCE = 0;
//
//    public static final int LEVEL_CITY = 1;
//
//    public static final int LEVEL_COUNTY = 2;
//
//    private ProgressDialog progressDialog;
//
//    private TextView titleText;
//
//    private Button backButton;
//
//    private ListView listView;
//
//    private ArrayAdapter<String> adapter;
//
//    private List<String> dataList = new ArrayList<>();
//
//    /**
//     * 省列表
//     */
//    private List<Province> provinceList;
//
//    /**
//     * 市列表
//     */
//    private List<City> cityList;
//
//    /**
//     * 县列表
//     */
//    private List<County> countyList;
//
//    /**
//     * 选中的省份
//     */
//    private Province selectedProvince;
//
//    /**
//     * 选中的城市
//     */
//    private City selectedCity;
//
//    /**
//     * 当前选中的级别
//     */
//    private int currentLevel;
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.choose_area, container, false);
//        titleText = (TextView) view.findViewById(R.id.title_text);
//        backButton = (Button) view.findViewById(R.id.back_button);
//        listView = (ListView) view.findViewById(R.id.list_view);
//        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
//        listView.setAdapter(adapter);
//        return view;
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (currentLevel == LEVEL_PROVINCE) {
//                    selectedProvince = provinceList.get(position);
//                    queryCities();
//                } else if (currentLevel == LEVEL_CITY) {
//                    selectedCity = cityList.get(position);
//                    queryCounties();
//                }
//            }
//        });
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (currentLevel == LEVEL_COUNTY) {
//                    queryCities();
//                } else if (currentLevel == LEVEL_CITY) {
//                    queryProvinces();
//                }
//            }
//        });
//        queryProvinces();
//    }
//
//    /**
//     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
//     */
//    private void queryProvinces() {
//        titleText.setText("中国");
//        backButton.setVisibility(View.GONE);
//        provinceList = DataSupport.findAll(Province.class);
//        if (provinceList.size() > 0) {
//            dataList.clear();
//            for (Province province : provinceList) {
//                dataList.add(province.getProvinceName());
//            }
//            adapter.notifyDataSetChanged();
//            listView.setSelection(0);
//            currentLevel = LEVEL_PROVINCE;
//        } else {
//            String address = "http://guolin.tech/api/china";
//            queryFromServer(address, "province");
//        }
//    }
//
//    /**
//     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
//     */
//    private void queryCities() {
//        titleText.setText(selectedProvince.getProvinceName());
//        backButton.setVisibility(View.VISIBLE);
//        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
//        if (cityList.size() > 0) {
//            dataList.clear();
//            for (City city : cityList) {
//                dataList.add(city.getCityName());
//            }
//            adapter.notifyDataSetChanged();
//            listView.setSelection(0);
//            currentLevel = LEVEL_CITY;
//        } else {
//            int provinceCode = selectedProvince.getProvinceCode();
//            String address = "http://guolin.tech/api/china/" + provinceCode;
//            queryFromServer(address, "city");
//        }
//    }
//
//    /**
//     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
//     */
//    private void queryCounties() {
//        titleText.setText(selectedCity.getCityName());
//        backButton.setVisibility(View.VISIBLE);
//        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
//        if (countyList.size() > 0) {
//            dataList.clear();
//            for (County county : countyList) {
//                dataList.add(county.getCountyName());
//            }
//            adapter.notifyDataSetChanged();
//            listView.setSelection(0);
//            currentLevel = LEVEL_COUNTY;
//        } else {
//            int provinceCode = selectedProvince.getProvinceCode();
//            int cityCode = selectedCity.getCityCode();
//            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
//            queryFromServer(address, "county");
//        }
//    }
//
//    /**
//     * 根据传入的地址和类型从服务器上查询省市县数据。
//     */
//    private void queryFromServer(String address, final String type) {
//        showProgressDialog();
//        HttpUtil.sendOkHttpRequest(address, new Callback() {
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String responseText = response.body().string();
//                boolean result = false;
//                if ("province".equals(type)) {
//                    result = Utility.handleProvinceResponse(responseText);
//                } else if ("city".equals(type)) {
//                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
//                } else if ("county".equals(type)) {
//                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
//                }
//                if (result) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            closeProgressDialog();
//                            if ("province".equals(type)) {
//                                queryProvinces();
//                            } else if ("city".equals(type)) {
//                                queryCities();
//                            } else if ("county".equals(type)) {
//                                queryCounties();
//                            }
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                // 通过runOnUiThread()方法回到主线程处理逻辑
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        closeProgressDialog();
//                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//    }
//
//    /**
//     * 显示进度对话框
//     */
//    private void showProgressDialog() {
//        if (progressDialog == null) {
//            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setMessage("正在加载...");
//            progressDialog.setCanceledOnTouchOutside(false);
//        }
//        progressDialog.show();
//    }
//
//    /**
//     * 关闭进度对话框
//     */
//    private void closeProgressDialog() {
//        if (progressDialog != null) {
//            progressDialog.dismiss();
//        }
//    }
//
//}