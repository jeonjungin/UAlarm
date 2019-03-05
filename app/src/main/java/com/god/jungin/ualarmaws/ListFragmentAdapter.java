package com.god.jungin.ualarmaws;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.god.jungin.ualarmaws.R;

import java.util.ArrayList;


//프래그먼트 안 리사이클 뷰(주소록 창)
public class ListFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<TelItem> data = new ArrayList<TelItem>();

    Context mContext;

    OnClickItem mOnClickItem;

    //프래그먼트에 구현된 OnClickItem 인터페이스 받음
    //프래그먼트엔 onItemCilckLisner가 없음
    ListFragmentAdapter(OnClickItem mOnClickItem){
        this.mOnClickItem=mOnClickItem;
    }

    public static class ListFragmentViewHolder extends RecyclerView.ViewHolder{

        View view;
        TextView tv_name;
        TextView tv_tel;

        ListFragmentViewHolder(View view){
            super(view);
            this.view=view;
            tv_name=(TextView)view.findViewById(R.id.listFragment_name);
            tv_tel=(TextView)view.findViewById(R.id.listFragment_tel);


        }

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listfragment_form, viewGroup, false);

        return new ListFragmentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final int pos=position;
        final ListFragmentViewHolder listFragmentViewHolder = (ListFragmentViewHolder)viewHolder;
        final TelItem item = data.get(pos);

        listFragmentViewHolder.tv_name.setText(item.getName());
        listFragmentViewHolder.tv_tel.setText(item.getTel());

        //아이템 리스너 맵핑
        //onClickItem은 TelListFragment에 정의됨
        ((ListFragmentViewHolder) viewHolder).view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                           mOnClickItem.onClickItem(item);
                    }
                }
        );




    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addItem(ArrayList<TelItem> data){

        if(this.data!=null){
            this.data.clear();
            this.data.addAll(data);

        }else{
            this.data=data;
        }
        notifyDataSetChanged();


    }

    //TelListFragment에 값을 보내기 위함
    public interface OnClickItem{
        public void onClickItem(TelItem item);
    }



}
