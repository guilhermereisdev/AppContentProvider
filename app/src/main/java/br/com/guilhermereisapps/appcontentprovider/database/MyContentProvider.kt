package br.com.guilhermereisapps.appcontentprovider.database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.UnsupportedSchemeException
import android.net.Uri
import android.provider.BaseColumns._ID
import br.com.guilhermereisapps.appcontentprovider.database.NotesDatabaseHelper.Companion.TABLE_NOTES

class MyContentProvider : ContentProvider() {

    private lateinit var mUriMatcher: UriMatcher
    private lateinit var dbHelper: NotesDatabaseHelper

    override fun onCreate(): Boolean {
        mUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        mUriMatcher.addURI(AUTHORITY, "notes", NOTES)
        mUriMatcher.addURI(AUTHORITY, "notes/#", NOTES_BY_ID)
        if (context != null) {
            dbHelper = NotesDatabaseHelper(context as Context)
        }

        return true
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // valida se é uma uri ID
        if (mUriMatcher.match(uri) == NOTES_BY_ID) {
            // faz a operação de deleção
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val linesAffect = db.delete(TABLE_NOTES, "_ID = ?", arrayOf(uri.lastPathSegment))
            db.close()

            // notifica o content provider que foi feita uma alteração na uri
            context?.contentResolver?.notifyChange(uri, null)

            // retorna as linhas afetadas
            return linesAffect
        } else {
            throw UnsupportedSchemeException("Uri inválida para exclusão")
        }
    }

    // é usado para requisições de arquivos
    override fun getType(uri: Uri): String =
        throw UnsupportedSchemeException("Uri não implementado")

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // verifica se é feita uma requisição de um valor
        if (mUriMatcher.match(uri) == NOTES) {
            // faz a operação de inserção
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val id = db.insert(TABLE_NOTES, null, values)
            val insertUri = Uri.withAppendedPath(BASE_URI, id.toString())
            db.close()

            // notifica o content provider que foi feita uma alteração na uri
            context?.contentResolver?.notifyChange(uri, null)

            // retorna as linhas afetadas
            return insertUri
        } else {
            throw UnsupportedSchemeException("Uri inválida para inserção")
        }
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return when {
            mUriMatcher.match(uri) == NOTES -> {
                // passa os dados para um cursor
                val db: SQLiteDatabase = dbHelper.writableDatabase
                val cursor = db.query(
                    TABLE_NOTES,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
                )
                //db.close()

                // cursor notifica que recebeu os dados
                cursor.setNotificationUri(context?.contentResolver, uri)
                cursor
            }
            mUriMatcher.match(uri) == NOTES_BY_ID -> {
                // passa os dados para um cursor
                val db: SQLiteDatabase = dbHelper.writableDatabase
                val cursor = db.query(
                    TABLE_NOTES,
                    projection,
                    "$_ID = ?",
                    arrayOf(uri.lastPathSegment),
                    null,
                    null,
                    sortOrder
                )
                //db.close()

                // cursor notifica que recebeu os dados
                cursor.setNotificationUri(context?.contentResolver, uri)
                cursor
            }
            else -> {
                throw UnsupportedSchemeException("Uri não implementada.")
            }
        }
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (mUriMatcher.match(uri) == NOTES_BY_ID) {
            // faz a operação de update
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val linesAffect =
                db.update(TABLE_NOTES, values, "$_ID = ?", arrayOf(uri.lastPathSegment))
            db.close()

            // notifica o content provider que foi feita uma alteração na uri
            context?.contentResolver?.notifyChange(uri, null)

            // retorna as linhas afetadas
            return linesAffect
        } else {
            throw UnsupportedSchemeException("Uri não implementada.")
        }
    }

    companion object {
        //"content://br.com.guilhermereisapps.appcontentprovider.provider/notes"
        const val AUTHORITY = "br.com.guilhermereisapps.appcontentprovider.provider"
        val BASE_URI = Uri.parse("content://$AUTHORITY")
        val URI_NOTES = Uri.withAppendedPath(BASE_URI, "notes")

        const val NOTES = 1
        const val NOTES_BY_ID = 2
    }

}