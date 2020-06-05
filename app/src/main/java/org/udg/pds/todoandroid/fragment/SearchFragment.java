package org.udg.pds.todoandroid.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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

/**
 * Created by imartin on 12/02/16.
 */
public class SearchFragment extends Fragment {

    static TodoApi mTodoService;
    private View view;
    private Integer elemDemanats;
    private String textDemanat;
    private NavController navController = null;
    private Integer elemPerPagina=20;
    private RecyclerView mRecyclerView;
    private TRAdapter mAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.search_list, container, false);
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

        Button hastagButton = getView().findViewById(R.id.ButtonHastag);
        hastagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("tag",textDemanat);
                SearchFragmentDirections.ActionActionSearchToHastags action =
                    SearchFragmentDirections.actionActionSearchToHastags(bundle.getString("tag"));
                action.setTag(bundle.getString("tag"));
                Navigation.findNavController(view).navigate(action);

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
                                SearchFragment.this.addUserList(response.body());
                            } else {
                                Toast.makeText(SearchFragment.this.getContext(), "Error reading users", Toast.LENGTH_LONG).show();
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

    private void showUserList(List<User> tl) {
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
            this.updateUserList("");
        }
    }




    private void updateUserList(String text) {
        textDemanat=text;
        elemDemanats=elemPerPagina;
        Call<List<User>> call = mTodoService.getUsers(text,0,elemPerPagina);


        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
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



    static class TRAdapter extends RecyclerView.Adapter<UserViewHolder>  {

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



        public void animate(RecyclerView.ViewHolder viewHolder) {
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
