// importScripts('https://www.gstatic.com/firebasejs/13.5.2/firebase-app-compat.js');
// importScripts('https://www.gstatic.com/firebasejs/13.5.2/firebase-messaging-compat.js');

importScripts('https://www.gstatic.com/firebasejs/9.1.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.1.0/firebase-messaging-compat.js');

firebase.initializeApp({
    apiKey: "AIzaSyDvHcdQr0rkGfODYcZIFtMtQ27fxZUEMYM",
    authDomain: "project2-20aa7.firebaseapp.com",
    projectId: "project2-20aa7",
    storageBucket: "project2-20aa7.appspot.com",
    messagingSenderId: "1082244665867",
    appId: "1:1082244665867:web:9584f28cfbe0db43b36d0e",
    measurementId: "G-HFTX7BFVLR"
});
const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] Received background message ', payload.data.title);
    // Customize notification here
    const notificationTitle = payload.data.title;
    const notificationOptions = {
      body: payload.data.body,
    //   icon: '/firebase-logo.png'
    };
  
    self.registration.showNotification(notificationTitle, notificationOptions);
  });


