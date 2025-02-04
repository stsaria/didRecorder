package si.f5.stsaria.didRecorder.SpringControllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import si.f5.stsaria.didRecorder.DidRecorderApplication;
import si.f5.stsaria.didRecorder.RecordFileControllers.FileLocks;
import si.f5.stsaria.didRecorder.Recorders.DidR;
import si.f5.stsaria.didRecorder.Recorders.UserR;
import si.f5.stsaria.didRecorder.Records.User;
import si.f5.stsaria.didRecorder.TimeUtils;
import si.f5.stsaria.didRecorder.checker.Login;

import java.util.Objects;

@Controller
public class RecordFormController {
    private final static String hMRegex = "([01]?[0-9]|2[0-3]):([0-5][0-9])";
    @RequestMapping(path = "/", method=RequestMethod.GET)
    public ModelAndView index(@CookieValue(name = "token", defaultValue = "", required = false) String token, @CookieValue(name = "result", defaultValue = "", required = false) String resultC, @RequestParam(value = "result", defaultValue = "-22", required = false) String result, ModelAndView mav, HttpServletResponse hsr) {
        mav.setViewName("redirect:/login");
        if (!Login.loginChecker(token)) return mav;
        try {Integer.valueOf(result);} catch (NumberFormatException ignore) {result = "-22";}
        if (!Objects.equals(result, "-22")){
            mav.setViewName("redirect:/");
            Cookie resultCookieN = new Cookie("result", result);
            hsr.addCookie(resultCookieN);
            return mav;
        }
        mav.setViewName("index");
        Cookie resultCookieN = new Cookie("result", "-22");
        hsr.addCookie(resultCookieN);
        result = resultC;
        try {Integer.valueOf(result);} catch (NumberFormatException ignore) {result = "-22";}
        int when = 4;
        String log = "";
        try {
            User user;
            synchronized (FileLocks.user) {
                user = new UserR().getUser(token.split("-")[0]);
            }
            synchronized (FileLocks.did) {
                DidR didR = new DidR();
                when = didR.nextWhen(user);
                log = didR.getLatestUserLog(user, 0);
            }
        } catch (Exception ignore) {
            result = "-1";
        }
        mav.addObject("minTime", DidRecorderApplication.properties.getPropertyInt("minTimeHours")+":00");
        mav.addObject("maxTime", DidRecorderApplication.properties.getPropertyInt("maxTimeHours")+":00");
        mav.addObject("result", Integer.valueOf(result));
        mav.addObject("when", when);
        mav.addObject("log", log);
        return mav;
    }
    @RequestMapping(path = "/record/0", method=RequestMethod.POST)
    public String recordComeTime(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("time") String time) {
        if (!Login.loginChecker(token)){
            return "redirect:/login";
        }
        if (!time.matches(hMRegex)){
            return "redirect:/?result=-2";
        }
        time = String.valueOf(TimeUtils.hMTimeToUnixTime(time));
        int result;
        try{
            User user;
            synchronized (FileLocks.user) {
                user = new UserR().getUser(token.split("-")[0]);
            }
            result = new DidR().add(user, "0", time);
        } catch (Exception ignore) {
            result = -1;
        }
        return "redirect:/?result="+result;
    }
    @RequestMapping(path = "/record/1", method=RequestMethod.POST)
    public String recordAmContent(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("content") String content) {
        if (!Login.loginChecker(token)){
            return "redirect:/login";
        }
        int result;
        try{
            User user;
            synchronized (FileLocks.user) {
                user = new UserR().getUser(token.split("-")[0]);
            }
            result = new DidR().add(user, "1", content);
        } catch (Exception ignore) {
            result = -1;
        }
        return "redirect:/?result="+result;
    }
    @RequestMapping(path = "/record/2", method=RequestMethod.POST)
    public String recordPmContent(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("content") String content) {
        if (!Login.loginChecker(token)){
            return "redirect:/login";
        }
        int result;
        try{
            User user;
            synchronized (FileLocks.user) {
                user = new UserR().getUser(token.split("-")[0]);
            }
            result = new DidR().add(user, "2", content);
        } catch (Exception ignore) {
            result = -1;
        }
        return "redirect:/?result="+result;
    }
    @RequestMapping(path = "/record/3", method=RequestMethod.POST)
    public String recordGoTime(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("time") String time) {
        if (!Login.loginChecker(token)){
            return "redirect:/login";
        }
        if (!time.matches(hMRegex)){
            return "redirect:/?result=-2";
        }
        time = String.valueOf(TimeUtils.hMTimeToUnixTime(time));
        int result;
        try{
            User user;
            synchronized (FileLocks.user) {
                user = new UserR().getUser(token.split("-")[0]);
            }
            result = new DidR().add(user, "3", time);
        } catch (Exception ignore) {
            result = -1;
        }
        return "redirect:/?result="+result;
    }
    @RequestMapping(path = "/record/all", method=RequestMethod.POST)
    public String recordAll(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam("comeTime") String comeTime, @RequestParam("amContent") String amContent, @RequestParam("pmContent") String pmContent, @RequestParam("goTime") String goTime) {
        if (!Login.loginChecker(token)) return "redirect:/login";
        String hMRegex = "([01]?[0-9]|2[0-3]):([0-5][0-9])";
        if (!(comeTime.matches(hMRegex) || goTime.matches(hMRegex))){
            return "redirect:/?result=-2";
        }
        String[] times = {comeTime, goTime};
        for (int i = 0; i < times.length; i++){
            times[i] = String.valueOf(TimeUtils.hMTimeToUnixTime(times[i]));
        }
        String[] contents = {times[0], amContent, pmContent, times[1]};
        int result = -22;
        User user;
        try{
            synchronized (FileLocks.user) {
                user = new UserR().getUser(token.split("-")[0]);
            }
        } catch (Exception ignore) {
            return "redirect:/?result=-1";
        }
        for (int i = 0; i < contents.length; i++){
            try{
                result = new DidR().add(user, String.valueOf(i), contents[i]);
                if (result != 0) break;
            } catch (Exception ignore) {
                result = -1;
                break;
            }
        }
        return "redirect:/?result="+result;
    }
}
