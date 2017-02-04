package com.wps.csvexcel.view.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.wps.csvexcel.R;
import com.wps.csvexcel.bean.Cell;
import com.wps.csvexcel.bean.sheet.filter.FilterMultValue;

/**
 * @author w_chenxiaoxuan
 */
public class FilterDataAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private FilterMultValue fmv;
    private boolean haveNull = false;
    private View goneView;
    private static final int DEFAULT_GET_AMOUNT = 20;
    private int amount;
    private boolean findAllCell = false;


    public FilterDataAdapter(Context context, FilterMultValue fmv) {
        inflater = LayoutInflater.from(context);
        this.fmv = fmv;
        goneView = new View(context);
        goneView.setVisibility(View.GONE);
        refreshAmount();
    }

    private void refreshAmount() {
        if (!findAllCell) {
            int end = amount + DEFAULT_GET_AMOUNT;
            for (; amount < end; amount++) {
                if (fmv.getCell(amount) == null) {
                    findAllCell = true;
                    break;
                }
            }
            notifyDataSetChanged();
        }
    }


    @Override
    public int getCount() {
        return amount;
    }

    @Override
    public Object getItem(int position) {
        return fmv.getCell(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(!findAllCell && position == amount-1){
            refreshAmount();
        }
        Cell cell = fmv.getCell(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_filter_content, null);
            TextView tv = (TextView) convertView.findViewById(R.id.tv);
            CheckBox cb = (CheckBox) convertView.findViewById(R.id.cb);
            ViewHolder vh = new ViewHolder();
            vh.setTextView(tv);
            vh.setCheckBox(cb);
            initView(vh, position, cell);
            convertView.setTag(vh);
        } else {
            ViewHolder vh = (ViewHolder) convertView.getTag();
            initView(vh, position, cell);
        }
        return convertView;
    }

    private void initView(ViewHolder vh, int position, Cell cell) {
        if(cell==null){
            throw new IllegalArgumentException("cell was null");
        }
        if (cell.getContent()== null) {
//            haveNull = true;
            vh.getTextView().setText("空白");
        } else {
            vh.getTextView().setText(fmv.getCell(position).getContent());
        }

        if (fmv.isSingleFilter()) {
            vh.getCheckBox().setVisibility(View.INVISIBLE);
        } else {
            vh.getCheckBox().setVisibility(View.VISIBLE);
            if (fmv.isChoice(position)) {
                vh.getCheckBox().setChecked(true);
            } else {
                vh.getCheckBox().setChecked(false);
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
//        haveNull = false;
        super.notifyDataSetChanged();
    }

    private static class ViewHolder {
        private TextView textView;
        private CheckBox checkBox;

        public TextView getTextView() {
            return textView;
        }

        public void setTextView(TextView textView) {
            this.textView = textView;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }

    }

}
