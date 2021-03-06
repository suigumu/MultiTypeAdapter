# MultiTypeAdapter

###### 它最大的特点就是不需要你继承Adapter并重写Adapter中的方法。大大的节省你的开发时间提高你的开发效率。
* * *

## 简介
    针对RecyclerVeiw的适配器的封装，可以令你简单优雅的实现一些常用功能。
    例如：多条目列表、悬浮列表、分页加载等，以及各种针对条目的事件的监听。
## 下载
###### 第一步：添加 JitPack 仓库到你项目根目录的 gradle 文件中。
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
###### 第二步：添加这个依赖。
```
dependencies {
    compile 'com.github.kelinZhou:MultiTypeAdapter:1.0.1'
}
```
## 效果 & 实现
#### 单条目列表
![loadMore](materials/gif_single_type_list.gif)
###### 实现代码
```
private SingleTypeAdapter<Person, ManHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("单条目列表");
        setContentView(R.layout.include_common_list_layout);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new SingleTypeAdapter<>(recyclerView, ManHolder.class);
        recyclerView.setAdapter(mAdapter);
        loadData();
    }

    private void loadData() {
        DataHelper.getInstance().getPersons().subscribe(new Action1<List<Person>>() {
            @Override
            public void call(List<Person> persons) {
                mAdapter.setDataList(persons);
                mAdapter.notifyRefresh();
            }
        });
    }
```
#### 多条目列表
![loadMore](materials/gif_multi_type_list.gif)
###### 实现代码
```
    private MultiTypeAdapter mMultiTypeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("多条目列表");
        setContentView(R.layout.include_common_list_layout);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mMultiTypeAdapter = new MultiTypeAdapter(recyclerView, 2);  //构建一个最多可将屏幕分为两份的多类型适配器。
        recyclerView.setAdapter(mMultiTypeAdapter);
        loadData();  //加载数据
    }

    private void loadData() {
        //模拟从网络获取数据。
        DataHelper.getInstance().getManAndWoman().subscribe(new Action1<People>() {
            @Override
            public void call(People people) {
                ItemAdapter<Integer> titleAdapter; //用来加载显示头的子适配器。
                ItemAdapter<Person> personAdapter; //用来显示条目的适配器
                //创建女生的头的子适配器。
                titleAdapter = new ItemAdapter<Integer>(CommonImageHolder.class, people.getWomanListImage());
                //创建用来显示女生列表的子适配器。
                personAdapter = new ItemAdapter<Person>(people.getWomanList(), 1, ManHolder2.class);
                //将两个子适配器添加到多类型适配器中。
                mMultiTypeAdapter.addAdapter(titleAdapter, personAdapter);

                //在创建一个男生的头的子适配器。
                titleAdapter = new ItemAdapter<Integer>(CommonImageHolder.class, people.getManListImage());
                //在创建一个用来显示男生列表的子适配器。
                personAdapter = new ItemAdapter<Person>(people.getManList(), 2, ManHolder.class);
                //将两个子适配器添加到多类型适配器中。
                mMultiTypeAdapter.addAdapter(titleAdapter, personAdapter);

                //刷新列表
                mMultiTypeAdapter.notifyRefresh();
            }
        });
    }
```
#### 悬浮吸顶条目列表
![loadMore](materials/gif_float_list.gif)
###### 代码实现
布局文件
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
             android:background="@color/white"
             android:layout_width="match_parent"
             android:layout_height="match_parent">

    <android.support.v7.widget.RecyclerView
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

    <!--要实现悬浮必须在RecyclerView的相同节点放置一个FloatLayout控件，
        并且一般情况下你不需要findViewById操作，也不需要指定尺寸及Visibility。-->
    <com.kelin.recycleradapter.FloatLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>

</FrameLayout>
```
Activity中的代码
```
    private MultiTypeAdapter mMultiTypeAdapter;
    private RecyclerView mRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("悬浮条列表");

        setContentView(R.layout.activity_float_list);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mMultiTypeAdapter = new MultiTypeAdapter(mRecyclerView);
        mRecyclerView.setAdapter(mMultiTypeAdapter);
        loadData();
    }

    private void loadData() {
        //加载数据。
        DataHelper.getInstance().getClassList().subscribe(new Action1<List<Classs>>() {
            @Override
            public void call(List<Classs> classses) {
                FloatItemAdapter<Classs> adapter;
                for (final Classs classs : classses) {
                    //构建一个用来显示班级的悬浮子Adapter。
                    adapter = new FloatItemAdapter<Classs>(ClassHolder.class, classs);
                    //设置条目事件监听。
                    adapter.setItemEventListener(new SuperItemAdapter.OnItemEventListener<Classs>() {
                        //当条目被点击。
                        @Override
                        public void onItemClick(int position, Classs o, int adapterPosition) {
                            Snackbar.make(mRecyclerView, "条目被点击：position=" + position + "|class=" + o.getClassName(), 2000).show();
                        }
                        //当条目被长按
                        @Override
                        public void onItemLongClick(int position, Classs o, int adapterPosition) {
                            Snackbar.make(mRecyclerView, "条目被长按：position=" + position + "|class=" + o.getClassName(), 2000).show();
                        }
                        //当条目中的子控件被点击
                        @Override
                        public void onItemChildClick(int position, Classs o, View view, int adapterPosition) {
                            if (view.getId() == R.id.tv_show_more) {
                                Snackbar.make(mRecyclerView, "您点击了显示更多：position=" + position + "|class=" + o.getClassName(), 2000).show();
                            }
                        }
                    });
                    //将子Adapter添加到多类型Adapter中。
                    mMultiTypeAdapter.addAdapter(adapter, new ItemAdapter<Person>(classs.getStudents(), ManHolder.class));
                }
                //刷新列表
                mMultiTypeAdapter.notifyRefresh();
            }
        });
    }
