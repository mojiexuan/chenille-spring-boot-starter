package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.model.property.ChenilleTime;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类（默认使用毫秒级时间戳）
 * @author ChenJiaBao
 */
public class ChenilleTimeUtils {

    private String zoneId = "Asia/Shanghai";
    private final DateTimeFormatter defaultFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of(zoneId));

    public ChenilleTimeUtils(ChenilleTime time){
        zoneId = time.getZoneId();
    }

    /* ========== 秒级与毫秒级转换 ========== */

    /**
     * 秒级时间戳转毫秒级
     * @param seconds 秒级时间戳
     * @return 毫秒级时间戳
     */
    public long toMillis(long seconds) {
        return seconds * 1000;
    }

    /**
     * 毫秒级时间戳转秒级
     * @param millis 毫秒级时间戳
     * @return 秒级时间戳
     */
    public long toSeconds(long millis) {
        return millis / 1000;
    }

    /* ========== 毫秒级时间戳操作 ========== */

    /**
     * 时间字符串转毫秒级时间戳
     * @param time 时间字符串
     * @param format 时间格式
     * @return 毫秒级时间戳
     */
    public long parseMillis(String time, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
        return dateTime.atZone(ZoneId.of(zoneId)).toInstant().toEpochMilli();
    }

    /**
     * 获取当前毫秒级时间戳
     * @return 毫秒级时间戳
     */
    public long currentMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 毫秒级时间戳转时间字符串
     * @param millis 毫秒级时间戳
     * @param format 时间格式
     * @return 格式化后的时间字符串
     */
    public String formatMillis(long millis, String format) {
        return DateTimeFormatter.ofPattern(format)
                .withZone(ZoneId.of(zoneId))
                .format(Instant.ofEpochMilli(millis));
    }

    /**
     * 毫秒级时间戳转默认格式时间字符串
     * @param millis 毫秒级时间戳
     * @return yyyy-MM-dd HH:mm:ss 格式字符串
     */
    public String formatMillis(long millis) {
        return defaultFormatter.format(Instant.ofEpochMilli(millis));
    }

    /**
     * 获取当天00:00:00的毫秒级时间戳
     * @return 毫秒级时间戳
     */
    public long todayStartMillis() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.of(zoneId))
                .toInstant()
                .toEpochMilli();
    }

    /* ========== 秒级时间戳操作（基于毫秒级转换） ========== */

    /**
     * 时间字符串转秒级时间戳
     * @param time 时间字符串
     * @param format 时间格式
     * @return 秒级时间戳
     */
    public long parseSeconds(String time, String format) {
        return toSeconds(parseMillis(time, format));
    }

    /**
     * 获取当前秒级时间戳
     * @return 秒级时间戳
     */
    public long currentSeconds() {
        return toSeconds(currentMillis());
    }

    /**
     * 秒级时间戳转时间字符串
     * @param seconds 秒级时间戳
     * @param format 时间格式
     * @return 格式化后的时间字符串
     */
    public String formatSeconds(long seconds, String format) {
        return formatMillis(toMillis(seconds), format);
    }

    /**
     * 秒级时间戳转默认格式时间字符串
     * @param seconds 秒级时间戳
     * @return yyyy-MM-dd HH:mm:ss 格式字符串
     */
    public String formatSeconds(long seconds) {
        return formatMillis(toMillis(seconds));
    }

    /**
     * 获取当天00:00:00的秒级时间戳
     * @return 秒级时间戳
     */
    public long todayStartSeconds() {
        return toSeconds(todayStartMillis());
    }

    /* ========== 其他实用方法 ========== */

    /**
     * 计算时间差(毫秒)
     * @param timestamp1 时间戳1
     * @param timestamp2 时间戳2
     * @return 时间差绝对值(毫秒)
     */
    public long diffMillis(long timestamp1, long timestamp2) {
        return Math.abs(timestamp1 - timestamp2);
    }

    /**
     * 计算时间差(秒)
     * @param timestamp1 时间戳1(秒级)
     * @param timestamp2 时间戳2(秒级)
     * @return 时间差绝对值(秒)
     */
    public long diffSeconds(long timestamp1, long timestamp2) {
        return diffMillis(toMillis(timestamp1), toMillis(timestamp2)) / 1000;
    }

    /**
     * 判断是否是工作日
     * @param millis 毫秒级时间戳
     * @return 是否工作日
     */
    public boolean isWeekday(long millis) {
        DayOfWeek day = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.of(zoneId))
                .getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    /**
     * 添加指定毫秒数
     * @param millis 原始时间戳(毫秒)
     * @param millisecondsToAdd 要添加的毫秒数
     * @return 新时间戳
     */
    public long plusMillis(long millis, long millisecondsToAdd) {
        return Instant.ofEpochMilli(millis)
                .plusMillis(millisecondsToAdd)
                .toEpochMilli();
    }

    /**
     * 添加指定秒数
     * @param seconds 原始时间戳(秒)
     * @param secondsToAdd 要添加的秒数
     * @return 新时间戳(秒)
     */
    public long plusSeconds(long seconds, long secondsToAdd) {
        return toSeconds(plusMillis(toMillis(seconds), secondsToAdd * 1000));
    }

    /**
     * 判断时间是否在指定范围内
     * @param time 要判断的时间（毫秒）
     * @param start 开始时间（毫秒）
     * @param end 结束时间（毫秒）
     * @return 是否在范围内
     */
    public boolean isBetween(long time, long start, long end) {
        return time >= start && time <= end;
    }

    /**
     * 格式化持续时间（例如：01:23:45）
     * @param durationMillis 持续时间（毫秒）
     * @return 格式化后的字符串
     */
    public String formatDuration(long durationMillis) {
        Duration duration = Duration.ofMillis(durationMillis);
        return String.format("%02d:%02d:%02d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart());
    }

    /**
     * 获取指定时间的小时数（0-23）
     * @param millis 毫秒级时间戳
     * @return 小时
     */
    public int getHour(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.of(zoneId))
                .getHour();
    }

    /* ========== 历史方法 ========== */

    /**
     * 获取当前时间字符串
     * @return yyyy-MM-dd HH:mm:ss 格式字符串
     */
    public String getNowTime(){
        return formatMillis(currentMillis());
    }

    /**
     * 获取当前时间字符串
     * @param format 指定格式
     * @return 指定格式的字符串
     */
    public String getNowTime(String format){
        return formatMillis(currentMillis(),format);
    }
}
