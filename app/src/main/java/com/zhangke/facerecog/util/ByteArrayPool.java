package com.zhangke.facerecog.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 用来缓存与重用 byte[] 对象以防止大量 I/O 操作带来的内存消耗。
 * Created by ZhangKe on 2017/11/8.
 */

public class ByteArrayPool {
    private final List<byte[]> mBuffersByLastUse = new LinkedList<byte[]>();
    private final List<byte[]> mBuffersBySize = new ArrayList<byte[]>(64);

    private int mCurrentSize = 0;

    private final int mSizeLimit;

    protected static final Comparator<byte[]> BUF_COMPARATOR = new Comparator<byte[]>() {
        @Override
        public int compare(byte[] lhs, byte[] rhs) {
            return lhs.length - rhs.length;
        }
    };

    public ByteArrayPool(int sizeLimit) {
        mSizeLimit = sizeLimit;
    }

    public synchronized byte[] getBuf(int len) {
        for (int i = 0; i < mBuffersBySize.size(); i++) {
            byte[] buf = mBuffersBySize.get(i);
            if (buf.length >= len) {
                mCurrentSize -= buf.length;
                mBuffersBySize.remove(i);
                remove(mBuffersByLastUse, buf);
                return buf;
            }
        }
        return new byte[len];
    }

    private void remove(List<byte[]> byteList, byte[] buf) {
        int position = -1;
        for (int i = 0; i < byteList.size(); i++) {
            byte[] bytes = byteList.get(i);
            if (bytes.length == buf.length) {
                position = i;
                break;
            }
        }
        if (position >= 0 && position < byteList.size()) {
            byteList.remove(position);
        }
    }

    public synchronized void returnBuf(byte[] buf) {
        if (buf == null || buf.length > mSizeLimit) {
            return;
        }
        mBuffersByLastUse.add(buf);
        int pos = Collections.binarySearch(mBuffersBySize, buf, BUF_COMPARATOR);
        if (pos < 0) {
            pos = -pos - 1;
        }
        mBuffersBySize.add(pos, buf);
        mCurrentSize += buf.length;
        trim();
    }

    private synchronized void trim() {
        while (mCurrentSize > mSizeLimit) {
            byte[] buf = mBuffersByLastUse.remove(0);
            remove(mBuffersBySize, buf);
            mCurrentSize -= buf.length;
        }
    }
}
