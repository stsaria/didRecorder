package si.f5.stsaria.didRecorder.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import si.f5.stsaria.didRecorder.Users;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LoginAndRegisterFormController {
    @RequestMapping(path = "/login", method= RequestMethod.GET)
    public ModelAndView login(ModelAndView mav) {
        mav.setViewName("login");
        ArrayList<String[]> users = new ArrayList<>(List.of());
        try{
            synchronized (Users.lock) {
                users = new Users().getFormatedUsers();
            }
        } catch (Exception ignore) {}
        mav.addObject("users", users);
        return mav;
    }
    @RequestMapping(path = "/login", method= RequestMethod.POST)
    public ModelAndView login(@RequestParam("id") String id, @RequestParam("pass") String pass, ModelAndView mav, HttpServletResponse hsr){
        mav.setViewName("redirect:/");
        String token = "";
        Users users = new Users();
        synchronized (Users.lock){
            try {
                if (users.authForPass(id, pass)) token = users.generateAndAppendAuthToken(id);
            } catch (Exception ignore) {}
        }
        if (!token.isEmpty()){
            Cookie tokenCookie = new Cookie("token", token);
            tokenCookie.setMaxAge(1728000);
            hsr.addCookie(tokenCookie);
        }
        return mav;
    }
    @RequestMapping(path = "/register", method= RequestMethod.GET)
    public ModelAndView register(ModelAndView mav) {
        mav.setViewName("register");
        return mav;
    }
    @RequestMapping(path = "/register", method= RequestMethod.POST)
    public ModelAndView register(@RequestParam("name") String name, @RequestParam("pass") String pass, ModelAndView mav, HttpServletResponse hsr){
        mav.setViewName("redirect:/");
        String token = "";
        Users users = new Users();
        synchronized (Users.lock){
            try {
                token = users.add(name, pass)[1];
            } catch (Exception ignore) {}
        }
        if (!token.isEmpty()){
            Cookie tokenCookie = new Cookie("token", token);
            tokenCookie.setMaxAge(1728000);
            hsr.addCookie(tokenCookie);
        }
        return mav;
    }
}
