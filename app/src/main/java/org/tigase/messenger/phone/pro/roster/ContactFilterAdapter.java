package org.tigase.messenger.phone.pro.roster;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.tigase.messenger.phone.pro.R;

import java.util.List;

public class ContactFilterAdapter extends ArrayAdapter<MyContact> {
    private List<MyContact> mycontacts;
    private LayoutInflater inflater;
    public ContactFilterAdapter(Context context, List<MyContact> mycontacts) {
        super(context, R.layout.fragement_contact_item, mycontacts);
        this.mycontacts = mycontacts;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mycontacts.size();
    }
    public static class ViewHolder{
        TextView name_contact;
        TextView numero_contact;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;
        if (view == null){
            view = inflater.inflate(R.layout.fragement_contact_item, null); //Recupere la view : l 'image le nom et le tel
            viewHolder = new ViewHolder();
            viewHolder.name_contact = (TextView) view.findViewById(R.id.name_contact);
            viewHolder.numero_contact = (TextView) view.findViewById(R.id.numero_contact);

            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag(); // recyclage
        }
        MyContact mycontact = mycontacts.get(position);
        viewHolder.name_contact.setText(mycontact.getName());
        viewHolder.numero_contact.setText(mycontact.getNumero());
        return view;
    }
}
