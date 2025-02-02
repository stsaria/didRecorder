package si.f5.stsaria.didRecorder.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import si.f5.stsaria.didRecorder.Recoder;
import si.f5.stsaria.didRecorder.TimeUtils;
import si.f5.stsaria.didRecorder.Users;
import si.f5.stsaria.didRecorder.checker.Login;

import java.util.Objects;

@Controller
public class RecordFormController {
    private final static String hMRegex = "([01]?[0-9]|2[0-3]):([0-5][0-9])";
    @RequestMapping(path = "/", method=RequestMethod.GET)
    public ModelAndView index(@CookieValue(name = "token", defaultValue = "", required = false) String token, @CookieValue(name = "result", defaultValue = "", required = false) String resultC, @RequestParam(value = "result", defaultValue = "-22", required = false) String result, ModelAndView mav, HttpServletResponse hsr) {
        try {Integer.valueOf(result);} catch (NumberFormatException ignore) {result = "-22";}
        if (!Objects.equals(result, "-22")){
            mav.setViewName("redirect:/");
            Cookie resultCookieN = new Cookie("result", result);
            hsr.addCookie(resultCookieN);
            return mav;
        }
        Cookie resultCookieN = new Cookie("result", "-22");
        hsr.addCookie(resultCookieN);
        result = resultC;
        try {Integer.valueOf(result);} catch (NumberFormatException ignore) {result = "-22";}
        String when = "";
        String log = "";
        mav.setViewName("redirect:/login");
        if (!Login.loginChecker(token)){
            return mav;
        }
        try {
            synchronized (Users.lock) {
                Recoder recoder = new Recoder(token.split("\\.")[0]);
                when = recoder.nextWhen();
                log = recoder.getLatestLog(0);
            }
        } catch (Exception ignore) {
            result = "-1";
        }
        mav.setViewName("index");
        mav.addObject("result", Integer.valueOf(result));
        mav.addObject("when", when);
        mav.addObject("log", log);
        return mav;
    }
    @RequestMapping(path = "/record/0", method=RequestMethod.POST)
    public String recordComeTime(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("time") String time, HttpServletResponse hsr) {
        if (!Login.loginChecker(token)){
            return "redirect:/login";
        }
        if (!time.matches(hMRegex)){
            return "redirect:/?result=-2";
        }
        time = String.valueOf(TimeUtils.hMTimeToUnixTime(time));
        Recoder recoder = new Recoder(token.split("\\.")[0]);
        int result;
        try{
            result = recoder.add("0", time);
        } catch (Exception ignore) {
            result = 1;
        }
        return "redirect:/?result="+result;
    }
    @RequestMapping(path = "/record/1", method=RequestMethod.POST)
    public String recordAmContent(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("content") String content, HttpServletResponse hsr) {
        if (!Login.loginChecker(token)){
            return "redirect:/login";
        }
        Recoder recoder = new Recoder(token.split("\\.")[0]);
        int result;
        try{
            result = recoder.add("1", content);
        } catch (Exception ignore) {
            result = -1;
        }
        return "redirect:/?result="+result;
    }
    @RequestMapping(path = "/record/2", method=RequestMethod.POST)
    public String recordPmContent(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("content") String content, HttpServletResponse hsr) {
        if (!Login.loginChecker(token)){
            return "redirect:/login";
        }
        Recoder recoder = new Recoder(token.split("\\.")[0]);
        int result;
        try{
            result = recoder.add("2", content);
        } catch (Exception ignore) {
            result = -1;
        }
        return "redirect:/?result="+result;
    }
    @RequestMapping(path = "/record/3", method=RequestMethod.POST)
    public String recordGoTime(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("time") String time, HttpServletResponse hsr) {
        if (!Login.loginChecker(token)){
            return "redirect:/login";
        }
        if (!time.matches(hMRegex)){
            return "redirect:/?result=-2";
        }
        time = String.valueOf(TimeUtils.hMTimeToUnixTime(time));
        Recoder recoder = new Recoder(token.split("\\.")[0]);
        int result;
        try{
            result = recoder.add("3", time);
        } catch (Exception ignore) {
            result = -1;
        }
        return "redirect:/?result="+result;
    }
    @RequestMapping(path = "/record/all", method=RequestMethod.POST)
    public String recordAll(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("comeTime") String comeTime, @RequestParam("amContent") String amContent, @RequestParam("pmContent") String pmContent, @RequestParam("goTime") String goTime, HttpServletResponse hsr) {
        boolean loggedIn = false;
        try {
            synchronized (Users.lock) {
                if (new Users().authForToken(token)) loggedIn = true;
            }
        } catch (Exception ignore) {}
        if (!loggedIn){
            return "redirect:/login";
        }
        String hMRegex = "([01]?[0-9]|2[0-3]):([0-5][0-9])";
        if (!(comeTime.matches(hMRegex) || goTime.matches(hMRegex))){
            return "redirect:/?result=-2";
        }
        String[] times = {comeTime, goTime};
        for (int i = 0; i < times.length; i++){
            times[i] = String.valueOf(TimeUtils.hMTimeToUnixTime(times[i]));
        }
        String[] contents = {times[0], amContent, pmContent, times[1]};
        Recoder recoder = new Recoder(token.split("\\.")[0]);
        int resultT;
        int result = -22;
        for (int i = 0; i < contents.length; i++){
            try{
                resultT = recoder.add(String.valueOf(i), contents[i]);
                result = resultT;
                if (resultT != 0) break;
            } catch (Exception ignore) {
                result = -1;
                break;
            }
        }
        return "redirect:/?result="+result;
    }
}
