package com.example.safetywalk2.ui;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.safetywalk2.R;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_product_detail);
//
//        // 获取 Deep Link 数据
//        Intent intent = getIntent();
//        Uri data = intent.getData();
//        if (data != null) {
//            String productId = data.getQueryParameter("id"); // 获取参数
//            loadProductDetails(productId); // 加载商品详情
//        }
    }

    private void loadProductDetails(String productId) {
        // 根据 productId 加载商品详情
    }
}
