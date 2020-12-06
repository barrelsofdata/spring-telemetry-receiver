package com.barrelsofdata.springexamples.controller;

import com.barrelsofdata.springexamples.dto.ExceptionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.RequestDispatcher;
import java.util.Date;
import java.util.Map;

@Controller
public class ApplicationErrorController implements ErrorController {

    @Autowired
    private ErrorAttributes errorAttributes;

    @GetMapping(value = "/error")
    public ResponseEntity<ExceptionDto> handleError(WebRequest request) {
        Map<String, Object> requestErrors = errorAttributes.getErrorAttributes(request, ErrorAttributeOptions.defaults());
        Object statusObject = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE, RequestAttributes.SCOPE_REQUEST);
        StringBuilder errorMessage = new StringBuilder();
        ExceptionDto.ExceptionDtoBuilder exceptionBuilder = ExceptionDto.builder();
        if(requestErrors.containsKey("timestamp")) exceptionBuilder.timestamp((Date) requestErrors.get("timestamp"));
        if(requestErrors.containsKey("error")) errorMessage.append(requestErrors.get("error"));
        if(requestErrors.containsKey("message")) errorMessage.append((String) requestErrors.get("message"));
        exceptionBuilder.error(errorMessage.toString());
        HttpStatus status = statusObject != null ? HttpStatus.resolve(Integer.parseInt(statusObject.toString())) : HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionDto exception = exceptionBuilder.build();
        assert status != null;
        return new ResponseEntity<>(exception, status);
    }

    @Override
    @SuppressWarnings( "deprecation" )
    public String getErrorPath() {
        return "/error";
    }
}
