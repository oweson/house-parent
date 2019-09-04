package com.mooc.house.common.constants;

public enum HouseUserType {
    /**
     * 1 售卖
     */
    SALE(1),
    /**
     * 2 收藏
     */
    BOOKMARK(2);

    public final Integer value;

    HouseUserType(Integer value) {
        this.value = value;
    }
}
