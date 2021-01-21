package com.smartosc.training.service;

import com.smartosc.training.dto.UserDTO;
import com.smartosc.training.dto.VerificationTokenDTO;
import com.smartosc.training.entity.PasswordResetToken;
import com.smartosc.training.entity.Role;
import com.smartosc.training.entity.Users;
import com.smartosc.training.entity.VerificationToken;
import com.smartosc.training.repositories.PasswordResetTokenRepository;
import com.smartosc.training.repositories.RoleRepository;
import com.smartosc.training.repositories.UserRepository;
import com.smartosc.training.repositories.VerificationTokenRepository;
import com.smartosc.training.utils.ConvertUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    public BCryptPasswordEncoder encoder;

    private static final String ROLE_DEFAULT = "ROLE_USER";

    public List<UserDTO> getAllUser() {
        List<Users> listUser = userRepository.findAll();
        List<UserDTO> userDTOs = new ArrayList<>();
        listUser.forEach(p -> {
            UserDTO userDTO = modelMapper.map(p, UserDTO.class);
            userDTOs.add(userDTO);
        });
        return userDTOs;
    }

    public UserDTO findById(Integer userId) {
        Users users = userRepository.findByUserId(userId);
        return modelMapper.map(users, UserDTO.class);
    }

    public void deleteUserById(int id) {
        userRepository.deleteById(id);
    }

    public Page<UserDTO> getAllUser(String searchValue, Integer pageNo, Integer pageSize, String sortBy) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Users> pageResult = userRepository.findBySearchValueAndRoles_Name(searchValue, ROLE_DEFAULT, pageable);
        return pageResult.map(ConvertUtils::convertUserToUserDTO);
    }

    // update for register

    /**
     * Register new User
     *
     * @param userDTO
     * @return new user has updated
     */
    public UserDTO addUser(UserDTO userDTO) {
        userDTO.setPassword(encoder.encode(userDTO.getPassword()));
        Users users = modelMapper.map(userDTO, Users.class);
        List<Role> roles = new ArrayList<>();
        roles.add(roleRepository.findByName(ROLE_DEFAULT));
        users.setStatus(1);
        users.setRoles(roles);
        Users response = userRepository.save(users);
        return modelMapper.map(response, UserDTO.class);
    }

    /**
     * Check a user with this username exist or not
     *
     * @param userName
     * @return user with this username
     */
    public UserDTO findByUserName(String userName) {
        Users user = userRepository.findByUserName(userName);
        if (user != null)
            return modelMapper.map(user, UserDTO.class);
        return null;
    }

    /**
     * Check a user with this email exist or not
     *
     * @param email
     * @return user with this email
     */
    public UserDTO findByEmail(String email) {
        Users user = userRepository.findByEmail(email);
        if (user != null)
            return modelMapper.map(user, UserDTO.class);
        return null;
    }

    /**
     * Create new verification token for confirm email
     *
     * @param userDTO
     * @return verification token
     */
    public String createVerificationToken(UserDTO userDTO) {
        Users user = modelMapper.map(userDTO, Users.class);
        String token = UUID.randomUUID().toString();
        VerificationToken myToken = new VerificationToken(token, user);
        return verificationTokenRepository.save(myToken).getToken();
    }

    /**
     * Find verification token fit with given token
     *
     * @param verifyToken
     * @return matched verification token
     */
    public VerificationTokenDTO getVerificationToken(String verifyToken) {
        VerificationToken token = verificationTokenRepository.findByToken(verifyToken);
        return getCommonToken(
                token.getId(),
                token.getToken(),
                token.getUser(),
                token.getExpiryDate());
    }

    /**
     * Activate a user has already confirm by email
     *
     * @param userDTO
     * @return this user
     */
    public UserDTO activeUser(UserDTO userDTO) {
        Users user = null;
        Users responseUser;
        Optional<Users> response = userRepository.findById(userDTO.getUserId());
        if (response.isPresent()) {
            user = response.get();
            user.setEnabled(true);
            responseUser = userRepository.save(user);
            return modelMapper.map(responseUser, UserDTO.class);
        } else {
            return new UserDTO();
        }
    }

    // update for reset password

    /**
     * Create new password reset token for confirm email
     *
     * @param userDTO
     * @return verification token
     */
    public String createPasswordResetToken(UserDTO userDTO) {
        Users user = modelMapper.map(userDTO, Users.class);
        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        return passwordResetTokenRepository.save(myToken).getToken();
    }

    /**
     * Find password reset token fit with given token
     *
     * @param verifyToken
     * @return matched password reset token
     */
    public VerificationTokenDTO getPasswordResetToken(String verifyToken) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(verifyToken);
        return getCommonToken(
                token.getId(),
                token.getToken(),
                token.getUser(),
                token.getExpiryDate());
    }

    /**
     * Save new password for user
     *
     * @param userDTO
     * @return this user has change password
     */
    public UserDTO saveChangePassword(UserDTO userDTO) {
        Users user = null;
        Users responseUser;
        Optional<Users> result = userRepository.findById(userDTO.getUserId());
        if (result.isPresent()) {
            user = result.get();
            user.setPassword(encoder.encode(userDTO.getPassword()));
            responseUser = userRepository.save(user);
            return modelMapper.map(responseUser, UserDTO.class);
        } else {
            return new UserDTO();
        }
    }

    /**
     * Get common type for all verify token
     *
     * @param id
     * @param verifyToken
     * @param user
     * @param expiryDate
     * @return common token
     */
    private VerificationTokenDTO getCommonToken(Long id, String verifyToken, Users user, Date expiryDate) {
        return new VerificationTokenDTO(
                id,
                verifyToken,
                modelMapper.map(user, UserDTO.class),
                expiryDate);
    }

    /**
     * Status updates are blocked or not for users
     *
     * @param id
     * @param status
     * @return user already updated
     */
    public UserDTO updateUserBlockedStatus(Integer id, Integer status) {
        Users user = null;
        Users userResponse = null;
        Optional<Users> result = userRepository.findById(id);
        if (result.isPresent()) {
            user = result.get();
            user.setStatus(status);
            userResponse = userRepository.save(user);
        }
        if (userResponse != null) {
            return ConvertUtils.convertUserToUserDTO(userResponse);
        }
        return new UserDTO();
    }

    public UserDTO updateUser(UserDTO userDTO) {
        Optional<Users> response = userRepository.findById(userDTO.getUserId());
        if (response.isPresent()) {
            Users user = response.get();
            user.setFullName(userDTO.getFullName());
            user.setUpdatedAt(new Date());
			return modelMapper.map(userRepository.save(user), UserDTO.class);
        } else {
        	return null;
		}
    }

    public Long countUserUnblocked() {
        return userRepository.countByStatus(1);
    }

    /**
     * Find all users is customer
     *
     * @return List of customer
     */
    public List<UserDTO> findAllCustomer() {
        List<Users> users = userRepository.findByRoles_Name(ROLE_DEFAULT);
        return users != null ? users.stream().map(ConvertUtils::convertUserToUserDTO).collect(Collectors.toList()) : null;
    }

    public Boolean saveAllUsers(List<UserDTO> users) {
        List<Users> prepareData = users.stream()
                .map(o -> {
                    Users oConvert = modelMapper.map(o, Users.class);
                    List<Role> roles = new ArrayList<>();
                    roles.add(roleRepository.findByName(ROLE_DEFAULT));
                    oConvert.setRoles(roles);
                    return oConvert;
                })
                .collect(Collectors.toList());
        List<Users> result = userRepository.saveAll(prepareData);
        return !result.isEmpty();
    }

}