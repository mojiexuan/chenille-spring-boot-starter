package com.chenjiabao.open.chenille.core;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * XML 工具类
 *
 * <p>基于 Jackson XmlMapper，提供对象与 XML 之间的序列化和反序列化。
 */
@Slf4j
public class ChenilleXmlUtils {

    private static final XmlMapper XML_MAPPER = (XmlMapper) new XmlMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    /**
     * 将对象序列化为 XML 字符串
     *
     * @param object 要序列化的对象
     * @return 序列化后的 XML 字符串
     */
    public String toXml(Object object) {
        try {
            return XML_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            log.error("序列化对象为 XML 字符串失败", e);
            return null;
        }
    }

    /**
     * 将对象序列化为 XML 字节数组
     *
     * @param object 要序列化的对象
     * @return 序列化后的 XML 字节数组
     */
    public byte[] toXmlBytes(Object object) {
        try {
            return XML_MAPPER.writeValueAsBytes(object);
        } catch (Exception e) {
            log.error("序列化对象为 XML 字节数组失败", e);
            return null;
        }
    }

    /**
     * 将 XML 字符串反序列化为对象
     *
     * @param xml  XML 字符串
     * @param type 对象类型
     * @param <T>  对象类型参数
     * @return 反序列化后的对象
     */
    public <T> T fromXml(String xml, Class<T> type) {
        try {
            return XML_MAPPER.readValue(xml, type);
        } catch (Exception e) {
            log.error("反序列化 XML 字符串为对象失败", e);
            return null;
        }
    }

    /**
     * 将 XML 字节数组反序列化为对象
     *
     * @param xml  XML 字节数组
     * @param type 对象类型
     * @param <T>  对象类型参数
     * @return 反序列化后的对象
     */
    public <T> T fromXmlBytes(byte[] xml, Class<T> type) {
        try {
            return XML_MAPPER.readValue(xml, type);
        } catch (Exception e) {
            log.error("反序列化 XML 字节数组为对象失败", e);
            return null;
        }
    }

    /**
     * 获取 XmlMapper 实例
     *
     * @return XmlMapper 实例
     */
    public XmlMapper mapper() {
        return XML_MAPPER;
    }

}
