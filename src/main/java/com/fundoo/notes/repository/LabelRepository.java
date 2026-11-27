package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Label;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for labels, always scoped to one user.
 *
 * @author Navya Raj
 */
@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    List<Label> findByUserIdOrderByNameAsc(Long userId);

    Optional<Label> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
