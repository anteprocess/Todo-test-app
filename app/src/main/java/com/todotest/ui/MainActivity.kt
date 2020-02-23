package com.todotest.ui

import android.app.Activity
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.todotest.adapters.TodoListAdapter
import com.todotest.data.database.TodoItem
import com.todotest.utilities.Constants
import com.todotest.utilities.convertMillis
import com.todotest.utilities.convertNumberToMonthName
import com.todotest.viewmodel.TodoViewModel
import com.todotest.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.todo_item_display_details_dialog.*
import kotlinx.android.synthetic.main.todo_list.*

class MainActivity : AppCompatActivity(), TodoListAdapter.TodoItemClickListener {

    private lateinit var todoViewModel: TodoViewModel
    private lateinit var searchView: SearchView
    private lateinit var todoAdapter: TodoListAdapter

    private var dialog: Dialog? = null
    private var countDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Prepare recycler view.
        todo_list.layoutManager = LinearLayoutManager(this)
        todoAdapter = TodoListAdapter(this)
        todo_list.adapter = todoAdapter

        todoViewModel = ViewModelProviders.of(this).get(TodoViewModel::class.java)
        todoViewModel.getAllTodoItemList().observe(this, Observer { it ->

            val itemsWithNoDeadline = mutableListOf<TodoItem>()
            val completedItems = mutableListOf<TodoItem>()

            for (item in it) {
                if (item.dueTime!!.toInt() == 0 && !item.completed) {
                    itemsWithNoDeadline.add(item)
                } else if (item.completed) {
                    completedItems.add(item)
                }
            }

            for (item in itemsWithNoDeadline) {
                it.remove(item)
            }

            for (item in completedItems) {
                it.remove(item)
            }

            it.sortBy { it.dueTime }

            it.addAll(itemsWithNoDeadline)
            it.addAll(completedItems)

            todoAdapter.setTodoItems(it)

            if (it.size == 0) {
                displayEmptyTaskText()
            }
        })

        fab_add_item.setOnClickListener {
            clearSearchView()
            val intent = Intent(this@MainActivity, EditTodoActivity::class.java)
            startActivityForResult(intent, Constants.INTENT_CREATE_TODO_ITEM)
        }
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
        countDialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val todoItem = data?.getParcelableExtra<TodoItem>(Constants.KEY_INTENT)!!
            when (requestCode) {
                Constants.INTENT_CREATE_TODO_ITEM -> {
                    todoViewModel.saveTodoItem(todoItem)

                    hideEmptyTaskText()
                }
                Constants.INTENT_EDIT_TODO_ITEM -> {

                    todoViewModel.updateTodoItem(todoItem)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_todo_search, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu?.findItem(R.id.search_todo)
            ?.actionView as SearchView
        searchView.setSearchableInfo(
            searchManager
                .getSearchableInfo(componentName)
        )
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                todoAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                todoAdapter.filter.filter(newText)
                return false
            }

        })
        return true
    }

    private fun clearSearchView() {
        if (!searchView.isIconified) {
            searchView.isIconified = true
            return
        }
    }

    override fun onDeleteClicked(todoItem: TodoItem) {
        todoViewModel.deleteTodoItem(todoItem)
    }

    override fun onItemClicked(todoItem: TodoItem) {
        clearSearchView()

        // display the details of the item in a dialog.
        displayEventDetails(todoItem)

    }

    override fun onCheckClicked(todoItem: TodoItem) {

        todoViewModel.toggleCompleteState(todoItem)
    }

    private fun displayEventDetails(todoItem: TodoItem) {
        dialog = Dialog(this)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(R.layout.todo_item_display_details_dialog)

        dialog!!.tv_todo_title_content.text = todoItem.title
        dialog!!.tv_todo_description_content.text = todoItem.description

        if (todoItem.dueTime!!.toInt() == 0) {
            dialog!!.tv_todo_due_content.text = "Date is not set"
        } else {
            val dateValues = convertMillis(todoItem.dueTime)
            val displayFormat: String
            if (dateValues[4] < 10) {
                displayFormat = String
                    .format(
                        getString(R.string.due_date_minute_less_than_ten),
                        convertNumberToMonthName(dateValues[1]),
                        dateValues[0],
                        dateValues[2],
                        dateValues[3],
                        dateValues[4]
                    )
            } else {
                displayFormat = String
                    .format(
                        getString(R.string.due_date_minute_greater_than_ten),
                        convertNumberToMonthName(dateValues[1]),
                        dateValues[0],
                        dateValues[2],
                        dateValues[3],
                        dateValues[4]
                    )
            }

            dialog!!.tv_todo_due_content.text = displayFormat
        }

        if (todoItem.completed) {
            dialog!!.button_complete_todo_item.text = getString(R.string.mark_as_incomplete)
        } else {
            dialog!!.button_complete_todo_item.text = getString(R.string.mark_as_complete)
        }
        dialog!!.button_complete_todo_item.setOnClickListener {
            if (!todoItem.completed) {
                dialog!!.button_complete_todo_item.text = getString(R.string.mark_as_incomplete)
            } else {
                dialog!!.button_complete_todo_item.text = getString(R.string.mark_as_complete)
            }
            onCheckClicked(todoItem)
        }


        dialog!!.button_edit_todo_item.setOnClickListener {
            val intent = Intent(this@MainActivity, EditTodoActivity::class.java)
            intent.putExtra(Constants.KEY_INTENT, todoItem)
            startActivityForResult(intent, Constants.INTENT_EDIT_TODO_ITEM)
            dialog!!.dismiss()
        }

        dialog!!.show()
    }

    // If there are no todo tasks, show the textview
    private fun hideEmptyTaskText() {
        if (iv_empty_task_list.visibility == View.VISIBLE) iv_empty_task_list.visibility = View.GONE
    }

    private fun displayEmptyTaskText() {
        if (iv_empty_task_list.visibility == View.GONE) iv_empty_task_list.visibility = View.VISIBLE
    }
}