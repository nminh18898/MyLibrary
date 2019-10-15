package com.nhatminh.example.datastorage.mylibrary.book;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhatminh.example.datastorage.mylibrary.R;

import java.util.ArrayList;

public class BookShelfAdapter extends RecyclerView.Adapter<BookShelfAdapter.BookShelfHolder> {

    Context context;
    ArrayList<Book> books;
    ArrayList<ThumbnailPath> thumbnailPaths;

    ImageLoader imageLoader;

    private static final String TAG = "BookShelfAdapter";

    public BookShelfAdapter(Context context, final ArrayList<Book> books, final ArrayList<ThumbnailPath> thumbnailPaths) {
        setHasStableIds(false);
        this.context = context;
        this.books = books;
        this.thumbnailPaths = thumbnailPaths;

        imageLoader = new ImageLoader(context, books, thumbnailPaths);

    }

    int viewCount=0;
    @NonNull
    @Override
    public BookShelfHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bookshelf_item, parent, false);
        view.setTag(viewCount);

        Log.e(TAG, "CreateViewHolder #" + viewCount);
        viewCount++;

        return new BookShelfHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookShelfHolder holder, int position) {
        Book book = books.get(position);
        ThumbnailPath path = thumbnailPaths.get(position);
        holder.bind(book, path, position);
        Log.e(TAG, "onBindViewHolder: pos - " + position);
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return books.size();
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

        public void bind(Book book, ThumbnailPath thumbnailPath, int position){
            if (thumbnailPath != null) {
                if (thumbnailPath.getDatabasePath() == null ||
                        !imageLoader.isFileExist(thumbnailPath.getDatabasePath()) ) {
                    if (isNetworkPath(thumbnailPath.getOriginalPath())) {
                        imageLoader.downloadImage(thumbnailPath, book, position);

                    }
                    else {
                        imageLoader.copyImageFileToDatabase(thumbnailPath.originalPath, book.getBookId(), position);
                    }

                }
                else {

                    imageLoader.loadImageToView(thumbnailPath.getDatabasePath(), ivThumbnail);
                }
            }

            tvBookName.setText(book.getName());
            tvAuthor.setText(book.getAuthor());
            tvContent.setText(book.getContent());
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BookShelfHolder holder) {
        super.onViewAttachedToWindow(holder);

       /* Book book = books.get(holder.getAdapterPosition());
        ThumbnailPath thumbnailPath = thumbnailPaths.get(holder.getAdapterPosition());
        holder.bind(book, thumbnailPath, holder.getAdapterPosition());*/

        Log.e(TAG, "onViewAttachedToWindow #" + holder.getAdapterPosition());
        Log.e(TAG, "----");
    }

    public boolean isNetworkPath(String path){
        if(path.contains("http") || path.contains("https")){
            return true;
        }
        return false;
    }

}
