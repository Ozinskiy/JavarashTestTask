package com.game.service;

public class Filter {

    private String value;
    private String field;
    private RequestParameter parameter;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public RequestParameter getParameter() {
        return parameter;
    }

    public void setParameter(RequestParameter parameter) {
        this.parameter = parameter;
    }
}
