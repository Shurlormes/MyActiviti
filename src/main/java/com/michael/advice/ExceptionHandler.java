package com.michael.advice;

import com.michael.exception.BaseException;
import com.michael.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

	@org.springframework.web.bind.annotation.ExceptionHandler({BaseException.class})
	public String baseExceptionHandler(BaseException e) {
		return e.getMessage();
	}

	@org.springframework.web.bind.annotation.ExceptionHandler({Exception.class})
	public String ExceptionHandler(Exception e) {
		e.printStackTrace();
		logger.error(e.getCause().getMessage());
		return "系统繁忙，请稍后再试";
	}
}
