package com.schedulemng.common.exception;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        model.addAttribute("status", status != null ? status : 500);
        model.addAttribute("message", message != null ? message : "예상치 못한 오류가 발생했습니다.");
        model.addAttribute("path", path != null ? path : request.getRequestURI());
        model.addAttribute("timestamp", LocalDateTime.now());
        return "error";
    }
}