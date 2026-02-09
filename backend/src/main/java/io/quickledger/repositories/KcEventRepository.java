package io.quickledger.repositories;

import io.quickledger.entities.KcEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KcEventRepository extends JpaRepository<KcEvent, Long> {
}