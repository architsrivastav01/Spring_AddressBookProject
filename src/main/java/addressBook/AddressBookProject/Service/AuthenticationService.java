package addressBook.AddressBookProject.Service;


import addressBook.AddressBookProject.DTO.AuthUserDTO;
import addressBook.AddressBookProject.DTO.LoginDTO;
import addressBook.AddressBookProject.Exception.UserException;
import addressBook.AddressBookProject.Interface.IAuthenticationService;
import addressBook.AddressBookProject.Repository.AuthenticationRepository;
import addressBook.AddressBookProject.Util.EmailSenderService;
import addressBook.AddressBookProject.Util.jwttoken;
import addressBook.AddressBookProject.Model.AuthUser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AuthenticationService implements IAuthenticationService {

    @Autowired
    AuthenticationRepository authUserRepository;

    @Autowired
    jwttoken tokenUtil;

    @Autowired
    EmailSenderService emailSenderService;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Override
    public AuthUser register(AuthUserDTO userDTO) throws Exception {
        try {
            log.info("Registering new user: {}", userDTO.getEmail());

            Optional<AuthUser> existingUser = Optional.ofNullable(authUserRepository.findByEmail(userDTO.getEmail()));
            if (existingUser.isPresent()) {
                log.error("User already exists: {}", existingUser.get().getEmail());
                throw new UserException("User with this email already exists.");
            }

            AuthUser user = new AuthUser(userDTO);
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

            authUserRepository.save(user);

            String token = tokenUtil.createToken(user.getUserId());

            emailSenderService.sendEmail(
                    user.getEmail(),
                    "Registered Successfully!",
                    "Hi " + user.getFirstName() + ",\nYou have been successfully registered!\n\n"
                            + "Your token: " + token);

            log.info("User {} registered successfully.", user.getEmail());

            return user;

        } catch (Exception e) {
            log.error("Error occurred while registering user: {}", e.getMessage());
            throw new UserException("Registration failed due to an internal error. Please try again.");
        }
    }


    @Override
    public String login(LoginDTO loginDTO) {
        try {
            log.info("Login attempt for email: {}", loginDTO.getEmail());
            Optional<AuthUser> user = Optional.ofNullable(authUserRepository.findByEmail(loginDTO.getEmail()));

            if (user.isPresent()) {
                if (passwordEncoder.matches(loginDTO.getPassword(), user.get().getPassword())) {
                    log.info("Login successful for user: {}", user.get().getEmail());

                    // Token generate and send on login
                    String token = tokenUtil.createToken(user.get().getUserId());

                    emailSenderService.sendEmail(
                            user.get().getEmail(),
                            "Logged in Successfully!",
                            "Hi " + user.get().getFirstName() + ",\n\n"
                                    + "You have successfully logged into Greeting App!\n"
                                    + "Your token: " + token);

                    return "Congratulations!! You have logged in successfully! Token: " + token;
                } else {
                    log.warn("Login failed: Incorrect password for email: {}", loginDTO.getEmail());
                    throw new UserException("Sorry! Email or Password is incorrect!");
                }
            } else {
                log.warn("Login failed: No user found for email: {}", loginDTO.getEmail());
                throw new UserException("Sorry! Email or Password is incorrect!");
            }

        } catch (Exception e) {
            log.error("Error during login process: {}", e.getMessage());
            throw new UserException("Login failed due to an internal error. Please try again.");
        }
    }


    @Override
    public String forgotPassword(String email, String newPassword) {
        try {
            log.info("Processing forgot password request for email: {}", email);
            AuthUser user = authUserRepository.findByEmail(email);

            if (user == null) {
                log.warn("Forgot password request failed: No user found for email: {}", email);
                throw new UserException("Sorry! We cannot find the user email: " + email);
            }

            String encryptedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encryptedPassword);
            authUserRepository.save(user);

            emailSenderService.sendEmail(
                    user.getEmail(),
                    "Password Forget Updation Successful",
                    "Hi " + user.getFirstName() + ",\n\nYour password has been successfully changed!");

            log.info("Password updated successfully for email: {}", email);

            return "Password has been changed successfully!";
        } catch (Exception e) {
            log.error("Error during forgot password process: {}", e.getMessage());
            throw new UserException("Error occurred while updating password. Please try again.");
        }
    }

    @Override
    public String resetPassword(String email, String currentPassword, String newPassword) {
        try {
            log.info("Resetting password for email: {}", email);
            AuthUser user = authUserRepository.findByEmail(email);

            if (user == null) {
                log.warn("Password reset failed: No user found for email: {}", email);
                throw new UserException("User not found with email: " + email);
            }

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                log.warn("Password reset failed: Incorrect current password for email: {}", email);
                throw new UserException("Current password is incorrect!");
            }

            String encryptedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encryptedPassword);
            authUserRepository.save(user);

            emailSenderService.sendEmail(
                    user.getEmail(),
                    "Password Reset Successful",
                    "Hi " + user.getFirstName() + ",\n\nYour password has been successfully updated!");

            log.info("Password reset successful for email: {}", email);

            return "Password reset successfully!";
        } catch (Exception e) {
            log.error("Error during password reset process: {}", e.getMessage());
            throw new UserException("Error occurred while resetting password. Please try again.");
        }
    }
}