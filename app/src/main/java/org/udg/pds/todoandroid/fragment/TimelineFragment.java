package org.udg.pds.todoandroid.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.activity.AddComment;
import org.udg.pds.todoandroid.activity.SeeTaggedUsers;
import org.udg.pds.todoandroid.entity.Publication;
import org.udg.pds.todoandroid.rest.TodoApi;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimelineFragment extends Fragment {
    private TodoApi mTodoService;
    private View view;

    private RecyclerView mRecyclerView;
    private TRAdapter mAdapter;
    private NavController navController = null;

    private Integer elemPerPagina=20;
    private Integer elemDemanats;

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

        SwipeRefreshLayout swipeRefreshLayout = getView().findViewById(R.id.refresh_timeline);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updatePublicationList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        updatePublicationList();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE && mAdapter.getItemCount()==elemDemanats ) {

                    Call<List<Publication>> call = mTodoService.getPublications((elemDemanats/elemPerPagina),elemPerPagina);
                    elemDemanats=elemDemanats+elemPerPagina;

                    call.enqueue(new Callback<List<Publication>>() {
                        @Override
                        public void onResponse(Call<List<Publication>> call, Response<List<Publication>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                addPublicationList(response.body());
                            } else {
                                Toast.makeText(TimelineFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Publication>> call, Throwable t) {

                        }
                    });

                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private void showPublicationList(List<Publication> pl){
        mAdapter.clear();
        for(Publication p : pl){
            mAdapter.add(p);
        }
    }

    private void launchErrorConnectingToServer(){
        Toast.makeText(TimelineFragment.this.getContext(), "Error connecting to server.", Toast.LENGTH_LONG).show();
    }

    private void updatePublicationList() {
        Call<List<Publication>> call = null;
        call = mTodoService.getPublications(0,elemPerPagina);
        elemDemanats=elemPerPagina;

        call.enqueue(new Callback<List<Publication>>() {
            @Override
            public void onResponse(Call<List<Publication>> call, Response<List<Publication>> response) {
                if (response.isSuccessful() && response.body() != null) {
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
        TextView nLikes;
        TextView nComments;
        ImageView likeImage;
        ImageView comment;
        ImageView taggedUsers;
        boolean haDonatLike = false;
        boolean haApretatUnCop = false;

        View view;

        PublicationViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            owner = itemView.findViewById(R.id.item_owner);
            publication = itemView.findViewById(R.id.item_publication);
            description = itemView.findViewById(R.id.item_description);
            nLikes = itemView.findViewById(R.id.item_nLikes);
            nComments = itemView.findViewById(R.id.item_nComments);
            likeImage = itemView.findViewById(R.id.item_likeImage);
            comment = itemView.findViewById(R.id.comment_button);
            taggedUsers = itemView.findViewById(R.id.taggedUsers);
        }
    }
    private void addPublicationList(List<Publication> tl) {
        for (Publication t : tl) {
            mAdapter.add(t);
        }
    }

    class TRAdapter extends RecyclerView.Adapter<TimelineFragment.PublicationViewHolder>{
        List<Publication> list = new ArrayList<>();
        Context context;

        private TRAdapter(Context context){
            this.context = context;
        }

        @Override
        public TimelineFragment.PublicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.publication_layout, parent, false);
            return new TimelineFragment.PublicationViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TimelineFragment.PublicationViewHolder holder, final int position){
            Call<List<Integer>> call = null;
            call = mTodoService.getLikes(list.get(position).id);

            call.enqueue(new Callback<List<Integer>>() {
                @Override
                public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {
                    if (response.isSuccessful()) {
                        holder.nLikes.setText(String.valueOf(response.body().get(0)));
                        if(response.body().get(1)==1) {
                            holder.likeImage.setImageResource(R.drawable.ic_like_pink_24dp);
                            holder.haDonatLike=true;
                       }
                    } else {
                        Toast.makeText(TimelineFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Integer>> call, Throwable t) {
                    TimelineFragment.this.launchErrorConnectingToServer();
                }
            });

            Call<Integer> call2 = mTodoService.getNumComments(list.get(position).id);

            call2.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful())
                        holder.nComments.setText(String.valueOf(response.body()));
                    else
                        Toast.makeText(TimelineFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                }
                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    TimelineFragment.this.launchErrorConnectingToServer();
                }
            });


            holder.owner.setText(list.get(position).userUsername);

<<<<<<< HEAD
            /** VER IMAGENES **/
            String filename = list.get(position).photo;

            Picasso.get().load(filename).into(holder.publication);

=======
            byte[] decodeString = Base64.decode(list.get(position).photo, Base64.DEFAULT);
            Bitmap decodeByte = BitmapFactory.decodeByteArray(decodeString,0,decodeString.length);
            holder.publication.setImageBitmap(decodeByte);
>>>>>>> 964f8109254cc3054fdcedc241f485f8865264a7
            holder.description.setText(list.get(position).description);

            holder.publication.setOnClickListener(new View.OnClickListener(){
                int i = 0;
                public void onClick(View view){
                    i++;

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            if (i == 1){
                                if(!holder.haApretatUnCop){
                                    holder.taggedUsers.setVisibility(View.VISIBLE);
                                    holder.haApretatUnCop=true;
                                    holder.taggedUsers.setOnClickListener(new View.OnClickListener(){
                                        @Override
                                        public void onClick(View view){
                                            Intent intent = new Intent(getActivity(), SeeTaggedUsers.class);
                                            Bundle b = new Bundle();
                                            b.putLong("id",list.get(position).id);
                                            intent.putExtras(b);
                                            startActivity(intent);
                                        }
                                    });
                                }
                                else{
                                    holder.taggedUsers.setVisibility(View.INVISIBLE);
                                    holder.haApretatUnCop=false;
                                }
                            } else if (i == 2){
                                if(! holder.haDonatLike) {
                                    Call<Publication> call = null;
                                    call = mTodoService.addLike(list.get(position).id);

                                    call.enqueue(new Callback<Publication>() {
                                        @Override
                                        public void onResponse(Call<Publication> call, Response<Publication> response) {
                                            if (response.isSuccessful()) {
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
                                }
                                else{
                                    Call<Publication> call = null;
                                    call = mTodoService.deleteLike(list.get(position).id);

                                    call.enqueue(new Callback<Publication>() {
                                        @Override
                                        public void onResponse(Call<Publication> call, Response<Publication> response) {
                                            if (response.isSuccessful()) {
                                                Toast.makeText(TimelineFragment.this.getContext(), "You have unliked this post", Toast.LENGTH_LONG).show();
                                                Publication pb = response.body();
                                                holder.haDonatLike = false;
                                                holder.likeImage.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                            } else {
                                                Toast.makeText(TimelineFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Publication> call, Throwable t) {
                                            TimelineFragment.this.launchErrorConnectingToServer();
                                        }
                                    });
                                }
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
                    if(! holder.haDonatLike) {
                        Call<Publication> call = null;
                        call = mTodoService.addLike(list.get(position).id);

                        call.enqueue(new Callback<Publication>() {
                            @Override
                            public void onResponse(Call<Publication> call, Response<Publication> response) {
                                if (response.isSuccessful()) {
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
                    }
                    else{
                        Call<Publication> call = null;
                        call = mTodoService.deleteLike(list.get(position).id);

                        call.enqueue(new Callback<Publication>() {
                            @Override
                            public void onResponse(Call<Publication> call, Response<Publication> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(TimelineFragment.this.getContext(), "You have unliked this post", Toast.LENGTH_LONG).show();
                                    Publication pb = response.body();
                                    holder.haDonatLike = false;
                                    holder.likeImage.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                } else {
                                    Toast.makeText(TimelineFragment.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Publication> call, Throwable t) {
                                TimelineFragment.this.launchErrorConnectingToServer();
                            }
                        });
                    }
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

            holder.comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), AddComment.class);
                    Bundle b = new Bundle();
                    b.putLong("id",list.get(position).id);
                    intent.putExtras(b);
                    startActivity(intent);
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