```
ViewHolder中的代码
```
@ItemLayout(R.layout.item_class_title_layout)
public class ClassHolder extends ItemViewHolder<Classs> {

    protected ClassHolder(View itemView) {
        super(itemView);
    }

    /**
     * 绑定数据的时候调用。
     *
     * @param position 当前的Item索引。
     * @param classs   当前索引对应的数据对象。
     */
    @Override
    public void onBindData(int position, Classs classs) {
        setText(R.id.tv_class_name, classs.getClassName());
        setText(R.id.tv_count, String.format(Locale.CHINA, "%d 人", classs.getCount()));
    }

    @Override
    public void onBindFloatLayoutData(ViewHelper viewHelper, Classs classs) {
        viewHelper.setText(R.id.tv_class_name, classs.getClassName());
        viewHelper.setText(R.id.tv_count, String.format(Locale.CHINA, "%d 人", classs.getCount()));
    }

    @Override
    public int[] onGetNeedListenerChildViewIds() {
        return new int[]{R.id.tv_show_more};
    }
}
```
*讲解：* 正如你所看到的，实现悬浮效果一共就三步：

1. 在xml布局文件中添加一个*FloatLayout*控件。
2. 创建*子Adapter*的时候使用*FloatItemAdapter*。
3. 在ViewHolder中Overwrite```public void onBindFloatLayoutData(ViewHelper viewHelper, Object object)```方法。
实现上面三步你就可以轻松实现悬浮效果，悬浮条的点击事件会在*OnItemEventListener*的回调，无需对悬浮条目的点击事件进行单独处理，只需要处理你的ViewHolder就可以了。
#### 分页加载
![loadMore](materials/gif_load_more_list.gif)
###### 实现代码
```
    /**
     * 定义每页的数量。
     */
    public static final int PAGE_SIZE = 10;
    /**
     * 记录当前需要加载的页数。
     */
    private int mPage = 1;
    private MultiTypeAdapter mMultiTypeAdapter;
    private ItemAdapter<Person> mItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("分页加载");
        setContentView(R.layout.include_common_list_layout);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mMultiTypeAdapter = new MultiTypeAdapter(recyclerView);
        recyclerView.setAdapter(mMultiTypeAdapter);
        //添加一个头条目。
        mMultiTypeAdapter.addAdapter(new ItemAdapter<Integer>(CommonImageHolder.class, R.mipmap.img_common_title));
        //创建用来显示内容的子条目。
        mItemAdapter = new ItemAdapter<>(ManHolder.class);
        //将子条目添加到多类型Adapter中。
        mMultiTypeAdapter.addAdapter(mItemAdapter);
        //设置加载更多的布局，有重载方法。这里用的是五个参数的，前三个分别是 加载中、点击重试和没有更多数据时显示的布局文件。
        //第四个参数是偏移值，就是在最后一个条目之前的第几个条目被显示时触发加载更多，最后一个是加载更多的回调。
        mMultiTypeAdapter.setLoadMoreView(R.layout.layout_load_more, R.layout.layout_load_more_failed, R.layout.layout_no_more_data, 1, new MultiTypeAdapter.OnLoadMoreListener() {

            @Override
            public void onLoadMore() {
                //模拟耗时操作。
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData(++mPage, PAGE_SIZE);
                    }
                }, 500);
            }

            @Override
            public void onReloadMore() {
                //模拟耗时操作。
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData(mPage, PAGE_SIZE);
                    }
                }, 500);
            }
        });
        loadData(mPage, PAGE_SIZE);
    }

    private void loadData(int page, int pageSize) {
        //加载数据
        DataHelper.getInstance().getPersons(page, pageSize).subscribe(new Subscriber<List<Person>>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {
                if (mMultiTypeAdapter.getItemCount() <= 10) {
                    //设置LoadMore不可用，因为如果当前总数据不足以显示一屏幕则会出现一只显示加载中却总也触发不了LoadMore的bug。
                    mMultiTypeAdapter.setLoadMoreUsable(false);
                } else {
                    //设置加载更多可用。
                    mMultiTypeAdapter.setLoadMoreUsable(true);
                    //设置加载更多完成。
                    mMultiTypeAdapter.setLoadMoreFailed();
                }
            }

            @Override
            public void onNext(List<Person> persons) {
                //将获取到的数据添加到子适配其中，这个addAll也是有重载方法的，这里调用的是一个参数的方法，这个方法默认会在添加数据后刷新列表。
                mItemAdapter.addAll(persons);
                //判断是否是最后一页了，一般情况下如果服务器给的数据是空的或者给的数据量不满足我们请求的pageSize就认为是最后一页了。
                if (persons.isEmpty() || persons.size() < PAGE_SIZE) {
                    mMultiTypeAdapter.setNoMoreData(); //设置没有更多数据了。
                } else {
                    mMultiTypeAdapter.setLoadMoreFinished();  //设置加载更多完成。
                }
            }
        });
    }
