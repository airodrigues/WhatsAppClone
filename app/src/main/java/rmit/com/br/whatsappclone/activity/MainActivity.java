package rmit.com.br.whatsappclone.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import rmit.com.br.whatsappclone.Adapter.TabAdapter;
import rmit.com.br.whatsappclone.R;
import rmit.com.br.whatsappclone.activity.config.ConfiguracaoFirebase;
import rmit.com.br.whatsappclone.activity.model.Contato;
import rmit.com.br.whatsappclone.activity.model.Usuario;
import rmit.com.br.whatsappclone.helper.Base64Custom;
import rmit.com.br.whatsappclone.helper.Preferencias;
import rmit.com.br.whatsappclone.helper.SlidingTabLayout;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private android.support.v7.widget.Toolbar toolbar;
    private FirebaseAuth usuarioAutenticacao;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private String identificadorContato;
    private DatabaseReference firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usuarioAutenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("WhatsApp");
        setSupportActionBar(toolbar);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.stl_tabs);
        viewPager = (ViewPager) findViewById(R.id.vp_pagina);

        //configurar sliding tabs
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(this,R.color.colorAccent));

        //configurar adapter
        TabAdapter tabAdapter = new  TabAdapter(getSupportFragmentManager());

        viewPager.setAdapter(tabAdapter);

        slidingTabLayout.setViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.item_sair:
                deslogarUsuario();
                return true;
            case R.id.item_configuracoes:
                return true;
            case R.id.item_adicionar:
                abrirCadastroContato();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void abrirCadastroContato(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        //configurações do dialog
        alertDialog.setTitle("Novo contato");
        alertDialog.setMessage("E-mail do usuário");
        alertDialog.setCancelable(false);

        final EditText editText = new EditText(MainActivity.this);
        alertDialog.setView(editText);

        //configura botões
        alertDialog.setPositiveButton("Cadastrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String emailContato = editText.getText().toString();

                //valida se o e-mail foi digitado
                if(emailContato.isEmpty()){
                    Toast.makeText(MainActivity.this, "Preencha o e-mail", Toast.LENGTH_LONG).show();
                }else{
                    //verificar se o usuário já está cadastrado no nosso app
                    identificadorContato = Base64Custom.codificarBase64(emailContato);

                    //recuperar instância firebase
                    firebase = ConfiguracaoFirebase.getFirebase().child("usuarios").child(identificadorContato);

                    firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.getValue() != null){

                                //recuperar dados do contato a ser adicionado
                                Usuario usuarioContato = dataSnapshot.getValue(Usuario.class);

                                //Recuperar identificador usuario logado (base64)
                                Preferencias preferencias = new Preferencias(MainActivity.this);
                                String identificadorUsuarioLogado = preferencias.getIdentificador();

                                firebase = ConfiguracaoFirebase.getFirebase();
                                firebase = firebase.child("contatos")
                                        .child(identificadorUsuarioLogado)
                                        .child(identificadorContato);

                                Contato contato = new Contato();
                                contato.setIdentificadorUsuario(identificadorContato);
                                contato.setEmail(usuarioContato.getEmail());
                                contato.setNome(usuarioContato.getNome());

                                firebase.setValue(contato);

                            }else{
                                Toast.makeText(MainActivity.this, "Usuário não possui cadastro.", Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        alertDialog.create();
        alertDialog.show();

    }

    private void deslogarUsuario(){

        usuarioAutenticacao.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

    }
}
