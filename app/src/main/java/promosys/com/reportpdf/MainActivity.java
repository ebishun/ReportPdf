package promosys.com.reportpdf;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private File pdfFile;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    public String strSiteName,strSiteId,strDate,strProblem;
    private ArrayList<String> lstTroubleShoot,lstSavedTroubleshoot;
    private ArrayList<String> lstNote,lstSavedNote;
    private ArrayList<String> lstImage;

    private TextView txtAddTshoot,txtAddNote,txtAddImage;
    private Button btnAddNote,btnAddTShoot,btnGeneratePdf,btnAddImage;
    private EditText edtAddNote,edtAddTShoot;
    private EditText edtSiteName,edtSiteId,edtSiteDate,edtSiteProblem;

    public ArrayAdapter<String> adapterTbShoot,adapterNote;

    private Spinner spnTbShoot,spnNote;

    Bitmap img01,img02,img03,img04;

    int GALLERY_REQUEST = 2;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private static String TBSHOOT_KEY = "lst_troubleshoot";
    private static String NOTE_KEY = "lst_note";

    private String strTbShootNone = "None";
    private String strNoteNone = "None";

    private boolean isGotStoragePermission = false;

    ListView lstvwNote;
    ArrayAdapter adapterLstvwNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("PromosysReport", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        lstTroubleShoot = new ArrayList<>();
        lstSavedTroubleshoot = new ArrayList<>();

        lstNote = new ArrayList<>();
        lstSavedNote = new ArrayList<>();

        lstImage = new ArrayList<>();

        restoreList(TBSHOOT_KEY,lstSavedTroubleshoot);
        restoreList(NOTE_KEY,lstSavedNote);

        adapterLstvwNote = new ArrayAdapter<String>(this, R.layout.activity_listview, lstNote);
        lstvwNote = (ListView) findViewById(R.id.lstvw_note);
        lstvwNote.setAdapter(adapterLstvwNote);

        /*
        final String strLstTbShoot = sharedPreferences.getString(TBSHOOT_KEY,"");
        if(strLstTbShoot.isEmpty()){
            lstSavedTroubleshoot.add("Add New");
            lstSavedTroubleshoot.add(0,"None");
            String dataStr = new Gson().toJson(lstSavedTroubleshoot);
            editor.putString(TBSHOOT_KEY,dataStr);
            editor.apply();
        }else{
            Type type = new TypeToken<ArrayList<String>>() { }.getType();
            List<String> restoreTbShoot = new Gson().fromJson(strLstTbShoot,type);
            for (int i = 0;i<restoreTbShoot.size();i++){
                lstSavedTroubleshoot.add(restoreTbShoot.get(i));
            }
        }

        final String strLstNote = sharedPreferences.getString(NOTE_KEY,"");
        if(strLstNote.isEmpty()){
            lstSavedNote.add("Add New");
            lstSavedNote.add(0,"None");
            String dataStr = new Gson().toJson(lstSavedNote);
            editor.putString(NOTE_KEY,dataStr);
            editor.apply();
        }else{
            Type type = new TypeToken<ArrayList<String>>() { }.getType();
            List<String> restoreNote = new Gson().fromJson(strLstNote,type);
            for (int i = 0;i<restoreNote.size();i++){
                lstSavedNote.add(restoreNote.get(i));
            }
        }
        */

        txtAddTshoot = (TextView)findViewById(R.id.txt_tshoot_add);
        txtAddNote = (TextView)findViewById(R.id.txt_note_add);
        txtAddImage = (TextView)findViewById(R.id.txt_img);

        edtAddNote = (EditText)findViewById(R.id.edt_site_note);
        edtAddNote.setVisibility(View.GONE);
        edtAddTShoot = (EditText)findViewById(R.id.edt_site_tbshoot);
        edtAddTShoot.setVisibility(View.GONE);

        edtSiteName = (EditText)findViewById(R.id.edt_site_name);
        edtSiteId = (EditText)findViewById(R.id.edt_site_id);
        edtSiteDate = (EditText)findViewById(R.id.edt_site_date);
        edtSiteProblem = (EditText)findViewById(R.id.edt_site_problem);

        btnAddTShoot = (Button)findViewById(R.id.btn_add_tbshoot);
        btnAddTShoot.setVisibility(View.GONE);
        btnAddTShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strTbShoot = edtAddTShoot.getText().toString();
                edtAddTShoot.setText("");
                if(lstTroubleShoot.isEmpty()){
                    lstTroubleShoot.add(strTbShoot);
                    String display = String.valueOf(lstTroubleShoot.size()) + ": " + strTbShoot;
                    txtAddTshoot.setText(display);
                }else{
                    lstTroubleShoot.add(strTbShoot);
                    txtAddTshoot.append("\n"+ String.valueOf(lstTroubleShoot.size()) + ": " + strTbShoot);
                }
                lstSavedTroubleshoot.add(strTbShoot);
                saveList(lstSavedTroubleshoot,TBSHOOT_KEY);

            }
        });

        spnTbShoot = (Spinner)findViewById(R.id.spn_tshoot);
        adapterTbShoot = new ArrayAdapter<String>(this, R.layout.spinner_item,lstSavedTroubleshoot);
        adapterTbShoot.setDropDownViewResource(R.layout.spinner_item);
        spnTbShoot.setAdapter(adapterTbShoot);
        spnTbShoot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(lstSavedTroubleshoot.get(position).equals("Add New")){
                    edtAddTShoot.setVisibility(View.VISIBLE);
                    btnAddTShoot.setVisibility(View.VISIBLE);
                }else if(lstSavedTroubleshoot.get(position).equals("None")){
                    strTbShootNone = "None";
                }else {
                    String strTbShoot = lstSavedTroubleshoot.get(position);
                    strTbShootNone = "Not Null";
                    if(lstTroubleShoot.isEmpty()){
                        lstTroubleShoot.add(strTbShoot);
                        String display = String.valueOf(lstTroubleShoot.size()) + ": " + strTbShoot;
                        txtAddTshoot.setText(display);
                    }else{
                        lstTroubleShoot.add(strTbShoot);
                        txtAddTshoot.append("\n"+ String.valueOf(lstTroubleShoot.size()) + ": " + strTbShoot);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnNote = (Spinner)findViewById(R.id.spn_note);
        adapterNote = new ArrayAdapter<String>(this, R.layout.spinner_item,lstSavedNote);
        adapterNote.setDropDownViewResource(R.layout.spinner_item);
        spnNote.setAdapter(adapterNote);
        spnNote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(lstSavedNote.get(position).equals("Add New")){
                    edtAddNote.setVisibility(View.VISIBLE);
                    btnAddNote.setVisibility(View.VISIBLE);
                }else if(lstSavedNote.get(position).equals("None")){
                    strNoteNone = "None";
                }else {
                    String strNote = lstSavedNote.get(position);
                    strNoteNone = "Not Null";
                    if(lstNote.isEmpty()){
                        lstNote.add(strNote);
                        String display = String.valueOf(lstNote.size()) + ": " + strNote;
                        txtAddNote.setText(display);
                    }else{
                        lstNote.add(strNote);
                        txtAddNote.append("\n"+ String.valueOf(lstNote.size()) + ": " + strNote);
                    }
                }
                adapterLstvwNote.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnAddNote = (Button)findViewById(R.id.btn_add_note);
        btnAddNote.setVisibility(View.GONE);
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strNote = edtAddNote.getText().toString();
                edtAddNote.setText("");
                if(lstNote.isEmpty()){
                    lstNote.add(strNote);
                    String note = String.valueOf(lstNote.size()) + ": "+strNote;
                    txtAddNote.setText(note);
                }else {
                    lstNote.add(strNote);
                    txtAddNote.append("\n"+ String.valueOf(lstNote.size()) + ": " + strNote);
                }

                lstSavedNote.add(strNote);
                saveList(lstSavedNote,NOTE_KEY);
            }
        });

        btnAddImage = (Button)findViewById(R.id.btn_add_image);
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lstImage.size()<4){
                    getImageFromAlbum();
                }else {
                    Toast.makeText(getApplicationContext(),"Max Image is 4",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGeneratePdf = (Button)findViewById(R.id.btn_generate_report);
        btnGeneratePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (lstImage.size()>1){
                        strSiteName = edtSiteName.getText().toString();
                        strSiteId = edtSiteId.getText().toString();
                        strDate = edtSiteDate.getText().toString();
                        strProblem = edtSiteProblem.getText().toString();

                        if(lstTroubleShoot.isEmpty()){
                            lstTroubleShoot.add("None");
                        }

                        if(lstNote.isEmpty()){
                            lstNote.add("None");
                        }

                        changeUiState(false);
                        if(isGotStoragePermission){
                            new GeneratePdf().execute("");
                        }else {
                            try {
                                createPdfWrapper();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (DocumentException e) {
                                e.printStackTrace();
                            }
                        }
                }else {
                    Toast.makeText(getApplicationContext(),"Minimum 2 images is required",Toast.LENGTH_SHORT).show();
                }
            }
        });

        try {
            createPdfWrapper();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void restoreList(String shKey,ArrayList<String> chosenList){
        final String strLst = sharedPreferences.getString(shKey,"");
        if(strLst.isEmpty()){
            chosenList.add("Add New");
            chosenList.add(0,"None");
            String dataStr = new Gson().toJson(chosenList);
            editor.putString(shKey,dataStr);
            editor.apply();
        }else{
            Type type = new TypeToken<ArrayList<String>>() { }.getType();
            List<String> restoreList = new Gson().fromJson(strLst,type);
            for (int i = 0;i<restoreList.size();i++){
                chosenList.add(restoreList.get(i));
            }
        }
    }

    private void saveList(ArrayList<String> chosenList,String strKey){
        String dataStr = new Gson().toJson(chosenList);
        editor.putString(strKey,dataStr);
        editor.apply();
    }

    private void getImageFromAlbum(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize=8;
                lstImage.add("image");
                if(lstImage.size() == 1){
                    img01 = BitmapFactory.decodeStream(imageStream,null,options);
                    txtAddImage.setText("Image 1");
                }else if(lstImage.size() == 2){
                    img02 = BitmapFactory.decodeStream(imageStream,null,options);
                    txtAddImage.append("\nImage 2");
                }else if(lstImage.size() == 3){
                    img03 = BitmapFactory.decodeStream(imageStream,null,options);
                    txtAddImage.append("\nImage 3");
                }else{
                    img04 = BitmapFactory.decodeStream(imageStream,null,options);
                    txtAddImage.append("\nImage 4");
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void createPdfWrapper() throws FileNotFoundException,DocumentException{
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("You need to allow access to Storage",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }
        else {
            isGotStoragePermission = true;
            //createPdf();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(this, "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private class GeneratePdf extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

                try {
                    createPdf();
                    //createPdfWrapper();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }

               return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(),"Done Generate",Toast.LENGTH_SHORT).show();
            changeUiState(true);
            doneGenerate();
        }

    }

    private void createPdf() throws FileNotFoundException, DocumentException {

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i("MainActivity", "Created a new directory for PDF");
        }

        pdfFile = new File(docsFolder.getAbsolutePath(),"RtuReport.pdf");
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document();
        //PdfWriter writer =  PdfWriter.getInstance(document, new FileOutputStream(docsFolder.getAbsolutePath() + "/RtuReport.pdf"));
        PdfWriter.getInstance(document, new FileOutputStream(docsFolder.getAbsolutePath() + "/RtuReport.pdf"));

        document.open();
        document.setPageSize(PageSize.A4);

        //LineSeparator lineSeparator = new LineSeparator();
        //lineSeparator.setLineColor(new BaseColor(0, 0, 0, 68));
        PdfPTable table = new PdfPTable(2);

        try {
            BaseFont urName = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont normalText = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            Font mOrderDetailsTitleFont = new Font(urName, 30.0f, Font.NORMAL, BaseColor.BLACK);

            Font mNormalFontItalic = new Font(normalText, 13.0f, Font.ITALIC, BaseColor.BLACK);
            Font titleFont = new Font(urName, 19.0f, Font.NORMAL, BaseColor.BLACK);

            Drawable d = getResources().getDrawable(R.drawable.promosys_logo2);
            BitmapDrawable bitDw = ((BitmapDrawable) d);
            Bitmap bmp = bitDw.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

            PdfPTable tableTop = new PdfPTable(3);
            tableTop.setWidths(new int[]{ 3, 12 ,3});
            Image image = Image.getInstance(stream.toByteArray());
            tableTop.addCell(new PdfPCell(image, true));

            Paragraph titlePara = new Paragraph("Site Visit Picture Detailed Report",titleFont);
            PdfPCell titleCell = new PdfPCell(new Phrase(titlePara));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            //tableTop.addCell(new Phrase("Site Visit Picture Detailed Report"));
            tableTop.addCell(titleCell);

            Paragraph siteNamePara = new Paragraph(strSiteId,titleFont);
            PdfPCell siteCell = new PdfPCell(new Phrase(siteNamePara));
            siteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableTop.addCell(siteCell);

            tableTop.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableTop.setTotalWidth(PageSize.A4.getWidth()-50);
            tableTop.setLockedWidth(true);
            tableTop.completeRow();
            document.add(tableTop);

            Paragraph gap = new Paragraph("");
            gap.setSpacingBefore(10f);
            document.add(gap);

            PdfPTable tablePic = new PdfPTable(2);
            tablePic.setTotalWidth(PageSize.A4.getWidth()-50);
            tablePic.setLockedWidth(true);

            for (int i = 0;i<lstImage.size();i++){
                if (i==0){
                    addPicToTable(img01,tablePic,R.drawable.img01);
                }else if(i==1){
                    addPicToTable(img02,tablePic,R.drawable.img02);
                }else if(i==2){
                    addPicToTable(img03,tablePic,R.drawable.img03);
                }else {
                    addPicToTable(img04,tablePic,R.drawable.img04);
                }
            }

            document.add(tablePic);
            Paragraph gap1 = new Paragraph("");
            gap1.setSpacingBefore(20f);
            document.add(gap1);

            addNewRow(table,"Site Name",strSiteName);
            addNewRow(table,"Site ID",strSiteId);
            addNewRow(table,"Date",strDate);
            addNewRow(table,"Problem",strProblem);

            for (int i = 0; i<lstTroubleShoot.size();i++){
                if(i==0){
                    addNewRow(table,"Troubleshoot",lstTroubleShoot.get(i));
                }else {
                    addNewRow(table,"",lstTroubleShoot.get(i));
                }

            }

            for (int i = 0; i<lstNote.size();i++){
                if(i==0){
                    addNewRow(table,"Note",lstNote.get(i));
                }else {
                    addNewRow(table,"",lstNote.get(i));
                }
            }

            table.completeRow();

            table.setWidths(new int[]{ 3, 12 });
            table.setTotalWidth(PageSize.A4.getWidth()-50);
            table.setLockedWidth(true);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);

            document.add(table);
            Paragraph bottomPara = new Paragraph("This is a computer generated report. No signature is required.",mNormalFontItalic);
            Paragraph gap2 = new Paragraph("");
            gap2.setSpacingBefore(20f);
            document.add(gap2);
            bottomPara.setAlignment(Element.ALIGN_CENTER);
            document.add(bottomPara);

        } catch (IOException e) {
            e.printStackTrace();
        }

        document.close();
    }

    public void addPicToTable(Bitmap addImage, PdfPTable table, int picResource){
        //Bitmap bitmap1 = decodeSampledBitmapFromResource(getResources(), picResource, 500, 300);
        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        addImage.compress(Bitmap.CompressFormat.PNG, 100, stream1);

        //bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream1);
        Image image1 = null;
        try {
            image1 = Image.getInstance(stream1.toByteArray());
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PdfPCell cellImage = new PdfPCell(image1);
        cellImage.setFixedHeight(200f);
        cellImage.setPadding(10f);
        cellImage.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cellImage);
        //table.completeRow();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public void addNewRow(PdfPTable table,String strTitle,String strBody){
        BaseFont urName = null;
        BaseFont normalText = null;
        try {
            urName = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            normalText = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Font mNormalFont = new Font(normalText, 13.0f, Font.NORMAL, BaseColor.BLACK);
        Font mNormalFontBold = new Font(urName, 13.0f, Font.NORMAL, BaseColor.BLACK);

        PdfPCell cellTitle = new PdfPCell(new Phrase(strTitle,mNormalFontBold));
        cellTitle.setPadding(3);

        /*
        if(strTitle.equals("Troubleshoot")){
            cellTitle.setRowspan(lstTroubleShoot.size());
            //cellTitle.setRowspan(7);
        }else if(strTitle.equals("Note")){
            cellTitle.setRowspan(5);
        }
        */

        table.addCell(cellTitle);

        PdfPCell cellBody = new PdfPCell(new Phrase(strBody,mNormalFont));
        cellBody.setPadding(3);
        table.addCell(cellBody);
    }

    private void changeUiState(boolean isEnabled){
        edtSiteProblem.setEnabled(isEnabled);
        edtSiteDate.setEnabled(isEnabled);
        edtSiteId.setEnabled(isEnabled);
        edtAddTShoot.setEnabled(isEnabled);
        edtAddNote.setEnabled(isEnabled);
        edtSiteName.setEnabled(isEnabled);
        btnAddImage.setEnabled(isEnabled);

        btnAddTShoot.setEnabled(isEnabled);
        btnAddNote.setEnabled(isEnabled);
        btnGeneratePdf.setEnabled(isEnabled);

    }

    private void doneGenerate(){
        lstNote.clear();
        lstTroubleShoot.clear();
        lstImage.clear();

        edtSiteName.setText("");
        edtSiteProblem.setText("");
        edtSiteDate.setText("");
        edtSiteId.setText("");
        edtAddTShoot.setText("");
        edtAddNote.setText("");

        txtAddImage.setText("");
        txtAddNote.setText("");
        txtAddTshoot.setText("");
    }


}
