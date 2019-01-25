package promosys.com.reportpdf;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

public class FragmentTwo extends Fragment {

    private View rootView;
    private Context context;
    private MainActivity2 mainActivity;

    public ArrayList<String> lstTroubleShoot,lstSavedTroubleshoot;
    public ArrayList<String> lstNote,lstSavedNote;

    public ArrayAdapter<String> adapterTbShoot,adapterNote;
    public Spinner spnTbShoot,spnNote;

    private Button btnNext,btnBack;

    ListView lstvwNote,lstvwTbShoot;
    ArrayAdapter adapterLstvwNote,adapterLstvwTbShoot;

    private String strTbShootNone = "None";
    private String strNoteNone = "None";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_two, container, false);
        context = rootView.getContext();
        mainActivity = (MainActivity2)context;

        lstTroubleShoot = new ArrayList<>();
        lstSavedTroubleshoot = new ArrayList<>();

        lstNote = new ArrayList<>();
        lstSavedNote = new ArrayList<>();

        mainActivity.restoreList(mainActivity.TBSHOOT_KEY,lstSavedTroubleshoot);
        mainActivity.restoreList(mainActivity.NOTE_KEY,lstSavedNote);

        lstvwNote = (ListView)rootView.findViewById(R.id.lstvw_note);
        lstvwTbShoot = (ListView)rootView.findViewById(R.id.lstvw_tbshoot);

        adapterLstvwNote = new ArrayAdapter<String>(context, R.layout.activity_listview, lstNote);
        lstvwNote.setAdapter(adapterLstvwNote);
        lstvwNote.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mainActivity.deleteDataDialog("Note",position);

                lstNote.remove(position);
                adapterLstvwNote.notifyDataSetChanged();
                return false;
            }
        });

        adapterLstvwTbShoot = new ArrayAdapter<String>(context, R.layout.activity_listview, lstTroubleShoot);
        lstvwTbShoot.setAdapter(adapterLstvwTbShoot);
        lstvwTbShoot.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                lstTroubleShoot.remove(position);
                adapterLstvwTbShoot.notifyDataSetChanged();
                return false;
            }
        });

        spnTbShoot = (Spinner)rootView.findViewById(R.id.spn_tshoot);
        adapterTbShoot = new ArrayAdapter<String>(context, R.layout.spinner_item,lstSavedTroubleshoot);
        adapterTbShoot.setDropDownViewResource(R.layout.spinner_item);
        spnTbShoot.setAdapter(adapterTbShoot);
        spnTbShoot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(lstSavedTroubleshoot.get(position).equals("Add New")){
                    mainActivity.addNewMessage("Troubleshoot");
                }else if(lstSavedTroubleshoot.get(position).equals("None")){
                    strTbShootNone = "None";
                }else {
                    String strTbShoot = lstSavedTroubleshoot.get(position);
                    strTbShootNone = "Not Null";
                    lstvwTbShoot.setVisibility(View.VISIBLE);
                    lstTroubleShoot.add(0,strTbShoot);
                }
                adapterLstvwTbShoot.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnNote = (Spinner)rootView.findViewById(R.id.spn_note);
        adapterNote = new ArrayAdapter<String>(context, R.layout.spinner_item,lstSavedNote);
        adapterNote.setDropDownViewResource(R.layout.spinner_item);
        spnNote.setAdapter(adapterNote);
        spnNote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(lstSavedNote.get(position).equals("Add New")){
                    mainActivity.addNewMessage("Note");
                }else if(lstSavedNote.get(position).equals("None")){
                    strNoteNone = "None";
                }else {
                    String strNote = lstSavedNote.get(position);
                    strNoteNone = "Not Null";
                    lstvwNote.setVisibility(View.VISIBLE);
                    lstNote.add(0,strNote);
                }
                adapterLstvwNote.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnNext = (Button)rootView.findViewById(R.id.btn_frag_two_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.changeFragment(3);
            }
        });

        btnBack = (Button)rootView.findViewById(R.id.btn_frag2_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.changeFragment(1);
            }
        });
        return rootView;
    }
}