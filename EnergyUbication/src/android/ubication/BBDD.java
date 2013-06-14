package android.ubication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class BBDD extends SQLiteOpenHelper {
	
	//Sentencia SQL para crear la tabla de Usuario
    String sqlCreate = "CREATE TABLE Usuario (user TEXT, id TEXT)"; 
    
 
    public BBDD(Context contexto, String nombre,CursorFactory factory, int version) {
        super(contexto, nombre, factory, version);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta la sentencia SQL de creación de la tabla
        db.execSQL(sqlCreate);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
 
        //Se elimina la versión anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS Usuario");
 
        //Se crea la nueva versión de la tabla
        db.execSQL(sqlCreate);
    }
}