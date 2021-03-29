package com.example.wtest.activities

import android.app.DownloadManager
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wtest.model.CodigoPostalModel
import com.example.wtest.CodigosPostaisAdapter
import com.example.wtest.DBHelper
import com.example.wtest.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.FileReader

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var sharedPreferences: SharedPreferences
    private var adapter: CodigosPostaisAdapter? = null

    private var codPostalList = ArrayList<CodigoPostalModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // No arranque da app é verificado o valor presente no sharedPreferences, para verificar se é a primeira vez que corre a APP
        // Caso seja a primeira vez, vai apresentar o Loader com as informações das tarefas que está a executar
        // Senão, apresenta de imediato os Códigos postais na Recycler View
        val appName = getString(R.string.app_name)
        sharedPreferences = this.getSharedPreferences(appName, Context.MODE_PRIVATE)
        val firstTime = sharedPreferences.getString("firstTime", "Yes")
        if (firstTime == "Yes") {
            txtLoader.text = getString(R.string.key_download_file)
            toggleLoader()
            downloadFile()
        } else {
            showPostalCodes()
        }

        createSearchBarListener()
    }

    // Cria o Listener para a search bar
    // Quando é inserido um caracter é feita uma nova pesquisa na base de dados
    private fun createSearchBarListener() {
        search_Bar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(value: String): Boolean {
                codPostalList = dbHelper.getResult(value)
                adapter?.updateList(codPostalList)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        })
    }

    // Neste método é feito o download do ficheiro CSV dos códigos postais
    private fun downloadFile(){
        val request = DownloadManager.Request(Uri.parse("https://raw.githubusercontent.com/centraldedados/codigos_postais/master/data/codigos_postais.csv"))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setTitle("codPostal.csv")
                .setDescription("Códigos Postais")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setDestinationInExternalFilesDir(applicationContext, Environment.DIRECTORY_DOWNLOADS, "codPostal.csv")

        val downloadManager= getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadID = downloadManager.enqueue(request)

        val br = object:BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val id = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 1)
                if(id == downloadID) {
                    // Quando o download acabar vai chamar o importToDb e muda a frase que surge no loading da app.
                    txtLoader.text = getString(R.string.key_creating_db)
                    Handler().postDelayed({
                        importToDB()
                    },100)
                }
            }
        }

        registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    // Neste método
    fun importToDB() {

        dbHelper = DBHelper(this)

        try {
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val fileReader: BufferedReader?
            var line: String?
            val tableName = "Cod_Postais"
            val columns = "num_cod_postal, ext_cod_postal, desig_postal"

            fileReader = BufferedReader(FileReader(applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/codPostal.csv")!!))

            val str1 = "INSERT INTO $tableName ($columns) values("
            val str2 = ");"

            db.beginTransaction()

            // Lê primeiro o header, para no próximo começar na primeira linha de dados
            line = fileReader.readLine()

            line = fileReader.readLine()

            // O while vai percorrer todas as linhas, e preencher uma string com a query para que seja inserida na base de dados os dados, em cada iteração.
            while (line != null) {

                val sb = StringBuilder(str1)
                val codPostais = line.split(",")

                sb.append("" + codPostais[codPostais.size - 3] + ",'")
                sb.append(codPostais[codPostais.size - 2] + "','")

                if (codPostais[codPostais.size - 1].contains("'"))
                    sb.append(codPostais[codPostais.size - 1].replace("'","''") + "'")
                else
                    sb.append(codPostais[codPostais.size - 1] + "'")

                sb.append(str2)
                db.execSQL(sb.toString())

                line = fileReader.readLine()
            }

            db.setTransactionSuccessful()
            db.endTransaction()

            toggleLoader()
            saveCache()
            showPostalCodes()

        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Erro a escrever na Base de dados!", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

    }

    // Neste método é guardado no sharedPreferences a informação que já descarregou e preencheu a base de dados de forma correta
    private fun saveCache() {
        sharedPreferences.edit().putString("firstTime", "No").apply()
    }

    // Neste método é feita a recolha dos valores para apresentar na recycler view.
    private fun showPostalCodes() {
        codPostalList = dbHelper.getResults()
        recyclerID.layoutManager = LinearLayoutManager(applicationContext)
        adapter = CodigosPostaisAdapter(codPostalList, applicationContext)
        recyclerID.adapter = adapter
    }

    // Neste método serve para esconder/mostrar o loader inicial e a action bar.
    private fun toggleLoader() {
        if (loaderCtn.visibility == View.VISIBLE) {
            supportActionBar?.show()
            loaderCtn.visibility = View.GONE
            search_Bar.visibility = View.VISIBLE
            recyclerID.visibility = View.VISIBLE
        } else {
            supportActionBar?.hide()
            search_Bar.visibility = View.INVISIBLE
            recyclerID.visibility = View.INVISIBLE
            loaderCtn.visibility = View.VISIBLE
        }
    }
}