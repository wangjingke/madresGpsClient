# madresGpsClient
This is the source code of the location tracking app [madresGPS](https://play.google.com/store/apps/details?id=com.wangjingke.madresgps). It is specially designed for the USC [MADRES](http://madrescenter.blogspot.com/) study with a few special features.
* The location data is encrypted with the AES algorithm according to IRB requirements, so no sensible personal information will be leaked when the phone gets lost. The decryption of data can be easily implemented in all sorts of platforms, like [this one](http://wangjingke.com/madresGpsDecryption/index.html) in javascript, and [this one](https://github.com/wangjingke/madresGpsProcess) in java.
* Participant ID will be shown as a notification on the phone all the time, and the results will be renamed with the participant ID once the recording is over.
* The start and termination of the app is controlled by inputting from command line, so it is unlikely for the participant to accidentally terminate the recording prematurely.
* The geographic locations from network (cellular and WiFi) and GPS are separated and recorded simultaneously so that further post-analysis can be conducted utilizing the data from only one of the two sources or both.
* The GPS location data contains information regarding accuracy and speed, as well as the number of satellites in view, and the number of satellites in use for estimation and determination of signal accuracy
* The network location data contains information regarding accuracy of measurements and the status of WiFi connection, such as whether the WiFi adapter is on, whether the phone is connected to WiFi signal so that the surrounding environment (indoor vs. outdoor) can be determined, which is specially beneficially for environmental exposure assessment.
* Two mode option
    * the wake lock mode uses partial `WakeLock` in android, and keep the phone awake all the time. This will generate accurate and constant recording at specified intervals, but it may drain the phone battery quickly, usually ~30 hrs.
    * the wake timer mode uses a combination of `AlarmManager` and `BroadcastReceiver` to wake up the phone regularly and record the location. This mode generates relatively constant recording with trivial variation in intervals, and it is much more battery friendly.
* Customizable recording intervals
* The recording will pause when the phone is powered off, but it will resume after the phone is on again.

More details about the code can be found in [this post](http://wangjingke.com/2016/09/23/Multiple-ways-to-schedule-repeated-tasks-in-android).
