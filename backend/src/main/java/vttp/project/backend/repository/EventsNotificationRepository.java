package vttp.project.backend.repository;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import vttp.project.backend.model.DailyNotification;
import vttp.project.backend.model.EventsNotification;
import vttp.project.backend.service.Utils;

@Repository
public class EventsNotificationRepository {

    @Autowired
    MongoTemplate template;

    public boolean addEvent(EventsNotification event) {

        String eventId = event.getEventId();
        deleteReminder(eventId);
        return template.insert(event) != null;

    }

    public boolean addDaily(DailyNotification daily) {

        String eventId = daily.getEventId();
        deleteReminder(eventId);
        return template.insert(daily) != null;

    }



    // db.events.find(
    // {"guardianId":{$regex:"qwer1235"},
    // "reminderType":{$regex:"event"}},
    // {"_class": 0, "_id":0}
    // );

    public List<EventsNotification> getAllEvents(String guardianId) {

        Query query = new Query();

        query.addCriteria(Criteria.where("guardianId").is(guardianId));
        query.addCriteria(Criteria.where("reminderType").is("event"));

        query.fields()
                .exclude("class", "_id");

        List<Document> result = template.find(query, Document.class, "events");
        List<EventsNotification> request = new LinkedList<>();
        if (result.size() > 0) {
            for (Document doc : result) {
                String docString = doc.toJson();
                // System.out.println("Document STring" + docString);

                EventsNotification event = Utils.stringToEventNotification(docString);
                request.add(event);
            }
        }

        return request;

    }


    public List<DailyNotification> getAllDaily(String guardianId){

        Query query = new Query();

        query.addCriteria(Criteria.where("guardianId").is(guardianId));
        query.addCriteria(Criteria.where("reminderType").is("daily"));

        query.fields()
                .exclude("class", "_id");

        List<Document> result = template.find(query, Document.class, "events");
        List<DailyNotification> request = new LinkedList<>();

        if (result.size() > 0) {
            for (Document doc : result) {
                String docString = doc.toJson();
                // System.out.println("Document STring" + docString);
                DailyNotification event = Utils.stringToDailyNotification(docString);
                request.add(event);
            }
        }
        return request;
    }

    public boolean deleteReminder(String eventId) {

        Query query = new Query();
        query.addCriteria(Criteria.where("eventId").is(eventId));
        return template.remove(query, "events").getDeletedCount() > 0;

    }


    public boolean deleteReminderByPatientId(String patientId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("patientId").is(patientId));
        return template.remove(query, "events").getDeletedCount() > 0;
    }





}
