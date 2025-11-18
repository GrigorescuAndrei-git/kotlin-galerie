package com.example.tema4

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

data class TagPoza(val idTag: Int, val tag: String)

data class Poza(val idPoza: Int, val numeFisier: String, val descriere: String?)

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tags.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_TAGS = "tags"
        private const val COLUMN_ID_TAG = "id_tag"
        private const val COLUMN_TAG = "tag_name"

        private const val TABLE_POZE = "poze"
        private const val COLUMN_ID_POZA = "id_poza"
        private const val COLUMN_NUME_FISIER = "nume_fisier"
        private const val COLUMN_DESCRIERE_POZA = "descriere_poza"

        private const val TABLE_POZE_TAGURI = "poze_taguri"
        private const val COLUMN_POZA_ID = "poza_id"
        private const val COLUMN_TAG_ID_FK = "tag_id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("DBHelper", "onCreate: Crearea tabelelor bazei de date.")
        val CREATE_TAGS_TABLE = "CREATE TABLE $TABLE_TAGS (" + "$COLUMN_ID_TAG INTEGER PRIMARY KEY AUTOINCREMENT," + "$COLUMN_TAG TEXT UNIQUE NOT NULL" + ")"
        db?.execSQL(CREATE_TAGS_TABLE)

        val CREATE_POZE_TABLE = "CREATE TABLE $TABLE_POZE (" + "$COLUMN_ID_POZA INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NUME_FISIER TEXT NOT NULL," + "$COLUMN_DESCRIERE_POZA TEXT" + ")"
        db?.execSQL(CREATE_POZE_TABLE)

        val CREATE_POZE_TAGURI_TABLE = "CREATE TABLE $TABLE_POZE_TAGURI (" + "$COLUMN_POZA_ID INTEGER," + "$COLUMN_TAG_ID_FK INTEGER," +
                "PRIMARY KEY ($COLUMN_POZA_ID, $COLUMN_TAG_ID_FK)," + "FOREIGN KEY ($COLUMN_POZA_ID) REFERENCES $TABLE_POZE($COLUMN_ID_POZA) ON DELETE CASCADE," +
                "FOREIGN KEY ($COLUMN_TAG_ID_FK) REFERENCES $TABLE_TAGS($COLUMN_ID_TAG) ON DELETE CASCADE" + ")"
        db?.execSQL(CREATE_POZE_TAGURI_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("DBHelper", "onUpgrade: Ștergerea și recrearea tabelelor (de la v$oldVersion la v$newVersion).")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_POZE_TAGURI")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_POZE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TAGS")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
        Log.d("DBHelper", "onConfigure: Foreign key constraints enabled.")
    }

    fun inserareTag(tag: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_TAG, tag)
        val result = db.insertWithOnConflict(TABLE_TAGS, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE)
        db.close()
        Log.d("DBHelper", "inserareTag: Tag '$tag' inserat cu ID: $result")
        return result
    }

    fun obtineToateTagurile(): List<TagPoza> {
        val tagList = mutableListOf<TagPoza>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TAGS ORDER BY $COLUMN_TAG ASC", null)

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(COLUMN_ID_TAG)
            val tagIndex = cursor.getColumnIndex(COLUMN_TAG)
            if (idIndex != -1 && tagIndex != -1) {
                do {
                    val id = cursor.getInt(idIndex)
                    val tagName = cursor.getString(tagIndex)
                    tagList.add(TagPoza(id, tagName))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        Log.d("DBHelper", "obtineToateTagurile: S-au găsit ${tagList.size} tag-uri.")
        return tagList
    }

    fun actualizeazaTag(idTag: Int, newTag: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_TAG, newTag)
        val rowsAffected = db.update(
            TABLE_TAGS, contentValues, "$COLUMN_ID_TAG = ? AND $COLUMN_TAG != ?", arrayOf(idTag.toString(), newTag)
        )
        db.close()
        val success = rowsAffected > 0
        Log.d("DBHelper", "actualizeazaTag: Actualizare tag ID $idTag la '$newTag'. Succes: $success ($rowsAffected rânduri afectate).")
        return success
    }

    fun stergeTag(idTag: Int): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(TABLE_TAGS, "$COLUMN_ID_TAG = ?", arrayOf(idTag.toString()))
        db.close()
        Log.d("DBHelper", "stergeTag: Tag ID $idTag șters. Rânduri șterse: $rowsDeleted.")
        return rowsDeleted
    }

    fun inserarePoza(numeFisier: String, descriere: String?): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NUME_FISIER, numeFisier)
        contentValues.put(COLUMN_DESCRIERE_POZA, descriere)
        val result = db.insert(TABLE_POZE, null, contentValues)
        db.close()
        Log.d("DBHelper", "inserarePoza: Poza '$numeFisier' inserată cu ID: $result")
        return result
    }

    fun obtineToatePozele(): List<Poza> {
        val pozaList = mutableListOf<Poza>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_POZE", null)

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(COLUMN_ID_POZA)
            val numeFisierIndex = cursor.getColumnIndex(COLUMN_NUME_FISIER)
            val descriereIndex = cursor.getColumnIndex(COLUMN_DESCRIERE_POZA)

            if (idIndex != -1 && numeFisierIndex != -1) {
                do {
                    val id = cursor.getInt(idIndex)
                    val nume = cursor.getString(numeFisierIndex)
                    val descriere = if (descriereIndex != -1) cursor.getString(descriereIndex) else null
                    pozaList.add(Poza(id, nume, descriere))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        Log.d("DBHelper", "obtineToatePozele: S-au găsit ${pozaList.size} poze.")
        return pozaList
    }

    fun obtinePozePentruTaguri(idTaguri: List<Int>): List<Poza> {
        val pozaList = mutableListOf<Poza>()
        if (idTaguri.isEmpty()) {
            Log.d("DBHelper", "obtinePozePentruTaguri: Lista de ID-uri tag goală, returnez listă goală.")
            return pozaList
        }

        val db = this.readableDatabase
        val placeholders = idTaguri.joinToString(",") { "?" }
        val selectionArgs = idTaguri.map { it.toString() }.toTypedArray()

        val query = "SELECT DISTINCT P.$COLUMN_ID_POZA, P.$COLUMN_NUME_FISIER, P.$COLUMN_DESCRIERE_POZA " +
                "FROM $TABLE_POZE P " + "INNER JOIN $TABLE_POZE_TAGURI PT ON P.$COLUMN_ID_POZA = PT.$COLUMN_POZA_ID " +
                "WHERE PT.$COLUMN_TAG_ID_FK IN ($placeholders)"

        val cursor = db.rawQuery(query, selectionArgs)

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(COLUMN_ID_POZA)
            val numeFisierIndex = cursor.getColumnIndex(COLUMN_NUME_FISIER)
            val descriereIndex = cursor.getColumnIndex(COLUMN_DESCRIERE_POZA)

            if (idIndex != -1 && numeFisierIndex != -1) {
                do {
                    val id = cursor.getInt(idIndex)
                    val nume = cursor.getString(numeFisierIndex)
                    val descriere = if (descriereIndex != -1) cursor.getString(descriereIndex) else null
                    pozaList.add(Poza(id, nume, descriere))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        Log.d("DBHelper", "obtinePozePentruTaguri: S-au găsit ${pozaList.size} poze pentru tag-urile: $idTaguri")
        return pozaList
    }

    fun adaugaTagLaPoza(idPoza: Int, idTag: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_POZA_ID, idPoza)
        contentValues.put(COLUMN_TAG_ID_FK, idTag)
        val result = db.insertWithOnConflict(TABLE_POZE_TAGURI, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE)
        db.close()
        if (result != -1L) {
            Log.d("DBHelper", "adaugaTagLaPoza: Asociere (Poza:$idPoza, Tag:$idTag) adăugată.")
        } else {
            Log.w("DBHelper", "adaugaTagLaPoza: Asociere (Poza:$idPoza, Tag:$idTag) a eșuat (posibil duplicat).")
        }
        return result
    }

    fun obtineToateAsocierileImagineTagPentruExport(): Map<Poza, List<TagPoza>> {
        val asocieri = mutableMapOf<Poza, MutableList<TagPoza>>()
        val db = this.readableDatabase

        val query = "SELECT P.$COLUMN_ID_POZA, P.$COLUMN_NUME_FISIER, P.$COLUMN_DESCRIERE_POZA, " + "T.$COLUMN_ID_TAG, T.$COLUMN_TAG " +
                "FROM $TABLE_POZE P " + "LEFT JOIN $TABLE_POZE_TAGURI PT ON P.$COLUMN_ID_POZA = PT.$COLUMN_POZA_ID " +
                "LEFT JOIN $TABLE_TAGS T ON PT.$COLUMN_TAG_ID_FK = T.$COLUMN_ID_TAG " + "ORDER BY P.$COLUMN_ID_POZA"

        val cursor = db.rawQuery(query, null)

        val idPozaIndex = cursor.getColumnIndex(COLUMN_ID_POZA)
        val numeFisierIndex = cursor.getColumnIndex(COLUMN_NUME_FISIER)
        val descrierePozaIndex = cursor.getColumnIndex(COLUMN_DESCRIERE_POZA)
        val idTagIndex = cursor.getColumnIndex(COLUMN_ID_TAG)
        val tagNameIndex = cursor.getColumnIndex(COLUMN_TAG)

        if (cursor.moveToFirst()) {
            do {
                if (idPozaIndex != -1 && numeFisierIndex != -1) {
                    val idPoza = cursor.getInt(idPozaIndex)
                    val numeFisier = cursor.getString(numeFisierIndex)
                    val descrierePoza = if (descrierePozaIndex != -1) cursor.getString(descrierePozaIndex) else null

                    val poza = Poza(idPoza, numeFisier, descrierePoza)

                    val taguriPentruPoza = asocieri.getOrPut(poza) { mutableListOf() }

                    if (idTagIndex != -1 && tagNameIndex != -1 && !cursor.isNull(idTagIndex)) {
                        val idTag = cursor.getInt(idTagIndex)
                        val tagName = cursor.getString(tagNameIndex)
                        taguriPentruPoza.add(TagPoza(idTag, tagName))
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        Log.d("DBHelper", "obtineToateAsocierileImagineTagPentruExport: S-au gasit ${asocieri.size} poze cu asocieri.")
        return asocieri
    }


    fun resetDatabase() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POZE_TAGURI")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POZE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TAGS")
        onCreate(db)
        db.close()
    }
}