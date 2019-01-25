package promosys.com.reportpdf;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FragmentThree extends Fragment {

    private View rootView;
    private Context context;
    private MainActivity2 mainActivity;

    public ArrayList<String> lstImage;

    private Button btnAddImage,btnBack;
    public Button btnGeneratePdf;

    public ImageView imgVw01,imgVw02,imgVw03,imgVw04;
    public boolean isImg1 = false;
    public boolean isImg2 = false;
    public boolean isImg3 = false;
    public boolean isImg4 = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_three, container, false);
        context = rootView.getContext();
        mainActivity = (MainActivity2)context;

        imgVw01 = (ImageView)rootView.findViewById(R.id.imgvw_1);
        imgVw01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImg1 = true;
                mainActivity.getImageFromAlbum();
            }
        });

        imgVw02 = (ImageView)rootView.findViewById(R.id.imgvw_2);
        imgVw02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImg2 = true;
                mainActivity.getImageFromAlbum();
            }
        });
        imgVw02.setEnabled(false);

        imgVw03 = (ImageView)rootView.findViewById(R.id.imgvw_3);
        imgVw03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImg3 = true;
                mainActivity.getImageFromAlbum();
            }
        });
        imgVw03.setEnabled(false);

        imgVw04 = (ImageView)rootView.findViewById(R.id.imgvw_4);
        imgVw04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImg4 = true;
                mainActivity.getImageFromAlbum();
            }
        });
        imgVw04.setEnabled(false);

        lstImage = new ArrayList<String>();
        btnAddImage = (Button)rootView.findViewById(R.id.btn_add_image);
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lstImage.size()<4){
                    mainActivity.getImageFromAlbum();
                }else {
                    Toast.makeText(context,"Max Image is 4",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGeneratePdf = (Button)rootView.findViewById(R.id.btn_generate_report);
        btnGeneratePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///mainActivity.startGeneratePdf();
                mainActivity.saveReportDialog();
            }
        });

        btnBack = (Button)rootView.findViewById(R.id.btn_frag3_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.changeFragment(2);
            }
        });
        return rootView;
    }
}
