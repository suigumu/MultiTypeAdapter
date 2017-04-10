package com.kelin.recycleradapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.kelin.recycleradapter.holder.HeaderFooterViewHolder;
import com.kelin.recycleradapter.holder.ItemViewHolder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述 多条目的 {@link RecyclerView} 的适配器。
 * 创建人 kelin
 * 创建时间 2016/12/6  下午6:30
 * 版本 v 1.0.0
 */

public class MultiTypeAdapter extends SupperAdapter<Object, ItemViewHolder<Object>> {

    private static final String HEADER_DATA_FLAG = "com.kelin.recycleradapter.header_data_flag";
    private static final String FOOTER_DATA_FLAG = "com.kelin.recycleradapter.footer_data_flag";
    /**
     * 用来存放不同数据模型的 {@link ItemViewHolder}。不同数据模型的 {@link ItemViewHolder} 只会被存储一份，且是最初创建的那个。
     */
    private Map<Class, ItemViewHolder> mItemViewHolderMap = new HashMap<>();;
    /**
     * 用来存放所有的子条目对象。
     */
    private List<ItemAdapter> mChildAdapters;
    /**
     * 与当前适配器绑定的 {@link RecyclerView} 对象。
     */
    private RecyclerView mRecyclerView;
    /**
     * 用来记录当前的总条目数。
     */
    private int addedItemCount;
    /**
     * 当前 {@link RecyclerView} 的宽度被均分成的份数。
     */
    private int mTotalSpanSize;
    /**
     * 子条目数据变化的观察者。
     */
    private ItemAdapterDataObserver mAdapterDataObserver = new ItemAdapterDataObserver();;

    /**
     * 构建 {@link MultiTypeAdapter} 的工厂类。
     */
    public final static class Factory {

        /**
         * 因为该类是静态工厂类，类中的方法也都是静态的。所以不需要实例化该类。
         */
        private Factory() {
            throw new RuntimeException("MultiTypeAdapter.Factory class Cannot be instantiated");
        }

        /**
         * 绑定 {@link RecyclerView}。
         * <P>初始化适配器并设置布局管理器，您不许要再对 {@link RecyclerView} 设置布局管理器。
         * <p>例如：{@link RecyclerView#setLayoutManager(RecyclerView.LayoutManager)} 方法不应该在被调用，否者可能会出现您不希望看到的效果。
         *
         * @param recyclerView 您要绑定的 {@link RecyclerView} 对象。
         * @return 返回绑定成功的 {@link MultiTypeAdapter} 对象。
         */
        public static MultiTypeAdapter create(@NonNull RecyclerView recyclerView) {
            return create(recyclerView, 1);
        }

        /**
         * 绑定 {@link RecyclerView}。
         * <P>初始化适配器并设置布局管理器，您不许要再对 {@link RecyclerView} 设置布局管理器。
         * <p>例如：{@link RecyclerView#setLayoutManager(RecyclerView.LayoutManager)} 方法不应该在被调用，否者可能会出现您不希望看到的效果。
         *
         * @param recyclerView  您要绑定的 {@link RecyclerView} 对象。
         * @param totalSpanSize 总的占屏比，通俗来讲就是 {@link RecyclerView} 的宽度被均分成了多少份。改值的范围是1~100之间的数(包含)。
         * @return 返回绑定成功的 {@link MultiTypeAdapter} 对象。
         */
        public static MultiTypeAdapter create(@NonNull RecyclerView recyclerView, @Size(min = 1, max = 10000) int totalSpanSize) {
            return new MultiTypeAdapter(recyclerView, totalSpanSize);
        }
    }

    /**
     * 构造方法。
     *
     * @param recyclerView  需要 {@link RecyclerView} 对象。
     * @param totalSpanSize 总的占屏比，通俗来讲就是 {@link RecyclerView} 的宽度被均分成了多少份。
     */
    private MultiTypeAdapter(@NonNull RecyclerView recyclerView, int totalSpanSize) {
        mTotalSpanSize = totalSpanSize < 1 ? 1 : totalSpanSize;
        mRecyclerView = recyclerView;
        mChildAdapters = new ArrayList<>();
        initLayoutManager();
    }

