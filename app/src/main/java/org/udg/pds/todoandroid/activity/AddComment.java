package org.udg.pds.todoandroid.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Comment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_comment_layout);
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        Bundle b = getIntent().getExtras();
        publicationId = b.getLong("id");
        //Toast.makeText(getApplicationContext(), "id: " + publicationId, Toast.LENGTH_LONG).show();


    }

    @Override
    public void onStart() {

        super.onStart();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        mRecyclerView = this.findViewById(R.id.recycle_view_comments);

        mAdapter = new TRAdapter(this.getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


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
/*
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == Global.RQ_ADD_TASK) {
                this.updateCommentList();
            }
        }*/
        // Button b = getView().findViewById(R.id.b_add_task_rv);
        // This is the listener to the "Add Task" button
        /*b.setOnClickListener(view -> {
            // When we press the "Add Task" button, the AddTask activity is called, where
            // we can introduce the data of the new task
            Intent i = new Intent(TaskList.this.getContext(), AddTask.class);
            // We launch the activity with startActivityForResult because we want to know when
            // the launched activity has finished. In this case, when the AddTask activity has finished
            // we will update the list to show the new task.
            startActivityForResult(i, Global.RQ_ADD_TASK);
        });*/


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

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView text;
        View view;

        CommentViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            text = itemView.findViewById(R.id.text_text);
            username = itemView.findViewById(R.id.username);

        }
    }

    static class TRAdapter extends RecyclerView.Adapter<CommentViewHolder>   {

        //List<User> list = new ArrayList<>();
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


            /*
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Bundle bundle = new Bundle();
                    bundle.putBoolean("is_private", false);
                    bundle.putLong("user_to_search", listFiltered.get(position).id);
                    SearchFragmentDirections.ActionActionSearchToActionProfile action =
                        SearchFragmentDirections.actionActionSearchToActionProfile();
                    action.setIsPrivate(bundle.getBoolean("is_private"));
                    action.setUserToSearch(bundle.getLong("user_to_search"));
                    Navigation.findNavController(view).navigate(action);
                }
            });

               */
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
        public void insert(int position, Comment data) {
            //list.add(position, data);
            listFiltered.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(User data) {
            //int position = list.indexOf(data);
            //list.remove(position);
            int position = listFiltered.indexOf(data);
            listFiltered.remove(position);
            notifyItemRemoved(position);
        }



        public void animate(RecyclerView.ViewHolder viewHolder) {
            final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
        }

        public void add(Comment t) {
            //list.add(t);
            listFiltered.add(t);
            this.notifyItemInserted(listFiltered.size() - 1);
        }

        public void clear() {
            int size = listFiltered.size();
            //list.clear();
            listFiltered.clear();
            this.notifyItemRangeRemoved(0, size);
        }

    }

}
