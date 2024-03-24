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
import com.diraapp.db.entities.Member;
import com.diraapp.res.Theme;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.activities.PreviewActivity;

import java.util.ArrayList;
import java.util.List;

public class StatusMemberAdapter extends RecyclerView.Adapter<StatusMemberAdapter.ViewHolder> {


    private final LayoutInflater layoutInflater;
    private final Activity context;
    private List<StatusMember> members = new ArrayList<>();


    public StatusMemberAdapter(Activity context, List<StatusMember> members) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.members = members;

    }

    public void setMembers(List<StatusMember> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public StatusMemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatusMemberAdapter.ViewHolder(layoutInflater.inflate(R.layout.status_member_element, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StatusMemberAdapter.ViewHolder holder, int position) {
        Member member = members.get(position).getMember();

        holder.memberName.setText(member.getNickname());
        Bitmap pic = AppStorage.getBitmapFromPath(member.getImagePath());

        if (members.get(position).getStatus() == MemberStatus.UNKNOWN) {
            holder.memberStatus.setTextColor(Theme.getColor(context, R.color.red));
            holder.memberStatus.setText(context.getString(R.string.room_encryption_renewing_status_unknown));
        } else if (members.get(position).getStatus() == MemberStatus.READY) {
            holder.memberStatus.setTextColor(Theme.getColor(context, R.color.accent));
            holder.memberStatus.setText(context.getString(R.string.room_encryption_renewing_status_ready));
        } else if (members.get(position).getStatus() == MemberStatus.WAITING) {
            holder.memberStatus.setTextColor(Theme.getColor(context, R.color.light_gray));
            holder.memberStatus.setText(context.getString(R.string.room_encryption_renewing_status_waiting));
        }


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

        } else {
            holder.memberPicture.setImageDrawable(context.getDrawable(R.drawable.placeholder));
        }

    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView memberName;
        TextView memberStatus;
        ImageView memberPicture;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            memberName = itemView.findViewById(R.id.member_name);
            memberPicture = itemView.findViewById(R.id.member_picture);
            memberStatus = itemView.findViewById(R.id.member_status);
        }
    }
}
