package com.sivalabs.urlshortener.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "URL Shortener - Thymeleaf");
        return "index";
    }


    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
