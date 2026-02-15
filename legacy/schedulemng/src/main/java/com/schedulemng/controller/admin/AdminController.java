package com.schedulemng.controller.admin;

import com.schedulemng.entity.User;
import com.schedulemng.entity.SelectedDate;
import com.schedulemng.security.CustomUserDetails;
import com.schedulemng.service.UserService;
import com.schedulemng.service.SelectedDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final SelectedDateService selectedDateService;

    @GetMapping
    public String adminMain(Model model) {
        // 현재 로그인한 사용자 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)  auth.getPrincipal();
        String userid = userDetails.getUserId();
        log.info("Current userid: {}", userid);

        
        User currentUser = userService.findByUserid(userid);
        log.info("Found user: {}", currentUser);
        
        if (currentUser == null || !currentUser.isAdmin()) {
            log.warn("User is null or not admin: {}", currentUser);
            return "redirect:/schedule/calendar";
        }
        
        return "admin/adminMain";
    }

    @GetMapping("/account")
    public String accountManagement(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)  auth.getPrincipal();
        String userid = userDetails.getUserId();
        User currentUser = userService.findByUserid(userid);

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/schedule/calendar";
        }

        return "admin/accountManagement";
    }

    @GetMapping("/schedule-summary")
    public String scheduleSummary(@RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
                                 @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month,
                                 Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)  auth.getPrincipal();
        String userid = userDetails.getUserId();
        User currentUser = userService.findByUserid(userid);

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/schedule/calendar";
        }

        List<SelectedDate> monthlyData = selectedDateService.getDatesByMonth(year, month);
        
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("monthlyData", monthlyData);
        return "admin/scheduleSummary";
    }

    // 새로운 관리자 페이지들
    @GetMapping("/settings")
    public String systemSettings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)  auth.getPrincipal();
        String userid = userDetails.getUserId();
        User currentUser = userService.findByUserid(userid);

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/schedule/calendar";
        }

        // 서비스 준비중 알럿을 위한 플래그 추가
        model.addAttribute("serviceNotReady", true);
        return "admin/systemSettings";
    }

    @GetMapping("/logs")
    public String logManagement(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)  auth.getPrincipal();
        User user = userDetails.getUser();
        String userid = user.getUserid();
        User currentUser = userService.findByUserid(userid);
        
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/schedule/calendar";
        }
        
        // 서비스 준비중 알럿을 위한 플래그 추가
        model.addAttribute("serviceNotReady", true);
        return "admin/logManagement";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)  auth.getPrincipal();
        User user = userDetails.getUser();
        String userid = user.getUserid();
        User currentUser = userService.findByUserid(userid);
        
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/schedule/calendar";
        }
        
        // 서비스 준비중 알럿을 위한 플래그 추가
        model.addAttribute("serviceNotReady", true);
        return "admin/reports";
    }

    @GetMapping("/schedule-management")
    public String scheduleManagement(@RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
                                   @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month,
                                   Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)  auth.getPrincipal();
        String userid = userDetails.getUserId();
        User currentUser = userService.findByUserid(userid);

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/schedule/calendar";
        }

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        return "admin/scheduleManagement";
    }

    @GetMapping("/schedule-special")
    public String scheduleSpecial() {
        return "admin/scheduleSpecial";
    }

    /**
     * 업무일지 화면 이동
     */
    @GetMapping("/work-log")
    public String workLogView(@RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month,
            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)  auth.getPrincipal();
        String userid = userDetails.getUserId();
        User currentUser = userService.findByUserid(userid);

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/schedule/calendar";
        }

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        return "admin/workLog";
    }
} 