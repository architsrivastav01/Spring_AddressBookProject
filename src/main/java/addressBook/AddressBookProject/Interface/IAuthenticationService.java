package addressBook.AddressBookProject.Interface;

import addressBook.AddressBookProject.DTO.AuthUserDTO;
import addressBook.AddressBookProject.DTO.LoginDTO;
import addressBook.AddressBookProject.Model.AuthUser;

public interface IAuthenticationService {
    AuthUser register(AuthUserDTO userDTO) throws Exception;
    String login(LoginDTO loginDTO);
    String forgotPassword(String email, String newPassword);
    String resetPassword(String email, String currentPassword, String newPassword);
}