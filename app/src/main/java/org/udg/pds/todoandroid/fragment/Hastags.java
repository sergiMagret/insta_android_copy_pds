package org.udg.pds.todoandroid.fragment;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.ImageButton;
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

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.activity.AddComment;
import org.udg.pds.todoandroid.entity.Publication;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Hastags extends Fragment {

    TodoApi mTodoService;
    View view;

    RecyclerView mRecyclerView;

    private TRAdapter mAdapter;


    NavController navController = null;

    String HastagName;
    Integer elemPerPagina = 20;
    Integer elemDemanats;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_hastags, container, false);
        try {
            HastagName = getArguments().getString("tag");
        } catch (NullPointerException e) {
            Toast.makeText(Hastags.this.getContext(), "Error loading user profile, bad arguments", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getActivity().getApplication()).getAPI();
        mRecyclerView = getView().findViewById(R.id.recycler_view_hastag);
        mAdapter = new TRAdapter(this.getActivity().getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.refresh_hastag);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updatePublicationList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        updatePublicationList();
        TextView hashtag = view.findViewById(R.id.HastagName);
        String h = "#" + HastagName;
        hashtag.setText(h);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!recyclerView.canScrollVertically(1)&& newState==RecyclerView.SCROLL_STATE_IDLE && mAdapter.getItemCount()==elemDemanats){
                    Call<List<Publication>> call;
                    call = mTodoService.getHastagPublicationsByName(HastagName,(elemDemanats / elemPerPagina), elemPerPagina);
                    elemDemanats=elemDemanats+elemPerPagina;
                    call.enqueue(new Callback<List<Publication>>() {
                        @Override
                        public void onResponse(Call<List<Publication>> call, Response<List<Publication>> response) {
                            if(response.isSuccessful()) {
                                addPublicationList(response.body());
                            }
                            else  {Toast.makeText(Hastags.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();}
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
    public void onResume() {
        super.onResume();
    }

    public void addPublicationList(List<Publication> tl) {
        for (Publication t : tl) {
            mAdapter.add(t);
        }
    }

    public void showPublicationList(List<Publication> pl){
        mAdapter.clear();
        for(Publication p : pl){
            mAdapter.add(p);
        }
    }

    private void launchErrorConnectingToServer(){
        Toast.makeText(Hastags.this.getContext(), "Error connecting to server.", Toast.LENGTH_LONG).show();
    }

    public void updatePublicationList(){
        Call<List<Publication>> call = null;
        call = mTodoService.getHastagPublicationsByName(HastagName, 0, elemPerPagina);
        elemDemanats=elemPerPagina;


        call.enqueue(new Callback<List<Publication>>() {
            @Override
            public void onResponse(Call<List<Publication>> call, Response<List<Publication>> response) {
                if (response.isSuccessful()) {
                    Hastags.this.showPublicationList(response.body());
                } else {
                    Toast.makeText(Hastags.this.getContext(), "Error reading user publications", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Publication>> call, Throwable t) {
                Hastags.this.launchErrorConnectingToServer();
            }
        });
    }


    static class PublicationViewHolder extends RecyclerView.ViewHolder {
        ImageView publication;
        TextView description;
        TextView owner;
        TextView nLikes;
        ImageView likeImage;
        ImageView comment;
        boolean haDonatLike = false;
        ImageButton more_btn;

        View view;

        PublicationViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            owner = itemView.findViewById(R.id.item_owner);
            publication = itemView.findViewById(R.id.item_publication);
            description = itemView.findViewById(R.id.item_description);
            nLikes = itemView.findViewById(R.id.item_nLikes);
            likeImage = itemView.findViewById(R.id.item_likeImage);
            more_btn = itemView.findViewById(R.id.more_publication_button);
            comment = itemView.findViewById(R.id.comment_button);
        }
    }

    class TRAdapter extends RecyclerView.Adapter<UserProfileFragment.PublicationViewHolder>{
        List<Publication> list = new ArrayList<>();
        Context context;

        public TRAdapter(Context context){
            this.context = context;
        }

        @Override
        public UserProfileFragment.PublicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.publication_layout, parent, false);
            UserProfileFragment.PublicationViewHolder holder = new UserProfileFragment.PublicationViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(UserProfileFragment.PublicationViewHolder holder, final int position){
            boolean haFetLike=false;
            Call<List<Integer>> call = null;
            call = mTodoService.getLikes(list.get(position).id);

            call.enqueue(new Callback<List<Integer>>() {
                @Override
                public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {
                    if (response.isSuccessful()) {
                        holder.nLikes.setText(String.valueOf(response.body().get(0)));
                        if(response.body().get(1)==1){
                            holder.likeImage.setImageResource(R.drawable.ic_like_pink_24dp);
                            holder.haDonatLike = true;
                        }
                    } else {
                        Toast.makeText(Hastags.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Integer>> call, Throwable t) {
                    Hastags.this.launchErrorConnectingToServer();
                }
            });

            byte[] decodeString = Base64.decode(list.get(position).photo, Base64.DEFAULT);
            Bitmap decodeByte = BitmapFactory.decodeByteArray(decodeString,0,decodeString.length);
            holder.publication.setImageBitmap(decodeByte);
            //Picasso.get().load(list.get(position).photo).into(holder.publication);
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
                                Toast.makeText(Hastags.this.getContext(), "Double click to like", Toast.LENGTH_LONG).show();
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
                                                Toast.makeText(Hastags.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Publication> call, Throwable t) {
                                            Hastags.this.launchErrorConnectingToServer();
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
                                                Toast.makeText(Hastags.this.getContext(), "You have unliked this post", Toast.LENGTH_LONG).show();
                                                Publication pb = response.body();
                                                holder.haDonatLike = false;
                                                holder.likeImage.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                            } else {
                                                Toast.makeText(Hastags.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Publication> call, Throwable t) {
                                            Hastags.this.launchErrorConnectingToServer();
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
                                    Toast.makeText(Hastags.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Publication> call, Throwable t) {
                                Hastags.this.launchErrorConnectingToServer();
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
                                    Toast.makeText(Hastags.this.getContext(), "You have unliked this post", Toast.LENGTH_LONG).show();
                                    Publication pb = response.body();
                                    holder.haDonatLike = false;
                                    holder.likeImage.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                } else {
                                    Toast.makeText(Hastags.this.getContext(), "Error reading publications", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Publication> call, Throwable t) {
                                Hastags.this.launchErrorConnectingToServer();
                            }
                        });
                    }
                    updatePublicationList();
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
