package zysy.iflytek.zhumang.utils;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class Result {
    private int code;
    private String msg;
    private boolean success;
    private Map<String, Object> data = new HashMap<>();

    public static Result ok(String msg) {
        Result result = new Result();
        result.setCode(200);
        result.setMsg(msg);
        result.setSuccess(true);
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.setCode(500);
        result.setMsg(msg);
        result.setSuccess(false);
        return result;
    }

    // 链式调用方法：接收键值对
    public Result data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}