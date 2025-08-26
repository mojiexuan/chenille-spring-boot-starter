package com.chenjiabao.open.chenille;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 分布式环境可使用的随机字符串生成工具
 * 具备以下特点：
 * -全局唯一
 * -短小紧凑
 * -有序递增
 * @author 陈佳宝 mail@chenjiabao.com
 */
public class SnowflakeUtils {

    // 起始时间戳 2025-1-1 00:00:00
    private static final long EPOCH = 1735660800000L;
    // 机器ID位数，10位，最多支持1024台机器
    private static final long MACHINE_ID_BITS = 10L;
    // 序列位数，12位，每毫秒最多生成4096个ID
    private static final long SEQUENCE_BITS = 12L;

    private final long machineId;
    private final long sequenceMask;

    private final AtomicLong lastTimestamp = new AtomicLong(-1L);
    private final AtomicLong sequence = new AtomicLong(0L);
    // Base62字符表(0-9、A-Z、a-z)
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public SnowflakeUtils(long machineId) {
        long maxMachineId = (1L << MACHINE_ID_BITS) - 1;
        if (machineId < 0 || machineId > maxMachineId) {
            throw new IllegalArgumentException("设备ID必须在0~" + maxMachineId);
        }
        this.machineId = machineId;
        this.sequenceMask = (1L << SEQUENCE_BITS) - 1;
    }

    /**
     * 生成唯一ID
     * @return Base62字符串
     */
    public String nextId() {
        long timestamp = System.currentTimeMillis();
        long currentSequence;

        synchronized (this) {
            if(timestamp < lastTimestamp.get()) {
                throw new RuntimeException("时间回退，拒绝生成ID");
            }

            if(timestamp == lastTimestamp.get()) {
                currentSequence = sequence.incrementAndGet() & sequenceMask;
                if(currentSequence == 0){
                    timestamp = waitNextMillis(timestamp);
                }
            }else {
                sequence.set(0);
                currentSequence = 0;
            }
            lastTimestamp.set(timestamp);
        }

        long id = ((timestamp - EPOCH) << (MACHINE_ID_BITS + SEQUENCE_BITS))
                | (machineId << SEQUENCE_BITS)
                | currentSequence;

        return toBase62(id);
    }

    /**
     * 将长整型转为Base62字符串
     * @param value 值
     * @return Base62字符串
     */
    private String toBase62(long value) {
        StringBuilder sb = new StringBuilder();
        do{
            sb.insert(0,BASE62.charAt((int)(value % 62)));
            value /= 62;
        }while(value > 0);
        return sb.toString();
    }

    /**
     * 等待下一毫秒
     */
    private long waitNextMillis(long currentTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= currentTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

}
