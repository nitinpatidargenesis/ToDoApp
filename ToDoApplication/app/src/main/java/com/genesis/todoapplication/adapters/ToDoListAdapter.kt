package com.genesis.todoapplication.adapters

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.genesis.todoapplication.R
import com.genesis.todoapplication.activity.MainActivity.Companion.isValidEmail
import com.genesis.todoapplication.model.ToDoData
import com.genesis.todoapplication.sqlite.SqliteHelper
import com.google.firebase.database.DatabaseReference
import java.util.ArrayList

class ToDoListAdapter(dataArrayList: ArrayList<ToDoData>,private var context: Context,databaseReference: DatabaseReference) : RecyclerView.Adapter<ToDoListAdapter.ToDoListViewHolder>() {
    private var list: MutableList<ToDoData> = dataArrayList
    private var reference : DatabaseReference? = databaseReference
    private lateinit var sqliteHelper: SqliteHelper


    //create view holder for recycler view list item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_cardlayout, parent, false)
        return ToDoListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoListViewHolder, position: Int) {

        sqliteHelper = SqliteHelper(context)

        val td = list[position]

        if(td.name.equals("") || td.name.equals("null"))
            holder.nameTV!!.text = "N/A"
        else
            holder.nameTV!!.text = td.name

        if(td.email.equals("") || td.email.equals("null"))
            holder.emailTV!!.text = "N/A"
        else
            holder.emailTV!!.text = td.email


        //delete list item listener
        holder.deleteButton.setOnClickListener { view ->
            val id = td.id
            val result = sqliteHelper.deleteTask(id.toString())
            if (result.count == 0) {
                Handler().post {
                    // Code here will run in UI thread
                    list.removeAt(position)
                    notifyItemRemoved(position)
                    reference!!.child(""+td.id).removeValue()
                    notifyItemRangeChanged(position, list.size)
                    notifyDataSetChanged()
                }
            }
        }

        //edit list item listener
        holder.editButton.setOnClickListener { view ->
            val dialog = Dialog(view.context)
            dialog.setContentView(R.layout.custom_dailog)
            dialog.show()
            val nameText = dialog.findViewById(R.id.nameText) as AppCompatEditText
            val emailText = dialog.findViewById(R.id.emailText) as AppCompatEditText
            val cancel = dialog.findViewById(R.id.btn_cancel) as AppCompatButton
            val save = dialog.findViewById(R.id.btn_save) as AppCompatButton
            nameText.setText(td.name)
            emailText.setText(td.email)
            save.text = "Update"

            //cancel edit list item button listener
            cancel.setOnClickListener {
                dialog.dismiss()
            }

            //edit list item button listener
            save.setOnClickListener { view ->
                if(nameText.text.toString().trim().equals("") || nameText.text.toString().trim().equals("null")){
                    Toast.makeText(context, "Please enter name",Toast.LENGTH_SHORT).show();
                }else if(emailText.text.toString().trim().equals("") || emailText.text.toString().trim().equals("null")){
                    Toast.makeText(context, "Please enter email",Toast.LENGTH_SHORT).show();
                }else if(!isValidEmail(emailText.text!!.toString())){
                    Toast.makeText(context, "Please enter valid email",Toast.LENGTH_SHORT).show();
                }else{
                    val updateTd = ToDoData()
                    updateTd.name = nameText.text.toString()
                    updateTd.email = emailText.text.toString()
                    val b = sqliteHelper.updateTask(td.id,updateTd.name,updateTd.email)
                    reference!!.child(""+ td.id).setValue(updateTd)
                    list[position] = updateTd
                    if (b > 0) {
                        Handler().post {
                            notifyDataSetChanged()// Code here will run in UI thread
                        }
                        dialog.hide()
                    } else {
                        dialog.hide()
                    }
                }
            }
        }
    }

    //list item count
    override fun getItemCount(): Int {
        return list.size
    }

    //create a view holder class for recycler view item
    inner class ToDoListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var nameTV: AppCompatTextView? = null
        internal var emailTV: AppCompatTextView? = null
        internal var editButton: AppCompatImageView
        internal var deleteButton: AppCompatImageView

        //initialize widget for list item
        init {
            nameTV = view.findViewById(R.id.nameText) as AppCompatTextView
            emailTV = view.findViewById(R.id.emailText) as AppCompatTextView
            editButton = view.findViewById(R.id.edit) as AppCompatImageView
            deleteButton = view.findViewById(R.id.delete) as AppCompatImageView
            view.setOnClickListener { }
        }
    }
}
