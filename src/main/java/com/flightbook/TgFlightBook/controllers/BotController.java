package com.flightbook.TgFlightBook.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class BotController {
    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        return "test";
    }
}
