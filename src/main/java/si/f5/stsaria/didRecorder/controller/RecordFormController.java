package si.f5.stsaria.didRecorder.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import si.f5.stsaria.didRecorder.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class RecordFormController {
    @GetMapping("/")
    @ResponseBody
    public ModelAndView index(@CookieValue("token") Cookie tokenCookie, ModelAndView mav) {
        mav.setViewName("index");
        boolean loggedIn = false;
        String token = tokenCookie.getValue();
        if (token == null) token = "";
        try {
            if (new User().authForToken(token)) loggedIn = true;
        } catch (Exception ignored) {}
        mav.addObject("loggedIn", loggedIn);
        return mav;
    }
}
