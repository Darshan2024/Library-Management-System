package edu.dugale.LibraryManagementSystem.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @GetMapping("/signin")
    public String signin(Model model){
        return "signin";
    }

    @PostMapping("/signin")
    public String handleSignin(@RequestParam("username") String username, RedirectAttributes redirectAttrs, jakarta.servlet.http.HttpSession session){
        session.setAttribute(
            "username", username);
        redirectAttrs.addFlashAttribute("username", username);
        return "redirect:/";
    }
}
