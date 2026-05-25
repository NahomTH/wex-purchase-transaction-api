package com.wex.purchasetransactionservice.repository;

import com.wex.purchasetransactionservice.entity.PurchaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<PurchaseEntity, UUID> {
}
