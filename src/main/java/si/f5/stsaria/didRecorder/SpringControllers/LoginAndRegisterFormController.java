package si.f5.stsaria.didRecorder.SpringControllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import si.f5.stsaria.didRecorder.RecordFileControllers.FileLocks;
import si.f5.stsaria.didRecorder.RecordFileControllers.UserFC;
import si.f5.stsaria.didRecorder.Recorders.UserR;
import si.f5.stsaria.didRecorder.Records.User;
import si.f5.stsaria.didRecorder.Records.UserAuth;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LoginAndRegisterFormController {
    @RequestMapping(path = "/login", method= RequestMethod.GET)
    public ModelAndView login(ModelAndView mav) {
        mav.setViewName("login");
        ArrayList<User> users = new ArrayList<>(List.of());
        try{
            synchronized (FileLocks.user){
                users = UserFC.records();
            }
        } catch (Exception ignore) {}
        mav.addObject("users", users);
        return mav;
    }
    @RequestMapping(path = "/login", method= RequestMethod.POST)
    public ModelAndView login(@RequestParam("id") String id, @RequestParam("pass") String pass, ModelAndView mav, HttpServletResponse hsr){
        mav.setViewName("redirect:/");
        String token = "";
        UserR userR = new UserR();
        synchronized (FileLocks.user){
            try {
                if (userR.authForPass(id, pass)){
                    User user = userR.getUser(id);
                    UserAuth auth = userR.generateAndAppendAuthToken(user);
                    token = id+"-"+auth.auth;
                }
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
        String[] userInfo;
        synchronized (FileLocks.user){
            try {
                userInfo = new UserR().add(name, pass);
                token = userInfo[0]+"-"+userInfo[1];
                System.out.println(token);
            } catch (Exception ignore) {}
        }
        if (!token.replace(".", "").isEmpty()){
            Cookie tokenCookie = new Cookie("token", token);
            tokenCookie.setMaxAge(1728000);
            hsr.addCookie(tokenCookie);
        }
        return mav;
    }
}
