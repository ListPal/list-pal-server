package com.glist.GroceriesList.controller;

import com.glist.GroceriesList.model.response.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class DemoController {
    @GetMapping("/demo")
    public ResponseEntity<Response> welcome() throws Exception {
        return ResponseEntity.ok(new Response(200, "Hello there"));
    }
}
