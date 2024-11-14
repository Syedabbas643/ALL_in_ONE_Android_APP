package com.example.gamer.http;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gamer.R;
import com.squareup.picasso.Picasso;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;


public class WhatsappActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Stack<String> folderStack;

    Filedownloader down;
    private PowerManager.WakeLock wakeLock;
    String httpurl;
    private FileAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whatsapp);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        httpurl = getIntent().getStringExtra("httpurl");
        down = Filedownloader.getInstance(this);
        folderStack = new Stack<>();
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        new webscare2().execute(httpurl);
    }


    private class webscare2 extends AsyncTask<String,Void, List<FileItem>> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(WhatsappActivity.this, android.R.anim.fade_in));

        }
        @Override
        protected List<FileItem> doInBackground(String... urls) {
            List<FileItem> fileItems = new ArrayList<>();

            try {
                String folderUrl = urls[0];
                int timeoutMillis = 5000;
                Connection connection = Jsoup.connect(folderUrl);
                connection.timeout(timeoutMillis);
                Document doc = connection.get();
                Elements elements = doc.getElementsByTag("a");
                for (Element element : elements) {
                    String fileName = element.text().toString();
                    String fileurl = element.attr("href");
                    boolean isDirectory = element.attr("href").endsWith("/");
                    fileItems.add(new FileItem(fileName,fileurl,isDirectory));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return fileItems;
        }
        @Override
        protected void onPostExecute(List<FileItem> fileItems) {
            //super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(WhatsappActivity.this, android.R.anim.fade_out));
            fileAdapter = new FileAdapter(fileItems);
            recyclerView.setAdapter(fileAdapter);

        }
    }
    private class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

        private List<FileItem> fileItems;
        private static Set<FileItem> selectedItems = new HashSet<>();


        public FileAdapter(List<FileItem> fileItems) {
            this.fileItems = fileItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fit_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FileItem fileItem = fileItems.get(position);
            boolean isSelected = selectedItems.contains(fileItem);
            holder.fileNameTextView.setText(fileItem.getFileName());
            holder.bind(fileItem,isSelected);

            if (fileItem.isDirectory()) {
                holder.imageView.setImageResource(R.drawable.baseline_folder_zip_24);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fileAdapter.clearSelection();
                        String folderUrl = httpurl + fileItem.getFileurl();
                        Intent intent1 = new Intent(WhatsappActivity.this, WhatsappActivity.class);
                        intent1.putExtra("httpurl",folderUrl);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);
                    }
                });
            }else {
                String filename  = fileItem.getFileName();
                if (filename.endsWith(".apk")){
                    holder.imageView.setImageResource(R.drawable.android_24);
                } else if (filename.endsWith(".mp4")||filename.endsWith(".mkv")) {
                    holder.imageView.setImageResource(R.drawable.movie_);
                } else if (filename.endsWith(".zip")||filename.endsWith(".rar")) {
                    holder.imageView.setImageResource(R.drawable.archive_24);
                } else if (filename.endsWith(".jpg")) {
                    holder.imageView.setImageResource(R.drawable.insert_photo_24);
                } else {
                    holder.imageView.setImageResource(R.drawable.attach_file_24);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Server-check:server");
                        wakeLock.acquire(30 * 60 * 1000L);
                        if (selectedItems.isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(WhatsappActivity.this);
                            builder.setTitle("Do you Want to Download this file?\n"+" ")
                                    .setPositiveButton("In This App", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String fileUrl = httpurl + fileItem.getFileurl();
                                            System.out.println(fileUrl);
                                            down.main(fileUrl, fileItem.getFileName(), 6);
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNeutralButton("To Another APP", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String fileUrl = httpurl + fileItem.getFileurl();
                                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                            shareIntent.setType("text/plain");
                                            shareIntent.putExtra(Intent.EXTRA_TEXT,fileUrl);
                                            Intent chooser = Intent.createChooser(shareIntent, "Share link");
                                            try {
                                                if (chooser.resolveActivity(getPackageManager()) != null) {
                                                    startActivity(chooser);
                                                }
                                            } catch (ActivityNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        } else {
                            toggleSelection(fileItem);
                            holder.bind(fileItem, selectedItems.contains(fileItem));
                        }
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        toggleSelection(fileItem);
                        holder.bind(fileItem, selectedItems.contains(fileItem));
                        return true;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return fileItems.size();
        }
        private void toggleSelection(FileItem fileItem) {
            if (selectedItems.contains(fileItem)) {
                selectedItems.remove(fileItem);
            } else {
                selectedItems.add(fileItem);
            }
            notifyDataSetChanged();
        }
        public void clearSelection() {
            selectedItems.clear();
            notifyDataSetChanged();
        }

        public Set<FileItem> getSelectedItems() {
            return selectedItems;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView fileNameTextView;
            ImageView imageView;
            boolean isSelected;
            RelativeLayout cardView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                fileNameTextView = itemView.findViewById(R.id.textView);
                cardView = itemView.findViewById(R.id.cardView);
                imageView = itemView.findViewById(R.id.imageView);
            }
            public void bind(FileItem fileItem,boolean isSelected) {
                fileNameTextView.setText(fileItem.getFileName());
                itemView.setSelected(isSelected);
                cardView.setActivated(isSelected);
            }

            public void setSelected(boolean selected) {
                isSelected = selected;
                itemView.setSelected(selected);
            }
        }
    }

    private static class FileItem {
        private String fileName;
        private String fileurl;
        private boolean isDirectory;

        public FileItem(String fileName,String fileurl, boolean isDirectory) {
            this.fileName = fileName;
            this.isDirectory = isDirectory;
            this.fileurl = fileurl;
        }

        public String getFileName() {
            return fileName;
        }
        public String getFileurl() {
            return fileurl;
        }
        public boolean isDirectory() {
            return isDirectory;
        }
    }
    public void downloadtapped(View view) {
        Set<FileItem> selectedItems = fileAdapter.getSelectedItems();
        for (FileItem item : selectedItems) {
            // Perform download for each selected item
            String fileUrl = httpurl + item.getFileurl();
            down.main(fileUrl, item.getFileName(), 6);
        }
        fileAdapter.clearSelection();
    }
}

