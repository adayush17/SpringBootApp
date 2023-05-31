package com.smartcontactmanager.service;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	public boolean sendEmail(String subject, String message, String to) {
		boolean f = false;
		
		String from = "adayush17@gmail.com";
		
		// Variable for Gmail
		String host = "smtp.gmail.com";
		
		// Get the system properties
		Properties properties = System.getProperties();
		System.out.println("PROPERTIES" + properties);
		
		// Host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.auth","true");
		properties.put("mail.smtp.ssl.enable", "true");
		
		
		//Step1: to get the session object
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("adayush17@gmail.com", "mmvuwizdbjqtgekh");
			}
		});
		
		session.setDebug(true);
		
		//Step2: compose the message [text, multi-media] 
		MimeMessage m = new MimeMessage(session);
		
		try {
			//from email
			m.setFrom(from);
			
			//adding recipient to message
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message
			m.setSubject(subject);
			
			//adding text to message
//			m.setText(message);
			m.setContent(message, "text/html");
			
			//Step3: send the message using Transport class
			Transport.send(m);
			
			System.out.println("Sent sucess.................");
			f = true;
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return f;
	}
}
