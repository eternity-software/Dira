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
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.requests.PingMembersRequest;
import com.diraapp.api.updates.BaseMemberUpdate;
import com.diraapp.api.updates.MemberUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.api.views.BaseMember;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Member;
import com.diraapp.res.Theme;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.activities.PreviewActivity;
import com.diraapp.ui.components.DiraPopup;

import java.util.ArrayList;
import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> implements UpdateListener {


    private final LayoutInflater layoutInflater;
    private final Activity context;
    private final String roomSecret;
    private List<StatusMember> members = new ArrayList<>();




    public MembersAdapter(Activity context, String roomSecret) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.roomSecret = roomSecret;
        UpdateProcessor.getInstance().addUpdateListener(this);



        new Thread(() -> {
            try
            {
                Thread.sleep(10 * 1000);
                for(StatusMember statusMember : members)
                {
                    if(statusMember.getStatus() == MemberStatus.WAITING)
                    {
                        statusMember.setStatus(MemberStatus.OFFLINE);
                        context.runOnUiThread(() -> notifyItemChanged(members.indexOf(statusMember)));
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }).start();
    }

    public void setMembers(List<Member> members) {
        for(Member member : members)
        {
            this.members.add(new StatusMember(member, MemberStatus.WAITING));
        }

    }

    @NonNull
    @Override
    public MembersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MembersAdapter.ViewHolder(layoutInflater.inflate(R.layout.member_element, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MembersAdapter.ViewHolder holder, int position) {
        StatusMember statusMember = members.get(position);

        Member member = statusMember.getMember();

        holder.memberName.setText(member.getNickname());

        Bitmap pic = AppStorage.getBitmapFromPath(member.getImagePath());

        if(statusMember.getStatus() == MemberStatus.OFFLINE)
        {
            holder.memberStatus.setText(context.getString(R.string.members_status_offline));
            holder.memberStatus.setTextColor(Theme.getColor(context, R.color.paintOrange));
        }
        else if(statusMember.getStatus() == MemberStatus.READY)
        {
            holder.memberStatus.setText(context.getString(R.string.members_status_online));
            holder.memberStatus.setTextColor(Theme.getColor(context, R.color.accent));
        }
        else if(statusMember.getStatus() == MemberStatus.WAITING)
        {
            holder.memberStatus.setText(context.getString(R.string.members_status_pinging));
            holder.memberStatus.setTextColor(Theme.getColor(context, R.color.light_gray));
        }

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
                                int index = members.indexOf(statusMember);
                                members.remove(statusMember);

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
        else
        {
            holder.memberPicture.setImageDrawable(context.getDrawable(R.drawable.placeholder));
        }
    }

    @Override
    public void onUpdate(Update update) {
        if(update.getUpdateType() != UpdateType.BASE_MEMBER_UPDATE) return;
        BaseMemberUpdate baseMemberUpdate = (BaseMemberUpdate) update;

        if (baseMemberUpdate.getRoomSecret().equals(roomSecret)) {


            BaseMember baseMember = baseMemberUpdate.getBaseMember();

            for(StatusMember statusMember : members)
            {
                if(baseMember.getId().equals(statusMember.getMember().getId()))
                {
                    statusMember.setStatus(MemberStatus.READY);
                    context.runOnUiThread(()-> {
                        notifyItemChanged(members.indexOf(statusMember));
                    });
                }
            }

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
        ImageView deleteButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            memberName = itemView.findViewById(R.id.member_name);
            memberStatus = itemView.findViewById(R.id.member_status);
            memberPicture = itemView.findViewById(R.id.member_picture);
            deleteButton = itemView.findViewById(R.id.button_delete_member);
        }
    }
}
