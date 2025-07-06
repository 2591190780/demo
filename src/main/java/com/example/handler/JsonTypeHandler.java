package com.example.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

/**
 * JSON类型处理器
 * 用于处理实体类中的JSON字段
 */
@MappedTypes(Object.class)
public class JsonTypeHandler extends JacksonTypeHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public JsonTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public Object parse(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Object.class);
        } catch (Exception e) {
            return json; // 返回原始字符串
        }
    }

    @Override
    public String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj); // 转为字符串
        }
    }

}