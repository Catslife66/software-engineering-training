package main.java.com.example.demo.dto;

public class Result<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;

    public Result(boolean success, String code, String message, T data){
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, "SUCCESS", message, data);
    }

    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(false, code, message, null);
    }

    public boolean isSuccess(){
        return success;
    }

    public String getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }

    public T getData() {
        return data;
    }
}
