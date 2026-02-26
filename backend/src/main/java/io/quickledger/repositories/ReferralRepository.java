package io.quickledger.repositories;

import io.quickledger.entities.Referral;
import io.quickledger.entities.ReferralStatus;
import io.quickledger.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ReferralRepository extends CrudRepository<Referral, Long> {

    List<Referral> findByReferrer(User referrer);

    List<Referral> findByReferrerOrderByCreatedDateDesc(User referrer);

    Optional<Referral> findByRefereeEmail(String refereeEmail);

    Optional<Referral> findByRefereeEmailAndStatus(String refereeEmail, ReferralStatus status);

    Optional<Referral> findByReferrerAndRefereeEmail(User referrer, String refereeEmail);

    boolean existsByReferrerAndRefereeEmail(User referrer, String refereeEmail);
}
