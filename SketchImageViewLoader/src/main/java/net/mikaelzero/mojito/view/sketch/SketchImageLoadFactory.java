package net.mikaelzero.mojito.view.sketch;

import android.net.Uri;
import android.view.View;

import com.example.c001apk.imageviewer.ImageViewLoadFactory;
import com.example.c001apk.imageviewer.ContentLoader;
import android.graphics.drawable.Drawable;

import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.SketchImageView;
import net.mikaelzero.mojito.view.sketch.core.state.OldStateImage;

import org.jetbrains.annotations.NotNull;


public class SketchImageLoadFactory implements ImageViewLoadFactory {
    @Override
    public void showImage(@NotNull View view, @NotNull Uri uri, boolean isPreview) {
        if (view instanceof SketchImageView) {
            SketchImageView sketchView = (SketchImageView) view;
            String path = uri.getPath();
            Drawable current = sketchView.getDrawable();

            net.mikaelzero.mojito.view.sketch.core.request.DisplayHelper helper =
                    Sketch.with(view.getContext()).display(path, sketchView);

            // Use OldStateImage to keep current thumbnail as placeholder during HD load.
            // This prevents the black flash when the new request starts.
            // We only apply this if it's not a GIF to avoid potential recycling crashes.
            if (current != null && !(current instanceof pl.droidsonroids.gif.GifDrawable)) {
                helper.loadingImage(new OldStateImage());
            }

            helper.commit();

            sketchView.post(() -> {
                if (sketchView.getZoomer() != null) {
                    sketchView.getZoomer().reset("postLoad");
                }
            });
        }
    }

    @NotNull
    @Override
    public ContentLoader newContentLoader() {
        return new SketchContentLoaderImpl();
    }
}
