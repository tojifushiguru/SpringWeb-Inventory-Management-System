package com.springweb.repository;

import com.springweb.entity.Setting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

    Optional<Setting> findBySettingKey(String settingKey);

    List<Setting> findBySettingKeyContainingIgnoreCase(String settingKey);

    Page<Setting> findBySettingKeyContainingIgnoreCase(String settingKey, Pageable pageable);

    List<Setting> findBySettingValueContainingIgnoreCase(String settingValue);

    Page<Setting> findBySettingValueContainingIgnoreCase(String settingValue, Pageable pageable);

    List<Setting> findBySettingKeyContainingIgnoreCaseOrSettingValueContainingIgnoreCase(String settingKey,
            String settingValue);

    Page<Setting> findBySettingKeyContainingIgnoreCaseOrSettingValueContainingIgnoreCase(String settingKey,
            String settingValue, Pageable pageable);

    @Query("SELECT s FROM Setting s WHERE s.settingKey LIKE :pattern OR s.settingValue LIKE :pattern")
    List<Setting> searchByKeyOrValue(@Param("pattern") String pattern);

    @Query("SELECT s FROM Setting s ORDER BY s.settingKey")
    List<Setting> findAllOrderBySettingKey();

    boolean existsBySettingKey(String settingKey);
}