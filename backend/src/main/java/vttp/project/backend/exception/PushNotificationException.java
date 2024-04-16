package vttp.project.backend.exception;

public class PushNotificationException extends Exception {

    public PushNotificationException(){
        super();
    }

    public PushNotificationException(String errorMessage){
        super(errorMessage);
    }
    
}