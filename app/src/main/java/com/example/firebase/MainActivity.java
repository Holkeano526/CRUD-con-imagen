package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private List<platillo> listPlatillo = new ArrayList<platillo>();
    private String url;
    private Button btnChoose;
    private ImageView imageView;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    ArrayAdapter<platillo> arrayAdapterPlatillos;

    EditText nombre,precio,detalles;
    ListView listaPlatillo;

    //realtime database
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //storage
    FirebaseStorage storage;
    StorageReference storageReference;

    //capturar platillo seleccionado
    platillo platilloSelected;

    Button btn;
    Button btnUrl;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nombre = (EditText)findViewById(R.id.txtNombre);
        precio = (EditText)findViewById(R.id.txtPrecio);
        detalles = (EditText)findViewById(R.id.txtDetalles);
        btnChoose = (Button) findViewById(R.id.btnChoose);
        imageView = (ImageView) findViewById(R.id.imgView);
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btn = (Button) findViewById(R.id.btnSalir2);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        btnUrl = (Button)findViewById (R.id.btnFirebase2);
        url="https://firebase.google.com/?hl=es";
        btnUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri  = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Quieres salir de la app?")
                        .setTitle("Alerta!!!");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "Saliendo de la app...",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this,":D",Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNeutralButton("Desarrollado en:", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this,"Esta app ha sido realizada con el IDE: Android Studio con BD No SQL Firebase by Google usando el lenguaje Java",Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog dialog =  builder.create();
                dialog.show();
            }
        });

        listaPlatillo = (ListView)findViewById(R.id.lv_datosPlatillos);

        listaPlatillo.setOnItemClickListener(new AdapterView.OnItemClickListener() { //para seleccionar desde nuestra lista
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                platilloSelected = (platillo) parent.getItemAtPosition(position);
                nombre.setText(platilloSelected.getNombre());
                precio.setText(platilloSelected.getPrecio());
                detalles.setText(platilloSelected.getDetalle());
            }
        });

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //abrir conexion a firebase
        //listar los datos en el listview
        inicializarFirebase();
        listarDatos();
    }

    private void listarDatos() {
        databaseReference.child("platillo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listPlatillo.clear();
                for(DataSnapshot objSnapShot : dataSnapshot.getChildren()){
                    platillo p = objSnapShot.getValue(platillo.class);
                    listPlatillo.add(p);

                    arrayAdapterPlatillos = new ArrayAdapter<platillo>(MainActivity.this, android.R.layout.simple_list_item_1,listPlatillo);//android.R.layour sirve para una plantilla de lista y lo rellenamos con nuestro list persona
                    listaPlatillo.setAdapter(arrayAdapterPlatillos); //mandamos el array a nuestra lista
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void inicializarFirebase()
    {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = firebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    @Override //creamos un menu y le importamos el menu que creamos en la carpeta res
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override //cuando hace clip en el menu
    public boolean onOptionsItemSelected(MenuItem item){

        String nom = nombre.getText().toString();
        String pre = precio.getText().toString();
        String det = detalles.getText().toString();

        switch (item.getItemId())
        {
            case R.id.icon_add:
            {
                if(nom.equals("")||pre.equals("")||det.equals("")||imageView.getDrawable() == null)
                {
                    validacion();
                    break;
                }
                else
                {
                    platillo p = new platillo();
                    p.setId(UUID.randomUUID().toString());
                    p.setNombre(nom);
                    p.setPrecio(pre);
                    p.setDetalle(det);
                    databaseReference.child("platillo").child(p.getId()).setValue(p);

                    Toast.makeText(this, "Agregado", Toast.LENGTH_LONG).show();
                    limpiarCajas();
                    uploadImage();
                    imageView.setImageResource(0);
                    break;
                }
            }
            case R.id.icon_save:{
                platillo p = new platillo();
                p.setId(platilloSelected.getId());
                p.setNombre(nombre.getText().toString().trim());
                p.setPrecio(precio.getText().toString().trim());
                p.setDetalle(detalles.getText().toString().trim());
                //acceder a la bd y reemplazamos el valor por los nuevos
                databaseReference.child("platillo").child(p.getId()).setValue(p);

                Toast.makeText(this, "Actualizado", Toast.LENGTH_LONG).show();
                limpiarCajas();
                break;
            }
            case R.id.icon_delete:{
                platillo p = new platillo();
                p.setId(platilloSelected.getId());
                databaseReference.child("platillo").child(p.getId()).removeValue();

                Toast.makeText(this, "Eliminado", Toast.LENGTH_LONG).show();
                limpiarCajas();
                break;
            }
            default:break;
        }
        return true;
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }
    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("platillos/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    public void validacion(){
        String nom=nombre.getText().toString();
        String pre=precio.getText().toString();
        String det=detalles.getText().toString();

        if(nom.equals("")){
            nombre.setError("Agregue un nombre");
        }
        else if(pre.equals("")){
            precio.setError("Agregue un precio");
        }
        else if(det.equals("")){
            detalles.setError("Agregue detalles");
        }
        else if(imageView.getDrawable() == null)
        {
            Toast.makeText(MainActivity.this, "Necesita agregar una imagen",Toast.LENGTH_SHORT).show();
        }

    }
    public void limpiarCajas(){
        nombre.setText("");
        precio.setText("");
        detalles.setText("");
    }
    public void cerrarsesion(View view)
    {
        Intent back = new Intent(this, login.class);
        startActivity(back);
    }
    public static class platillo {
        public String id;
        public String nombre;
        public String precio;
        public String detalle;

        public platillo() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getPrecio() { return precio; }

        public void setPrecio(String precio) { this.precio = precio; }

        public String getDetalle() {
            return detalle;
        }

        public void setDetalle(String detalle) {
            this.detalle = detalle;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }
}