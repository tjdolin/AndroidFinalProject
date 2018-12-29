package cs123.sbcc.edu.finalproject2

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import cs123.sbcc.edu.finalproject2.R.menu.menu_main
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.*
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.jsoup.Jsoup

class MainActivity : AppCompatActivity() {

    private var faves = HashSet<String>()
    private var requestQueue: RequestQueue? = null
    var jobs = ArrayList<String>()
    var color = arrayOf(Color.argb(255, 255, 255, 255), Color.argb(255, 255, 0, 0), Color.argb(255, 0, 255, 155), Color.argb(255, 0, 0, 255))
    var currentColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        var inflater = getMenuInflater()
        inflater.inflate(menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.clearFaves ->{

                clearFaves()
                return true
            }

            R.id.changeColor -> {

                changeColor()

                 return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    fun clearFaves(){
        faves.clear()
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putStringSet("faves", faves?.toSet())

        editor.commit()

    }

    fun changeColor() {

       when(currentColor){
           0 -> currentColor = 1
           1 -> currentColor = 2
           2 -> currentColor = 3
           3 -> currentColor = 0
       }

        mainLayout.setBackgroundColor(color[currentColor])


        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putStringSet("color", faves?.toSet())
        editor.commit()
    }

    fun hornClicked(v: View){
        createRequest("horn")
    }

    fun trumpetClicked(v: View){
        createRequest("trumpet")
    }

    fun tromboneClicked(v: View){
        createRequest("trombone")
    }

    fun tubaClicked(v: View){
        createRequest("tuba-and-euphonium")
    }

    fun faveClicked(v: View){
        if (faves.size > 0)
        startActivityForResult(intentFor<JobList>("jobs" to faves.toList(), "color" to currentColor),1)
        else toast("list is empty")
    }

    fun onResponse(response:String)= runBlocking<Unit>{


        var listSeperator = ListSeperator(response, jobs).execute()

        var links = async(Unconfined ){getLinks(response) }

        jobs = listSeperator.get()



        startActivityForResult(intentFor<JobList>("jobs" to jobs, "color" to currentColor, "links" to links.await()),0)
    }

    suspend fun getLinks(response: String) : ArrayList<String>{

        var doc = Jsoup.parse(response.substringAfter("recently added").substringBefore("Past Auditions"))
        var toAdd = doc.select("p > strong > a[href]")
        var returnList = ArrayList<String>()


        for (i in toAdd){

            returnList.add(i.attr("href"))

        }

        return returnList
    }

    fun onErrorResponse(error: VolleyError){
        toast("Network Error")
    }

    fun createRequest(instrument: String){

        val stringRequest = StringRequest(
                Request.Method.GET,
                "https://www.lastrowmusic.com/" + instrument + "/auditions/",
                this::onResponse,this::onErrorResponse)

        requestQueue?.add(stringRequest)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(resultCode == RESULT_OK) {

            if(requestCode == 0) {
                var newFaves = data?.extras?.getStringArrayList("faves")
                if (newFaves != null) {
                    faves = newFaves.toHashSet()
                }
            }
            if(requestCode == 1){
                var newFaves = data?.extras?.getStringArrayList("faves")
                if (newFaves != null) {


                    faves.removeAll(newFaves.toHashSet())
                    val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                    editor.putStringSet("faves", faves?.toSet())
                    editor.commit()
               }
            }
            }
        }


    override fun onPause(){

        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putStringSet("faves", faves?.toSet())
        editor.putInt("color", currentColor)
        editor.commit()
        super.onPause()

    }

    override fun onResume(){

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        var savedFaves = prefs.getStringSet("faves", null)
   //     var savedColor = prefs.getInt("color", 0)
        var savedColor = 0

        if(savedFaves.size > 0){
            faves.addAll(savedFaves)
        }
        mainLayout.setBackgroundColor(color[savedColor]);
        super.onResume()
    }

    class ListSeperator(var html: String, var jobs : ArrayList<String>): AsyncTask<Unit, Unit, ArrayList<String>>() {

        override fun doInBackground(vararg params: Unit?):ArrayList<String> {

            var doc = Jsoup.parse(html.substringAfter("recently added").substringBefore("Past Auditions"))
                    //.substringBefore("Past Auditions").substringAfter("Last Updated")
            var returnList = ArrayList<String>()

            var toAdd = doc.select("p > strong > a[href]")

            for (i in toAdd){

                returnList.add(i.text())

            }


            return returnList

        }

    //    override fun onPostExecute(result: ArrayList<String>?) {
      //      jobs.clear()
        //    jobs.addAll(result!!)
          //  return result!!
        //}


    }


}


