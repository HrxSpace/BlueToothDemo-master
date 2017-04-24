package com.kaka.btdemo.recyclerview;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaka.btdemo.R;
import com.kaka.recyclerviewlib.base.BaseViewHolder;

/**
 * Created by hrx on 2017/4/23.
 */

public class HeaderViewHolder extends BaseViewHolder {
    private TextView title;
    private TextView type;

    public HeaderViewHolder(ViewGroup parent, View itemView) {
        super(parent, itemView);
    }

    @Override
    public void findViews() {
        title = (TextView) itemView.findViewById(R.id.item_header_title);
        type = (TextView) itemView.findViewById(R.id.item_header_type);
    }

    @Override
    public void onBindViewHolder(Object o) {

    }

    @Override
    public boolean clickAble() {
        return false;
    }
}
