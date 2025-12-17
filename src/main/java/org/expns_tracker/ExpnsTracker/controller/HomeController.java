package org.expns_tracker.ExpnsTracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(){
        return "index";
    }

    @GetMapping("/transactions")
    public String transactions(){
        return "transactions";
    }

    @GetMapping("/new")
    public String New(){
        return "new";
    }

    @GetMapping("/admin")
    public String admin(){
        return "admin";
    }

    @GetMapping("/settings")
    public String settings(){
        return "settings";
    }

    @GetMapping("/profile")
    public String profile(){
        return "profile";
    }

    @GetMapping("/import")
    public String imports(){
        return "import";
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        return "dashboard";
    }

    @GetMapping("/charts")
    public String charts(){
        return "charts";
    }

    @GetMapping("/nav")
    public String nav(){
        return "navbar";
    }

    @GetMapping("/register")
    public String register(){
        return "auth/register";
    }

}
