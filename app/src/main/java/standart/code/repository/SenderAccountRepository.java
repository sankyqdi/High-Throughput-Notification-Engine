package standart.code.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import standart.code.model.SenderAccount;
import standart.code.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface SenderAccountRepository extends JpaRepository<SenderAccount, Long> {

    List<SenderAccount> findAllByOwner(User owner);

    Optional<SenderAccount> findByIdAndOwner(Long id, User owner);

}
