package org.udg.pds.todoandroid.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.activity.Login;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){

        super.onCreate(savedInstance);
        view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        Button logout_interface_btn = (Button) view.findViewById(R.id.logout_interface_button);
        boolean private_profile = getArguments().getBoolean("is_private");
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

        boolean private_profile = getArguments().getBoolean("is_private");
        long idToSearch = -1;
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
            try {
                idToSearch = getArguments().getLong("user_to_search");
            }catch(NullPointerException e) { // If there's no user to search
                Toast.makeText(UserProfileFragment.this.getContext(), "Error loading user profile, there's no id user to search.", Toast.LENGTH_LONG).show();
            }

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

        updatePublicationList(idToSearch);
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

    public void updateProfileInfo(Response<User> response){
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
        userFollowers.setText(Integer.toString(u.numberFollowers));

        // Per al nombre de seguits
        TextView userFollowing = view.findViewById(R.id.user_number_following);
        userFollowing.setText(Integer.toString(u.numberFollowed));

        // Per el nombre de publicacions que tingui l'usuari.
        TextView userPublications = view.findViewById(R.id.user_number_publications);
        userPublications.setText(Integer.toString(u.numberPublications)); // setText requires to be text

        // Per posar la foto de perfil.
        ImageView profilePicture = view.findViewById(R.id.user_profile_picture);
        Picasso.get().load(u.profilePicture).into(profilePicture);
    }

    public void launchErrorConnectingToServer(){
        Toast.makeText(UserProfileFragment.this.getContext(), "Error connecting to server.", Toast.LENGTH_LONG).show();
    }

    public void updatePublicationList(long idToSearch){
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

        View view;

        PublicationViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            owner = itemView.findViewById(R.id.item_owner);
            publication = itemView.findViewById(R.id.item_publication);
            description = itemView.findViewById(R.id.item_description);
        }
    }

    static class TRAdapter extends RecyclerView.Adapter<PublicationViewHolder>{
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
            Picasso.get().load(list.get(position).photo).into(holder.publication);
            holder.description.setText(list.get(position).description);
            holder.owner.setText(list.get(position).userUsername);

            holder.view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Toast.makeText(context, "Hey! I'm publication " + position,  Toast.LENGTH_LONG).show();
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
