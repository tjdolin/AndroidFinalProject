package cs123.sbcc.edu.finalproject2

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_job_list.*
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast



class JobList : AppCompatActivity(), AdapterView.OnItemClickListener {


    var color = arrayOf(Color.argb(255, 255, 255, 255), Color.argb(255, 255, 0, 0), Color.argb(255, 0, 255, 155), Color.argb(255, 0, 0, 255))



    var jobsAdapter: ArrayAdapter<Any>? = null

    val newFaves = HashSet<String>()

    var jobs : ArrayList<String>? = null

    var links : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_list)
        val intent = getIntent()
        jobs = intent.getStringArrayListExtra("jobs")
        var savedColor = intent.getIntExtra("color", 0)
        jobLayout.setBackgroundColor(color[savedColor]);
        links = intent.getStringArrayListExtra("links")
        jobsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, jobs as List<String>)
        jobsList.setAdapter(jobsAdapter)
        jobsList.setOnItemClickListener(this)
        jobsList.setLongClickable(true)
        jobsList.setOnItemLongClickListener(AdapterView.OnItemLongClickListener { parent, v, position, id ->
            val uri = Uri.parse(links!![position])
            val it = Intent(Intent.ACTION_VIEW, uri)
            startActivity(it)
            false
        })

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        if(newFaves.contains(jobs!![position].substring(1))){
            newFaves.remove(jobs!![position].substring(1))
            jobs!![position] = jobs!![position].substring(1)

            }
        else{
            newFaves.add(jobs!![position])
            jobs!![position] = "*" + jobs!![position]
        }

        jobsAdapter?.notifyDataSetChanged()

    }


    fun doneClicked(v:View){

        var intent = Intent()
        intent.putExtra("faves", ArrayList<String>(newFaves))
        setResult(RESULT_OK, intent)
        finish()

    }


}
