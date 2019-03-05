package com.god.jungin.ualarmaws;

//알람 리스트에서 알람을 제거 애니메이션 및 동작
public interface ItemMoveListener {
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemRemove(int position);

}
