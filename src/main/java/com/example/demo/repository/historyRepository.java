package com.example.demo.repository;

import com.example.demo.model.History;
import com.example.demo.model.HistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface historyRepository extends JpaRepository<History, HistoryId> {

    @Query(value = "select if((select count(*) from history where user_id=?1 and good_id=?2)>0,1,0)",nativeQuery = true)
    Integer existsById(String user_id,String good_id);


    @Query(value = "select * from history where user_id=?1 and good_id=?2",nativeQuery = true)
    History getOneHistory(String user_id, String good_id);


    @Modifying
    @Query(value = "delete from history where user_id=?1 and good_id=?2",nativeQuery = true)
    void removeOneHistory(String user_id,String good_id);


    @Modifying
    @Query(value = "delete from history where user_id=?1",nativeQuery = true)
    void removeAllHistory(String user_id);


    @Query(value = "select * from history where user_id=?1 order by date desc",nativeQuery = true)
    List<History> getAllHistory(String user_id);
}
