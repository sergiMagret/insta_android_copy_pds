package org.udg.pds.todoandroid.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Comment;
import org.udg.pds.todoandroid.entity.CommentPost;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddComment extends AppCompatActivity {

    TodoApi mTodoService;
    Long publicationId;
    RecyclerView mRecyclerView;
    private TRAdapter mAdapter;
    Integer elemDemanats;
    Integer elemPerPagina=20;
    String selfUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_comment_layout);
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        Bundle b = getIntent().getExtras();
        publicationId = b.getLong("id");
        ImageButton send_btn = findViewById(R.id.send_comment);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView comment = AddComment.this.findViewById(R.id.comment_publication);
                AddComment.this.sendComment(comment.getText().toString(),publicationId);
            }
        });


    }
    void sendComment(String text, Long publicationId){
        if(text.length()==0){
            Toast.makeText(getApplicationContext(), "Write a comment first", Toast.LENGTH_LONG).show();
        }
        else {
            CommentPost c = new CommentPost();
            c.text = text;
            c.publicationId = publicationId;
            Call<String> call = mTodoService.sendComment(c);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if(response.isSuccessful()){
                        Intent intent = new Intent(AddComment.this, AddComment.class);
                        Bundle b = new Bundle();
                        b.putLong("id",publicationId);
                        intent.putExtras(b);
                        finish();
                        startActivity(intent);
                    }
                    else{
                        Toast toast = Toast.makeText(AddComment.this, "Error sendComment bad response", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast toast = Toast.makeText(AddComment.this, "Error sendComment no response", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

    }

    @Override
    public void onStart() {

        super.onStart();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        getSelfUsername();
        mRecyclerView = this.findViewById(R.id.recycle_view_comments);

        mAdapter = new TRAdapter(this.getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.refresh_comments);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateCommentList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE && mAdapter.getItemCount() == elemDemanats) {

                    Call<List<Comment>> call = mTodoService.getComments(publicationId, (elemDemanats / elemPerPagina), elemPerPagina);
                    elemDemanats = elemDemanats + elemPerPagina;

                    call.enqueue(new Callback<List<Comment>>() {
                        @Override
                        public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                            if (response.isSuccessful()) {
                                AddComment.this.addCommentList(response.body());
                            } else {
                                Toast.makeText(getApplicationContext(), "Error reading users", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Comment>> call, Throwable t) {

                        }
                    });

                }
            }
        });
    }

        @Override
        public void onResume() {
            super.onResume();
            this.updateCommentList();
        }

        public void showCommentList(List<Comment> tl) {
            mAdapter.clear();
            for (Comment t : tl) {
                mAdapter.add(t);
            }
        }

        public void addCommentList(List<Comment> tl) {
            for (Comment t : tl) {
                mAdapter.add(t);
            }
        }

    public void getSelfUsername(){
        Call<User> call = mTodoService.getUserProfile();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    selfUsername = response.body().username;
                } else {
                    Toast.makeText(getApplicationContext(), "Error reading username", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }


    public void updateCommentList() {

        elemDemanats=elemPerPagina;
        Call<List<Comment>> call = mTodoService.getComments(publicationId,0,elemPerPagina);


        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()) {
                   AddComment.this.showCommentList(response.body());
                } else {
                    Toast.makeText(getApplicationContext(), "Error reading comments", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {

            }
        });
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView text;
        View view;
        ImageView delete_comment_btn;
        Button  edit_comment_btn;

        CommentViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            text = itemView.findViewById(R.id.text_text);
            username = itemView.findViewById(R.id.username);
            delete_comment_btn = itemView.findViewById(R.id.delete_comment_button);
            edit_comment_btn = itemView.findViewById(R.id.edit_comment_button);
        }
    }

    class TRAdapter extends RecyclerView.Adapter<CommentViewHolder>   {

        List<Comment> listFiltered = new ArrayList<>();
        Context context;


        public TRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
            CommentViewHolder holder = new CommentViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, final int position) {

            holder.text.setText(listFiltered.get(position).text); //list.get(position).username

            holder.username.setText("@" + listFiltered.get(position).userUsername + ":"); //list.get(position).username

            if(!selfUsername.equals(listFiltered.get(position).userUsername)) {
                holder.delete_comment_btn.setVisibility(View.GONE);
                holder.edit_comment_btn.setVisibility(View.GONE);
            }

            holder.delete_comment_btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    deleteComment(position);
                }
            });

            holder.edit_comment_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editComment(position);
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


        public void deleteComment(int position){
            Call<String> call = mTodoService.deleteComment(publicationId,listFiltered.get(position).id);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    if (response.isSuccessful()) {
                        listFiltered.remove(position);
                        AddComment.this.finish();
                        AddComment.this.startActivity(getIntent());

                    } else {
                        Toast toast = Toast.makeText(AddComment.this, "Error deleting comment", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast toast = Toast.makeText(AddComment.this, "Error deleting comment", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

        public void editComment(int position){
            AlertDialog.Builder edit_dialog = new AlertDialog.Builder(AddComment.this);
            View dialog_view = getLayoutInflater().inflate(R.layout.edit_comment_layout, null);
            edit_dialog.setView(dialog_view);

            EditText new_comment = dialog_view.findViewById(R.id.comment_edited);
            new_comment.setText(listFiltered.get(position).text);

            Button edit_btn = dialog_view.findViewById(R.id.comment_edited_button);
            edit_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String new_text = new_comment.getText().toString();
                    if(new_text.length()==0){
                        Toast.makeText(getApplicationContext(), "Write a comment first", Toast.LENGTH_LONG).show();
                    }
                    else{
                        CommentPost cp = new CommentPost();
                        cp.text = new_text;
                        cp.publicationId = publicationId;
                        Long commentId = listFiltered.get(position).id;
                        Call<Comment> call = mTodoService.editComment(publicationId,commentId,cp);
                        call.enqueue(new Callback<Comment>() {
                            @Override
                            public void onResponse(Call<Comment> call, Response<Comment> response) {
                                if(response.isSuccessful()){
                                    Intent intent = new Intent(AddComment.this,AddComment.class);
                                    Bundle b = new Bundle();
                                    b.putLong("id",publicationId);
                                    intent.putExtras(b);
                                    finish();
                                    startActivity(intent);
                                }
                                else{
                                    Toast toast = Toast.makeText(AddComment.this, "Error editComment bad response", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Comment> call, Throwable t) {
                                Toast toast = Toast.makeText(AddComment.this, "Error editComment no response", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                    }
                }
            });


            AlertDialog dialog = edit_dialog.create();
            dialog.show();

            Button edit_cancel_btn = dialog_view.findViewById(R.id.edit_comment_cancel_button);
            edit_cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });
        }

        // Insert a new item to the RecyclerView
        public void insert(int position, Comment data) {
            listFiltered.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(User data) {
            int position = listFiltered.indexOf(data);
            listFiltered.remove(position);
            notifyItemRemoved(position);
        }



        public void animate(RecyclerView.ViewHolder viewHolder) {
            final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
        }

        public void add(Comment t) {
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
