package com.deriska.psydtrader.controller;

import com.deriska.psydtrader.entity.StandardResponse;
import com.deriska.psydtrader.entity.User;
import com.deriska.psydtrader.service.CalculationService;
import com.deriska.psydtrader.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/api/registration")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CalculationService calculationService;

    @PostConstruct
    public void initRoleAndUser() {
        userService.initRoleAndUser();
    }

//    @PostConstruct
//    public void getWatchedPrices(){
//        calculationService.getWatchedPrices();
//    }

    @PostMapping({"/registerNewUser"})
    public ResponseEntity<StandardResponse> registerNewUser(@RequestBody User user) {
        return userService.registerNewUser(user);
    }

    @GetMapping({"/forAdmin"})
    @PreAuthorize("hasRole('Admin')")
    public String forAdmin(){
        return "This URL is only accessible to the admin";
    }

    @GetMapping({"/forUser"})
    @PreAuthorize("hasRole('User')")
    public String forUser(){
        return "This URL is only accessible to the user";
    }
}
