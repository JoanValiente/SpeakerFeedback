package edu.upc.citm.android.speakerfeedback;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EnterIDRoom extends AppCompatActivity {
    TextView enter_room_id;
    private  String password_text = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private App app;
    private RecyclerView prevRoomsView;
    private Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_enter);
        enter_room_id = findViewById(R.id.room_ID);

        adapter = new Adapter();
        app = (App)getApplication();
        prevRoomsView = findViewById(R.id.rooms_view);
        prevRoomsView.setLayoutManager(new LinearLayoutManager(this));
        prevRoomsView.setAdapter(adapter);

    }

    public void onClickEnterRoom(View view)
    {
        final String roomID = enter_room_id.getText().toString();

        if(roomID.equals("")) {
            Toast.makeText(this, "Enter room ID:/", Toast.LENGTH_SHORT).show();
        }
        else
        {
            db.collection("rooms").document(roomID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.i("SpeakerFeedback", documentSnapshot.toString());
                    if(documentSnapshot.exists() && documentSnapshot.contains("open"))
                    {
                        if(documentSnapshot.contains("password") && !documentSnapshot.getString("password").isEmpty())
                        {
                            comparePassword(documentSnapshot.get("password").toString());
                            App.Room room = new App.Room(documentSnapshot.getString("name"), roomID);
                            app.addPrevRoom(room);
                        }
                        else {
                            Intent data = new Intent();
                            data.putExtra("room_id", enter_room_id.getText().toString());
                            setResult(RESULT_OK, data);
                            App.Room room = new App.Room(documentSnapshot.getString("name"), roomID);
                            app.addPrevRoom(room);
                            finish();
                        }
                    }
                    else {

                        if (!documentSnapshot.exists()) {
                            Toast.makeText(EnterIDRoom.this,
                                    "Room with ID " + "'" + roomID + ":" + " NOT EXIST", Toast.LENGTH_SHORT).show();
                        } else if (!documentSnapshot.contains("open")) {
                            Toast.makeText(EnterIDRoom.this,
                                    "Room with ID " + "'" + roomID + "'" + " NOT OPEN", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EnterIDRoom.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("SpeakerFeedback", e.getMessage());
                }
            });
        }
    }
    protected  void comparePassword(final String password)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter password:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                password_text = input.getText().toString();
                if (password_text.equals(password))
                {
                    Toast.makeText(EnterIDRoom.this, "Password correct", Toast.LENGTH_SHORT).show();

                    Intent data = new Intent();
                    data.putExtra("room_id", enter_room_id.getText().toString());
                    setResult(RESULT_OK, data);
                    finish();
                }
                else
                    Toast.makeText(EnterIDRoom.this, "Password incorrect!", Toast.LENGTH_SHORT).show();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    class Adapter extends RecyclerView.Adapter<EnterIDRoom.ViewHolder> {

        @NonNull
        @Override
        public EnterIDRoom.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.prev_rooms_view, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull EnterIDRoom.ViewHolder holder, int position) {
            App.Room room = app.prevRooms.get(position);

            holder.room_name_text.setText(room.getName());
            holder.room_id_text.setText(room.getId());
        }

        @Override
        public int getItemCount() {
            return app.prevRooms.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView room_name_text;
        private TextView room_id_text;

        public ViewHolder(final View itemView) {
            super(itemView);

            room_name_text = itemView.findViewById(R.id.room_name_text);
            room_id_text = itemView.findViewById(R.id.room_id_text);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final App.Room room = app.prevRooms.get(getAdapterPosition());

                    db.collection("rooms").document(room.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                if (documentSnapshot.contains("open") && documentSnapshot.getBoolean("open")) {
                                    enter_room_id.setText(room.getId());
                                    Intent data = new Intent();
                                    data.putExtra("room_id", enter_room_id.getText().toString());
                                    setResult(RESULT_OK, data);
                                    App.Room tmp = new App.Room(documentSnapshot.getString("name"), room.getId().toString());
                                    app.addPrevRoom(tmp);
                                    finish();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(EnterIDRoom.this);
                                    String roomName = documentSnapshot.getString("name");
                                    builder.setTitle(String.format("The room '%s' is closed. Do you want to delete it from Recent Rooms?", roomName));
                                    builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            app.deletePrevRoom(room);
                                            adapter.notify();
                                        }
                                    });
                                    builder.setNegativeButton("Close", null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(EnterIDRoom.this);
                                builder.setTitle(String.format("The room doesn't exist. Do you want to delete it from Recent Rooms?"));
                                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        app.deletePrevRoom(room);
                                        adapter.notify();
                                    }
                                });
                                builder.setNegativeButton("Close", null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            });
        }
    }

}