package com.shopcart.shopcart.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopcart.shopcart.R;
import com.shopcart.shopcart.models.Message;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Message> messagesList;
    private Context context;
    private String cUser;
    private static final int CHAT_END = 1;
    private static final int CHAT_END_IM = 3;
    private static final int CHAT_START = 2;
    private static final int CHAT_START_IM = 4;
    private String md;
    private String user_profile="default";

    public MessageAdapter(List<Message> messagesList , Context context, String cUser , String md){
        this.messagesList = messagesList;
        this.context = context;
        this.cUser =cUser;
        this.md=md;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view;

        if (viewType == CHAT_END) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.outgoing_message, parent, false);
        } else if(viewType==CHAT_START){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.incoming_message, parent, false);
        }else if (viewType==CHAT_END_IM){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_outcoming_image_message, parent, false);
        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_incoming_image_message, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesList.get(position).getmId().equals(md)) {
           if(messagesList.get(position).getType().equals("text")){
               return CHAT_END;
           }else{
               return CHAT_END_IM;
           }
        }else{
            if(messagesList.get(position).getType().equals("text")){
                return CHAT_START;
            }else{
                return CHAT_START_IM;
            }


        }

    }


    class MessageViewHolder extends RecyclerView.ViewHolder{

        private TextView message_id;
        private TextView message_time;

        MessageViewHolder(final View itemView) {
            super(itemView);

            message_id = itemView.findViewById(R.id.tvMessage);
            message_time= itemView.findViewById(R.id.messageTime);



        }
    }
    @Override
    public void onBindViewHolder(final MessageViewHolder holder, final int position) {

          final Message messages = messagesList.get(position);


         try {
             if(messages.getFrom().equals(cUser)){
                 if(messages.getType().equals("text")){
//                     Timestamp stamp = new Timestamp(messages.getTime());
//                     Date date = new Date(stamp.getTime());
                     holder.message_id.setText(messages.getMessage());
//                     holder.message_time.setText(date.getHours()+":"+date.getMinutes());
                 }else{
//                     Timestamp stamp = new Timestamp(messages.getTime());
//                     Date date = new Date(stamp.getTime());
//                     holder.message_time.setText(date.getHours()+":"+date.getMinutes());
                 }

             }else {
                if(messages.getType().equals("text")){
//                    Timestamp stamp = new Timestamp(messages.getTime());
//                    Date date = new Date(stamp.getTime());
                    holder.message_id.setText(messages.getMessage());
//                    holder.message_time.setText(date.getHours()+":"+date.getMinutes());

             }
             }
         }catch (Exception e){
             e.printStackTrace();
         }



    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


}
