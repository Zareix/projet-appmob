package fr.iut.appmobprojet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.iut.appmobprojet.data.model.Product;

public class AllProductsRecyclerViewAdapter extends RecyclerView.Adapter<AllProductsRecyclerViewAdapter.ViewHolder> {

    private final List<Product> mValues;

    public AllProductsRecyclerViewAdapter(List<Product> items) {
        mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mTitleView.setText(mValues.get(position).getTitre());
        holder.mMarqueView.setText(mValues.get(position).getMarque());
        holder.mCategorieView.setText(mValues.get(position).getCategorie());
        holder.mAddedDateView.setText(mValues.get(position).getDateAjout());
        holder.mPermeateDateView.setText(mValues.get(position).getPeremption());
        holder.mDonneurView.setText(mValues.get(position).getDonneur());
        holder.mCodePostalView.setText(mValues.get(position).getCodePostal());
        holder.mReserverButton.setOnClickListener(v -> reserver(mValues.get(position), v));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void reserver(Product p, View v) {
        FirebaseFirestore fDb = FirebaseFirestore.getInstance();
        fDb.collection("products").document(p.getId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.getString("reservePar") == null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("reservePar", v.getContext().getSharedPreferences("user", Context.MODE_PRIVATE).getString("username", null));
                    fDb.collection("products").document(p.getId()).update(data).addOnSuccessListener(aVoid -> {
                        //Toast.makeText(v.getContext(), "Produit réservé !", Toast.LENGTH_SHORT).show();
                        Snackbar.make(v.getRootView().findViewById(R.id.home), R.string.reservation_complete, Snackbar.LENGTH_LONG).setAction(R.string.undo, v1 -> annulerReservation(p, v)).show();
                        notifyDonneur(p, v);
                    });
                } else {
                    Toast.makeText(v.getContext(), "Ce produit est déjà réservé", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void notifyDonneur(Product p, View v) {
        Map<String, Object> data = new HashMap<>();
        data.put("reserverPar", v.getContext().getSharedPreferences("user", Context.MODE_PRIVATE).getString("username", null));
        data.put("produitReserve", p.getId());
        FirebaseFirestore.getInstance().collection("users").document(p.getDonneur()).collection("notifications").document(p.getDonneur() + p.getId()).set(data);
    }

    public void annulerReservation(Product p, View v) {
        Map<String, Object> data = new HashMap<>();
        data.put("reservePar", null);
        FirebaseFirestore.getInstance().collection("products").document(p.getId()).update(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(v.getContext(), "Reservation annulée", Toast.LENGTH_SHORT).show();
                annulerNotif(p);
            }
        });
    }

    public void annulerNotif(Product p) {
        FirebaseFirestore.getInstance().collection("users").document(p.getDonneur()).collection("notifications").document(p.getDonneur() + p.getId()).delete();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mMarqueView;
        public final TextView mCategorieView;
        public final TextView mAddedDateView;
        public final TextView mPermeateDateView;
        public final TextView mDonneurView;
        public final TextView mCodePostalView;
        public final Button mReserverButton;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.titre_product_item);
            mMarqueView = view.findViewById(R.id.marque_product_item);
            mCategorieView = view.findViewById(R.id.categorie_list_item);
            mAddedDateView = view.findViewById(R.id.added_date_item_product);
            mPermeateDateView = view.findViewById(R.id.permeate_date_item_product);
            mDonneurView = view.findViewById(R.id.donneur_item_product);
            mCodePostalView = view.findViewById(R.id.code_postal_item_product);
            mReserverButton = view.findViewById(R.id.reserver_btn_item_product);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString();
        }
    }
}