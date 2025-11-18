package com.example.tema4

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Activity4 : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var btngenereazaCsv: AppCompatButton
    private lateinit var trimiteEmail: AppCompatButton
    private lateinit var textViewStatus: TextView

    private lateinit var btnCatreEcran1: AppCompatButton
    private lateinit var btnCatreEcran2: AppCompatButton
    private lateinit var btnCatreEcran3: AppCompatButton

    private var CSVgenerat: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_4)

        dbHelper = DBHelper(this)
        btngenereazaCsv = findViewById(R.id.btngenereazaCsv)
        trimiteEmail = findViewById(R.id.trimiteEmail)
        textViewStatus = findViewById(R.id.textViewStatus)

        btnCatreEcran1 = findViewById(R.id.catreEcran1)
        btnCatreEcran2 = findViewById(R.id.catreEcran2)
        btnCatreEcran3 = findViewById(R.id.catreEcran3)

        btngenereazaCsv.setOnClickListener {
            genereazaCsvFile()
        }

        trimiteEmail.setOnClickListener {
            trimiteCSVprinEmail()
        }

        btnCatreEcran1.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnCatreEcran2.setOnClickListener {
            val intent = Intent(this, Activity2::class.java)
            startActivity(intent)
            finish()
        }

        btnCatreEcran3.setOnClickListener {
            val intent = Intent(this, Activity3::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun genereazaCsvFile() {
        val asocieri = dbHelper.obtineToateAsocierileImagineTagPentruExport()

        if (asocieri.isEmpty()) {
            Toast.makeText(this, "Nu exista asocieri imagine-tag pentru export", Toast.LENGTH_LONG).show()
            textViewStatus.text = "Generare esuata."
            CSVgenerat = null
            return
        }

        val csvNumeFisier = "asocieri_poze_taguri_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val fisierAux = File(cacheDir, csvNumeFisier)

        try {
            FileOutputStream(fisierAux).use { fos ->
                OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                    writer.append("nume poza taguri asociate\n")

                    asocieri.forEach { (poza, taguri) ->
                        val taguriStr = if (taguri.isEmpty()) {
                            "fara taguri"
                        } else {
                            taguri.joinToString(";") { it.tag.replace("\"", "\"\"") }
                        }
                        writer.append("${poza.numeFisier},\"$taguriStr\"\n")
                    }
                }
            }
            CSVgenerat = fisierAux
            Toast.makeText(this, "CSV generat: ${fisierAux.name}", Toast.LENGTH_LONG).show()
            textViewStatus.text = "CSV generat: ${fisierAux.name}"

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Eroare la generarea CSV: ${e.message}", Toast.LENGTH_LONG).show()
            textViewStatus.text = "Eroare la generare: ${e.message}"
            CSVgenerat = null
        }
    }

    private fun trimiteCSVprinEmail() {
        if (CSVgenerat == null || !CSVgenerat!!.exists()) {
            Toast.makeText(this, "Nu exista niciun CSV, fa unul", Toast.LENGTH_LONG).show()
            textViewStatus.text = "fisier negenerat"
            return
        }

        val fileUri: Uri? = FileProvider.getUriForFile(
            this, "${applicationContext.packageName}.fileprovider",
            CSVgenerat!!
        )

        if (fileUri != null) {
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("coacaze4@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Asocieri poze și taguri - Export ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}")
                putExtra(Intent.EXTRA_TEXT, "CSV cu asocierile dintre poze si tag-uri")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(emailIntent, "Trimite CSV prin..."))
                textViewStatus.text = "Intent de email trimis!"
            } else {
                Toast.makeText(this, "Nu ai nicio aplicatie de email.", Toast.LENGTH_LONG).show()
                textViewStatus.text = "Nu ai nicio aplicatie de email"
            }
        } else {
            Toast.makeText(this, "Eroare la generarea URI", Toast.LENGTH_LONG).show()
            textViewStatus.text = "Eroare la URI"
        }
    }
}