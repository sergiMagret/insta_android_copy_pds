package org.udg.pds.todoandroid.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.activity.Login;
import org.udg.pds.todoandroid.entity.IdObject;
import org.udg.pds.todoandroid.entity.Publication;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UserProfileFragment extends Fragment {
    TodoApi mTodoService;
    View view;

    RecyclerView mRecyclerView;
    private TRAdapter mAdapter;

    NavController navController = null;

    private boolean private_profile;
    long idToSearch;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){

        super.onCreate(savedInstance);
        view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        try {
            private_profile = getArguments().getBoolean("is_private");
            idToSearch = getArguments().getLong("user_to_search");
        }catch (NullPointerException e){
            Toast.makeText(UserProfileFragment.this.getContext(), "Error loading user profile, no arguments", Toast.LENGTH_LONG).show();
        }

        // Configuració botó de logout
        Button logout_interface_btn = (Button) view.findViewById(R.id.logout_interface_button);
        if(private_profile){
            logout_interface_btn.setVisibility(View.VISIBLE);
            logout_interface_btn.setOnClickListener(new View.OnClickListener(){
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v){
                    AlertDialog.Builder logout_dialog = new AlertDialog.Builder(getContext());
                    View dialog_view = getLayoutInflater().inflate(R.layout.logout_layout, null);
                    Button logout_btn = (Button) dialog_view.findViewById(R.id.logout_button);
                    logout_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            UserProfileFragment.this.checkCredentials();
                        }
                    });

                    logout_dialog.setView(dialog_view);
                    AlertDialog dialog = logout_dialog.create();
                    dialog.show();
                    Button logout_cancel_btn = (Button) dialog_view.findViewById(R.id.logout_cancel_button);

                    logout_cancel_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                }
            });
        }
        else{
            logout_interface_btn.setVisibility(View.GONE);
        }

        return view;
    }

    public void checkCredentials() {
        Call<String> call = mTodoService.logout();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if (response.isSuccessful()) {
                    UserProfileFragment.this.startActivity(new Intent(getContext(), Login.class));

                } else {
                    Toast toast = Toast.makeText(getContext(), "Error logging out", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t){
                Toast toast = Toast.makeText(getContext(), "Error logging out", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }


    @Override
    public void onStart(){
        super.onStart();

        mTodoService = ((TodoApp) this.getActivity().getApplication()).getAPI();

        mRecyclerView = getView().findViewById(R.id.recycler_view_publication);
        mAdapter = new TRAdapter(this.getActivity().getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        try {
            private_profile = getArguments().getBoolean("is_private");
            idToSearch = getArguments().getLong("user_to_search");
        }catch (NullPointerException e){
            Toast.makeText(UserProfileFragment.this.getContext(), "Error loading user profile, no arguments", Toast.LENGTH_LONG).show();
        }

        if(private_profile) {
            Button follow_button = view.findViewById(R.id.follow_unfollow_button);
            follow_button.setVisibility(View.INVISIBLE);
            Call<User> call = mTodoService.getUserProfile();
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if(response.isSuccessful() && response.body() != null){
                        UserProfileFragment.this.updateProfileInfo(response);
                    }else{
                        Toast.makeText(UserProfileFragment.this.getContext(), "Error reading profile information.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    UserProfileFragment.this.launchErrorConnectingToServer();
                }
            });
        }else{
            Call<User> call = mTodoService.getUserProfileByID(idToSearch);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserProfileFragment.this.updateProfileInfo(response);
                    } else {
                        Toast.makeText(UserProfileFragment.this.getContext(), "Error reading profile information.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    UserProfileFragment.this.launchErrorConnectingToServer();
                }
            });
        }

        updatePublicationList();
    }

    @Override
    public void onResume(){
        super.onResume();
        //this.updatePublicationList();
    }

    public void showPublicationList(List<Publication> pl){
        mAdapter.clear();
        for(Publication p : pl){
            mAdapter.add(p);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == Global.RQ_ADD_TASK){
            //this.updatePublicationList();
        }
    }

    private void updateProfileInfo(Response<User> response){
        User u = response.body();

        // Per al nom de l'usuari
        TextView userName = view.findViewById(R.id.user_name);
        userName.setText(u.name);

        // Per al username de l'usuari
        TextView userUsername = view.findViewById(R.id.user_username);
        String s = "@" + u.username;
        userUsername.setText(s); // Done this way to avoid a warning

        // Per la descripció de l'usuari
        TextView userDesc = view.findViewById(R.id.user_description);
        userDesc.setText(u.description);

        // Per al nombre de seguidors
        TextView userFollowers = view.findViewById(R.id.user_number_followers);
        userFollowers.setText(Integer.toString(u.numberFollowers)); // setText requires to be text
        ConstraintLayout layoutFollowers = view.findViewById(R.id.layout_number_followers);
        layoutFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileFragmentDirections.ActionActionProfileToFollowingList action = UserProfileFragmentDirections.actionActionProfileToFollowingList();
                action.setIsPrivate(private_profile);
                action.setUsersToShow("followers");
                action.setUserToSearch(idToSearch);
                Navigation.findNavController(view).navigate(action);
            }
        });

        // Per al nombre de seguits
        TextView userFollowing = view.findViewById(R.id.user_number_following);
        userFollowing.setText(Integer.toString(u.numberFollowed)); // setText requires to be text
        ConstraintLayout layoutFollowed = view.findViewById(R.id.layout_number_following);
        layoutFollowed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileFragmentDirections.ActionActionProfileToFollowingList action = UserProfileFragmentDirections.actionActionProfileToFollowingList();
                action.setIsPrivate(private_profile);
                action.setUsersToShow("followed");
                action.setUserToSearch(idToSearch);
                Navigation.findNavController(view).navigate(action);
            }
        });

        // Per el nombre de publicacions que tingui l'usuari.
        TextView userPublications = view.findViewById(R.id.user_number_publications);
        userPublications.setText(Integer.toString(u.numberPublications)); // setText requires to be text

        // Per posar la foto de perfil.
        ImageView profilePicture = view.findViewById(R.id.user_profile_picture);
        Picasso.get().load(u.profilePicture).into(profilePicture);

        // Per al boto de follow/unfollow
        Button follow_button = view.findViewById(R.id.follow_unfollow_button);
        if(private_profile) { // If you are watching your own profile
            follow_button.setVisibility(View.GONE);
        }else{
            updateFollowStatus(u.followsUser);
        }
    }

    private void updateFollowStatus(boolean followed) {
        Button follow_button = view.findViewById(R.id.follow_unfollow_button);
        if(followed) { // If following, show the unfollow button
            follow_button.setBackgroundResource(R.drawable.button_unfollow);
            follow_button.setTextColor(Color.WHITE);
            follow_button.setText(R.string.button_unfollow);
            follow_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Call<String> call = mTodoService.deleteFollowed(idToSearch); // The new follow is the user's profile you just searched
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if(response.isSuccessful()){
                                UserProfileFragmentDirections.ActionActionProfileSelf action = UserProfileFragmentDirections.actionActionProfileSelf();
                                action.setIsPrivate(private_profile);
                                action.setUserToSearch(idToSearch);
                                Navigation.findNavController(view).navigate(action); // Update the profile with the new followers and button
                            }else{
                                Toast.makeText(UserProfileFragment.this.getContext(), "Error unfollowing the user.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            UserProfileFragment.this.launchErrorConnectingToServer();
                        }
                    });
                }
            });
        }else{ // If not following show the follow button
            follow_button.setBackgroundResource(R.drawable.button_follow);
            follow_button.setTextColor(Color.BLACK);
            follow_button.setText(R.string.button_follow);
            follow_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IdObject id = new IdObject();
                    id.id = idToSearch;
                    Call<String> call = mTodoService.addFollowed(id); // The new follow is the user's profile you just searched
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if(response.isSuccessful()){
                                UserProfileFragmentDirections.ActionActionProfileSelf action = UserProfileFragmentDirections.actionActionProfileSelf();
                                action.setIsPrivate(private_profile);
                                action.setUserToSearch(idToSearch);
                                Navigation.findNavController(view).navigate(action); // Update the profile with the new followers and button
                            }else{
                                Toast.makeText(UserProfileFragment.this.getContext(), "Error following the user.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            UserProfileFragment.this.launchErrorConnectingToServer();
                        }
                    });
                }
            });
        }
    }

    private void launchErrorConnectingToServer(){
        Toast.makeText(UserProfileFragment.this.getContext(), "Error connecting to server.", Toast.LENGTH_LONG).show();
    }

    public void updatePublicationList(){
        Call<List<Publication>> call = null;
        if(idToSearch == -1) {
            call = mTodoService.getUserPublications();
        }else {
            call = mTodoService.getUserPublicationsByID(idToSearch);
        }

        call.enqueue(new Callback<List<Publication>>() {
            @Override
            public void onResponse(Call<List<Publication>> call, Response<List<Publication>> response) {
                if (response.isSuccessful()) {
                    UserProfileFragment.this.showPublicationList(response.body());
                } else {
                    Toast.makeText(UserProfileFragment.this.getContext(), "Error reading user publications", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Publication>> call, Throwable t) {
                UserProfileFragment.this.launchErrorConnectingToServer();
            }
        });
    }

    static class PublicationViewHolder extends RecyclerView.ViewHolder {
        ImageView publication;
        TextView description;
        TextView owner;
        EditText nLikes;
        ImageView likeImage;

        View view;

        PublicationViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            owner = itemView.findViewById(R.id.item_owner);
            publication = itemView.findViewById(R.id.item_publication);
            description = itemView.findViewById(R.id.item_description);
            nLikes = itemView.findViewById(R.id.item_nLikes);
            likeImage = itemView.findViewById(R.id.item_likeImage);
        }
    }

    class TRAdapter extends RecyclerView.Adapter<PublicationViewHolder>{
        List<Publication> list = new ArrayList<>();
        Context context;

        public TRAdapter(Context context){
            this.context = context;
        }

        @Override
        public PublicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.publication_layout, parent, false);
            PublicationViewHolder holder = new PublicationViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(PublicationViewHolder holder, final int position){
            Call<Integer> call = null;
            call = mTodoService.getLikes(list.get(position).id);

            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful()) {
                        holder.nLikes.setText(String.valueOf(response.body()));
                    } else {
                        Toast.makeText(UserProfileFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    UserProfileFragment.this.launchErrorConnectingToServer();
                }
            });

            Picasso.get().load(list.get(position).photo).into(holder.publication);
            holder.description.setText(list.get(position).description);
            holder.owner.setText(list.get(position).userUsername);

            /*holder.view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Toast.makeText(context, "Hey! I'm publication " + position,  Toast.LENGTH_LONG).show();
                }
            });*/

            holder.publication.setOnClickListener(new View.OnClickListener(){
                int i = 0;
                public void onClick(View view){
                    i++;

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            if (i == 1){
                                Toast.makeText(UserProfileFragment.this.getContext(), "Double click to like", Toast.LENGTH_LONG).show();
                            } else if (i == 2){
                                Call<Publication> call = null;
                                call = mTodoService.addLike(list.get(position).id);

                                call.enqueue(new Callback<Publication>() {
                                    @Override
                                    public void onResponse(Call<Publication> call, Response<Publication> response) {
                                        if (response.isSuccessful()) {
                                            //holder.likeImage.setImageResource(R.drawable.ic_like_pink_24dp);
                                            Toast.makeText(UserProfileFragment.this.getContext(), "You liked this photo", Toast.LENGTH_LONG).show();
                                            Publication pb = response.body();
                                        } else {
                                            Toast.makeText(UserProfileFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Publication> call, Throwable t) {
                                        UserProfileFragment.this.launchErrorConnectingToServer();
                                    }
                                });
                                updatePublicationList();
                            }
                            i = 0;
                        }
                    }, 500);
                }
            });

            holder.likeImage.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Call<Publication> call = null;
                    call = mTodoService.addLike(list.get(position).id);

                    call.enqueue(new Callback<Publication>() {
                        @Override
                        public void onResponse(Call<Publication> call, Response<Publication> response) {
                            if (response.isSuccessful()) {
                                //holder.likeImage.setImageResource(R.drawable.ic_like_pink_24dp);
                                Publication pb = response.body();
                                Toast.makeText(UserProfileFragment.this.getContext(), "You liked this photo", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(UserProfileFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Publication> call, Throwable t) {
                            UserProfileFragment.this.launchErrorConnectingToServer();
                        }
                    });
                    updatePublicationList();
                }
            });

            //animate(holder);
        }

        @Override
        public int getItemCount(){
            return list.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView){
            super.onAttachedToRecyclerView(recyclerView);
        }

        // Insert a new item to the RecyclerView
        public void insert(int position, Publication data){
            list.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecycleView item containing the data object
        public void remove(Publication data){
            int position = list.indexOf(data);
            list.remove(position);
            notifyItemRemoved(position);
        }

        public void animate(RecyclerView.ViewHolder viewHolder){
            final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.anticipate_overshoot_interpolator);
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
        }

        public void add(Publication p){
            list.add(p);
            this.notifyItemInserted(list.size()-1);
        }

        public void clear(){
            int size = list.size();
            list.clear();
            this.notifyItemRangeRemoved(0, size);
        }
    }
}
