package edu.upc.citm.android.speakerfeedback;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App extends Application
{
    public static final String channel_ID = "SpeakerFeedback";

    static class Room {

        private String id;
        private String name;
        public Room (String name, String id) {
            this.id = id;
            this.name = name;
        }

        public String getName(){
            return name;
        }

        public String getId() {
            return id;
        }
    }

    public List<Room> prevRooms;

    @Override
    public void onCreate()
    {
        prevRooms = new ArrayList<>();
        loadPrevRoomsList();
        CreateNotificationChannels();
        super.onCreate();
    }

    public void Save() {
        savePrevRoomList();
    }

    private void CreateNotificationChannels()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(channel_ID, "SpeakerFeedbackChannel", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public boolean addPrevRoom(Room room){

        for (Room currentRoom : prevRooms) {
            if (currentRoom.id.equals(room.id))
                return false;
        }

        // We use this for limit the number of recent rooms,
        if (prevRooms.size() == 4)
            prevRooms.remove(3);

        prevRooms.add(room);
        return true;
    }

    public boolean deletePrevRoom(Room room)
    {
        if (!prevRooms.contains(room))
            return false;

        prevRooms.remove(room);
        return true;
    }

    private void savePrevRoomList() {
        try {
            FileOutputStream outputStream = openFileOutput("prevRoomList.txt", MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);

            for (int i = 0; i < prevRooms.size(); i++) {
                Room room = prevRooms.get(i);
                writer.write(String.format("%s;%s\n", room.getName(), room.getId()));
            }
            writer.close();
        }
        catch (FileNotFoundException e) {
            Log.e("SpeakerFeedback", "saveRecentRoomsList: FileNotFoundException");
        }
        catch (IOException e) {
            Log.e("SpeakerFeedback", "saveRecentRoomsList: IOException when write");
        }
    }

    private void loadPrevRoomsList() {
        try {
            FileInputStream inputStream = openFileInput("recentRoomList.txt");
            InputStreamReader reader = new InputStreamReader(inputStream);
            Scanner scanner = new Scanner(reader);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");
                prevRooms.add(new Room(parts[0], parts[1]));
            }
        }
        catch (FileNotFoundException e) {
            Log.e("SpeakerFeedback", "readRoomsList: FileNotFoundException");
        }
    }
}

