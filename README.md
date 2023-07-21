# spaceZ
This is a viewer app for tutorial videos. Also user is able to buy a video tutorial inside the app with a paypal account. This app is a companion app for the http://spacez.live web application

Content.java
In this class is the code for insert/edit comments, and likes for each video. Only the register users are able to comment and like videos. Also this class containes the Exoplayer and an inner class for downloading the payed or free video.

UserInfo.java
This class is a part of the payment mechanism. My app use the paypal sdk for payments (implementation 'com.paypal.sdk:paypal-android-sdk:')

Videos.java
This class presents the list of videos in each tutorial, with the helpof a custom adapter 
