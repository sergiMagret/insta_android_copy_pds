package org.udg.pds.todoandroid.fragment;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FollowingList extends Fragment {
    private static TodoApi mTodoService;
    SearchView mSearchView;
    private View view;
    private Integer elemDemanats;
    private String textDemanat;
    private NavController navController = null;
    private Integer elemPerPagina=20;
    private RecyclerView mRecyclerView;
    private TRAdapter mAdapter;
    private boolean is_private = false;
    private long idToSearch = -1;
    private String usersToShow = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_following_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getActivity().getApplication()).getAPI();
        try {
            is_private = getArguments().getBoolean("is_private");
            idToSearch = getArguments().getLong("user_to_search");
            usersToShow = getArguments().getString("users_to_show");
            mRecyclerView = getView().findViewById(R.id.recycler_view_users);
        }catch(NullPointerException e) {
            Toast.makeText(FollowingList.this.getContext(), "Too few arguments passed, error onStart()", Toast.LENGTH_LONG).show();
        }

        TextView title = view.findViewById(R.id.item_list_name);
        if(usersToShow.equals("followers")){
            title.setText(R.string.tag_followers);
        }else if(usersToShow.equals("followed")){
            title.setText(R.string.tag_following);
        }

        mAdapter = new TRAdapter(this.getActivity().getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                Call<List<User>> call = null;
                if(usersToShow.equals("followed") && is_private) {
                    call = mTodoService.getFollowed((elemDemanats/elemPerPagina),elemPerPagina);
                }else if(usersToShow.equals("followed") && !is_private) {
                    call = mTodoService.getFollowedById(idToSearch, (elemDemanats/elemPerPagina) , elemPerPagina);
                }else if(usersToShow.equals("followers") && is_private){
                    call = mTodoService.getFollowers((elemDemanats/elemPerPagina), elemPerPagina);
                }else if(usersToShow.equals("followers") && !is_private){
                    call = mTodoService.getFollowersById(idToSearch, (elemDemanats/elemPerPagina) , elemPerPagina);
                }else{
                    Toast.makeText(FollowingList.this.getContext(), "Error making the call to the server", Toast.LENGTH_LONG).show();
                }


                elemDemanats=elemDemanats+elemPerPagina;

                call.enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            FollowingList.this.addUserList(response.body());
                        } else {
                            Toast.makeText(FollowingList.this.getContext(), "Error reading users", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {

                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateUserList();
    }

    public void showUserList(List<User> tl) {
        mAdapter.clear();
        for (User t : tl) {
            mAdapter.add(t);
        }
    }

    private void addUserList(List<User> tl) {
        for (User t : tl) {
            mAdapter.add(t);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Global.RQ_ADD_TASK) {
            this.updateUserList();
        }
    }


    private void updateUserList() {
        elemDemanats=elemPerPagina;
        Call<List<User>> call = null;
        if(usersToShow.equals("followed") && is_private) {
            call = mTodoService.getFollowed(0,elemPerPagina);
        }else if(usersToShow.equals("followed") && !is_private) {
            call = mTodoService.getFollowedById(idToSearch, 0 , elemPerPagina);
        }else if(usersToShow.equals("followers") && is_private){
            call = mTodoService.getFollowers(0, elemPerPagina);
        }else if(usersToShow.equals("followers") && !is_private){
            call = mTodoService.getFollowersById(idToSearch, 0 , elemPerPagina);
        }else{
            Toast.makeText(FollowingList.this.getContext(), "Error making the call to the server", Toast.LENGTH_LONG).show();
        }

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FollowingList.this.addUserList(response.body());
                } else {
                    Toast.makeText(FollowingList.this.getContext(), "Error reading users", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                launchErrorConnectingToServer();
            }
        });
    }

    private void launchErrorConnectingToServer(){
        Toast.makeText(FollowingList.this.getContext(), "Error connecting to server.", Toast.LENGTH_LONG).show();
    }



    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView name;
        ImageView profilePicture;
        View view;

        UserViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            username = itemView.findViewById(R.id.itemUsername);
            name = itemView.findViewById(R.id.itemName);
            profilePicture = itemView.findViewById(R.id.item_profile_picture);
        }
    }



    static class TRAdapter extends RecyclerView.Adapter<FollowingList.UserViewHolder>  {

        List<User> list = new ArrayList<>();
        Context context;


        private TRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public FollowingList.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_layout, parent, false);
            return new FollowingList.UserViewHolder(v);
        }

        @Override
        public void onBindViewHolder(FollowingList.UserViewHolder holder, final int position) {

            holder.name.setText(list.get(position).name);
            holder.username.setText("@" + list.get(position).username);
            Picasso.get().load(list.get(position).profilePicture).into(holder.profilePicture);


            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FollowingListDirections.ActionFollowingListToActionProfile action = FollowingListDirections.actionFollowingListToActionProfile();
                    action.setIsPrivate(false);
                    action.setUserToSearch(list.get(position).id);
                    Navigation.findNavController(view).navigate(action);
                }
            });

            animate(holder);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {

            super.onAttachedToRecyclerView(recyclerView);
        }

        // Insert a new item to the RecyclerView
        public void insert(int position, User data) {
            list.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(User data) {
            int position = list.indexOf(data);
            list.remove(position);
            notifyItemRemoved(position);
        }

        private void animate(RecyclerView.ViewHolder viewHolder) {
            final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
        }

        public void add(User t) {
            list.add(t);
            this.notifyItemInserted(list.size() - 1);
        }

        public void clear() {
            int size = list.size();
            list.clear();
            this.notifyItemRangeRemoved(0, size);
        }

    }
}
