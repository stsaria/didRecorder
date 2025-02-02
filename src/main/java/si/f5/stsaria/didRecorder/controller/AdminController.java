package si.f5.stsaria.didRecorder.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import si.f5.stsaria.didRecorder.RealNames;
import si.f5.stsaria.didRecorder.Recorders;
import si.f5.stsaria.didRecorder.Users;
import si.f5.stsaria.didRecorder.checker.Login;

import java.nio.charset.StandardCharsets;
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
        String[] latestDidsS = new String[typeLength];
        String[] oneGapLatestDidsS = new String[typeLength];
        try {
            synchronized (Recorders.lock) {
                for (int i = 0; i < typeLength; i++){
                    latestDidsS[i] = Recorders.getLatestLog(0, i);
                    oneGapLatestDidsS[i] = Recorders.getLatestLog(1, i);
                }
            }
        } catch (Exception ignore){}
        mav.addObject("latestLogs", latestDidsS);
        mav.addObject("oneGapLatestLogs", oneGapLatestDidsS);
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
    @RequestMapping(path = "/admin/selectGapDidsS", method= RequestMethod.GET)
    public ModelAndView selectGapDids(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam(value = "gap", defaultValue = "0", required = false) String gap, ModelAndView mav) {
        try {Integer.valueOf(gap);} catch (NumberFormatException ignore) {gap = "0";}
        mav.setViewName("redirect:/");
        if (!Login.adminLoginChecker(token)) return mav;
        mav.setViewName("admin/selectGapDidsS");
        int typeLength = 2;
        String[] didsS = new String[typeLength];
        try {
            synchronized (Recorders.lock) {
                for (int i = 0; i < typeLength; i++){
                    didsS[i] = Recorders.getLatestLog(Integer.parseInt(gap), i);
                }
            }
        } catch (Exception ignore){}
        mav.addObject("didsS", didsS);
        mav.addObject("attendeesDidsDownloadURL", "download/today/attendees/csv?gap="+gap);
        mav.addObject("allDidsDownloadURL", "download/today/all/csv?gap="+gap);
        return mav;
    }
    @RequestMapping(path = "/admin/download/today/all/csv", method= RequestMethod.GET)
    public ResponseEntity<byte[]> downloadTodayAllCsv(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam(value = "gap", defaultValue = "0", required = false) String gap) {
        if (!Login.adminLoginChecker(token)) return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/").build();
        try {Integer.valueOf(gap);} catch (NumberFormatException ignore) {gap = "0";}
        String didsStr = "";
        try{
            synchronized (Recorders.lock){
                didsStr = Recorders.getLatestLog(Integer.parseInt(gap), 0);
            }
        } catch (Exception ignore) {}
        byte[] didsBytes = didsStr.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"todayAll(gap="+gap+").csv\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return new ResponseEntity<>(didsBytes, headers, HttpStatus.OK);
    }
    @RequestMapping(path = "/admin/download/today/attendees/csv", method= RequestMethod.GET)
    public ResponseEntity<byte[]> downloadTodayAttendeesCsv(@CookieValue(name = "token", defaultValue = "", required = false) String token, @RequestParam(value = "gap", defaultValue = "0", required = false) String gap) {
        if (!Login.adminLoginChecker(token)) return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/").build();
        try {Integer.valueOf(gap);} catch (NumberFormatException ignore) {gap = "0";}
        String didsStr = "";
        try{
            synchronized (Recorders.lock){
                didsStr = Recorders.getLatestLog(Integer.parseInt(gap), 1);
            }
        } catch (Exception ignore) {}
        byte[] didsBytes = didsStr.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"todayAttendees(gap="+gap+").csv\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return new ResponseEntity<>(didsBytes, headers, HttpStatus.OK);
    }
}
