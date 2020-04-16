package org.udg.pds.todoandroid.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
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
import org.udg.pds.todoandroid.entity.Publication;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimelineFragment extends Fragment {
    TodoApi mTodoService;
    View view;

    RecyclerView mRecyclerView;
    private TRAdapter mAdapter;
    NavController navController = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        super.onCreate(savedInstance);
        view = inflater.inflate(R.layout.fragment_timeline, container, false);
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
        mRecyclerView = getView().findViewById(R.id.RecyclerView_timeline);
        mAdapter = new TimelineFragment.TRAdapter(this.getActivity().getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

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

    public void launchErrorConnectingToServer(){
        Toast.makeText(TimelineFragment.this.getContext(), "Error connecting to server.", Toast.LENGTH_LONG).show();
    }

    public void updatePublicationList() {
        Call<List<Publication>> call = null;
        call = mTodoService.getPublications();

        call.enqueue(new Callback<List<Publication>>() {
            @Override
            public void onResponse(Call<List<Publication>> call, Response<List<Publication>> response) {
                if (response.isSuccessful()) {
                    TimelineFragment.this.showPublicationList(response.body());
                } else {
                    Toast.makeText(TimelineFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Publication>> call, Throwable t) {
                TimelineFragment.this.launchErrorConnectingToServer();
            }
        });
    }

    class PublicationViewHolder extends RecyclerView.ViewHolder {
        TextView owner;
        ImageView publication;
        TextView description;
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

    class TRAdapter extends RecyclerView.Adapter<TimelineFragment.PublicationViewHolder>{
        List<Publication> list = new ArrayList<>();
        Context context;

        public TRAdapter(Context context){
            this.context = context;
        }

        @Override
        public TimelineFragment.PublicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.publication_layout, parent, false);
            TimelineFragment.PublicationViewHolder holder = new TimelineFragment.PublicationViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(TimelineFragment.PublicationViewHolder holder, final int position){
            Call<Integer> call = null;
            call = mTodoService.getLikes(list.get(position).id);

            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful()) {
                        holder.nLikes.setText(String.valueOf(response.body()));
                    } else {
                        Toast.makeText(TimelineFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    TimelineFragment.this.launchErrorConnectingToServer();
                }
            });
            holder.owner.setText(list.get(position).userUsername);
            Picasso.get().load(list.get(position).photo).into(holder.publication);
            holder.description.setText(list.get(position).description);

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
                                Toast.makeText(TimelineFragment.this.getContext(), "Double click to like", Toast.LENGTH_LONG).show();
                            } else if (i == 2){
                                Call<Publication> call = null;
                                call = mTodoService.addLike(list.get(position).id);

                                call.enqueue(new Callback<Publication>() {
                                    @Override
                                    public void onResponse(Call<Publication> call, Response<Publication> response) {
                                        if (response.isSuccessful()) {
                                            //holder.likeImage.setImageResource(R.drawable.ic_like_pink_24dp);
                                            Toast.makeText(TimelineFragment.this.getContext(), "You liked this photo", Toast.LENGTH_LONG).show();
                                            Publication pb = response.body();
                                        } else {
                                            Toast.makeText(TimelineFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Publication> call, Throwable t) {
                                        TimelineFragment.this.launchErrorConnectingToServer();
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
                                Toast.makeText(TimelineFragment.this.getContext(), "You liked this photo", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(TimelineFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Publication> call, Throwable t) {
                            TimelineFragment.this.launchErrorConnectingToServer();
                        }
                    });
                    updatePublicationList();
                }
            });

            holder.owner.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    TimelineFragmentDirections.ActionActionHomeToActionProfile action = TimelineFragmentDirections.actionActionHomeToActionProfile();
                    action.setIsPrivate(false);
                    action.setUserToSearch(list.get(position).userId);
                    Navigation.findNavController(view).navigate(action);
                }
            });
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
