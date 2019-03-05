package com.god.jungin.ualarmaws;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.god.jungin.ualarmaws.R;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class TelListFragment extends Fragment implements ListFragmentAdapter.OnClickItem{


    RecyclerView mRecyclerView;
    ListFragmentAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    ArrayList<TelItem> data=new ArrayList<TelItem>();

    OnClickFragment mOnClickFragment;
    GetClickedTel mGetClickedTel;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mOnClickFragment=(OnClickFragment) activity;
            mGetClickedTel=(GetClickedTel)activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()+ " must implements OnClickOutSide");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        data=getTelList();

        Log.e("fragment","start");


        View view= inflater.inflate(R.layout.listfragment_layout,container,false);

        mRecyclerView=(RecyclerView)view.findViewById(R.id.telListReView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager=new LinearLayoutManager(getActivity());
        mRecyclerView.scrollToPosition(0);
        mAdapter=new ListFragmentAdapter(this);
        mAdapter.addItem(data);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        view.findViewById(R.id.lf_Linear).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnClickFragment.onClickOutSide(true);
                    }
                }
        );




        return view;
    }

    //주소록에서 전화번호와 이름을 가져오는 메소드
    public ArrayList<TelItem> getTelList(){

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] rawData=new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        };
        String[] selectionArgs=null;
        String sortOrder=ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        Cursor cursor = getContext().getContentResolver().query(uri,rawData, null, selectionArgs, sortOrder);
        LinkedHashSet<TelItem> hashList= new LinkedHashSet<>();

        if(cursor.moveToFirst()){

            do{
                TelItem item = new TelItem();
                item.setTel(cursor.getString(0));
                item.setName(cursor.getString(1));

                hashList.add(item);
            }while(cursor.moveToNext());

        }


        return new ArrayList<>(hashList);
    }

    //리사이클뷰 어댑터에서 onClickItem으로 받은 TelItem을 다시 AlarmSet에 보냄.
    @Override
    public void onClickItem(TelItem item) {
        mGetClickedTel.getClickedTel(item);
    }

    //Activity와 통신하기 위한 인터페이스
    //attch에서 Activity에 구현을 강제함.
    //true일 시, Fragment를 숨김
    public interface OnClickFragment{
        public void onClickOutSide(boolean clicked);

    }

    //AlarmSet에 TelItem을 보내기 위한 인터페이스
    public interface GetClickedTel{
        public void getClickedTel(TelItem item);

    }
}
