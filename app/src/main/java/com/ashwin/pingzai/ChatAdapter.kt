package com.ashwin.pingzai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val onFeedback: (String, String, Int) -> Unit = { _, _, _ -> }
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_AI = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_ai, parent, false)
            AIViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) {
            holder.userText.text = message.text
        } else if (holder is AIViewHolder) {
            holder.aiText.text = message.text
            holder.btnLike.setOnClickListener {
                if (position > 0 && messages[position - 1].isUser) {
                    onFeedback(messages[position - 1].text, message.text, 1)
                }
            }
            holder.btnDislike.setOnClickListener {
                if (position > 0 && messages[position - 1].isUser) {
                    onFeedback(messages[position - 1].text, message.text, -1)
                }
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userText: TextView = itemView.findViewById(R.id.userText)
    }

    class AIViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val aiText: TextView = itemView.findViewById(R.id.aiText)
        val btnLike: android.widget.ImageButton = itemView.findViewById(R.id.btnLike)
        val btnDislike: android.widget.ImageButton = itemView.findViewById(R.id.btnDislike)
    }
}