```
#### EmptyView列表
![empty](materials/gif_empty_view_list.gif)
###### 实现代码
```
    private SingleTypeAdapter<Person, ManHolder> mAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("EmptyView列表");
        setContentView(R.layout.include_common_list_layout);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new SingleTypeAdapter<>(recyclerView, ManHolder.class);
        mAdapter.setEmptyView(R.layout.layout_common_empty_layout);  //设置列表为空要显示的布局ID。
        recyclerView.setAdapter(mAdapter);
    }

    private void loadData() {
        //加载数据
        DataHelper.getInstance().getPersons().subscribe(new Action1<List<Person>>() {
            @Override
            public void call(List<Person> persons) {
                mAdapter.setDataList(persons);
                mAdapter.notifyRefresh(); //刷新列表
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //添加两个menu用来操作清空列表和加载数据。
        getMenuInflater().inflate(R.menu.menu_load, menu);
        getMenuInflater().inflate(R.menu.menu_clear, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_load:  //如果点击了加载数据则进行加载数据。
                loadData();
                break;
            case R.id.menu_clear:  //如果点击了清空列表则进行清空列表。
                mAdapter.clear();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
```
#### Drag&Swiped
![drag](materials/gif_drag_list.gif)
###### 实现代码
```
    private RecyclerView mRecyclerView;
    private MultiTypeAdapter mMultiTypeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Drag&Swiped列表");
        setContentView(R.layout.include_common_list_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mMultiTypeAdapter = new MultiTypeAdapter(mRecyclerView, 2);  //构建一个最多可将屏幕分为两份的多类型适配器。
        //设置move和swiped可用，并监听拖拽结果。
        mMultiTypeAdapter.setItemDragEnable(true, true, new ItemDragResultListener<Object>() {
            @Override
            public void onItemMoved(int fromPosition, int toPosition, Object o) {
                Person object = (Person) o;
                Spanned html = Html.fromHtml("将 <font color=\"#1682FB\">" + object.getName() + "</font> 从位置：" + fromPosition + " 移动到了 " + toPosition);
                Snackbar.make(mRecyclerView, html, 2000).show();
            }

            @Override
            public void onItemDismissed(int position, Object o) {
                Person object = (Person) o;
                Spanned html = Html.fromHtml("将 <font color=\"#DC554C\">" + object.getName() + "</font> 从位置：" +  + position + " 删除了");
                Snackbar.make(mRecyclerView, html, 2000).show();
            }
        });
        mRecyclerView.setAdapter(mMultiTypeAdapter);
        loadData();  //加载数据
    }

    private void loadData() {
        //模拟从网络获取数据。
        DataHelper.getInstance().getManAndWoman().subscribe(new Action1<People>() {
            @Override
            public void call(People people) {
                ItemAdapter<Integer> titleAdapter; //用来加载显示头的子适配器。
                ItemAdapter<Person> personAdapter; //用来显示条目的适配器
                //创建女生的头的子适配器。
                titleAdapter = new ItemAdapter<Integer>(CommonImageHolder.class, people.getWomanListImage());
                //创建用来显示女生列表的子适配器。
                personAdapter = new ItemAdapter<Person>(people.getWomanList(), 2, DragManHolder.class);
                //将两个子适配器添加到多类型适配器中。
                mMultiTypeAdapter.addAdapter(titleAdapter, personAdapter);

                //在创建一个男生的头的子适配器。
                titleAdapter = new ItemAdapter<Integer>(CommonImageHolder.class, people.getManListImage());
                //在创建一个用来显示男生列表的子适配器。
                personAdapter = new ItemAdapter<Person>(people.getManList(), 1, DragManHolder2.class);
                //将两个子适配器添加到多类型适配器中。
                mMultiTypeAdapter.addAdapter(titleAdapter, personAdapter);

                //刷新列表
                mMultiTypeAdapter.notifyRefresh();
            }
        });
    }
```

* * *
### License
```
Copyright 2016 kelin410@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
