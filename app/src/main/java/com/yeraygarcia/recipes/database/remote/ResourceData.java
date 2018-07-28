package com.yeraygarcia.recipes.database.remote;

public class ResourceData<T> {

    private int status;
    private String code;
    private String message;
    private T result;

    public ResourceData(int status, String code, String message, T result) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public boolean isSuccessful() {
        return status >= 200 && status < 300;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;

    }
    @Override
    public String toString() {
        return "ResourceData{" +
                "status=" + status +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", result=" + result.toString() +
                '}';
    }
}
