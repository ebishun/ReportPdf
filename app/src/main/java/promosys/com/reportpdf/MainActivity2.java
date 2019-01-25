package promosys.com.reportpdf;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    FragmentTransaction transaction;
    FragmentOne fragOne;
    FragmentTwo fragTwo;
    FragmentThree fragThree;


    Bitmap img01,img02,img03,img04;
    int GALLERY_REQUEST = 2;

    public boolean isFragOne = false;
    public boolean isFragTwo = false;
    public boolean isFragThree = false;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public static String TBSHOOT_KEY = "lst_troubleshoot";
    public static String NOTE_KEY = "lst_note";

    public String strSiteId = "";
    public String strSiteName = "";
    public String strSiteDate = "";
    public String strSiteProblem = "";

    private boolean isGotStoragePermission = false;

    private Toolbar toolbar;

    private File pdfFile;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    public String strReportName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        sharedPreferences = getSharedPreferences("PromosysReport", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.setTitle("Report Generator");

        fragOne = new FragmentOne();
        fragTwo = new FragmentTwo();
        fragThree = new FragmentThree();

        transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.content, fragOne, "1");
        transaction.add(R.id.content, fragTwo, "2");
        transaction.add(R.id.content, fragThree, "2");
        transaction.hide(fragTwo);
        transaction.hide(fragThree);
        transaction.commit();

        isFragOne = true;

        try {
            createPdfWrapper();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if(isFragTwo){
            changeFragment(1);
        }else if(isFragThree){
            changeFragment(2);
        }
        //
    }

    public void saveList(ArrayList<String> chosenList, String strKey){
        String dataStr = new Gson().toJson(chosenList);
        editor.putString(strKey,dataStr);
        editor.apply();
    }

    public void restoreList(String shKey,ArrayList<String> chosenList){
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

    public void startGeneratePdf(){
        if (fragThree.lstImage.size()>1){

            if(fragTwo.lstTroubleShoot.isEmpty()){
                fragTwo.lstTroubleShoot.add("None");
            }

            if(fragTwo.lstNote.isEmpty()){
                fragTwo.lstNote.add("None");
            }

            //changeUiState(false);
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
            //changeUiState(true);
            doneGenerate();
        }

    }

    private void createPdf() throws FileNotFoundException, DocumentException {

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Promosys Report");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i("MainActivity", "Created a new directory for PDF");
        }

        //pdfFile = new File(docsFolder.getAbsolutePath(),"RtuReport.pdf");
        pdfFile = new File(docsFolder.getAbsolutePath(),strReportName);
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document();
        //PdfWriter.getInstance(document, new FileOutputStream(docsFolder.getAbsolutePath() + "/RtuReport.pdf"));
        PdfWriter.getInstance(document, new FileOutputStream(docsFolder.getAbsolutePath() + "/" + strReportName));

        document.open();
        document.setPageSize(PageSize.A4);

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

            for (int i = 0;i<fragThree.lstImage.size();i++){
                if (i==0){
                    addPicToTable(img01,tablePic,i);
                    Log.i("MainActivity","Add Pic One");
                }else if(i==1){
                    addPicToTable(img02,tablePic,i);
                    Log.i("MainActivity","Add Pic Two");
                }else if(i==2){
                    addPicToTable(img03,tablePic,i);
                    Log.i("MainActivity","Add Pic Three");
                }else {
                    addPicToTable(img04,tablePic,i);
                    Log.i("MainActivity","Add Pic Four");
                }
            }

            document.add(tablePic);
            Paragraph gap1 = new Paragraph("");
            gap1.setSpacingBefore(20f);
            document.add(gap1);

            addNewRow(table,"Site Name",strSiteName);
            addNewRow(table,"Site ID",strSiteId);
            addNewRow(table,"Date",strSiteDate);
            addNewRow(table,"Problem",strSiteProblem);

            StringBuffer strBuff_tbshoot = new StringBuffer();

            for (int i = 0; i<fragTwo.lstTroubleShoot.size();i++){
                String addString = (i+1) + ". " + fragTwo.lstTroubleShoot.get(i) + "\n";
                strBuff_tbshoot.append(addString);
            }
            addNewRow(table,"Troubleshoot",strBuff_tbshoot.toString());

            StringBuffer strBuff_note = new StringBuffer();
            for (int i = 0; i<fragTwo.lstNote.size();i++){
                String addString = (i+1) + ". " + fragTwo.lstNote.get(i) + "\n";
                strBuff_note.append(addString);
            }
            addNewRow(table,"Note",strBuff_note.toString());

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

        table.addCell(cellTitle);

        PdfPCell cellBody = new PdfPCell(new Phrase(strBody,mNormalFont));
        cellBody.setPadding(3);
        table.addCell(cellBody);
    }

    public void addPicToTable(Bitmap addImage, PdfPTable table, int idxImage){
        //Bitmap bitmap1 = decodeSampledBitmapFromResource(getResources(), picResource, 500, 300);
        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        addImage.compress(Bitmap.CompressFormat.PNG, 100, stream1);

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
        if(fragThree.lstImage.size() == 3){
            if(idxImage == 2){
                cellImage.setColspan(2);
            }
        }
        table.addCell(cellImage);
        //table.completeRow();
    }

    private void doneGenerate(){
        fragTwo.lstNote.clear();
        fragTwo.lstTroubleShoot.clear();
        fragThree.lstImage.clear();

        fragThree.imgVw01.setImageResource(R.drawable.ic_add_a_photo_100dp);
        fragThree.imgVw02.setImageResource(R.drawable.ic_add_a_photo_100dp);
        fragThree.imgVw03.setImageResource(R.drawable.ic_add_a_photo_100dp);
        fragThree.imgVw04.setImageResource(R.drawable.ic_add_a_photo_100dp);

        fragThree.imgVw02.setEnabled(false);
        fragThree.imgVw03.setEnabled(false);
        fragThree.imgVw04.setEnabled(false);

        fragOne.edtSiteName.setText("");
        fragOne.edtSiteProblem.setText("");
        fragOne.edtSiteDate.setText("");
        fragOne.edtSiteID.setText("");

        fragTwo.spnTbShoot.setSelection(0);
        fragTwo.spnNote.setSelection(0);

        changeFragment(1);
    }

    public void changeFragment(int fragNumber){
        transaction = getFragmentManager().beginTransaction();
        switch (fragNumber) {
            case 1:
                isFragOne = true;
                isFragTwo = false;
                isFragThree = false;

                transaction.hide(fragTwo);
                transaction.hide(fragThree);
                transaction.show(fragOne);
                transaction.commit();

                break;

            case 2:
                isFragTwo = true;
                isFragOne = false;
                isFragThree = false;

                transaction.hide(fragOne);
                transaction.hide(fragThree);
                transaction.show(fragTwo);
                transaction.commit();

                break;

            case 3:
                isFragTwo = false;
                isFragOne = false;
                isFragThree = true;

                transaction.hide(fragOne);
                transaction.hide(fragTwo);
                transaction.show(fragThree);
                transaction.commit();
                break;
        }
    }

    public void getImageFromAlbum(){
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
                if(fragThree.lstImage.size()<4){
                    fragThree.lstImage.add("image");
                }

                if(fragThree.isImg1){
                    img01 = BitmapFactory.decodeStream(imageStream,null,options);
                    fragThree.imgVw01.setImageBitmap(img01);
                    fragThree.isImg1 = false;
                    fragThree.imgVw02.setEnabled(true);
                }else if(fragThree.isImg2){
                    img02 = BitmapFactory.decodeStream(imageStream,null,options);
                    fragThree.imgVw02.setImageBitmap(img02);
                    fragThree.imgVw03.setEnabled(true);
                    fragThree.isImg2 = false;
                }else if(fragThree.isImg3){
                    img03 = BitmapFactory.decodeStream(imageStream,null,options);
                    fragThree.imgVw03.setImageBitmap(img03);
                    fragThree.imgVw04.setEnabled(true);
                    fragThree.isImg3 = false;
                }else {
                    img04 = BitmapFactory.decodeStream(imageStream,null,options);
                    fragThree.imgVw04.setImageBitmap(img04);
                    fragThree.isImg4 = false;
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

    public void addNewMessage(final String strMessage){
        final Dialog openDialog = new Dialog(this);
        openDialog.setContentView(R.layout.dialog_add_new);
        openDialog.setTitle("Add New");
        openDialog.setCancelable(false);
        openDialog.setCanceledOnTouchOutside(false);

        final EditText edtAddNew = (EditText) openDialog.findViewById(R.id.edt_add_new);

        final Button btnAddNew = (Button) openDialog.findViewById(R.id.dialog_edtBtn_ok);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(strMessage.equals("Troubleshoot")){
                    String strTbShoot = edtAddNew.getText().toString();
                    fragTwo.lstSavedTroubleshoot.add(strTbShoot);
                    saveList(fragTwo.lstSavedTroubleshoot,TBSHOOT_KEY);
                    //fragTwo.lstTroubleShoot.add(strTbShoot);
                    //fragTwo.adapterLstvwTbShoot.notifyDataSetChanged();
                }else {
                    String strNote = edtAddNew.getText().toString();
                    fragTwo.lstSavedNote.add(strNote);
                    saveList(fragTwo.lstSavedNote,NOTE_KEY);
                }
                openDialog.dismiss();
            }
        });

        final Button btnCancel = (Button)openDialog.findViewById(R.id.dialog_edtBtn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        openDialog.show();
    }

    public void saveReportDialog(){
        final Dialog openDialog = new Dialog(this);
        openDialog.setContentView(R.layout.dialog_save_report);
        openDialog.setTitle("Add New");
        openDialog.setCancelable(false);
        openDialog.setCanceledOnTouchOutside(false);

        final EditText edtSaveReport = (EditText) openDialog.findViewById(R.id.edt_save_report);
        edtSaveReport.setText(getCurrentTime());
        final Button btnSaveReport = (Button) openDialog.findViewById(R.id.dialog_edtBtn_ok);
        btnSaveReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strReportName = edtSaveReport.getText().toString() + ".pdf";
                if(strReportName.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please enter report name",Toast.LENGTH_SHORT).show();
                }else {
                    startGeneratePdf();
                }
                openDialog.dismiss();
            }
        });

        final Button btnCancel = (Button)openDialog.findViewById(R.id.dialog_edtBtn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        openDialog.show();
    }

    //Convert epoch time to normal time
    private String getCurrentTime(){
        long date = System.currentTimeMillis();
        Log.i("getSystemTime","date: " + date);
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_ss");
        String dateString = sdf.format(date);

        return dateString;
    }

    public void deleteDataDialog(final String strProcess, final int idxDelete){
        final Dialog openDialog = new Dialog(this);
        openDialog.setContentView(R.layout.dialog_delete_data);
        openDialog.setTitle("Delete");
        openDialog.setCancelable(false);
        openDialog.setCanceledOnTouchOutside(false);

        final Button btnDeleteList = (Button) openDialog.findViewById(R.id.dialog_btn_del_list);
        btnDeleteList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(strProcess.equals("Note")){
                    fragTwo.lstNote.remove(idxDelete);
                    fragTwo.adapterLstvwNote.notifyDataSetChanged();
                }else {
                    fragTwo.lstTroubleShoot.remove(idxDelete);
                    fragTwo.adapterLstvwTbShoot.notifyDataSetChanged();
                }
                openDialog.dismiss();
            }
        });

        final Button btnDeleteHistory = (Button) openDialog.findViewById(R.id.dialog_btn_del_history);
        btnDeleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(strProcess.equals("Note")){

                }else {

                }
                openDialog.dismiss();
            }
        });

        final Button btnCancel = (Button)openDialog.findViewById(R.id.dialog_edtBtn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();
            }
        });
        openDialog.show();
    }


}
