package saru.com.app.connectors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import saru.com.app.R;
import saru.com.app.models.Address;

public class AddressAdapter extends ArrayAdapter<Address> {
    private Context context;
    private List<Address> addressList;

    public AddressAdapter(Context context, List<Address> addressList) {
        super(context, R.layout.address_item, addressList);
        this.context = context;
        this.addressList = addressList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.address_item, null);
        }

        Address address = addressList.get(position);

        TextView txtAddressName = convertView.findViewById(R.id.txtAddressName);
        TextView txtAddressPhone = convertView.findViewById(R.id.txtAddressPhone);
        TextView txtAddressDetail = convertView.findViewById(R.id.txtAddressDetail);

        txtAddressName.setText(address.getName());
        txtAddressPhone.setText(address.getPhone());
        txtAddressDetail.setText(address.getAddress());

        return convertView;
    }

}
