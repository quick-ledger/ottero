
- install Capacitor: Added @capacitor/core, @capacitor/cli, and @capacitor/android dependencies.
- Initialized Capacitor: Configured the project with App ID io.quickledger.app and set dist as the web directory.
- Built the App: Successfully compiled the React app to the dist folder.
- Added Android Platform: Created the native Android project structure.
- To open the project in Android Studio: npx cap open android

if you change things
npm run build
npx cap sync
npx cap run android


---

Android does not need your React dev server (npm run dev/ run_local.sh) to be running, UNLESS you are using the "Live Reload" feature we discussed earlier.

Scenario A: Standard Mode (What 
run_android.sh does)

You run npm run build. This creates static HTML/JS/CSS files in the dist folder.
npx cap sync copies those files into the Android APK project.
npx cap run android installs that APK on the phone.
The app is now self-contained. You can stop the React dev server, turn off your internet (mostly), and the app UI will still load. It only needs the Java Backend running to get data.

Scenario B: Live Reload Mode (Development)

If you configured server: { url: ... } in 
capacitor.config.ts
 or run with npx cap run android -l.
Then the Android app basically acts like a browser pointing to your computer. If you stop the React server, the app dies/shows a white screen.
Currently: You are in Scenario A (Standard Mode). You can safely stop your React 
run_local.sh terminal. The Android app is loading the bundled files from the phone's storage.


 ----

