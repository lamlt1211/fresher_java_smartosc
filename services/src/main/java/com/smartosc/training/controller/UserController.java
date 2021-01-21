package com.smartosc.training.controller;

import com.smartosc.training.dto.UserDTO;
import com.smartosc.training.dto.VerificationTokenDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping("user/{id}")
	public ResponseEntity<APIResponse<UserDTO>> findById(@PathVariable("id") Integer idProduct) {
		UserDTO userDTO = userService.findById(idProduct);
		APIResponse<UserDTO> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setMessage("Find By Id successful");
		responseData.setData(userDTO);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@GetMapping("user/username/{username}")
	public ResponseEntity<APIResponse<UserDTO>> findByUsername(@PathVariable("username") String username) {
		UserDTO userDTO = userService.findByUserName(username);
		APIResponse<UserDTO> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setMessage("Find By Id successful");
		responseData.setData(userDTO);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	// update for register
	@PostMapping("user")
	public ResponseEntity<APIResponse<UserDTO>> registerNewUser(@RequestBody UserDTO userDTO) {
		APIResponse<UserDTO> responseData = new APIResponse<>();
		if (isEmailExits(userDTO.getEmail()) || isUsernameExist(userDTO.getUserName())) {
			responseData.setStatus(HttpStatus.BAD_REQUEST.value());
			responseData.setMessage("Register new user failed");
			return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
		} else {
			userDTO.setStatus(0);
			UserDTO response = userService.addUser(userDTO);
			responseData.setStatus(HttpStatus.OK.value());
			responseData.setMessage("Register new user successful");
			if (response != null) {
				responseData.setData(response);
			}
			return new ResponseEntity<>(responseData, null, HttpStatus.OK);
		}
	}
	private boolean isUsernameExist(String userName) {
		UserDTO user = userService.findByUserName(userName);
		return (user != null);
	}

	private boolean isEmailExits(String email) {
		UserDTO user = userService.findByEmail(email);
		return (user != null);
	}

	@DeleteMapping("user/{id}")
	public ResponseEntity<APIResponse<UserDTO>> deleteProductById(@PathVariable(name = "id") int id) {
		userService.deleteUserById(id);
		APIResponse<UserDTO> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setMessage("ADD User successful");
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	// bonus for security
	@GetMapping("user/email/{email}")
	public ResponseEntity<APIResponse<UserDTO>> findByEmail(@PathVariable("email") String email) {
		UserDTO userDTO = userService.findByEmail(email);
		APIResponse<UserDTO> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setMessage("Get user success!");
		responseData.setData(userDTO);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	// update for user management
	@GetMapping("users")
	public ResponseEntity<APIResponse<Page<UserDTO>>> getAllUsers(
			@RequestParam(defaultValue = "", required = false) String searchValue,
			@RequestParam(defaultValue = "0", required = false) Integer page,
			@RequestParam(defaultValue = "5", required = false) Integer size,
			@RequestParam(defaultValue = "userId", required = false) String sortBy) {
		Page<UserDTO> users = userService.getAllUser(searchValue, page, size, sortBy);
		APIResponse<Page<UserDTO>> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setMessage("Find all customer successful");
		responseData.setData(users);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@PostMapping("user/generate-verify-token")
	public ResponseEntity<APIResponse<String>> generateVerificationToken(@RequestBody UserDTO userDTO) {
		String token = userService.createVerificationToken(userDTO);
		APIResponse<String> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(token);
		responseData.setMessage("generate verification token for mail successfull");
		return new ResponseEntity<>(responseData, null, HttpStatus.OK);
	}

	@GetMapping("user/verify-token/{token}")
	public ResponseEntity<APIResponse<VerificationTokenDTO>> getVerificationToken(@PathVariable("token") String token) {
		VerificationTokenDTO verifyToken = userService.getVerificationToken(token);
		APIResponse<VerificationTokenDTO> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(verifyToken);
		responseData.setMessage("get verification token successfull");
		return new ResponseEntity<>(responseData, null, HttpStatus.OK);
	}

	@PostMapping("user/active")
	public ResponseEntity<APIResponse<UserDTO>> activeRegisteredUser(@RequestBody UserDTO userDTO) {
		UserDTO user = userService.activeUser(userDTO);
		APIResponse<UserDTO> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(user);
		responseData.setMessage("get verification token successfull");
		return new ResponseEntity<>(responseData, null, HttpStatus.OK);
	}
	
	@PostMapping("user/changePassword")
	public ResponseEntity<APIResponse<UserDTO>> updateNewPassword(@RequestBody UserDTO userDTO) {
		UserDTO user = userService.saveChangePassword(userDTO);
		APIResponse<UserDTO> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(user);
		responseData.setMessage("Change password successfull");
		return new ResponseEntity<>(responseData, null, HttpStatus.OK);
	}
	
	@PostMapping("user/generate-pass-reset-token")
	public ResponseEntity<APIResponse<String>> generatePasswordResetToken(@RequestBody UserDTO userDTO) {
		String token = userService.createPasswordResetToken(userDTO);
		APIResponse<String> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(token);
		responseData.setMessage("generate password token for mail successfull");
		return new ResponseEntity<>(responseData, null, HttpStatus.OK);
	}

	@GetMapping("user/pass-reset-token/{token}")
	public ResponseEntity<APIResponse<VerificationTokenDTO>> getPasswordResetToken(@PathVariable("token") String token) {
		VerificationTokenDTO verifyToken = userService.getPasswordResetToken(token);
		APIResponse<VerificationTokenDTO> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(verifyToken);
		responseData.setMessage("get password reset token successfull");
		return new ResponseEntity<>(responseData, null, HttpStatus.OK);
	}
	
	@PostMapping("user/toggle-block")
	public ResponseEntity<APIResponse<UserDTO>> updateUserBlockedStatus(
			@RequestParam Integer id, @RequestParam Integer status) {
		UserDTO user = userService.updateUserBlockedStatus(id, status);
		APIResponse<UserDTO> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(user);
		responseData.setMessage("Update user status successfull!");
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}
	
	@PutMapping("user/update")
    public ResponseEntity<Object> updateUser(@RequestBody UserDTO userDTO) {
        UserDTO result = userService.updateUser(userDTO);
		APIResponse<UserDTO> responseData = new APIResponse<>();
		responseData.setData(result);
		if (Objects.isNull(result)) {
			responseData.setStatus(HttpStatus.NOT_FOUND.value());
			responseData.setMessage("Update user failed");
		} else {
			responseData.setStatus(HttpStatus.OK.value());
			responseData.setMessage("Update user successful");
		}
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
	
	@GetMapping("user/count")
	public ResponseEntity<APIResponse<Long>> getNumberOfUserIsUnblocked() {
		Long userNum = userService.countUserUnblocked();
		APIResponse<Long> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(userNum);
		responseData.setMessage("get number of user unblocked successfull");
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@GetMapping("users/all")
	public ResponseEntity<APIResponse<List<UserDTO>>> getAllUsers() {
		List<UserDTO> users = userService.findAllCustomer();
		APIResponse<List<UserDTO>> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(users);
		responseData.setMessage("Get all customer successfully!");
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@PostMapping("users/save_all")
	public ResponseEntity<APIResponse<Boolean>> saveAllUsers(@RequestBody List<UserDTO> users) {
		Boolean result = userService.saveAllUsers(users);
		APIResponse<Boolean> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(result);
		responseData.setMessage("Insert all customer successfully!");
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

}