    /**
     * 初始化布局管理器。并设置给 {@link RecyclerView}。
     */
    private void initLayoutManager() {
        RecyclerView recyclerView = mRecyclerView;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(recyclerView.getContext(), getTotalSpanSize()) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) {
                return getItemSpanSize(position);
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    /**
     * 添加条目适配器。
     *
     * @param adapters {@link ItemAdapter} 对象。
     * @see ItemAdapter#ItemAdapter(Class)
     * @see ItemAdapter#ItemAdapter(int, Class)
     * @see ItemAdapter#ItemAdapter(List, Class)
     * @see ItemAdapter#ItemAdapter(List, int, Class)
     */
    public MultiTypeAdapter addAdapter(@NonNull ItemAdapter... adapters) {
        for (ItemAdapter adapter : adapters) {
            adapter.registerObserver(mAdapterDataObserver);
            adapter.firstItemPosition = addedItemCount;
            mChildAdapters.add(adapter);
            addedItemCount += adapter.getItemCount();
            adapter.lastItemPosition = addedItemCount - 1;
            if (adapter.haveHeader()) {
                addData(HEADER_DATA_FLAG);
            }
            addDataList(adapter.getDataList());
            if (adapter.haveFooter()) {
                addData(FOOTER_DATA_FLAG);
            }
            adapter.setParent(this);
        }
        return this;
    }

    @Override
    public int getItemCount() {
        return getDataList().size();
    }

    @Override
    public ItemViewHolder<Object> onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemViewHolder holder = null;
        for (ItemAdapter adapter : mChildAdapters) {
            if (adapter.getItemViewType() == viewType) {
                holder = adapter.onCreateViewHolder(parent, viewType);
                Class itemModelClass = adapter.getItemModelClass();
                if (!mItemViewHolderMap.containsKey(itemModelClass)) {
                    mItemViewHolderMap.put(itemModelClass, holder);
                }
            } else if (adapter.haveHeader() && adapter.getHeaderItemViewType() == viewType) {
                holder = adapter.onCreateHeaderViewHolder(parent, viewType);
            } else if (adapter.haveFooter() && adapter.getFooterItemViewType() == viewType) {
                holder = adapter.onCreateFooterViewHolder(parent, viewType);
            }
            if (holder != null) {
                return holder;
            }
        }
        throw new RuntimeException("viewType not found !");
    }

    @Override
    public void onBindViewHolder(ItemViewHolder<Object> holder, int position) {}

