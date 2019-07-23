package com.bt.sdk;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BtValue {
    //mValue可以是String, int, list或Map
    private final Object mValue;

    public BtValue(byte[] mValue) {
        this.mValue = mValue;
    }

    public BtValue(String mValue) throws UnsupportedEncodingException {
        this.mValue = mValue.getBytes("UTF-8");
    }

    public BtValue(String mValue, String enc) throws UnsupportedEncodingException {
        this.mValue = mValue.getBytes(enc);
    }

    public BtValue(int mValue) {
        this.mValue = mValue;
    }

    public BtValue(long mValue) {
        this.mValue = mValue;
    }

    public BtValue(Number mValue) {
        this.mValue = mValue;
    }

    public BtValue(List<BtValue> mValue) {
        this.mValue = mValue;
    }

    public BtValue(Map<String, BtValue> mValue) {
        this.mValue = mValue;
    }

    public Object getValue() {
        return this.mValue;
    }

    /**
     * 将BtValue作为String返回, 使用UTF-8进行编码
     */
    public String getString() throws InvalidBtEncodingException {
        return getString("UTF-8");

    }

    public String getString(String encoding) throws InvalidBtEncodingException {
        try {
            return new String(getBytes(), encoding);
        } catch (ClassCastException e) {
            throw new InvalidBtEncodingException(e.toString());
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * 将Btvalue对象作为byte[]数组返回
     */
    public byte[] getBytes() throws InvalidBtEncodingException {
        try {
            return (byte[]) mValue;
        } catch (ClassCastException e) {
            throw new InvalidBtEncodingException(e.toString());
        }
    }

    /**
     * 将BtValue对象作为数字返回
     */
    public Number getNumber() throws InvalidBtEncodingException {
        try {
            return (Number) mValue;
        } catch (ClassCastException e) {
            throw new InvalidBtEncodingException(e.toString());
        }
    }

    /**
     * 将BtValue对象作为short返回
     */
    public short getShort() throws InvalidBtEncodingException {
        return getNumber().shortValue();
    }

    /**
     * 将BtValue对象作为int返回
     */
    public int getInt() throws InvalidBtEncodingException {
        return getNumber().intValue();
    }

    /**
     * 将BtValue对象作为long返回
     */
    public long getLong() throws InvalidBtEncodingException {
        return getNumber().longValue();
    }

    /**
     * 将BtValue对象作为List返回
     */
    @SuppressWarnings("unchecked")
    public List<BtValue> getList() throws InvalidBtEncodingException {
        if (mValue instanceof ArrayList) {
            return (ArrayList<BtValue>) mValue;
        } else {
            throw new InvalidBtEncodingException("Excepted List<BtValue> !");
        }
    }

    /**
     * 将BtValue对象作为Map返回
     */
    @SuppressWarnings("unchecked")
    public Map<String, BtValue> getMap() throws InvalidBtEncodingException {
        if (mValue instanceof HashMap) {
            return (Map<String, BtValue>) mValue;
        } else {
            throw new InvalidBtEncodingException("Expected Map<String, BtValue> !");
        }
    }
}