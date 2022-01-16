package com.power.powerBattery;

public class ValueHolder {

    private static ValueHolder single_instance = null;
    public long reward;

    private ValueHolder() {
        reward = 0;
    }

    public static ValueHolder getInstance() {
        if (single_instance == null)
            single_instance = new ValueHolder();
        return single_instance;
    }
}
