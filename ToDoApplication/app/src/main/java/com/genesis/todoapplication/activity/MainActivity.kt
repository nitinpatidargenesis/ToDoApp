package com.genesis.todoapplication.activity


import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.genesis.todoapplication.R
import com.genesis.todoapplication.adapters.ToDoListAdapter
import com.genesis.todoapplication.model.ToDoData
import com.genesis.todoapplication.sqlite.SqliteHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.lang.IndexOutOfBoundsException
import java.util.ArrayList
import kotlin.collections.contains as contains1


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var addTask: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var dataList = ArrayList<ToDoData>()
    private var firebaseDataList = ArrayList<ToDoData>()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var cd: ConnectionDetector
    private lateinit var databaseReference: DatabaseReference
    private var id : String? = null
    private var resultValue : Int = 0
    private lateinit var sqliteHelper : SqliteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sqliteHelper = SqliteHelper(applicationContext)
        databaseReference = FirebaseDatabase.getInstance().getReference("ToDoData")
        cd = ConnectionDetector()
        cd.isConnectingToInternet(this@MainActivity)
        recyclerView = findViewById(R.id.recycler_view_s)
        layoutManager = LinearLayoutManager(applicationContext)
        addTask = findViewById(R.id.imageButton)
        swipeRefreshLayout = findViewById(R.id.swiperefresh)
        adapter = ToDoListAdapter(dataList, applicationContext,databaseReference)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        swipeRefreshLayout.setOnRefreshListener(this)

        swipeRefreshLayout.post {
            swipeRefreshLayout.isRefreshing = true
            updateList()
        }

        addTask.setOnClickListener {
            //create a custom dialog for add to do task
            val dialog = Dialog(this@MainActivity)
            dialog.setContentView(R.layout.custom_dailog)
            dialog.show()

            val save = dialog.findViewById(R.id.btn_save) as AppCompatButton
            val cancel = dialog.findViewById(R.id.btn_cancel) as AppCompatButton

            cancel.setOnClickListener { dialog.dismiss() }
            save.setOnClickListener {

                var nameText = dialog.findViewById(R.id.nameText) as AppCompatEditText
                var emailText = dialog.findViewById(R.id.emailText) as AppCompatEditText

                if(nameText.text.toString().trim().equals("") || nameText.text.toString().trim().equals("null")){
                    Toast.makeText(this, "Please enter name",Toast.LENGTH_SHORT).show();
                }else if(emailText.text.toString().trim().equals("") || emailText.text.toString().trim().equals("null")){
                    Toast.makeText(this, "Please enter email",Toast.LENGTH_SHORT).show();
                }else if(!isValidEmail(emailText.text!!.toString())){
                    Toast.makeText(this, "Please enter valid email",Toast.LENGTH_SHORT).show();
                }else{
                    id=databaseReference.push().key  //generate unique key from firebase
                    insert(id,nameText.text.toString(),emailText.text.toString())
                    if (resultValue>0) {
                        dialog.hide()
                        updateList()
                    }
                }
            }
        }
    }


    // for update list item
    private fun updateList() {
        swipeRefreshLayout.isRefreshing = true
        if (cd.isConnectingToInternet(this@MainActivity)) {
                databaseReference.addValueEventListener(object: ValueEventListener {
                   override fun onDataChange(dataSnapshot: DataSnapshot) {
                       dataList.clear()
                       firebaseDataList.clear()
                       getLocalData() // get local database
                       for (data in dataSnapshot.children){
                       var key = data.key.toString() // get unique key from firebase
                       val toDoData = data.getValue(ToDoData::class.java)
                       toDoData!!.id=key
                       firebaseDataList.add(toDoData) //get firebase data in list
                   }

                   for (i in 0 until firebaseDataList.size ){
                       try {
                           // compare sqlite and firebase data
                           if (firebaseDataList[i].name != dataList[i].name || firebaseDataList[i].email != dataList[i].email) {
                               val b= sqliteHelper.updateTask(firebaseDataList[i].id, firebaseDataList[i].name, firebaseDataList[i].email) // for update data in sqlite database
                           }
                       }catch (ex: IndexOutOfBoundsException){
                          insert(firebaseDataList[i].id,firebaseDataList[i].name.toString(),firebaseDataList[i].email.toString())
                       }
                   }
                   dataList.clear()
                   dataList.addAll(firebaseDataList)
                   adapter.notifyDataSetChanged() // refreshing data in layout
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        } else {
           getLocalData()
        }
        swipeRefreshLayout.isRefreshing = false
    }


    // for insert data in sqlite database
    private fun insert(id: String?,name: String,email: String) {
        resultValue = sqliteHelper.insertInto(id,name,email)
        if (resultValue>0) {
            var data= ToDoData()
            data.name= name
            data.email=email
            databaseReference.child(""+id).setValue(data)
        } else {
            Toast.makeText(applicationContext, "Some thing went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    // for getting data from sqlite database
    private fun getLocalData(){
        val result = sqliteHelper.selectAllData()
        if (result.count == 0) {
            dataList.clear()
            adapter.notifyDataSetChanged()
        } else {
            dataList.clear()
            adapter.notifyDataSetChanged()
            while (result.moveToNext()) {
                val data = ToDoData()
                data.id=result.getString(0)
                data.name=result.getString(1)
                data.email=result.getString(2)
                dataList.add(data)
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onRefresh() {
        updateList()
    }

    // for checking email validation
    companion object{
        fun isValidEmail(target:CharSequence):Boolean {
            return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }
}
