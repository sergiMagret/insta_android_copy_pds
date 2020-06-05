package org.udg.pds.todoandroid.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeeTaggedUsers extends AppCompatActivity {
    TodoApi mTodoService;
    Long publicationId;
    RecyclerView mRecyclerView;
    private TRAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tagged_users_layout);
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        Bundle b = getIntent().getExtras();
        publicationId = b.getLong("id");
    }

    @Override
    public void onStart() {

        super.onStart();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        mRecyclerView = this.findViewById(R.id.taggedUsersList);

        mAdapter = new TRAdapter(this.getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageView end = findViewById(R.id.item_finish);

        end.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SeeTaggedUsers.this.startActivity(new Intent(SeeTaggedUsers.this, NavigationActivity.class));
                    SeeTaggedUsers.this.finish();
                }
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateTaggedList();
    }

    public void showTaggedList(List<User> tl) {
        mAdapter.clear();
        for (User u : tl) {
            mAdapter.add(u);
        }
    }

    public void addTaggedList(List<User> tl) {
        for (User u : tl) {
            mAdapter.add(u);
        }
    }

    public void updateTaggedList() {
        Call<List<User>> call = mTodoService.getTaggedUsers(publicationId);

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SeeTaggedUsers.this.showTaggedList(response.body());
                } else {
                    Toast.makeText(getApplicationContext(), "Error reading tagged users", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }

    static class TaggedUsersHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView name;
        ImageView profilePicture;
        View view;

        TaggedUsersHolder(View itemView) {
            super(itemView);
            view = itemView;
            username = itemView.findViewById(R.id.itemUsername);
            name = itemView.findViewById(R.id.itemName);
            profilePicture = itemView.findViewById(R.id.item_profile_picture);
        }
    }

    static class TRAdapter extends RecyclerView.Adapter<TaggedUsersHolder> {
        List<User> users = new ArrayList<>();
        Context context;


        private TRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public SeeTaggedUsers.TaggedUsersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_layout, parent, false);
            return new TaggedUsersHolder(v);
        }

        @Override
        public void onBindViewHolder(TaggedUsersHolder holder, final int position) {
            holder.name.setText(users.get(position).name);
            holder.username.setText("@" + users.get(position).username);
            Picasso.get().load(users.get(position).profilePicture).into(holder.profilePicture);
            animate(holder);
        }


        @Override
        public int getItemCount() {
            return users.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {

            super.onAttachedToRecyclerView(recyclerView);
        }

        public void insert(int position, User data) {
            users.add(position,data);
            notifyItemInserted(position);
        }

        public void remove(User u) {
            int position = users.indexOf(u);
            users.remove(position);
            notifyItemRemoved(position);
        }


        private void animate(RecyclerView.ViewHolder viewHolder) {
            final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
        }

        public void add(User u) {
            users.add(u);
            this.notifyItemInserted(users.size() - 1);
        }

        public void clear() {
            int size = users.size();
            users.clear();
            this.notifyItemRangeRemoved(0, size);
        }
    }
}

