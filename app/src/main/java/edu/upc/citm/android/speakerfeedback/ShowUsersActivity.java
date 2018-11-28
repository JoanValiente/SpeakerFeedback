package edu.upc.citm.android.speakerfeedback;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShowUsersActivity extends AppCompatActivity {

    private Adapter adapter;
    private RecyclerView userList;
    private List<String> users;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ListenerRegistration registrationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);

        adapter = new Adapter();
        userList = findViewById(R.id.usersRecyclerView);
        userList.setLayoutManager(new LinearLayoutManager(this));
        userList.setAdapter(adapter);
        users = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registrationListener = db.collection("users").whereEqualTo("room", "testroom").addSnapshotListener(usersListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        registrationListener.remove();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView usersView;

        public ViewHolder(View view) {
            super(view);
            this.usersView = view.findViewById(R.id.usersRecyclerView);
        }
    }

    private EventListener<QuerySnapshot> usersListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if(e != null){
                Log.e("SpeakerFeedback", "EventListener Error", e);
                return;
            }
            users.clear();
            for (DocumentSnapshot doc : documentSnapshots){
                users.add(doc.getString("name"));
            }
            adapter.notifyDataSetChanged();
        }
    };

    class Adapter extends RecyclerView.Adapter<ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.user_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.usersView.setText(users.get(position));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }
}
