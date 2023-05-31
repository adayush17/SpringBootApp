package com.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartcontactmanager.dao.ContactRepository;
import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.entities.Contact;
import com.smartcontactmanager.entities.User;
import com.smartcontactmanager.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String  userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		model.addAttribute("user", user);
	}
	
	
	@GetMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam(value = "profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			// Uploading and processing file
			if(file.isEmpty()) {
				System.out.println("File is empty");
				contact.setImage("contact.png");
			}else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			
			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Your contact is added Successfully! Add More...", "success"));
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			
			session.setAttribute("message", new Message("Something went wrong! Try Again...", "danger"));
		}
		
		return "normal/add_contact_form";
	}
	
	
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page")Integer page, Model m, Principal principal) {
		m.addAttribute("title", "Show User Contacts");
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 8);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		Optional<Contact> contact = this.contactRepository.findById(cId);
		Contact result = contact.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==result.getUser().getId()) {
			model.addAttribute("contact",result);
			model.addAttribute("title", result.getName());
		}else 
			model.addAttribute("title","Error!");		
		
		return "normal/contact_detail";
	}
	
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Principal principal, HttpSession session) {
		Contact contact = this.contactRepository.findById(cId).get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
	
		if(user.getId()==contact.getUser().getId()) { 
			contact.setUser(null);
			this.contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact deleted successfully...!", "success"));
		}
		
		return"redirect:/user/show-contacts/0";
	}
	
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cId, Model model) {
		model.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam(value = "profileImage") MultipartFile file, Principal principal) {
		try {
			Contact oldContact = this.contactRepository.findById(contact.getcId()).get();
			
			
			if(!file.isEmpty()) {
				//delete old photo
				File delteFile = new ClassPathResource("static/images").getFile();
				File file1 = new File(delteFile, oldContact.getImage());
				file1.delete();
				
				//update new photo
				File saveFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			
			}else {
				contact.setImage(oldContact.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile Page");
		
		return "normal/profile";
	}
	
	
	@GetMapping("/setting")
	public String openSettings() {
		return "normal/setting";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession httpSession) {
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			
			httpSession.setAttribute("message", new Message("Your password is successfully changed", "success"));
		}else {
			httpSession.setAttribute("message", new Message("Please enter correct old password", "danger"));
			return "redirect:/user/setting";
		}
		
		return "redirect:/user/index";
	}
}
