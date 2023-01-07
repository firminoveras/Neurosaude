package com.firmino.neurossaude.admin.panel;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firmino.neurossaude.admin.users.Profile;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsAdapter extends RecyclerView.Adapter<UserDetailsAdapter.QuestionViewHolder> {

    private final List<UserDetailView> users;
    private final Context mContext;
    private OnUserDetailsItemClickListener onUserDetailsItemClickListener;

    public UserDetailsAdapter(Context context) {
        this.users = new ArrayList<>();
        mContext = context;
        onUserDetailsItemClickListener = profile -> {
        };
    }

    public void clearAllViews() {
        notifyItemRangeRemoved(0, users.size());
        users.clear();
    }

    public List<Profile> getSelectedItems() {
        List<Profile> profiles = new ArrayList<>();
        for(UserDetailView u : users){
            if(u.isChecked()) profiles.add(u.getProfile());
        }
        return profiles;
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        public final LinearLayout layout;

        public QuestionViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);
            layout = itemView;
            layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        public void setQuestao(UserDetailView q) {
            if (q.getParent() != null) ((LinearLayout) q.getParent()).removeAllViews();
            layout.removeAllViews();
            layout.addView(q);
        }
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new QuestionViewHolder(new LinearLayout(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(QuestionViewHolder holder, int position) {
        holder.setQuestao(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void addUserDetailView(Profile profile) {
        UserDetailView view = new UserDetailView(mContext);
        view.setup(profile);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setOnUserDetailsViewClick(() -> onUserDetailsItemClickListener.onUserDetailsItemClickListener(profile));
        users.add(view);
        notifyItemInserted(users.size() - 1);
    }

    public void selectAll() {
        for (UserDetailView view : users) view.setChecked(true);
    }

    public void selectClear() {
        for (UserDetailView view : users) view.setChecked(false);
    }

    public void setOnUserDetailsItemClickListener(OnUserDetailsItemClickListener onUserDetailsItemClickListener) {
        this.onUserDetailsItemClickListener = onUserDetailsItemClickListener;
    }

    interface OnUserDetailsItemClickListener {
        void onUserDetailsItemClickListener(Profile profile);
    }

}
