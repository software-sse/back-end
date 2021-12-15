package com.example.demo.service;

import com.example.demo.controller.resultBody.*;
import com.example.demo.model.Comment;
import com.example.demo.model.CommentId;
import com.example.demo.repository.commentRepository;
import com.example.demo.repository.goodRepository;
import com.example.demo.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class CommentService implements IDGenenrator{
    @Autowired
    commentRepository commentRepo;
    @Autowired
    userRepository userRepo;
    @Autowired
    goodRepository goodRepo;

    public String getText(String comment_id,Integer level){
        return commentRepo.getText(comment_id,level);
    }

    public Comment getComment(String comment_id,Integer level){
        return commentRepo.getComment(comment_id,level);
    }

    public void newComment(String u_id,String g_id,String text){
        if(!userRepo.existsById(u_id)||!goodRepo.existsById(g_id)||(text==null)||text.isEmpty()){return;}
        Comment comment=new Comment();
        comment.setId(generateID(10),0);
        //CommentId id=new CommentId();
        //id.setCommentId(generateID(10));
        //id.setLevel(0);
        //comment.setId(id);
        comment.setDate(Instant.now());
        comment.setText(text);
        comment.setGoodId(g_id);
        comment.setUserId(u_id);
        commentRepo.save(comment);
        //commentRepo.newComment(generateID(10),u_id,g_id,
        //        text,Instant.now(),0);
    }

    public void addComment(String c_id,String u_id,String text){
        if(commentRepo.ifExistsByCommentID(c_id)==0){return;}
        Comment comment=new Comment();
        comment.setId(c_id,commentRepo.getCurrentLevel(c_id)+1);
        comment.setUserId(u_id);
        comment.setGoodId(commentRepo.getGoodIdByCommentId(c_id));
        comment.setText(text);
        comment.setDate(Instant.now());
//        commentRepo.newComment(c_id,u_id,commentRepo.getGoodIdByCommentId(c_id),
//                text,Instant.now(),commentRepo.getCurrentLevel(c_id)+1);
        commentRepo.save(comment);
    }

    public List<commentListBody> getAllByGood(String good_id){
        if(goodRepo.existsById(good_id)) {
            List<Comment> commentList = commentRepo.getAllbyGood(good_id);
            List<commentListBody> result = new ArrayList<commentListBody>();
            for (Comment c : commentList) {
                commentListBody element = new commentListBody(c.getId().getCommentId(),
                        userRepo.getName(c.getUserId()), c.getText(), c.getDate());
                result.add(element);
            }
            return result;
        }
        return null;
    }

    public List<commentListBody> listAllBelow(String comment_id){
        if(commentRepo.ifExistsByCommentID(comment_id)==1){
            List<commentListBody> result=new ArrayList<commentListBody>();
            List<Comment> commentList = commentRepo.getAllBehind(comment_id);
            for(Comment c:commentList){
                result.add(new commentListBody(comment_id,
                        userRepo.getName(c.getUserId()),
                        c.getText(),c.getDate()));
            }
            return result;
        }
        return null;
    }

    public void deleteComment(String comment_id,Integer level){
        if(commentRepo.existsById(new CommentId(comment_id,level))){
            if(level>0) {
                commentRepo.delete(commentRepo.findById(new CommentId(comment_id, level)).get());
                commentRepo.retrieveLevel(comment_id, level);
            }else{
                commentRepo.removeComment(comment_id);
            }
        }
    }

    @Override
    public StringBuilder tryGetID(int length) {
        StringBuilder id=new StringBuilder();
        Random rd = new SecureRandom();
        for(int i=0;i<length;i++){
            int bit = rd.nextInt(10);
            id.append(String.valueOf(bit));
        }
        return id;
    }

    @Override
    public String generateID(int length) {
        while(true)
        {
            StringBuilder id=tryGetID(length);
            if(commentRepo.ifExistsByCommentID(id.toString())==0) return id.toString();
        }
    }
}