package cz.phsoft.hokej.exceptions;

import org.springframework.http.HttpStatus;

public class AccountNotActivatedException extends BusinessException {

    public AccountNotActivatedException() {
        super("První musíte aktivovat účet pomocí odkazu v emailu", HttpStatus.FORBIDDEN); // 403
    }
}
