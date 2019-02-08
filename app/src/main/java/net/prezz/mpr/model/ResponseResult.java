package net.prezz.mpr.model;


import java.util.HashMap;
import java.util.Map;

public class ResponseResult {

    public enum ValueType {
        VOLUME,
        PLAYER_STATE;
    }

    private boolean success;
    private Map<ValueType, Object> responseValues;

    public ResponseResult(boolean success) {
        this.success = success;
        this.responseValues = new HashMap<ValueType, Object>();
    }

    public boolean isSuccess() {
        return success;
    }

    public ResponseResult putResponseValue(ValueType valueType, Object value) {
        responseValues.put(valueType, value);
        return this;
    }

    public Object getResponseValue(ValueType valueType) {
        return responseValues.get(valueType);
    }
}
