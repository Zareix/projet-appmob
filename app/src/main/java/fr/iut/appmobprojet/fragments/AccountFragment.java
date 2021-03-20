package fr.iut.appmobprojet.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.iut.appmobprojet.LoginActivity;
import fr.iut.appmobprojet.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String username;
    private String email;
    private String statut;
    private List<String> allNotif = new ArrayList<>();

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        FirebaseFirestore fDb = FirebaseFirestore.getInstance();

        DocumentReference docRef = fDb.collection("users").document(getContext().getSharedPreferences("user", Context.MODE_PRIVATE).getString("username", null));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    username = (String) document.get("username");
                    email = (String) document.get("email");
                    statut = (String) document.get("statut");
                    Fragment currentFragment = getFragmentManager().findFragmentByTag("currentFragment");
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.detach(currentFragment);
                    fragmentTransaction.attach(currentFragment);
                    fragmentTransaction.commit();
                }
            }
        });

        DocumentReference notifRef = fDb.collection("users").document(getContext().getSharedPreferences("user", Context.MODE_PRIVATE).getString("username", null)).collection("notifications").document("r6WPqAT1MOPZ9yWEo1Yz");
        notifRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    allNotif.add(document.getString("reservePar"));
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView usernameV = view.findViewById(R.id.username_profil);
        TextView emailV = view.findViewById(R.id.email_profil);
        TextView statutV = view.findViewById(R.id.statut_profil);

        EditText mEmail = view.findViewById(R.id.change_email);
        EditText mPhoto = view.findViewById(R.id.change_picture);
        EditText mJustificatif = view.findViewById(R.id.justificatif_statut);
        Button buttonValidate = view.findViewById(R.id.validate_btn);

        view.findViewById(R.id.disconnect_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getContext(), "Déconnecté", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });

        usernameV.setText(username);
        emailV.setText(email);
        statutV.setText(statut);

        /*LinearLayout notifications = view.findViewById(R.id.notification_layout_profile);

        for (String n : allNotif) {
            TextView newNotif = new TextView(getContext());
            newNotif.setText(n);
            notifications.addView(newNotif);
        }*/

        buttonValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Map<String, Object> data = new HashMap<>();

                if (!mEmail.getText().toString().equals("")) {
                    data.put("email", mEmail.getText().toString());
                    user.updateEmail((String) data.get("email"))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // complete
                                }
                            });
                    emailV.setText((String) data.get("email"));
                }

                if (!mPhoto.getText().toString().equals(""))
                    data.put("imageUrl", "to be defined");

                if (!mJustificatif.getText().toString().equals(""))
                    data.put("justificatifStatut", mJustificatif.getText().toString());

                FirebaseFirestore.getInstance().collection("users").document(getContext().getSharedPreferences("user", Context.MODE_PRIVATE).getString("username", null)).update(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Informations modifiées", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}