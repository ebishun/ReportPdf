package promosys.com.reportpdf;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;

public class FragmentOne extends Fragment {

    private View rootView;
    private Context context;
    private MainActivity2 mainActivity;

    private Button btnNext;

    public EditText edtSiteID,edtSiteName,edtSiteDate,edtSiteProblem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_one, container, false);
        context = rootView.getContext();
        mainActivity = (MainActivity2)context;

        edtSiteID = (EditText)rootView.findViewById(R.id.edt_site_id);
        edtSiteDate = (EditText)rootView.findViewById(R.id.edt_site_date);
        edtSiteName = (EditText)rootView.findViewById(R.id.edt_site_name);
        edtSiteProblem = (EditText)rootView.findViewById(R.id.edt_site_problem);

        btnNext = (Button)rootView.findViewById(R.id.btn_frag_one_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.strSiteId = edtSiteID.getText().toString();
                mainActivity.strSiteDate = edtSiteDate.getText().toString();
                mainActivity.strSiteName = edtSiteName.getText().toString();
                mainActivity.strSiteProblem = edtSiteProblem.getText().toString();

                mainActivity.changeFragment(2);

            }
        });
        //mainActivity.volleyGetNotification();
        return rootView;
    }
}
