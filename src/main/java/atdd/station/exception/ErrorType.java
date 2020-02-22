package atdd.station.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {
    INTERNAL_SERVER_ERROR(-100, HttpStatus.INTERNAL_SERVER_ERROR, "internal server error"),
    NOT_FOUND(-101, HttpStatus.NOT_FOUND, "not found");

    private int code;
    private HttpStatus status;
    private String message;

    ErrorType(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}