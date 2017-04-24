package com.kaka.btdemo.recyclerview;

import android.view.ViewGroup;

import com.kaka.btdemo.R;
import com.kaka.recyclerviewlib.base.BaseDelegate;
import com.kaka.recyclerviewlib.base.BaseViewHolder;
import com.kaka.recyclerviewlib.mode.ItemData;

/**
 * Created by hrx on 2017/4/23.
 */

public class HolderDelegate extends BaseDelegate<ItemData> {
    public static final int HEADER_TYPE = 1;
    public static final int DEVICE_TYPE = 2;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case HEADER_TYPE:
                return new HeaderViewHolder(viewGroup, getItemView(viewGroup, viewType));
            case DEVICE_TYPE:
                return new DeviceViewHolder(viewGroup, getItemView(viewGroup, viewType));
        }
        return null;
    }

    @Override
    public int getItemViewType(ItemData itemData) {
        return itemData.holderType;
    }

    @Override
    public int getLayoutId(int viewType) {
        switch (viewType) {
            case HEADER_TYPE:
                return R.layout.item_header;
            case DEVICE_TYPE:
                return R.layout.item_device;
        }
        return 0;
    }
}
