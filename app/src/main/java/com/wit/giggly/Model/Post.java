package com.wit.giggly.Model;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.firebase.database.Exclude;

public class Post extends BaseAdModel {
    private String description;
    private String image;
    private String postId;
    private String audio;
    private String publisher;
    private Long timestamp;
    private String title;
    private String category;
    private int points;
    private int likesCount;
    private int dislikescount;
    private int commentcount;

    public Post() {
        super(false, null);
        timestamp = System.currentTimeMillis();
    }

    public Post(NativeAd nativeAd) {
        super(true, nativeAd);
        timestamp = System.currentTimeMillis();
    }



    public Post(String description, String image, String postId, String publisher, String category,String title,int points) {
        super(false, null);
        this.description = description;
        this.image = image;
        this.postId = postId;
        this.publisher = publisher;
        this.timestamp = System.currentTimeMillis();
        this.category = category;
        this.title = title;
        this.points = points;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String imageURL) {
        this.image = imageURL;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Exclude
    public int getLikesCount() {
        return likesCount;
    }

    @Exclude
    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getDislikescount() {
        return dislikescount;
    }

    public void setDislikescount(int dislikescount) {
        this.dislikescount = dislikescount;
    }

    public int getCommentcount() {
        return commentcount;
    }

    public void setCommentcount(int commentcount) {
        this.commentcount = commentcount;
    }
}
