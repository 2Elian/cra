package com.cra.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data // Lombok 自动生成：getter / setter / toString()
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化为 JSON 时，值为 null 的字段不会返回
public class Response<T> {
    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    public Response() {
        this.timestamp = System.currentTimeMillis();
    }

    public Response(int code, String message) {
        this(); // 调用无参构造，确保 timestamp 一定被初始化
        this.code = code;
        this.message = message;
    }

    public Response(int code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    // 成功响应

    // 新增 / 删除 / 更新成功 --> 无需返回数据
    public static <T> Response<T> success() {
        return new Response<>(200, "操作成功");
    }
    // 查询接口 --> 返回业务数据
    public static <T> Response<T> success(T data) {
        return new Response<>(200, "操作成功", data);
    }
    // 自定义成功提示 --> 更友好的前端提示
    public static <T> Response<T> success(String message, T data) {
        return new Response<>(200, message, data);
    }

    // 失败响应
    public static <T> Response<T> fail() {
        return new Response<>(500, "操作失败");
    }

    public static <T> Response<T> fail(String message) {
        return new Response<>(500, message);
    }

    public static <T> Response<T> fail(int code, String message) {
        return new Response<>(code, message);
    }

    public static <T> Response<T> fail(int code, String message, T data) {
        return new Response<>(code, message, data);
    }
}