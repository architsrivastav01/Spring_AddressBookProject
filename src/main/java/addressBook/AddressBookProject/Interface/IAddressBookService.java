package addressBook.AddressBookProject.Interface;

import addressBook.AddressBookProject.DTO.AddressBookDTO;

import java.util.List;

public interface IAddressBookService {
    List<AddressBookDTO> getAllContacts();
    AddressBookDTO saveContact(AddressBookDTO dto);
}