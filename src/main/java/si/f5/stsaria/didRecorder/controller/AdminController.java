package si.f5.stsaria.didRecorder.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import si.f5.stsaria.didRecorder.RealNames;
import si.f5.stsaria.didRecorder.Recoders;
import si.f5.stsaria.didRecorder.Users;
import si.f5.stsaria.didRecorder.checker.Login;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class AdminController {
    @RequestMapping(path = "/admin/", method= RequestMethod.GET)
    public ModelAndView index(@CookieValue(name = "token", defaultValue = "", required = false) String token, ModelAndView mav) {
        mav.setViewName("redirect:/");
        if (!Login.adminLoginChecker(token)) return mav;
        mav.setViewName("admin/index");
        int typeLength = 2;
        String[] latestLogs = new String[typeLength];
        String[] oneGapLatestLogs = new String[typeLength];
        try {
            synchronized (Recoders.lock) {
                for (int i = 0; i < typeLength; i++){
                    latestLogs[i] = Recoders.getLatestLog(0, i);
                    oneGapLatestLogs[i] = Recoders.getLatestLog(1, i);
                }
            }
        } catch (Exception ignore){}
        mav.addObject("latestLogs", latestLogs);
        mav.addObject("oneGapLatestLogs", oneGapLatestLogs);
        return mav;
    }
    @RequestMapping(path = "/admin/setRealName", method= RequestMethod.GET)
    public ModelAndView setRealName(@CookieValue(name = "token", defaultValue = "", required = false) String token, @CookieValue(name = "result", defaultValue = "", required = false) String resultC, @RequestParam(value = "result", defaultValue = "-22", required = false) String result, ModelAndView mav, HttpServletResponse hsr) {
        try {Integer.valueOf(result);} catch (NumberFormatException ignore) {result = "-22";}
        if (!Objects.equals(result, "-22")){
            mav.setViewName("redirect:/admin/setRealName");
            Cookie resultCookieN = new Cookie("result", result);
            hsr.addCookie(resultCookieN);
            return mav;
        }
        Cookie resultCookieN = new Cookie("result", "-22");
        hsr.addCookie(resultCookieN);
        result = resultC;
        try {Integer.valueOf(result);} catch (NumberFormatException ignore) {result = "-22";}
        mav.setViewName("redirect:/");
        if (!Login.adminLoginChecker(token)) return mav;
        mav.setViewName("admin/setRealName");
        ArrayList<String[]> users = new ArrayList<>(List.of());
        try{
            synchronized (Users.lock) {
                users = new Users().getFormatedUsers();
            }
        } catch (Exception ignore) {}
        mav.addObject("users", users);
        mav.addObject("result", result);
        return mav;
    }
    @RequestMapping(path = "/admin/setRealName", method= RequestMethod.POST)
    public String setRealName(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("id") String id, @RequestParam("name") String name) {
        if (!Login.adminLoginChecker(token)) return "redirect:/";
        int result;
        try{
            synchronized (Users.lock) {
                result = new RealNames().add(id, name);
            }
        } catch (Exception ignore) {
            result = -1;
        }
        return "redirect:/admin/setRealName?result="+result;
    }
}
