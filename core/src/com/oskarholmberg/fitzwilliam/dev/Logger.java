package com.oskarholmberg.fitzwilliam.dev;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Logger {

    private FileHandle logFile = Gdx.files.local("log.txt");

    public void logAction(String action){
        logFile.writeString(action, true);
        logFile.writeString("\n", true);
    }
}
