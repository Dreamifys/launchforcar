package com.launchforcar.carlauncher.ui.drawer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.launchforcar.carlauncher.data.model.AppItem;
import com.launchforcar.carlauncher.databinding.ItemAppDrawerBinding;

import java.util.ArrayList;
import java.util.List;

public class AppDrawerAdapter extends RecyclerView.Adapter<AppDrawerAdapter.AppViewHolder> {

    private final List<AppItem> items = new ArrayList<>();

    public void submitList(List<AppItem> appItems) {
        items.clear();
        items.addAll(appItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new AppViewHolder(ItemAppDrawerBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {

        private final ItemAppDrawerBinding binding;

        AppViewHolder(ItemAppDrawerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AppItem item) {
            binding.appName.setText(item.getName());
            binding.appCategory.setText(item.getCategory());
            binding.appAvatar.setText(item.getName().substring(0, 1));
        }
    }
}