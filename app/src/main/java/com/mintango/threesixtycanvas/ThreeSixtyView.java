package com.mintango.threesixtycanvas;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

/**
 * Created by jaimani on 18/5/16.
 */
public class ThreeSixtyView extends View implements View.OnTouchListener{

    Bitmap sprite;
    private float oldXMove;
    private int smoothness = 5;
    private Rect src;
    private Rect dst;

    private int left, top, right, bottom;
    private int cols,rows;
    private int width, height;
    private int currRow, currCol;
    private int viewWidth, viewHeight;
    private String url;

    public ThreeSixtyView(final Context context, final AttributeSet attrs) {
        super(context);

        // TODO: Check if source points to remote resource or local and process accordingly.
        //Get image from image uri
        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);

        //Get attributes and set data
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ThreeSixtyView,
                0, 0);

        try {
            url = a.getString(R.styleable.ThreeSixtyView_spriteSrc);
            height = a.getInt(R.styleable.ThreeSixtyView_spriteCellHeight, 0);
            width = a.getInt(R.styleable.ThreeSixtyView_spriteCellWidth, 0);
            rows = a.getInt(R.styleable.ThreeSixtyView_spriteRows, 0);
            cols = a.getInt(R.styleable.ThreeSixtyView_spriteCols, 0);
            //Show a temp image
            Bitmap loading =BitmapFactory.decodeResource(getResources(), R.drawable.loading);
            calculateViewDimensions(loading, loading.getWidth(), loading.getHeight());
            //Toast.makeText(context, "Loading 360 View", Toast.LENGTH_LONG).show();

            // Now we have data lets get the image and paint the view
            Ion.with(context)
                    .load(url).withBitmap().smartSize(false).asBitmap()
                    .setCallback(new FutureCallback<Bitmap>() {
                        @Override
                        public void onCompleted(Exception e, Bitmap result) {
                            bindTouchListener();
                            calculateViewDimensions(result, width, height);
                        }
                    });
        } finally {
            a.recycle();
        }

    }


    private void calculateViewDimensions(Bitmap result, int width, int height){
        ThreeSixtyView.this.sprite = result;
        //We have the sprite Start painting the canvas
        int dLeft, dTop, dRight, dBottom;
        //Aspect ratio is width divided by height
        float aspectRatio = this.width / this.height;
        //We already know how to render the first image from sprite so we do that
        src = new Rect(0, 0, this.width, this.height);

        //Destination rectangle should be the same aspect ratio as the source image
        //Three things need to be determined height , width and padding (horizontal or vertical)
        //To get height from width h/a and for width h*a
        //First get what is shorter height or width
        Log.e("Three Sixty "  , viewHeight + "  " + viewWidth);
        if(viewHeight < viewWidth){
            //make height of rect same as screen height and scale accordingly
            dTop = 0;
            dBottom = viewHeight;
            // to get width from height
            dRight = (int)(viewHeight * aspectRatio);

            //Now comes the padding. In this case padding needs to be only applied to dleft
            //So space left on screen divided by 2 is what d left will be
            int padding  = (int)((viewWidth - dRight)/2);
            //We must add this padding to both left and right

            dLeft = padding;
            dRight += padding;

        }else{
            //make width of rect same as screen width and scale accordingly
            dLeft = 0;
            dRight = viewWidth;

            // to get height from width
            dBottom = (int)(viewWidth / aspectRatio);

            //Now comes the padding. In this case padding needs to be only applied to dleft
            //So space left on screen divided by 2 is what d left will be
            int padding  = (int)((viewHeight - dBottom)/2);
            //We must add this padding to both left and right

            dTop = padding;
            dBottom += padding;
        }
        Log.e("Three Sixty" , "show image called");
        dst = new Rect(dLeft, dTop, dRight, dBottom);
        showImage();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawARGB(255, 0, 0, 0);
        if(sprite != null){
            canvas.drawBitmap(sprite, src, dst, null);
        }
    }

    private void left() {
        //Log.e("RC", currRow + "  " + currCol);
        //Code to point to correct cell in the sprite sheet
        if(currCol == cols -1){
            currCol = 0;
            if(currRow == rows -1){
                currRow = 0;
            }else{
                currRow++;
            }
        }else{
            currCol++;
        }
        //Calculate position based on row and col and show cropped image
        showImage();
    }

    private void right() {
//            Log.e("RC", currRow + "  " + currCol);
        //Code to point to correct cell in the sprite sheet
        if(currCol == 0){
            currCol = cols -1;
            if(currRow == 0){
                currRow = rows -1;
            }else{
                currRow--;
            }
        }else{
            currCol--;
        }
        //Calculate position based on row and col
        showImage();
    }

    private void showImage() {
        left   = currCol * width;
        top    = currRow * height;
        right  = left    + width;
        bottom = top     + height;

        src = new Rect(left, top, right, bottom);
        invalidate();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:

                if(motionEvent.getX() - oldXMove > smoothness){
                    right();
                    oldXMove = motionEvent.getX();
                }
                if(motionEvent.getX() - oldXMove < (-1 * smoothness)){
                    left();
                    oldXMove = motionEvent.getX();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(viewWidth, viewHeight);
        calculateViewDimensions(this.sprite, width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void bindTouchListener(){
        // This looks like an adult joke :P
        this.setOnTouchListener(this);
    }
}