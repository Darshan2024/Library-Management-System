package edu.dugale.LibraryManagementSystem.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/signin")
    public String signin(Model model){
        model.addAttribute("heading", "Sign In");
        return "under-construction";
    }

    @GetMapping("/books")
    public String books(Model model){
        model.addAttribute("heading", "View Books");
        return "under-construction";
    }
}
