package com.example.demo.repository;

import com.example.demo.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface chatRepository extends JpaRepository<Chat,String> {
    @Query("select c from Chat c where c.group_id=?1 order by c.time desc")
    List<Chat> getChatsByGroup(String group_id);

    @Modifying
    @Query("update Chat c set c.isRead='1' where c.group_id=?1 and c.receiverId=?2")
    void setIsRead(String group_id,String receiver_id);

//    @Query("select min(c.isRead) from Chat c where c.receiverId=?1 and c.senderId=?2")
//    String getIsRead(String receiver,String sender);
}
