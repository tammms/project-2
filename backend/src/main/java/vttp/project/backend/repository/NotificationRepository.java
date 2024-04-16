package vttp.project.backend.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;


import vttp.project.backend.model.NotificationRequest;
import vttp.project.backend.service.Utils;

@Repository
public class NotificationRepository {

    @Autowired
    MongoTemplate template;

    public Boolean addNotificationSchedule(NotificationRequest request) {

        String patiendId = request.getPatientId();
        String guardianId = request.getGuardianId();

        // delete notification record first before adding
        deteleNotificationSchedule(patiendId, guardianId);

        // only add record to db if input reminder > 0
        if (request.getReminderFrequencies().size() > 0) {

            Boolean result = template.insert(request) != null;
            System.out.println("Insert DB successful: " + result);
            return result;
        }
        return false;
    }

    public boolean deteleNotificationSchedule(String patientId, String guardianId) {
        Query query = new Query();

        query.addCriteria(Criteria.where("patientId").is(patientId));
        query.addCriteria(Criteria.where("guardianId").is(guardianId));

         return template.remove(query, "notifications").getDeletedCount()>0;

    }

    public List<NotificationRequest> getAllNotificationSchedule(String guardianId) {
        

        Query query = new Query();

        query.addCriteria(Criteria.where("guardianId").is(guardianId));
        query.fields()
            .exclude("class", "_id");

        List<Document> result = template.find(query, Document.class, "notifications");

        //  {"guardianId": "qwer1235", "patientId": "zxcv0995", "patientFirstName": "belly", "timings": {"beforeBreakfast": "04:02", "afterBreakfast": "09:00", "beforeLunch": "12:00", "afterLunch": "13:00", "beforeDinner": "18:00", "afterDinner": "20:00"}, "reminderFrequencies": ["beforeDinner", "beforeBreakfast", "beforeLunch"], "hasWeekly": true, "weeklyStart": 1, "_class": "vttp.project.backend.model.NotificationRequest"}
        List<NotificationRequest> remindersList = new LinkedList<>();

        if (result.size()>0) {
            for(Document doc : result){
                // System.out.println("Notification Request from DB: " + doc.toJson());
                String docString = doc.toJson();

                NotificationRequest request = Utils.stringToNotificationRequest(docString);
                remindersList.add(request);
            }
        }
        // System.out.println("\n get notifications is ok: ");

        // System.out.println("\n NOtifications from DB: " + remindersList.toString());
        return remindersList;

    }

    public Optional<String> getPatientNotificationSchedule(String guardianId, String patientId) {
        

        Query query = new Query();

        query.addCriteria(Criteria.where("guardianId").is(guardianId));
        query.addCriteria(Criteria.where("patientId").is(patientId));
        query.fields()
            .exclude("class", "_id");

        Document result = template.findOne(query, Document.class, "notifications");

        if (result == null) {
            return Optional.empty();
        } else {
            String schedule = result.toJson();
            System.out.println("Get notification schedule as string: " + schedule);

            return Optional.of(schedule);
        }

    }



    

}
