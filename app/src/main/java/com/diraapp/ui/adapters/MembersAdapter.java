package com.diraapp.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.ui.activities.PreviewActivity;
import com.diraapp.ui.components.DiraPopup;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Member;
import com.diraapp.storage.AppStorage;

import java.util.ArrayList;
import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {


    private final LayoutInflater layoutInflater;
    private final Activity context;
    private final String roomSecret;
    private List<Member> members = new ArrayList<>();


    public MembersAdapter(Activity context, String roomSecret) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.roomSecret = roomSecret;

    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public MembersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MembersAdapter.ViewHolder(layoutInflater.inflate(R.layout.member_element, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MembersAdapter.ViewHolder holder, int position) {
        Member member = members.get(position);

        holder.memberName.setText(member.getNickname());
        Bitmap pic = AppStorage.getBitmapFromPath(member.getImagePath());

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiraPopup diraPopup = new DiraPopup(context);
                diraPopup.setCancellable(true);
                diraPopup.show(context.getString(R.string.delete_member_title),
                        context.getString(R.string.delete_member_text),
                        null,
                        null, new Runnable() {
                            @Override
                            public void run() {
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DiraRoomDatabase.getDatabase(context).getMemberDao().delete(member);

                                    }
                                });
                                thread.start();
                                int index = members.indexOf(member);
                                members.remove(member);

                                notifyItemRemoved(index);

                            }
                        });
            }
        });
        if (pic != null) {
            holder.memberPicture.setImageBitmap(pic);
            holder.memberPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(layoutInflater.getContext(), PreviewActivity.class);
                    intent.putExtra(PreviewActivity.URI, member.getImagePath());
                    intent.putExtra(PreviewActivity.IS_VIDEO, false);
                    layoutInflater.getContext().startActivity(intent);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView memberName;
        ImageView memberPicture;
        ImageView deleteButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            memberName = itemView.findViewById(R.id.member_name);
            memberPicture = itemView.findViewById(R.id.member_picture);
            deleteButton = itemView.findViewById(R.id.button_delete_member);
        }
    }
}
