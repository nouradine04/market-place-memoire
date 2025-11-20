package com.nrd.messagerie.repository;

import com.nrd.messagerie.entity.BlockedUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedUserRepository extends MongoRepository<BlockedUser, String> {
    
    @Query("{'blockerId': ?0, 'blockedId': ?1}")
    BlockedUser findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    
    default boolean isUserBlocked(Long blockerId, Long blockedId) {
        return findByBlockerIdAndBlockedId(blockerId, blockedId) != null;
    }
    
    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}