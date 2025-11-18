package com.example.tema4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Activity3 : AppCompatActivity() {

    private lateinit var listViewTags: ListView
    private lateinit var gridViewImages: GridView
    private lateinit var btnAsociaza: Button

    private lateinit var btnCatreEcran1: Button
    private lateinit var btnCatreEcran2: Button
    private lateinit var btnCatreEcran4: Button

    private lateinit var dbHelper: DBHelper

    private var tagList = mutableListOf<TagPoza>()
    private var tagSelectatId: Int? = null

    private lateinit var imageAdapter: SelectableImageAdapter
    private val pozeSelectate = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3)

        dbHelper = DBHelper(this)

        listViewTags = findViewById(R.id.listViewTags)
        gridViewImages = findViewById(R.id.gridViewImages)
        btnAsociaza = findViewById(R.id.btnAsociaza)

        btnCatreEcran1 = findViewById(R.id.catreEcran1)
        btnCatreEcran2 = findViewById(R.id.catreEcran2)
        btnCatreEcran4 = findViewById(R.id.catreEcran4)

        incarcaTaguri()
        incarcaGalerie()

        listViewTags.setOnItemClickListener { _, _, position, _ ->
            val tagSelectat = tagList[position]
            tagSelectatId = tagSelectat.idTag
            Toast.makeText(this, "Tag selectat: ${tagSelectat.tag}", Toast.LENGTH_SHORT).show()
        }

        btnAsociaza.setOnClickListener {
            if (tagSelectatId == null) {
                Toast.makeText(this, "Selecteaza un tag", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pozeSelectate.isEmpty()) {
                Toast.makeText(this, "Selecteazao poza", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            for (idPoza in pozeSelectate) {
                val result = dbHelper.adaugaTagLaPoza(idPoza, tagSelectatId!!)
                if (result == -1L) {
                    Log.w("Activity3", "Asocierea poza: $idPoza cu tag: $tagSelectatId a esuat (posibil duplicat).")
                }
            }

            Toast.makeText(this, "Tag-ul a fost asociat pozelor", Toast.LENGTH_LONG).show()

            pozeSelectate.clear()
            imageAdapter.clearSelections()
            tagSelectatId = null
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

        btnCatreEcran4.setOnClickListener {
            val intent = Intent(this, Activity4::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun incarcaTaguri() {
        tagList = dbHelper.obtineToateTagurile().toMutableList()
        val tagNumeList = tagList.map { it.tag }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tagNumeList)
        listViewTags.adapter = adapter
    }

    private fun incarcaGalerie() {
        val poze = dbHelper.obtineToatePozele()

        val drawableIds = mutableListOf<Int>()
        for (poza in poze) {
            val resId = resources.getIdentifier(poza.numeFisier, "drawable", packageName)
            Log.d("Activity3_IMG_DEBUG", "Nume DB: ${poza.numeFisier}, resId: $resId")
            if (resId != 0) {
                drawableIds.add(resId)
            }
        }

        imageAdapter = SelectableImageAdapter(this, drawableIds) { pozitie, isSelectat ->
            if (pozitie < poze.size) {
                val idPoza = poze[pozitie].idPoza
                if (isSelectat) {
                    pozeSelectate.add(idPoza)
                } else {
                    pozeSelectate.remove(idPoza)
                }
            }
            Log.d("Activity3_IMG_DEBUG", "Poze selectate: $pozeSelectate")
        }
        gridViewImages.adapter = imageAdapter
    }
}