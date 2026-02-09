package io.quickledger.repositories;

import io.quickledger.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByExternalId(String externalId);

    Optional<User> findByStripeCustomerId(String stripeCustomerId);
}