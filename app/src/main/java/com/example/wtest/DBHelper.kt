package com.example.wtest

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.wtest.model.CodigoPostalModel

const val DB_NAME = "WTestDB"
const val TABLE_NAME = "Cod_Postais"


class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {

    // Neste método é feita a criação da base de dados com os 3 atributos necessários
    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE IF NOT EXISTS $TABLE_NAME(num_cod_postal TEXT, ext_cod_postal TEXT, desig_postal TEXT)"
        db?.execSQL(query)
    }

    // Método criado e usado em função de testes
    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        val query = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(query)
        onCreate(db)
    }


    // Este método é chamado sempre que é inserido um caracter na search bar, alterando assim a query do Select, apresentado os resultados confome a pesquisa.
    // Ficou a faltar a pesquisa utilizando acentos, caso se pesquise, por exemplo, "Sao" não funciona apenas irá funcionar se pesquisar por "São".
    fun getResult(value: String): ArrayList<CodigoPostalModel> {

        var strWhere = ""
        // Replace do - por espaço e separa os valores para dentro de uma lista, retirando os espaços
        val splitValue = value.replace("-"," ").split(" ").filter { it.isNotBlank() }

        // Neste ciclo é feita a construção da string Where para todos os valores numéricos
        splitValue.forEach { str ->
            if (isNumber(str)) {
                // Caso a string Where esteja vazia, então sabemos que não é necessário adicionar o AND no inicio
                if (strWhere == "") {
                    // Se o tamanho do item do splitValue nesta iteração tiver apenas 3 de tamanho então pesquisa no campo ext_cod_postal
                    // Senão, se tiver 4 vai pesquisar no campo num_cod_postal
                    if (str.length == 3) {
                        strWhere += "ext_cod_postal like '%${str}%' "
                    } else if (str.length == 4){
                        strWhere += "num_cod_postal like '%${str}%' "
                    }
                } else {
                    if (str.length == 3) {
                        strWhere += "AND ext_cod_postal like '%${str}%' "
                    } else if (str.length == 4){
                        strWhere += "AND num_cod_postal like '%${str}%' "
                    }
                }

            }
        }

        // Neste ciclo é feita a construção da string Where para todos os textos (strings)
        splitValue.forEach { str ->
            if (!isNumber(str)) {
                if (strWhere == "") {
                    strWhere += "desig_postal like '%${str}%' "
                } else {
                    strWhere += "AND desig_postal like '%${str}%' "
                }
            }
        }

        val results: ArrayList<CodigoPostalModel> = ArrayList()
        // Caso a string do Where esteja vazia apresenta todos os resultados
        val selectQuery = if(strWhere == "")
            "SELECT * FROM $TABLE_NAME"
        else
            "SELECT * FROM $TABLE_NAME WHERE $strWhere"

        val database = this.writableDatabase
        val cursor: Cursor = database.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val codPostalValue = CodigoPostalModel(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
                results.add(codPostalValue)
            } while (cursor.moveToNext())
        }
        return results
    }

    // Método que retorna todos os dados presentes na tabela Cod_Postais
    fun getResults(): ArrayList<CodigoPostalModel> {

        val results: ArrayList<CodigoPostalModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val database = this.writableDatabase
        val cursor: Cursor = database.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val codPostalValue = CodigoPostalModel(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
                results.add(codPostalValue)
            } while (cursor.moveToNext())
        }
        return results
    }

    private fun isNumber(s: String): Boolean {
        return when(s.toIntOrNull()) {
            null -> false
            else -> true
        }
    }

}
