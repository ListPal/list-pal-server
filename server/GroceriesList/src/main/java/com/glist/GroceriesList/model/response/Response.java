package com.glist.GroceriesList.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response {
    private int status;
    private Object body;

    public Response() {
    }
    public Response(int status, Object body) {
        setStatus(status);
        setBody(body);
    }

}
