package org.thomaschen.sprawl.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class TaskNotInProgressException extends RuntimeException {
    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public TaskNotInProgressException( String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not in progress %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}