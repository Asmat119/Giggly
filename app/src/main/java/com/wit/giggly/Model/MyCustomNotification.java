package com.wit.giggly.Model;

public class MyCustomNotification {

   private Long timestamp;
   private String goingToUserID;
   private String notifID;
   private String message;
   private String sentByUserID;
   private String notifType;
   private String postID;

   public MyCustomNotification() {
      timestamp = System.currentTimeMillis();
   }

   public MyCustomNotification(String goingToUserID, String notifID, String message, String sentByUserID,String notifType,String postID) {
      this.timestamp = System.currentTimeMillis();
      this.goingToUserID = goingToUserID;
      this.notifID = notifID;
      this.message = message;
      this.sentByUserID = sentByUserID;
      this.notifType = notifType;
      this.postID = postID;
   }

   public MyCustomNotification(String goingToUserID, String notifID, String message, String sentByUserID,String notifType) {
      this.timestamp = System.currentTimeMillis();
      this.goingToUserID = goingToUserID;
      this.notifID = notifID;
      this.message = message;
      this.sentByUserID = sentByUserID;
      this.notifType = notifType;

   }

   public String getPostID() {
      return postID;
   }

   public void setPostID(String postID) {
      this.postID = postID;
   }

   public String getNotifType() {
      return notifType;
   }

   public void setNotifType(String notifType) {
      this.notifType = notifType;
   }

   public Long getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(Long timestamp) {
      this.timestamp = timestamp;
   }

   public String getGoingToUserID() {
      return goingToUserID;
   }

   public void setGoingToUserID(String goingToUserID) {
      this.goingToUserID = goingToUserID;
   }

   public String getNotifID() {
      return notifID;
   }

   public void setNotifID(String notifID) {
      this.notifID = notifID;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getSentByUserID() {
      return sentByUserID;
   }

   public void setSentByUserID(String sentByUserID) {
      this.sentByUserID = sentByUserID;
   }
}