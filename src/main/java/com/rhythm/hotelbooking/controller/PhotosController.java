package com.rhythm.hotelbooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PhotosController {

    @GetMapping("/photos")
    public String photosPage() {
        return "photos";
    }
}