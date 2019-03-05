package com.god.jungin.ualarmaws;

import android.content.Context;
import android.telephony.TelephonyManager;

import static android.content.Context.TELEPHONY_SERVICE;

/*
*  서버에 클라이언트를 특정하기 위한 핸드폰 번호
* */
public class PhoneNum {
    String getPhoneNum(Context mContext) {

        TelephonyManager telManager = (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);

        String PhoneNum=null;


        try{
            PhoneNum = telManager.getLine1Number();
            if (PhoneNum.startsWith("+82")) {
                PhoneNum = PhoneNum.replace("+82", "0");
            }

        }catch (SecurityException e){
            e.printStackTrace();
        }finally {
            return PhoneNum;
        }

    }
}
