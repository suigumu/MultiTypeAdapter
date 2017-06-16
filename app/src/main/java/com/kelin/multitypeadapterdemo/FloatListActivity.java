package com.kelin.multitypeadapterdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kelin.multitypeadapterdemo.holder.Type1Holder;
import com.kelin.multitypeadapterdemo.holder.Type2Holder;
import com.kelin.recycleradapter.ItemAdapter;
import com.kelin.recycleradapter.MultiTypeAdapter;
import com.kelin.recycleradapter.view.FloatLayout;

/**
 * 描述 多条目列表的页面。
 * 创建人 kelin
 * 创建时间 2016/12/6  下午6:23
 * 版本 v 1.0.0
 */
public class FloatListActivity extends AppCompatActivity {

    private MultiTypeAdapter mMultiTypeAdapter;
    private FloatLayout mFloatLayout;

    /**
     * 启动自身，可通过其他Activity调用此方法来启动MultiTypeListActivity。
     *
     * @param activityContext 需要一个上下文Activity作为参数，一般把Activity.this作为参数即可。
     */
    public static void startAction(Activity activityContext) {
        activityContext.startActivity(new Intent(activityContext, FloatListActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("悬浮条列表");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_float_list);
        mFloatLayout = (FloatLayout) findViewById(R.id.fl_float_layout);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mMultiTypeAdapter = new MultiTypeAdapter(recyclerView, 1);
        mMultiTypeAdapter.setFloatLayout(mFloatLayout);
        recyclerView.setAdapter(mMultiTypeAdapter);
        loadData();
    }

    private void loadData() {
        for (int i = 0; i < 100; i++) {
            ItemAdapter<String> adapter;
            if (i % 2 == 0) {
                ItemAdapter<String> itemAdapter = new ItemAdapter<>(Type1Holder.class);
                itemAdapter.addItem("A类型条目" + i + "-0");
                itemAdapter.addItem("A类型条目" + i + "-1");
                itemAdapter.addItem("A类型条目" + i + "-2");
                itemAdapter.addItem("A类型条目" + i + "-3");
                itemAdapter.addItem("A类型条目" + i + "-4");
                adapter = itemAdapter;
            } else {
                ItemAdapter<String> itemAdapter = new ItemAdapter<>(Type2Holder.class);
                itemAdapter.addItem("B类型条目" + i);
                itemAdapter.setFloatAble(true);
                adapter = itemAdapter;
            }
            adapter.setItemEventListener(new ItemAdapter.OnItemEventListener<String>() {
                @Override
                public void onItemClick(int position, String s, int adapterPosition) {
                    Toast.makeText(getApplicationContext(), "条目点击position=" + position + "|s=" + s, Toast.LENGTH_SHORT).show();
                    getAdapter().addItem(adapterPosition, "我是新增条目" + position);
                }

                @Override
                public void onItemLongClick(int position, String s, int adapterPosition) {
                    Log.i("onItemLongClick", "position=" + position + " | s=" + s + " | adapterPosition=" + adapterPosition);
                }

                @Override
                public void onItemChildClick(int position, String s, View view, int adapterPosition) {
                    Toast.makeText(getApplicationContext(), "子控件position=" + position + "|s=" + s, Toast.LENGTH_SHORT).show();
                    getAdapter().removeItem(adapterPosition);
                }
            });
            mMultiTypeAdapter.addAdapter(adapter);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}