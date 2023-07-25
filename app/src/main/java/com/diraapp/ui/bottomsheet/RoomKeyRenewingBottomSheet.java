package com.diraapp.ui.bottomsheet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.requests.PingMembersRequest;
import com.diraapp.api.requests.encryption.KeyRenewRequest;
import com.diraapp.api.updates.AcceptedStatusAnswer;
import com.diraapp.api.updates.BaseMemberUpdate;
import com.diraapp.api.updates.PingUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.api.views.BaseMember;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.ui.adapters.MemberStatus;
import com.diraapp.ui.adapters.StatusMember;
import com.diraapp.ui.adapters.StatusMemberAdapter;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.StringFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class RoomKeyRenewingBottomSheet  extends BottomSheetDialogFragment implements UpdateListener {

    private Room room;

    private View v;
    private int readyCount = 0;
    private int renewingMembersCount = 0;
    private List<StatusMember> statusMembers = new ArrayList<>();

    private StatusMemberAdapter statusMemberAdapter;

    public RoomKeyRenewingBottomSheet(Room room) {
        this.room = room;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Пустой фон
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.v = inflater.inflate(R.layout.bottom_sheet_renewing, container, true);

        v.findViewById(R.id.button_start_renewing).setOnClickListener((view) -> {



            view.setVisibility(View.GONE);
            v.findViewById(R.id.progress_circular).setVisibility(View.VISIBLE);
            v.findViewById(R.id.recycler_view).setVisibility(View.GONE);
            TextView status = v.findViewById(R.id.status_text);
            status.setText(String.format(getString(R.string.room_encryption_renewing_generating), readyCount, 30));
            renewingMembersCount = readyCount;
            Thread countdown = new Thread(() -> {

                try {
                    UpdateProcessor.getInstance(getContext()).sendRequest(new KeyRenewRequest(room.getSecretName()), new UpdateListener() {
                        @Override
                        public void onUpdate(Update update) {
                            AcceptedStatusAnswer acceptedStatusAnswer = (AcceptedStatusAnswer) update;

                            if(!acceptedStatusAnswer.isAccepted())
                            {

                                if(getActivity() != null)
                                {
                                    getActivity().runOnUiThread(() ->  dismiss());
                                }
                            }
                        }
                    }, room.getServerAddress());
                } catch (UnablePerformRequestException e) {

                }

                int sec = 30;
                while (!isDetached()){
                    try {
                        Thread.sleep(1000);

                        int finalSec = sec;


                        getActivity().runOnUiThread(() -> {
                            try {
                                status.setText(String.format(getString(R.string.room_encryption_renewing_generating), renewingMembersCount, finalSec));
                            }
                            catch (Exception e)
                            {

                            }
                        });
                        if(sec == 0)
                        {
                            break;
                        }
                        sec -= 1;
                    } catch (Exception e) {
                        break;
                    }
               }
            });

            countdown.start();
        });
        UpdateProcessor.getInstance().addUpdateListener(this);
        Thread thread = new Thread(() -> {
            List<Member> memberList = DiraRoomDatabase.getDatabase(getContext()).getMemberDao().getMembersByRoomSecret(room.getSecretName());

            for (Member member : memberList) {
                StatusMember statusMember = new StatusMember(member, MemberStatus.WAITING);
                statusMembers.add(statusMember);
            }

            CacheUtils cacheUtils = new CacheUtils(getContext());
            String id = cacheUtils.getString(CacheUtils.ID);
            String nickname = cacheUtils.getString(CacheUtils.NICKNAME);
            String pic = cacheUtils.getString(CacheUtils.PICTURE);



            BaseMember baseMember = new BaseMember(id, nickname);

            Member member = new Member(baseMember.getId(), baseMember.getNickname(), pic, room.getSecretName(), System.currentTimeMillis());
            StatusMember statusMember = new StatusMember(member, MemberStatus.WAITING);

            statusMembers.add(statusMember);

            getActivity().runOnUiThread(() -> {
                updateWaitingInfo();
                statusMemberAdapter = new StatusMemberAdapter(getActivity(), statusMembers);
                ((RecyclerView) v.findViewById(R.id.recycler_view)).setAdapter(statusMemberAdapter);
            });

            try {
                UpdateProcessor.getInstance().sendRequest(new PingMembersRequest(room.getSecretName()), room.getServerAddress());
            } catch (UnablePerformRequestException e) {
                e.printStackTrace();
            }

        });
        thread.start();




        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        UpdateProcessor.getInstance().removeUpdateListener(this);
    }

    private void updateWaitingInfo()
    {
        try {
            TextView membersReadyText = v.findViewById(R.id.status_text);
            membersReadyText.setText(String.format(getString(R.string.room_encryption_renewing_waiting), readyCount, statusMembers.size()));

            if(readyCount >= statusMembers.size())
            {
                TextView button =  v.findViewById(R.id.button_start_renewing);
                button.setBackground(getContext().getResources().getDrawable(R.drawable.accent_rounded));
                button.setTextColor(getContext().getResources().getColor(R.color.dark));

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public void onUpdate(Update update) {
        if(update.getUpdateType() == UpdateType.BASE_MEMBER_UPDATE)
        {

            if(update.getRoomSecret().equals(room.getSecretName()))
            {
                BaseMemberUpdate baseMemberUpdate = (BaseMemberUpdate) update;
                BaseMember baseMember = baseMemberUpdate.getBaseMember();


                readyCount++;
                boolean foundMember = false;
                for(StatusMember statusMember : statusMembers)
                {
                    if(baseMember.getId().equals(statusMember.getMember().getId()))
                    {
                        statusMember.setStatus(MemberStatus.READY);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusMemberAdapter.notifyItemChanged(statusMembers.indexOf(statusMember));
                                updateWaitingInfo();
                            }
                        });
                        foundMember = true;
                    }
                }

                if(!foundMember)
                {
                    Member member = new Member(baseMember.getId(), baseMember.getNickname(), null, update.getRoomSecret(), System.currentTimeMillis());
                    StatusMember statusMember = new StatusMember(member, MemberStatus.UNKNOWN);
                    statusMembers.add(statusMember);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateWaitingInfo();
                            statusMemberAdapter.notifyItemInserted(statusMembers.indexOf(statusMember));
                        }
                    });
                }


            }

        }
        else if(update.getUpdateType() == UpdateType.RENEWING_CONFIRMED)
        {
            if(getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                    Toast.makeText(getActivity(), "Key updated", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(update.getUpdateType() == UpdateType.RENEWING_CANCEL)
        {
            if(getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        dismiss();
                    Toast.makeText(getActivity(), "Key update failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
