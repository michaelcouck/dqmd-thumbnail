package com.doqmind.thumbnail.database;

import com.doqmind.thumbnail.model.Thumbnail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Interface of JPARepository for Thumbnail database access.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 09-11-2023
 */
@Repository
@EnableTransactionManagement
@Transactional(value = Transactional.TxType.SUPPORTS)
public interface ThumbnailRepository extends JpaRepository<Thumbnail, Long> {

    @SuppressWarnings("JpaQlInspection")
    @Query(value = "select o from Thumbnail o where o.thumbnailId = :thumbnailId")
    List<Thumbnail> getThumbnailById(final String thumbnailId);

}