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
import android.widget.SearchView;
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


public class TagPeople extends AppCompatActivity {
    TodoApi mTodoService;
    Long publicationId;
    private TRAdapter mAdapter;
    RecyclerView mRecyclerView;
    Integer elemDemanats;
    Integer elemPerPagina=20;
    String textDemanat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taguser_layout);
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        Bundle b = getIntent().getExtras();
        publicationId = b.getLong("id");

        ImageView finish = findViewById(R.id.checkFinish);
        finish.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                TagPeople.this.startActivity(new Intent(TagPeople.this, NavigationActivity.class));
                TagPeople.this.finish();
            }
        });
    }

    void tagUser (String username){
        if(username.length()>0){
            Call<Integer> call = mTodoService.tagUser(publicationId, username);
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body() == 0) {
                            Toast toast = Toast.makeText(TagPeople.this, "User " + username + " tagged successfully", Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (response.body() == 1){
                            Toast toast = Toast.makeText(TagPeople.this, "You have tagged the maximum amount of users (20)", Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (response.body() == 2) {
                            Toast toast = Toast.makeText(TagPeople.this, "This user is already tagged", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } else {
                        Toast toast = Toast.makeText(TagPeople.this, "Error TagPeople, maybe you entedered an unexisting username", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Toast toast = Toast.makeText(TagPeople.this, "Error TagPeople no response", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
        else {
            Toast toast = Toast.makeText(TagPeople.this, "Write a username", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onStart() {

        super.onStart();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        mRecyclerView = this.findViewById(R.id.RecyclerView_TagUser);

        mAdapter = new TRAdapter(this.getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SearchView searchView = TagPeople.this.findViewById(R.id.searchView_TagUser);

        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                updateUserList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed

                updateUserList(query);
                return false;
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE && mAdapter.getItemCount()==elemDemanats ) {

                    Call<List<User>> call = mTodoService.getUsers(textDemanat,(elemDemanats/elemPerPagina),elemPerPagina);
                    elemDemanats=elemDemanats+elemPerPagina;

                    call.enqueue(new Callback<List<User>>() {
                        @Override
                        public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                TagPeople.this.addUserList(response.body());
                            } else {
                                Toast.makeText(TagPeople.this, "Error reading users", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<User>> call, Throwable t) {

                        }
                    });

                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateUserList("");
    }

    public void showUserList(List<User> tl) {
        mAdapter.clear();
        for (User t : tl) {
            mAdapter.add(t);
        }
    }

    public void addUserList(List<User> tl) {
        for (User t : tl) {
            mAdapter.add(t);
        }
    }

    public void updateUserList(String text) {
        textDemanat=text;
        elemDemanats=elemPerPagina;
        Call<List<User>> call = mTodoService.getUsers(text,0,elemPerPagina);


        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TagPeople.this.showUserList(response.body());
                } else {
                    Toast.makeText(TagPeople.this, "Error reading users", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView name;
        ImageView  profilePicture;
        View view;

        UserViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            username = itemView.findViewById(R.id.itemUsername);
            name = itemView.findViewById(R.id.itemName);
            profilePicture = itemView.findViewById(R.id.item_profile_picture);
        }
    }

    class TRAdapter extends RecyclerView.Adapter<UserViewHolder> {

        List<User> listFiltered = new ArrayList<>();
        Context context;


        private TRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_layout, parent, false);
            return new UserViewHolder(v);
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, final int position) {

            holder.name.setText(listFiltered.get(position).name); //list.get(position).username
            holder.username.setText("@" + listFiltered.get(position).username); //list.get(position).username
            Picasso.get().load(listFiltered.get(position).profilePicture).into(holder.profilePicture);


            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tagUser(listFiltered.get(position).username);
                }
            });


            animate(holder);
        }

        @Override

        public int getItemCount() {
            return listFiltered.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {

            super.onAttachedToRecyclerView(recyclerView);
        }

        // Insert a new item to the RecyclerView
        public void insert(int position, User data) {
            listFiltered.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(User data) {
            int position = listFiltered.indexOf(data);
            listFiltered.remove(position);
            notifyItemRemoved(position);
        }


        private void animate(RecyclerView.ViewHolder viewHolder) {
            final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
        }

        public void add(User t) {
            listFiltered.add(t);
            this.notifyItemInserted(listFiltered.size() - 1);
        }

        public void clear() {
            int size = listFiltered.size();
            listFiltered.clear();
            this.notifyItemRangeRemoved(0, size);
        }
    }

}
