package vttp.project.backend.exception;

public class PatientException extends Exception {

    public PatientException(){
        super();
    }

    public PatientException(String errorMessage){
        super(errorMessage);
    }
    
}
