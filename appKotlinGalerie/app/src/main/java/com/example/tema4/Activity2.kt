package com.example.tema4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class Activity2 : AppCompatActivity() {

    private lateinit var btnSelectTags: Button
    private lateinit var gridViewImages: GridView
    private lateinit var dbHelper: DBHelper

    private val taguriSelectate = mutableListOf<Int>()

    private lateinit var btnCatreEcran1: AppCompatButton
    private lateinit var btnCatreEcran3: AppCompatButton
    private lateinit var btnCatreEcran4: AppCompatButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        dbHelper = DBHelper(this)

        btnSelectTags = findViewById(R.id.btnSelectTags)
        gridViewImages = findViewById(R.id.gridViewImages)

        btnCatreEcran1 = findViewById(R.id.catreEcran1)
        btnCatreEcran3 = findViewById(R.id.catreEcran3)
        btnCatreEcran4 = findViewById(R.id.catreEcran4)

        btnSelectTags.setOnClickListener {
            afiseazaDialogSelectareTaguri()
        }

        btnCatreEcran1.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnCatreEcran3.setOnClickListener {
            val intent = Intent(this, Activity3::class.java)
            startActivity(intent)
            finish()
        }

        btnCatreEcran4.setOnClickListener {
            val intent = Intent(this, Activity4::class.java)
            startActivity(intent)
            finish()
        }

        afiseazaPozeCuFiltru(emptyList())
    }

    private fun afiseazaDialogSelectareTaguri() {
        val toateTagurile = dbHelper.obtineToateTagurile()

        if (toateTagurile.isEmpty()) {
            AlertDialog.Builder(this).setTitle("Nu exista taguri").setMessage("Nu ai creat inca taguri, fa cateva pe Ecran 1").setPositiveButton("OK", null).show()
            return
        }

        val taguriNume = toateTagurile.map { it.tag }.toTypedArray()
        val checkedItems = BooleanArray(taguriNume.size) { i ->
            taguriSelectate.contains(toateTagurile[i].idTag)
        }

        AlertDialog.Builder(this).setTitle("Selecteaza taguri").setMultiChoiceItems(taguriNume, checkedItems) { _, which, isChecked ->
                val idTag = toateTagurile[which].idTag
                if (isChecked) {
                    if (!taguriSelectate.contains(idTag)) taguriSelectate.add(idTag)
                } else {
                    taguriSelectate.remove(idTag)
                }
            }.setPositiveButton("OK") { _, _ -> afiseazaPozeCuFiltru(taguriSelectate)
            }.setNegativeButton("Anuleaza", null).show()
    }

    private fun afiseazaPozeCuFiltru(idTaguri: List<Int>) {
        val imaginiFiltrate = if (idTaguri.isEmpty()) {
            dbHelper.obtineToatePozele()
        } else {
            dbHelper.obtinePozePentruTaguri(idTaguri)
        }

        val drawableIds = imaginiFiltrate.mapNotNull { poza ->
            val resId = resources.getIdentifier(poza.numeFisier, "drawable", packageName)
            Log.d("Activity2_IMG_DEBUG", "Nume DB: ${poza.numeFisier}, resId: $resId")
            if (resId != 0) resId else null
        }

        val adapter = ImageAdapter(this, drawableIds)
        gridViewImages.adapter = adapter
    }
}