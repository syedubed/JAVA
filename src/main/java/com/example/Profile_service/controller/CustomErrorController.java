package com.example.Profile_service.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponse> handleError(
            HttpServletRequest request) {

        Object statusAttribute =
                request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        int status = statusAttribute instanceof Integer code
                ? code
                : 500;

        String message = status == 404
                ? "The requested path does not exist."
                : "The request could not be completed.";

        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, message));
    }

    public record ErrorResponse(int status, String message) {}
}
