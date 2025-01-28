package si.f5.stsaria.didRecorder.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class RecordFormController {
    @GetMapping("/")
    public ModelAndView index(ModelAndView mav) {
        LocalDateTime nowTime = LocalDateTime.now();
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        mav.setViewName("index");
        mav.addObject("today", nowTime.format(timeFormat));
        mav.addObject("loggedIn", nowTime.format(timeFormat));
        return mav;
    }
}
