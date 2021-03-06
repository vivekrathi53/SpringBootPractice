package com.vivek.spring.restAPI.controllers;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.vivek.spring.restAPI.entities.dto.SessionDto;
import com.vivek.spring.restAPI.entities.dto.UserDto;
import com.vivek.spring.restAPI.service.*;
import com.vivek.spring.restAPI.service.SessionService;
import com.vivek.spring.restAPI.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.vivek.spring.restAPI.entities.Session;
import com.vivek.spring.restAPI.entities.User;


@RestController
public class restAPIController {

	@Autowired
	private UserService userService;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private SessionLogService sessionLogService;
	@Autowired
	private AttendanceService attendanceService;

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public SessionService getSessionService() {
		return sessionService;
	}

	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	public restAPIController(UserService userService, SessionService sessionService) {
		this.userService = userService;
		this.sessionService = sessionService;
	}

	@PostMapping("/api/signup")
	public ResponseEntity<Object> signUpUser(@RequestBody User user)
	{
		userService.registerUser(user);
		return new ResponseEntity<>("New User Registration Successful",HttpStatus.OK);
	}
	
	@PostMapping("/api/login")
	public ResponseEntity<Object> loginUser(@RequestBody Map<String,String> data)
	{
		if(!userService.authenticate(data.get("username"),data.get("password")))
			return new ResponseEntity<>("Invalid Username or Password", HttpStatus.NOT_FOUND);
		Session existing = sessionService.getSessionByUsername(data.get("username"));
		if(existing!=null && sessionService.checkSessionId(existing.getSessionId()))
		{
			return new ResponseEntity<>(convertToDto(existing),HttpStatus.OK);
		}
		sessionLogService.saveSession(sessionService.getSessionByUsername(data.get("username")));
		return new ResponseEntity<>(convertToDto(sessionService.getNewSession(data.get("username"))),HttpStatus.OK);
	}
	
	@PostMapping("/api/user")
	public ResponseEntity<Object> getUserDetails(@RequestBody Map<String,String> request)
	{
		if(!sessionService.checkSessionId(request.get("sessionId")))
			return new ResponseEntity<>("Invalid Session Id",HttpStatus.NOT_FOUND);
		Session session = sessionService.getSession(request.get("sessionId"));

		return new ResponseEntity<>(convertToDto(userService.getUserDetails(session.getUsername())),HttpStatus.OK);
	}
	
	@PostMapping("/api/logout")
	public ResponseEntity<Object> logoutUser(@RequestBody Map<String,String> request)
	{
		Session session = sessionService.getSession(request.get("sessionId"));
		if(sessionService.logoutSession(request.get("sessionId"))){
			sessionLogService.saveSession(session);
			return new ResponseEntity<>("Logout Successful",HttpStatus.OK);
		}
		else
			return new ResponseEntity<>("Logout Unsuccessful",HttpStatus.OK);
	}
	
	@PostMapping("/api/updateContact")
	public ResponseEntity<Object> updateContact(@RequestBody Map<String,String> request)
	{
		if(sessionService.checkSessionId(request.get("sessionId")))
		{
			return new ResponseEntity<>(userService.updateContact(sessionService.getSession(request.get("sessionId")).getUsername(),request.get("contact")),HttpStatus.OK);
		}
		else
			return new ResponseEntity<>("Invalid Session ID", HttpStatus.OK);
	}

	@PostMapping("/api/attendance")
	public ResponseEntity<Object> markAttendance(@RequestBody Map<String,String> request)
	{
		if(sessionService.checkSessionId(request.get("sessionId")))
		{
			String username = sessionService.getSession(request.get("sessionId")).getUsername();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			Date date = new Date();
			Timestamp today =  getTimestamp(dateFormat.format(date).toString());
			if(attendanceService.Attendance(username,today,new Timestamp(System.currentTimeMillis())).isEmpty())
				return new ResponseEntity<>(attendanceService.markAttendance(username),HttpStatus.OK);
			else
				return new ResponseEntity<>("Already Marked Present", HttpStatus.OK);
		}
		else
			return new ResponseEntity<>("Invalid Session ID", HttpStatus.OK);
	}

	@PostMapping("/api/get_attendance")
	public ResponseEntity<Object> getAttendance(@RequestBody Map<String,String> request_load)
	{
		if(sessionService.checkSessionId(request_load.get("sessionId")))
		{
			return new ResponseEntity<>(attendanceService.Attendance(sessionService.getSession(request_load.get("sessionId")).getUsername(),getTimestamp(request_load.get("startTime")),getTimestamp(request_load.get("endTime"))),HttpStatus.OK);
		}
		else
			return new ResponseEntity<>("Invalid Session ID", HttpStatus.OK);
	}

	private Timestamp getTimestamp(String timestamp)
	{
		System.out.println(timestamp);
		if(timestamp==null)
			return null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		Date parsedDate = null;
		try {
			parsedDate = dateFormat.parse(timestamp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new java.sql.Timestamp(parsedDate.getTime());
	}

	private SessionDto convertToDto(Session session) {
		return new SessionDto(session.getSessionId(),new Timestamp(session.getStartTime().getTime()+30*60*1000));
	}

	private UserDto convertToDto(User user) {
		return new UserDto(user.getUsername(),user.getContact(),user.getName());
	}
}
