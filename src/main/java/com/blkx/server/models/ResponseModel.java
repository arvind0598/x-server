package com.blkx.server.models;

import com.blkx.server.constants.ResponseMessage;

public class ResponseModel {
    private Boolean success;
    private String message;
    private Object data;

    public ResponseModel(Boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ResponseModel() {
        this.success = true;
        this.message = ResponseMessage.TEMP_MESSAGE.toString();
        this.data = null;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
