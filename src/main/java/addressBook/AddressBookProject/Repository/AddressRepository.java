package addressBook.AddressBookProject.Repository;

import addressBook.AddressBookProject.Model.AddressBookModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<AddressBookModel,Long> {
}