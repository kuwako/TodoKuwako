package com.todo.kuwako.todokuwako.contract;


import java.util.List;

/**
 * Created by m_kuwako on 2016/11/27.
 */

public final class PreferenceContract {
    public static class Vibrate {
        public static final long[] SHORT = {0, 500, 300, 1000};
        public static final long[] NORMAL = {0, 1000, 300, 2000};
        public static final long[] LONG = {0, 5000, 300, 5000};
        public static final long[] LONG_AND_STRONG = {0, 120000};
        public static final long[][] TYPES = {
            SHORT, NORMAL, LONG, LONG_AND_STRONG
        };
    }
}