    @Override
    public void onBindViewHolder(ItemViewHolder<Object> holder, int position, List<Object> payloads) {
        if (holder instanceof HeaderFooterViewHolder) return;
        for (int i = 0, total = 0; i < mChildAdapters.size(); i++) {
            ItemAdapter adapter = mChildAdapters.get(i);
            int itemCount = adapter.getItemCount();
            if (position < itemCount + total) {
                adapter.onBindViewHolder(holder, position - total, payloads);
                return;
            } else {
                total += itemCount;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        for (int i = 0, total = 0; i < mChildAdapters.size(); i++) {
            ItemAdapter adapter = mChildAdapters.get(i);
            int itemCount = adapter.getItemCount();
            if (position < itemCount + total) {
                if (adapter.haveHeader() && position == adapter.firstItemPosition) {
                    return adapter.getHeaderItemViewType();
                } else if (adapter.haveFooter() && position == adapter.lastItemPosition) {
                    return adapter.getFooterItemViewType();
                } else {
                    return adapter.getItemViewType();
                }
            } else {
                total += itemCount;
            }
        }
        throw new RuntimeException("not find item type");
    }

    /**
     * 获取总的占屏比，通俗来讲就是获取 {@link RecyclerView} 的宽度被均分成了多少份。
     * <P>该方法的返回值取决于 {@link Factory#create(RecyclerView, int)} 方法中的第二个参数。
     *
     * @return 总的份数。
     * @see Factory#create(RecyclerView, int);
     */
    private int getTotalSpanSize() {
        return mTotalSpanSize;
    }

    /**
     * 根据位置获取条目的占屏值。
     *
     * @param position 当前的位置。
     * @return 返回当前条目的占屏值。
     */
    private int getItemSpanSize(int position) {
        ItemAdapter adapter = getChildAdapterByPosition(position);

        if (adapter == null) return getTotalSpanSize();

        int itemSpanSize = adapter.getItemSpanSize();
        return itemSpanSize == ItemAdapter.SPAN_SIZE_FULL_SCREEN ? getTotalSpanSize() : itemSpanSize;
    }

    /**
     * 根据索引获取对应的子适配器。
     *
     * @param position 当前的索引位置。
     * @return 返回对应的适配器。
     */
    ItemAdapter getChildAdapterByPosition(int position) {
        for (int i = 0, total = 0; i < mChildAdapters.size(); i++) {
            ItemAdapter adapter = mChildAdapters.get(i);
            int itemCount = adapter.getItemCount();
            if (position < itemCount + total) {
                return adapter;
            } else {
                total += itemCount;
            }
        }
        throw new RuntimeException("ItemAdapter not found!");
    }

    /**
     * 通过{@link RecyclerView}的Adapter的position获取{@link ItemAdapter}中对应的position。
     * @param position 当前 {@link RecyclerView} 的position。
     */
    int getItemAdapterPosition(int position) {
        for (int i = 0, total = 0; i < mChildAdapters.size(); i++) {
            ItemAdapter adapter = mChildAdapters.get(i);
            int itemCount = adapter.getItemCount();
            if (position < itemCount + total) {
                return position - total;
            } else {
                total += itemCount;
            }
        }
        throw new RuntimeException("ItemAdapter not found!");
    }

    @Override
    protected boolean areItemsTheSame(Object oldItemData, Object newItemData) {
        if (oldItemData.getClass() != newItemData.getClass()) {
            return false;
        }
        ItemViewHolder viewHolder = getViewHolder(oldItemData.getClass());
        return viewHolder == null || viewHolder.areItemsTheSame(oldItemData, newItemData);
    }

    @Override
    protected boolean areContentsTheSame(Object oldItemData, Object newItemData) {
        if (oldItemData.getClass() != newItemData.getClass()) {
            return false;
        }
        ItemViewHolder viewHolder = getViewHolder(oldItemData.getClass());
        return viewHolder == null || viewHolder.areContentsTheSame(oldItemData, newItemData);
    }

    @Override
    protected void getChangePayload(Object oldItemData, Object newItemData, Bundle bundle) {
        if (oldItemData.getClass() != newItemData.getClass()) {
            return;
        }
        ItemViewHolder viewHolder = getViewHolder(oldItemData.getClass());
        if (viewHolder != null) {
            viewHolder.getChangePayload(oldItemData, newItemData, bundle);
        }
    }

    /**
     * 根据Holder的数据模型类型获取 {@link ItemViewHolder} 对象。
     * @param holderModelClazz {@link ItemViewHolder} 中泛型指定的数据模型的字节码对象。
     * @return 返回 {@link ItemViewHolder} 对象。
     */
    private ItemViewHolder getViewHolder(Class<?> holderModelClazz) {
        return mItemViewHolderMap.get(holderModelClazz);
    }

    private class ItemAdapterDataObserver extends SingleTypeAdapter.AdapterDataObserver {

        @Override
        protected void add(int position, Object o, EditableSupperAdapter adapter) {
            ItemAdapter itemAdapter = (ItemAdapter) adapter;
            getDataList().add(position + itemAdapter.firstItemPosition + itemAdapter.getHeaderCount(), o);
            itemAdapter.lastItemPosition += 1;
            updateFirstAndLastPosition(itemAdapter, 1, true);
        }

        @Override
        protected void addAll(int firstPosition, Collection<Object> dataList, EditableSupperAdapter adapter) {
            ItemAdapter itemAdapter = (ItemAdapter) adapter;
            boolean addAll = getDataList().addAll(firstPosition + itemAdapter.firstItemPosition + itemAdapter.getHeaderCount(), dataList);
            if (addAll) {
                updateFirstAndLastPosition(itemAdapter, dataList.size(), true);
            }
        }

        @Override
        protected void remove(Object o, EditableSupperAdapter adapter) {
            boolean remove = getDataList().remove(o);
            if (remove) {
                updateFirstAndLastPosition((ItemAdapter) adapter, 1, false);
            }
        }

        @Override
        protected void removeAll(Collection<Object> dataList, EditableSupperAdapter adapter) {
            boolean removeAll = getDataList().removeAll(dataList);
            if (removeAll) {
                updateFirstAndLastPosition((ItemAdapter) adapter, dataList.size(), false);
            }
        }

        private void updateFirstAndLastPosition(ItemAdapter adapter, int updateSize, boolean isAdd) {
            if (isAdd) {
                int index = mChildAdapters.indexOf(adapter) + 1;
                adapter.lastItemPosition += updateSize;
                for (int i = index; i < mChildAdapters.size(); i++) {
                    adapter = mChildAdapters.get(i);
                    adapter.firstItemPosition += updateSize;
                    adapter.lastItemPosition += updateSize;
                }
            } else {
                int index = mChildAdapters.indexOf(adapter);
                adapter.lastItemPosition -= updateSize;
                if (adapter.isEmptyList()) {
                    //先删除Footer否则会角标错位。
                    if (adapter.haveFooter()) {
                        Object remove = getDataList().remove(adapter.lastItemPosition);
                        if (remove != null) {
                            updateSize += 1;
                        }
                    }
                    if (adapter.haveHeader()) {
                        Object remove = getDataList().remove(adapter.firstItemPosition);
                        if (remove != null) {
                            updateSize += 1;
                        }
                    }
                    adapter.unregisterAll();
                    mChildAdapters.remove(adapter);
                } else {
                    index += 1;
                }
                for (int i = index; i < mChildAdapters.size(); i++) {
                    adapter = mChildAdapters.get(i);
                    adapter.firstItemPosition -= updateSize;
                    adapter.lastItemPosition -= updateSize;
                }
            }
        }
    }
}