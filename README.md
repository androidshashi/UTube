### UTube - Play youtube video in background.

#### This is only for educational purpose and not meant for use for personal profit.


This project is demonstration of Background Service in android along with floating window service.

### Output

![utube](https://github.com/androidshashi/UTube/assets/91884965/a087cf6b-6fa7-4693-bb3c-d0a3a8a09510)


### Idea

I thought of making this project as a fun of playing YouTube video in background in android devices.
I went through many articles, idea and then suddenly one day I thought of this.

### How it works

1. Create a background service that runs continuously. (Above android 8.0 you need to show notification by implementing foreground services).
2. Create a floating window that can float over other apps as a dialog. (For this you have to user overlay feature)
3. Embed a webview inside this floating dialog and load youtube url in this.
4. Resize the floating window as per the requirement.

### Features

1. Play youtube videos as you do in native YouTube app
2. Play videos as audio
3. Play in backgoudn mode while performing other tasks
4. Play in when your phone is locked

### Limitations:
1. Ads still popup as you get in normal YouTube app.
2. You have to install .apk file manually in your Phone
3. Can not play videos in landscape mode

### Improvements
1. Working on removing ads (Tried a lot of ways but not getting breakthrough.)

