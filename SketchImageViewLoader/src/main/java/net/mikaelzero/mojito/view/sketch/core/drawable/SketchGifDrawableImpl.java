/*
 * Copyright (C) 2019 Peng fei Pan <panpfpanpf@outlook.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mikaelzero.mojito.view.sketch.core.drawable;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import net.mikaelzero.mojito.view.sketch.core.decode.ImageAttrs;
import net.mikaelzero.mojito.view.sketch.core.request.ImageFrom;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;

// GIF 缓冲区由解码库管理，适配层只补充 Sketch 图片信息与可见性控制。
// SketchGifDrawableImpl类配置了混淆时忽略警告，以后内部类有变化时需要同步调整混淆配置，并打包验证
public class SketchGifDrawableImpl extends GifDrawable implements SketchGifDrawable {
    private static final String NAME = "SketchGifDrawableImpl";

    private String key;
    private String uri;
    private ImageAttrs imageAttrs;
    private ImageFrom imageFrom;

    private Map<AnimationListener, pl.droidsonroids.gif.AnimationListener> listenerMap;

    SketchGifDrawableImpl(String key, String uri, ImageAttrs imageAttrs, ImageFrom imageFrom,
                          AssetFileDescriptor afd) throws IOException {
        super(afd);
        this.key = key;
        this.uri = uri;
        this.imageAttrs = imageAttrs;
        this.imageFrom = imageFrom;
    }

    SketchGifDrawableImpl(String key, String uri, ImageAttrs imageAttrs, ImageFrom imageFrom,
                          AssetManager assets, String assetName) throws IOException {
        super(assets, assetName);
        this.key = key;
        this.uri = uri;
        this.imageAttrs = imageAttrs;
        this.imageFrom = imageFrom;
    }

    SketchGifDrawableImpl(String key, String uri, ImageAttrs imageAttrs, ImageFrom imageFrom,
                          ByteBuffer buffer) throws IOException {
        super(buffer);
        this.key = key;
        this.uri = uri;
        this.imageAttrs = imageAttrs;
        this.imageFrom = imageFrom;
    }

    SketchGifDrawableImpl(String key, String uri, ImageAttrs imageAttrs, ImageFrom imageFrom,
                          byte[] bytes) throws IOException {
        super(bytes);
        this.key = key;
        this.uri = uri;
        this.imageAttrs = imageAttrs;
        this.imageFrom = imageFrom;
    }

    SketchGifDrawableImpl(String key, String uri, ImageAttrs imageAttrs, ImageFrom imageFrom,
                          FileDescriptor fd) throws IOException {
        super(fd);
        this.key = key;
        this.uri = uri;
        this.imageAttrs = imageAttrs;
        this.imageFrom = imageFrom;
    }

    SketchGifDrawableImpl(String key, String uri, ImageAttrs imageAttrs, ImageFrom imageFrom,
                          File file) throws IOException {
        super(file);
        this.key = key;
        this.uri = uri;
        this.imageAttrs = imageAttrs;
        this.imageFrom = imageFrom;
    }

    SketchGifDrawableImpl(String key, String uri, ImageAttrs imageAttrs, ImageFrom imageFrom,
                          String filePath) throws IOException {
        super(filePath);
        this.key = key;
        this.uri = uri;
        this.imageAttrs = imageAttrs;
        this.imageFrom = imageFrom;
    }

    SketchGifDrawableImpl(String key, String uri, ImageAttrs imageAttrs, ImageFrom imageFrom,
                          Resources res, int id) throws Resources.NotFoundException, IOException {
        super(res, id);
        this.key = key;
        this.uri = uri;
        this.imageAttrs = imageAttrs;
        this.imageFrom = imageFrom;
    }

    SketchGifDrawableImpl(String key, String imageUri, ImageAttrs imageAttrs, ImageFrom imageFrom,
                          ContentResolver resolver, Uri uri) throws IOException {
        super(resolver, uri);
        this.key = key;
        this.uri = imageUri;
        this.imageAttrs = imageAttrs;
        this.imageFrom = imageFrom;
    }

    SketchGifDrawableImpl(String key, String uri, ImageAttrs imageAttrs, ImageFrom imageFrom,
                          InputStream stream) throws IOException {
        super(stream);
        this.key = key;
        this.uri = uri;
        this.imageAttrs = imageAttrs;
        this.imageFrom = imageFrom;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public int getOriginWidth() {
        return imageAttrs.getWidth();
    }

    @Override
    public int getOriginHeight() {
        return imageAttrs.getHeight();
    }

    @Override
    public String getMimeType() {
        return imageAttrs.getMimeType();
    }

    @Override
    public int getExifOrientation() {
        return imageAttrs.getExifOrientation();
    }

    @Override
    public ImageFrom getImageFrom() {
        return imageFrom;
    }

    @Override
    public String getInfo() {
        return String.format(Locale.US, "%s(image=%dx%d,%s, bytes=%d)",
                NAME, getOriginWidth(), getOriginHeight(), getMimeType(), getAllocationByteCount());
    }

    @Override
    public int getByteCount() {
        return (int) getAllocationByteCount();
    }

    @Override
    public Bitmap.Config getBitmapConfig() {
        return isRecycled() ? null : Bitmap.Config.ARGB_8888;
    }

    @Override
    public void addAnimationListener(@NonNull final AnimationListener listener) {
        if (listenerMap == null) {
            listenerMap = new HashMap<>();
        }

        // 这个内部类配置了混淆时忽略警告，以后有变化时需要同步调整混淆配置，并打包验证
        pl.droidsonroids.gif.AnimationListener animationListener = new pl.droidsonroids.gif.AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                listener.onAnimationCompleted(loopNumber);
            }
        };
        addAnimationListener(animationListener);
        listenerMap.put(listener, animationListener);
    }

    @Override
    public boolean removeAnimationListener(AnimationListener listener) {
        if (listenerMap == null || listenerMap.isEmpty()) {
            return false;
        }

        pl.droidsonroids.gif.AnimationListener animationListener = listenerMap.remove(listener);
        return animationListener != null && removeAnimationListener(animationListener);
    }

    @Override
    public void followPageVisible(boolean userVisible, boolean fromDisplayCompleted) {
        if (userVisible) {
            start();
        } else {
            if (fromDisplayCompleted) {
                // 图片加载完了，但是页面还不可见的时候就停留着在第一帧
                seekToFrame(0);
                stop();
            } else {
                stop();
            }
        }
    }
}
