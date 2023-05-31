package com.smartcontactmanager.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.entities.User;
import com.smartcontactmanager.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	

	// Email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {		
		Random random = new Random(1000);
		int otp = random.nextInt(999999);
		
		// Code for Send OTP to Email.
		String subject = "OTP From Smart Contact Manager";
		String message = ""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+ "OTP is "
				+ "<b>" + otp
				+ "</n>"
				+ "</h1>"
				+ "</div>";
		String to = email;
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}else {
			session.setAttribute("message", "Check your email id!");
			return "forgot";
		}
	}
	
	
	//Verify OTP
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		int myOtp = (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		
		if(myOtp == otp) {
			User user = this.repository.getUserByUserName(email);
			
			if(user == null) {
				session.setAttribute("message", "User does not exist with this email!");
				return "forgot";
			}else {
				
			}
			
			return "change_password";
			
		}else{
			session.setAttribute("message", "You have entered wrong OTP!");
			return "verify_otp";
		}
	}
	
	
	//Change Password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		String email=(String)session.getAttribute("email");
		User user = this.repository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.repository.save(user);
		
		return "redirect:/signin?change=Password is changed successfully!";
		
	}
	
}
