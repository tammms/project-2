const CLIENT_ID = "NOT_SET"
const API_KEY = "NOT_SET"

const DISCOVERY_DOC =
  "https://www.googleapis.com/discovery/v1/apis/calendar/v3/rest";
  const SCOPES = "https://www.googleapis.com/auth/calendar";


  let tokenClient;
  let gapiInited = false;
  let gisInited = false;



  function gapiLoaded() {
    gapi.load("client", initializeGapiClient);
  }

  
  async function initializeGapiClient() {
    await gapi.client.init({
      apiKey: API_KEY,
      discoveryDocs: [DISCOVERY_DOC],
    });
    gapiInited = true;
  }


  function gisLoaded() {
    tokenClient = google.accounts.oauth2.initTokenClient({
      client_id: CLIENT_ID,
      scope: SCOPES,
      callback: "", // defined later
    });
    gisInited = true;
  }


  function createGoogleEvent(eventDetails) {
    tokenClient.callback = async (resp) => {
      if (resp.error !== undefined) {
        throw resp;
      }
      await scheduleEvent(eventDetails);
    };
    if (gapi.client.getToken() === null) {
      tokenClient.requestAccessToken({ prompt: "consent" });
    } else {
      tokenClient.requestAccessToken({ prompt: "" });
    }
  }

  function scheduleEvent(eventDetails) {

    const attendees = eventDetails.attendees.map((email) => ({ email }));
    const isRepeat = eventDetails.recurrence !="";

    let event;

    if(isRepeat){
    event = {
      summary: eventDetails.summary,
      location: eventDetails.location,
      description: eventDetails.description,
      start: {
        dateTime: eventDetails.startTime,
        timeZone: "Asia/Singapore",
      },
      end: {
        dateTime: eventDetails.endTime,
        timeZone: "Asia/Singapore",
      },
      recurrence: [eventDetails.recurrence],
      attendees: attendees,
      reminders: {
        useDefault: false,
        overrides: [
          { method: "email", minutes: 24 * 60 },
          { method: "popup", minutes: 10 },
        ],
      },
    };} else{
      event = {
        summary: eventDetails.summary,
        location: eventDetails.location,
        description: eventDetails.description,
        start: {
          dateTime: eventDetails.startTime,
          timeZone: "Asia/Singapore",
        },
        end: {
          dateTime: eventDetails.endTime,
          timeZone: "Asia/Singapore",
        },
        attendees: attendees,
        reminders: {
          useDefault: false,
          overrides: [
            { method: "email", minutes: 24 * 60 },
            { method: "popup", minutes: 10 },
          ],
        },
      };
    }

    console.info("Event INput: ", event)
    const request = gapi.client.calendar.events.insert({
      calendarId: "primary",
      resource: event,
    });
    request.execute(function (event) {
      console.info("Event created: " + event.htmlLink);
    });
  }

  
