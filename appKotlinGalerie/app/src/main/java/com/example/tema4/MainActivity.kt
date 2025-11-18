package com.example.tema4

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var listViewTags: ListView
    private lateinit var btnAddTag: Button
    private lateinit var editTextTag: EditText
    private lateinit var btnCatreEcran2: Button
    private lateinit var btnCatreEcran3: Button
    private lateinit var btnCatreEcran4: Button
    private lateinit var btnResetDB: Button

    private var tagList = mutableListOf<TagPoza>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        populeazaImaginiInitial()

        listViewTags = findViewById(R.id.listViewTags)
        btnAddTag = findViewById(R.id.btnAddTag)
        editTextTag = findViewById(R.id.editTextTag)
        btnCatreEcran2 = findViewById(R.id.catreEcran2)
        btnCatreEcran3 = findViewById(R.id.catreEcran3)
        btnCatreEcran4 = findViewById(R.id.catreEcran4)
        btnResetDB = findViewById(R.id.btnResetDB)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listViewTags.adapter = adapter

        incarcaTaguri()

        btnAddTag.setOnClickListener {
            val tagText = editTextTag.text.toString().trim()
            if (tagText.isEmpty()) {
                Toast.makeText(this, "Introdu un tag", Toast.LENGTH_SHORT).show()
            } else {
                val id = dbHelper.inserareTag(tagText)
                if (id == -1L) {
                    Toast.makeText(this, "Tag-ul exista deja", Toast.LENGTH_SHORT).show()
                } else {
                    editTextTag.text.clear()
                    incarcaTaguri()
                    Toast.makeText(this, "Tag adaugat", Toast.LENGTH_SHORT).show()
                }
            }
        }

        listViewTags.setOnItemClickListener { _, _, position, _ ->
            val tagSelectat = tagList[position]
            val options = arrayOf("Modifica", "Sterge")

            AlertDialog.Builder(this).setTitle("Actiuni pentru tag-ul: \"${tagSelectat.tag}\"").setItems(options) { dialog, which ->
                    when (which) {
                        0 -> {
                            afiseazaDialogModificareTag(tagSelectat)
                        }
                        1 -> {
                            AlertDialog.Builder(this).setTitle("Sterge tag").setMessage("Esti sigur ca vrei sa stergi tag-ul: \"${tagSelectat.tag}\" ?")
                                .setPositiveButton("Da") { _, _ -> dbHelper.stergeTag(tagSelectat.idTag)
                                    incarcaTaguri()
                                    Toast.makeText(this, "S-a sters tag-ul", Toast.LENGTH_SHORT).show()
                                }.setNegativeButton("Nu", null).show()
                        }
                    }
                }.show()
        }

        btnCatreEcran2.setOnClickListener {
            startActivity(Intent(this, Activity2::class.java))
        }

        btnCatreEcran3.setOnClickListener {
            startActivity(Intent(this, Activity3::class.java))
        }

        btnCatreEcran4.setOnClickListener {
            startActivity(Intent(this, Activity4::class.java))
        }

        btnResetDB.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Reseteaza db").setMessage("Stergi tot, esti sigur?")
                .setPositiveButton("Da") { _, _ -> resetBazaDeDate()
                    Toast.makeText(this, "db a fost resetata!", Toast.LENGTH_LONG).show()
                }.setNegativeButton("Anuleaza", null).show()
        }
    }

    private fun incarcaTaguri() {
        tagList = dbHelper.obtineToateTagurile().toMutableList()
        val tagNumeList = tagList.map { it.tag }
        adapter.clear()
        adapter.addAll(tagNumeList)
        adapter.notifyDataSetChanged()
    }

    private fun afiseazaDialogModificareTag(tag: TagPoza) {
        val input = EditText(this)
        input.setText(tag.tag)
        input.setSelectAllOnFocus(true)

        AlertDialog.Builder(this).setTitle("Modifica tag-ul").setMessage("Introdu noul nume pentru tag-ul \"${tag.tag}\":")
            .setView(input).setPositiveButton("Modifica") { dialog, _ -> val newTagText = input.text.toString().trim()
                if (newTagText.isEmpty()) {
                    Toast.makeText(this, "Numele tag-ului nu poate fi gol", Toast.LENGTH_SHORT).show()
                } else if (newTagText == tag.tag) {
                    Toast.makeText(this, "Noul nume este identic cu cel vechi", Toast.LENGTH_SHORT).show()
                } else {
                    val succes = dbHelper.actualizeazaTag(tag.idTag, newTagText)
                    if (succes) {
                        incarcaTaguri()
                        Toast.makeText(this, "Tag-ul a fost actualizat", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Eroare la actualizarea tag-ului. Poate exista deja un tag cu acest nume.", Toast.LENGTH_LONG).show()
                    }
                }
            }.setNegativeButton("Anuleaza", null).show()
    }

    private fun resetBazaDeDate() {
        dbHelper.close()
        this.deleteDatabase("tags.db")
        dbHelper = DBHelper(this)
        populeazaImaginiInitial()
        incarcaTaguri()
    }

    private fun populeazaImaginiInitial() {
        val pozeExistente = dbHelper.obtineToatePozele()

        if (pozeExistente.isEmpty()) {
            Log.d("MainActivity", "Baza de date cu poze este goala. Populare initiala...")

            val pozeInitiale = mutableListOf<String>()
            for (i in 2..10) //am lasat de la 2 pentru că am luat de la proiectul din SGBD și se numea img_2 prima și am lăsat așa
                {
                pozeInitiale.add("img_$i")
            }

            var imagesAdded = 0
            for (imageName in pozeInitiale) {
                val resId = resources.getIdentifier(imageName, "drawable", packageName)
                if (resId != 0) {
                    val id = dbHelper.inserarePoza(imageName, "O imagine din galerie")
                    if (id != -1L) {
                        imagesAdded++
                        Log.d("MainActivity", "Adaugat imagine: $imageName cu ID: $id")
                    } else {
                        Log.e("MainActivity", "Eroare la adaugarea imaginii $imageName sau exista deja.")
                    }
                } else {
                    Log.w("MainActivity", "Resursa drawable '$imageName' nu a fost gasita. Nu va fi adaugata in DB.")
                    Toast.makeText(this, "Atentie: Imaginea '$imageName' nu exista in 'drawable'!", Toast.LENGTH_LONG).show()
                }
            }
            if (imagesAdded > 0) {
                Toast.makeText(this, "$imagesAdded imagini adaugate automat!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Nicio imagine nu a fost adaugata automat (verifica Logcat).", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d("MainActivity", "Baza de date cu poze nu este goala. Nu se face populare initiala.")
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}