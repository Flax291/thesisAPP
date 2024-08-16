package com.example.routeapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RouteView extends View {
    private MainActivity mainActivity;
    private List<Location> routePoints = new ArrayList<>();
    private List<CustomMarker> customMarkers = new ArrayList<>();
    private Paint paint;
    private Paint userMarkerPaint;
    private float minLat = Float.MAX_VALUE, maxLat = Float.MIN_VALUE;
    private float minLon = Float.MAX_VALUE, maxLon = Float.MIN_VALUE;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor = 1.0f;
    private float translateX = 0.0f, translateY = 0.0f;
    private float focusX = 0.0f, focusY = 0.0f;
    private Location currentLocation;
    private Bitmap selectedMarkerBitmap; // Для хранения выбранного маркера
    private float markerX, markerY; // Координаты выбранного маркера


    public RouteView(Context context) {
        super(context);
        init(context);
    }

    public RouteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RouteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5f);

        userMarkerPaint = new Paint();
        userMarkerPaint.setColor(Color.BLUE);
        userMarkerPaint.setStyle(Paint.Style.FILL);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void addPoint(Location location) {
        routePoints.add(location);
        updateBounds(location);
        invalidate(); // Request to redraw the view
    }

    public void setSelectedMarker(int markerResource) {
        // Загрузка выбранного маркера из ресурсов
        selectedMarkerBitmap = BitmapFactory.decodeResource(getResources(), markerResource);
        invalidate();
    }

    public void setCurrentLocation(Location location) {
        currentLocation = location;
        invalidate(); // Request to redraw the view
    }

    public void clearPoints() {
        routePoints.clear();
        customMarkers.clear();
        minLat = Float.MAX_VALUE;
        maxLat = Float.MIN_VALUE;
        minLon = Float.MAX_VALUE;
        maxLon = Float.MIN_VALUE;
        translateX = 0.0f;
        translateY = 0.0f;
        focusX = 0.0f;
        focusY = 0.0f;
    }

    public List<Location> getRoutePoints() {

        return routePoints;
    }

    public void addCustomMarker(Location location, int markerResource) {
        CustomMarker marker = new CustomMarker(location, markerResource);
        customMarkers.add(marker);
        updateBounds(location); // Обновляем границы карты
        invalidate(); // Перерисовываем карту
    }

    private void updateBounds(Location location) {
        minLat = Math.min(minLat, (float) location.getLatitude());
        maxLat = Math.max(maxLat, (float) location.getLatitude());
        minLon = Math.min(minLon, (float) location.getLongitude());
        maxLon = Math.max(maxLon, (float) location.getLongitude());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mainActivity.isRouteActive()) return;
        canvas.save();
        canvas.translate(translateX + focusX, translateY + focusY);
        canvas.scale(scaleFactor, scaleFactor);

        if (routePoints.size() > 1) {
            float width = getWidth();
            float height = getHeight();

            for (int i = 1; i < routePoints.size(); i++) {
                Location start = routePoints.get(i - 1);
                Location end = routePoints.get(i);

                float startX = getXCoordinate(start.getLongitude(), width);
                float startY = getYCoordinate(start.getLatitude(), height);
                float endX = getXCoordinate(end.getLongitude(), width);
                float endY = getYCoordinate(end.getLatitude(), height);

                canvas.drawLine(startX, startY, endX, endY, paint);
            }
        }

        for (CustomMarker marker : customMarkers) {
            float markerX = getXCoordinate(marker.getLocation().getLongitude(), getWidth());
            float markerY = getYCoordinate(marker.getLocation().getLatitude(), getHeight());

            Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(), marker.getMarkerResource());
            float markerWidth = markerBitmap.getWidth() / 2;
            float markerHeight = markerBitmap.getHeight() / 2;

            canvas.drawBitmap(markerBitmap, markerX - markerWidth, markerY - markerHeight, null);
        }

        if (currentLocation != null) {
            float width = getWidth();
            float height = getHeight();
            float userX = getXCoordinate(currentLocation.getLongitude(), width);
            float userY = getYCoordinate(currentLocation.getLatitude(), height);

            Bitmap userMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_marker); // Загрузка изображения маркера пользователя
            int markerWidth = userMarkerBitmap.getWidth();
            int markerHeight = userMarkerBitmap.getHeight();
            // Уменьшение размера маркера
            float scaleFactor = 0.5f; // Масштабный коэффициент для уменьшения размера в половину
            int scaledMarkerWidth = (int) (markerWidth * scaleFactor);
            int scaledMarkerHeight = (int) (markerHeight * scaleFactor);
            // Вычисление новых координат и размеров прямоугольника для отрисовки маркера
            float left = userX - scaledMarkerWidth / 2;
            float top = userY - scaledMarkerHeight / 2;
            float right = left + scaledMarkerWidth;
            float bottom = top + scaledMarkerHeight;
            RectF markerRect = new RectF(left, top, right, bottom);
            canvas.drawBitmap(userMarkerBitmap, null, markerRect, null);// Отрисовка маркера текущего положения пользователя
        }
        if (selectedMarkerBitmap != null) {
            // Отображение выбранного маркера
            canvas.drawBitmap(selectedMarkerBitmap, markerX, markerY, null);
        }
        canvas.restore();
    }

    private float getXCoordinate(double longitude, float width) {
        float xCoordinate;
        if (minLon < 0 &&        maxLon > 0) {
            xCoordinate = (float) ((longitude - minLon) / (maxLon - minLon) * width);
        } else {
            xCoordinate = (float) ((longitude - minLon) / (maxLon - minLon) * width);
        }
        Log.d("DEBUG_COORDINATE", "Longitude: " + longitude + ", X: " + xCoordinate);
        return xCoordinate;
    }

    private float getYCoordinate(double latitude, float height) {
        float yCoordinate;
        if (minLat < 0 && maxLat > 0) {
            yCoordinate = (float) ((maxLat - latitude) / (maxLat - minLat) * height);
        } else {
            yCoordinate = (float) ((maxLat - latitude) / (maxLat - minLat) * height);
        }
        Log.d("DEBUG_COORDINATE", "Latitude: " + latitude + ", Y: " + yCoordinate);
        return yCoordinate;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(x, y);
                break;
        }
        return true;
    }

    private void handleActionDown(float x, float y) {
        float width = getWidth();
        float height = getHeight();
        float markerX = (x - translateX - focusX) / scaleFactor;
        float markerY = (y - translateY - focusY) / scaleFactor;

        // Проверяем, нажата ли точка на маркере
        for (CustomMarker marker : customMarkers) {
            float markerScreenX = getXCoordinate(marker.getLocation().getLongitude(), width);
            float markerScreenY = getYCoordinate(marker.getLocation().getLatitude(), height);
            if (Math.abs(markerX - markerScreenX) < 50 && Math.abs(markerY - markerScreenY) < 50) {
                mainActivity.setTargetLocation(marker.getLocation());
                return;
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
            invalidate();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            translateX -= distanceX / scaleFactor;
            translateY -= distanceY / scaleFactor;
            invalidate();
            return true;
        }
    }

    private static class CustomMarker {
        private Location location;
        private int markerResource;

        public CustomMarker(Location location, int markerResource) {
            this.location = location;
            this.markerResource = markerResource;
        }

        public Location getLocation() {
            return location;
        }

        public int getMarkerResource() {
            return markerResource;
        }
    }
}

