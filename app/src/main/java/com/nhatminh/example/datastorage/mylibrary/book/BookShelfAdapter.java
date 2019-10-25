package com.nhatminh.example.datastorage.mylibrary.book;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhatminh.example.datastorage.mylibrary.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class BookShelfAdapter extends RecyclerView.Adapter<BookShelfAdapter.BookShelfHolder> {

    Context context;
    ImageLoader imageLoader;
    ArrayList<Book> books;

    RecyclerView recyclerView;

    List<Pair<Integer, Bitmap>> thumbnails;


    Bitmap defaultThumbnail;

    private static final String TAG = "BookShelfAdapter";
    private static final int MAX_THUMBNAIL_ELEMENT = 10;


    public BookShelfAdapter(final Context context, final ArrayList<Book> books) {
        setHasStableIds(false);
        this.context = context;
        this.books = books;
        this.imageLoader = new ImageLoader(context);
        this.thumbnails = new LinkedList<>();

        imageLoader.setOnBitmapLoadingCompletedListener(new ImageLoader.OnBitmapLoadingCompletedListener() {
            @Override
            public void onBitmapLoadingCompleted(Bitmap bitmap, int id) {

                BookShelfHolder holder = (BookShelfHolder) recyclerView.findViewHolderForAdapterPosition(id);
                if(holder != null){
                    holder.ivThumbnail.setImageBitmap(bitmap);
                    Log.e(TAG, "Thumbnail #" + id + " is shown");
                }

                if(!isThumbnailExistInList(id)){
                    addDecodedThumbnailToList(id, bitmap);
                }

            }
        });

        defaultThumbnail = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_image);
    }

    private void addDecodedThumbnailToList(int position, Bitmap bitmap){
       if(thumbnails.size() >= MAX_THUMBNAIL_ELEMENT){
            thumbnails.remove(0);
       }

        thumbnails.add(new Pair<Integer, Bitmap>(position, bitmap));

    }

    private boolean isThumbnailExistInList(int id){
        for(int i=0;i<thumbnails.size();i++){
            if(thumbnails.get(i).first == id){
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public BookShelfHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bookshelf_item, parent, false);
        return new BookShelfHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookShelfHolder holder, int position) {

        Book book = books.get(position);
        holder.bind(book, position);
        Log.e(TAG, "onBindViewHolder: pos - " + position);
    }

    public void triggerSkipMode(int firstVisiblePos, int lastVisiblePos){
        if(imageLoader!=null){
            imageLoader.triggerSkipMode(firstVisiblePos, lastVisiblePos);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;

    }


    public class BookShelfHolder extends RecyclerView.ViewHolder{

        ImageView ivThumbnail;
        TextView tvBookName, tvAuthor, tvContent;

        public BookShelfHolder(@NonNull View itemView) {
            super(itemView);
            tvBookName = itemView.findViewById(R.id.tvBookName);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvContent = itemView.findViewById(R.id.tvContent);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);

            itemView.setOnCreateContextMenuListener((BookShelf)context);
        }

        public void bind(Book book, int position){
            tvBookName.setText(book.getName());
            tvAuthor.setText(book.getAuthor());
            tvContent.setText(book.getContent());
            imageLoader.getImage(book.getThumbnailPath(), position, 150, 200);

        }

        public void updateThumbnail(Bitmap bitmap){
            if(ivThumbnail != null){
                ivThumbnail.setImageBitmap(bitmap);
            }
        }

        public boolean isDefaultThumbnail(){
            if(ivThumbnail != null) {
                Bitmap bitmap = ((BitmapDrawable) ivThumbnail.getBackground()).getBitmap();

                if(bitmap.sameAs(defaultThumbnail)){
                    return true;
                }

            }
            return false;
        }
    }

    public Bitmap getThumbnailAvailable(int position){
        for(int i = 0; i < thumbnails.size(); i++){
            if(thumbnails.get(i).first == position){
                return thumbnails.get(i).second;
            }
        }
        return null;
    }


    @Override
    public void onViewAttachedToWindow(@NonNull BookShelfHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.e(TAG, "onViewAttachedToWindow #" + holder.getAdapterPosition());

        int position = holder.getAdapterPosition();

        Bitmap bitmap = getThumbnailAvailable(position);
        if(bitmap != null){
            holder.updateThumbnail(bitmap);
            Log.e(TAG, "Thumbnail #" + holder.getAdapterPosition() + " is available in list, get it and update");
        }

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BookShelfHolder holder) {
        super.onViewDetachedFromWindow(holder);

        holder.updateThumbnail(defaultThumbnail);
    }

    public boolean isNetworkPath(String path){
        if(path.contains("http") || path.contains("https")){
            return true;
        }
        return false;
    }


    public void clean(){
        unregisterImageLoaderBroadcastReceiver();
        recycleBitmapList();
        closeImageLoader();
    }

    private void unregisterImageLoaderBroadcastReceiver(){
        if(imageLoader != null){
            imageLoader.unregisterBroadcastReceiver();
        }
    }

    private void closeImageLoader(){
        imageLoader.cancelLoading();
    }

    private void recycleBitmapList(){
        if(thumbnails != null) {
            for (int i = 0; i < thumbnails.size(); i++) {
                if (thumbnails.get(i).second != null) {
                    thumbnails.get(i).second.recycle();
                }
            }
            thumbnails = null;
        }
    }

}
