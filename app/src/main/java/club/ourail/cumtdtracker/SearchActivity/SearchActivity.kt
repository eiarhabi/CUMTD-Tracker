package club.ourail.cumtdtracker

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import club.ourail.cumtdtracker.MainActivity.MainActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.GsonBuilder
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.io.File


class SearchActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var listview: RecyclerView
    private lateinit var adapter: RecyclerView.Adapter<StopHolder>
    private lateinit var manager: RecyclerView.LayoutManager
    private lateinit var textView: TextInputEditText

    private var list = mutableListOf<MainActivity.Stop>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarColor)
        val currentNightMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }


        val arrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        toolbar = findViewById(R.id.toolbar_search)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(arrow)


        manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = StopListAdapter(list)
        listview = findViewById(R.id.list_search_result)
        listview.layoutManager = manager
        listview.adapter = adapter

        textView = findViewById(R.id.search_input)
        textView.requestFocus()

        textView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                query(p0.toString())
                toolbar.menu.findItem(R.id.clear).isVisible = !p0.isNullOrEmpty()
            }
        })

        val file = File(applicationContext.filesDir, "getstops.json")
        val a = GsonBuilder().create().fromJson(file.readText(), AllStopsResponse::class.java) as AllStopsResponse
        for (i in a.stops) {
            val j = MainActivity.Stop("", i.stop_id, i.stop_name, 1145141919810.0, 0)
            list.add(j)
        }
        adapter.notifyDataSetChanged()
        query("")
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.clear_button, menu)
        toolbar.menu.findItem(R.id.clear).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.clear -> {
                textView.text?.clear()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    @Throws(Exception::class)
    fun query(input: String) {
        if (input == "") {
            list.sortBy { it.stopname }
            adapter.notifyDataSetChanged()
            return
        }
//        list.clear()
//        var temp = input.split(" ", "\'", "\"").toMutableList()
//        var str = "select * from stoptable where name like \'%${temp[0]}%\'"
//
//        for (i in temp.takeLast(temp.size - 1))
//            str += "and name like '%$i%'"
//        str += " order by name"
//
//        Log.e("input", str)
//
//        val cursor = db.rawQuery(str, null)
//        cursor.moveToFirst()
//        if (!cursor.isAfterLast) {
//            val tmp = MainActivity.Stop(
//                "",
//                cursor.getString(0).substringBeforeLast(":"),
//                cursor.getString(1).substringBeforeLast("("),
//                1145141919810.0
//            )
//            list.add(tmp)
//            cursor.moveToNext()
//        }
//
//        while (!cursor.isAfterLast) {
//            val name = cursor.getString(1).substringBeforeLast("(")
//            val id = cursor.getString(0).substringBeforeLast(":")
//            if (list.last().stopname != name) {
//                var tmp = MainActivity.Stop("", id, name, 1145141919810.0)
//                list.add(tmp)
//            }
//            cursor.moveToNext()
//        }
//
//        adapter.notifyDataSetChanged()
//        cursor.close()
        for (i in list) {
            i.ratio = FuzzySearch.tokenSortPartialRatio(input, i.stopname)
        }

        list.sortByDescending { it.ratio }
        adapter.notifyDataSetChanged()
    }


//    override fun onDestroy() {
//        dbHelper.close()
//        super.onDestroy()
//    }
}


