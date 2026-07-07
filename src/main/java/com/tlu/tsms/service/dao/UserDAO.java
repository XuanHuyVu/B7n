package com.tlu.tsms.service.dao;

import com.tlu.tsms.entity.UserEntity;
import com.tlu.tsms.repository.UserRepository;
import com.tlu.tsms.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDAO extends BaseDAO {

    private final UserRepository userRepository;

    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void save(UserEntity entity) {
        String adCode = getAdCode().orElse("SYSTEM");
        Date now = DateUtils.now();
        Optional.ofNullable(entity)
                .map(e -> {
                    if (Objects.isNull(e.getId())) {
                        e.setCreatedBy(adCode);
                        e.setCreatedDate(now);
                    }
                    e.setLastUpdatedBy(adCode);
                    e.setLastUpdatedDate(now);
                    return e;
                })
                .ifPresent(userRepository::save);
    }

    public Optional<UserEntity> findActiveById(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id);
    }

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsActiveByEmail(String email) {
        return userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    public boolean existsActiveByUserCode(String userCode) {
        return userRepository.existsByUserCodeAndDeletedAtIsNull(userCode);
    }

    public Page<UserEntity> findAllActive(Pageable pageable) {
        return userRepository.findAllActive(pageable);
    }

    public Page<UserEntity> searchActive(String q, Pageable pageable) {
        return userRepository.searchActive(q, pageable);
    }

    public Page<UserEntity> findAllDeleted(Pageable pageable) {
        return userRepository.findAllDeleted(pageable);
    }

    public void delete(UserEntity entity) {
        userRepository.delete(entity);
    }
}
