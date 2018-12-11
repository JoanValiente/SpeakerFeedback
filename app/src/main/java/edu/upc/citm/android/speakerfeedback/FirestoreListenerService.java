package edu.upc.citm.android.speakerfeedback;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreListenerService extends Service
{
    private boolean connected = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(!connected)
        {
            createForegroundNotification();
        }

        db.collection("rooms").document("testroom")
                .collection("polls").whereEqualTo("open", true)
                .addSnapshotListener(pollListener);


        return START_NOT_STICKY;
    }

    private void createForegroundNotification()
    {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this,App.channel_ID)
                .setContentTitle(String.format("Connected to 'testroom'"))
                .setSmallIcon(R.drawable.ic_message)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        connected = true;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private EventListener<QuerySnapshot> pollListener = new EventListener<QuerySnapshot>(){
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error al rebre polls", e);
                return;
            }

            for (DocumentSnapshot doc : documentSnapshots)
            {
                Poll poll = doc.toObject(Poll.class);
                if(poll.isOpen())
                {
                    Log.d("SpeakerFeedback", poll.getQuestion());
                    Intent intent = new Intent(FirestoreListenerService.this, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(FirestoreListenerService.this, 0, intent, 0);
                    Notification notification = new NotificationCompat.Builder(FirestoreListenerService.this, App.channel_ID).setContentTitle("New poll: " +String.format(poll.getQuestion()))
                            .setSmallIcon(R.drawable.ic_message)
                            .setContentIntent(pendingIntent)
                            .setVibrate(new long[] { 250, 250, 250, 250, 250 })
                            .setAutoCancel(true)
                            .build();
                    startForeground(1, notification);
                }
            }
        }
    };
}
