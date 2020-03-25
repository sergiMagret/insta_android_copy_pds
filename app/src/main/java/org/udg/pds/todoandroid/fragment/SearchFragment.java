package org.udg.pds.todoandroid.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by imartin on 12/02/16.
 */
public class SearchFragment extends Fragment {

    TodoApi mTodoService;
    SearchView mSearchView;
    RecyclerView mRecyclerView;
    private TRAdapter mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.search_list, container, false);
    }

    @Override
    public void onStart() {

        super.onStart();
        mTodoService = ((TodoApp) this.getActivity().getApplication()).getAPI();

        mRecyclerView = getView().findViewById(R.id.recyclerView1);

        mAdapter = new TRAdapter(this.getActivity().getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        SearchView searchView = (SearchView) getView().findViewById(R.id.action_search);


        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });


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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Global.RQ_ADD_TASK) {
            this.updateUserList();
        }
    }

    public void updateUserList() {

        Call<List<User>> call = mTodoService.getUsers();

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    SearchFragment.this.showUserList(response.body());
                } else {
                    Toast.makeText(SearchFragment.this.getContext(), "Error reading users", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username;

        View view;

        UserViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            username = itemView.findViewById(R.id.itemUsername);

        }
    }

    static class TRAdapter extends RecyclerView.Adapter<UserViewHolder> implements Filterable {

        List<User> list = new ArrayList<>();
        List<User> listFiltered = new ArrayList<>();
        Context context;

        public TRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_layout, parent, false);
            UserViewHolder holder = new UserViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, final int position) {
            holder.username.setText(listFiltered.get(position).username); //list.get(position).username


            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Activity activity  = (Activity) view.getContext();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("is_private", false);
                    bundle.putLong("user_to_search", list.get(position).id);
                    UserProfileFragment userProf = new UserProfileFragment();
                    userProf.setArguments(bundle);
                    activity.getFragmentManager().beginTransaction().replace(R.id.main_content, userProf).commit();
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
            list.add(position, data);
            listFiltered.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(User data) {
            int position = list.indexOf(data);
            list.remove(position);
            position = listFiltered.indexOf(data);
            listFiltered.remove(position);
            notifyItemRemoved(position);
        }

        public void animate(RecyclerView.ViewHolder viewHolder) {
            final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.anticipate_overshoot_interpolator);
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
        }

        public void add(User t) {
            list.add(t);
            listFiltered.add(t);
            this.notifyItemInserted(listFiltered.size() - 1);
        }

        public void clear() {
            int size = list.size();
            list.clear();
            listFiltered.clear();
            this.notifyItemRangeRemoved(0, size);
        }
        @Override
        public Filter getFilter() {
            return new Filter() {


                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        listFiltered = list;
                    } else {
                        List<User> filteredList = new ArrayList<>();
                        for (User row : list) {

                            // name match condition. this might differ depending on your requirement
                            // here we are looking for name or phone number match
                            if (row.getUsername().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }

                        listFiltered = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = listFiltered;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    listFiltered = (ArrayList<User>) filterResults.values;

                    // refresh the list with filtered data
                    notifyDataSetChanged();
                }
            };
        }
    }
}
