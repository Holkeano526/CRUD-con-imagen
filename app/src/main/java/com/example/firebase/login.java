package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

public class login extends AppCompatActivity {
    private EditText usuario, password;
    private Button login;
    String usu = "admin";
    String pass = "123890";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usuario = (EditText) findViewById(R.id.txtUsuario);
        password = (EditText) findViewById(R.id.txtPassword);
        login = (Button) findViewById(R.id.btnLogear);
    }
    public void login(View view)
    {
        String cuenta = String.valueOf(usuario.getText());
        String contra = String.valueOf(password.getText());

        if(cuenta.equals(usu)&&contra.equals(pass))
        {
            Toast.makeText(this, "Logeado exitosamente", Toast.LENGTH_LONG).show();
            mainActivity();
        }
        else
        {
            Toast.makeText(this, "ERROR EN LAS CREDENCIALES", Toast.LENGTH_LONG).show();
            usuario.setError("Incorrecto");
            password.setError("Incorrecto");
            limpiarcajas();
        }
    }
    public void limpiarcajas()
    {
        usuario.setText("");
        password.setText("");
    }
    public void mainActivity()
    {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
    }
}