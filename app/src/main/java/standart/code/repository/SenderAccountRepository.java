package standart.code.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import standart.code.model.SenderAccount;

@Repository
public interface SenderAccountRepository extends JpaRepository<SenderAccount, Long> {


}
