package broughton.stephen.continuum;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MyView implements SurfaceHolder.Callback, View.OnTouchListener {

    private final float PIECE_DIM = 0.2f;
    private final float MARK_DIM = 1f;

    private final SurfaceView view;
    private final SurfaceHolder holder;
    private final Resources resources;
    private final Drawable[] pieceIcons;
    private final Drawable gridBg;
    private final Drawable checkWork;
    private final Drawable viewLevels;

    private Drawable checkMark;
    private Drawable exMark;
    private Drawable mark;
    private long showMark;

    public final Level[] levelList = {
            new Level(new byte[][]{
                    {0b0000, 0b0000, 0b0000, 0b0000},
                    {0b0000, 0b0011, 0b0011, 0b0000},
                    {0b0000, 0b0011, 0b0011, 0b0000},
                    {0b0000, 0b0000, 0b0000, 0b0000}
            }),
            new Level(new byte[][]{
                    {0b0000, 0b0000, 0b0000, 0b0000, 0b0000, 0b0000, 0b0000, 0b0000},
                    {0b0000, 0b0000, 0b1100, 0b0100, 0b0110, 0b1001, 0b0000, 0b0000},
                    {0b0000, 0b0000, 0b0011, 0b1100, 0b1100, 0b1101, 0b0000, 0b0000},
                    {0b0000, 0b0000, 0b0000, 0b0101, 0b0000, 0b0101, 0b0000, 0b0000},
                    {0b0000, 0b0000, 0b0011, 0b1110, 0b0000, 0b1010, 0b0000, 0b0000},
                    {0b0000, 0b0011, 0b1111, 0b0110, 0b1100, 0b0110, 0b0000, 0b0000},
                    {0b0000, 0b1100, 0b0111, 0b1010, 0b0011, 0b0000, 0b0000, 0b0000},
                    {0b0000, 0b0000, 0b0000, 0b0000, 0b0000, 0b0000, 0b0000, 0b0000},
            })
    };
    public Level currentLevel = null;

    MyView(SurfaceView view, Resources res){
        this.view = view;
        resources = res;

        pieceIcons = new Drawable[]{
                res.getDrawable(R.drawable.piece0000), res.getDrawable(R.drawable.piece0001),
                res.getDrawable(R.drawable.piece0010), res.getDrawable(R.drawable.piece0011),
                res.getDrawable(R.drawable.piece0100), res.getDrawable(R.drawable.piece0101),
                res.getDrawable(R.drawable.piece0110), res.getDrawable(R.drawable.piece0111),
                res.getDrawable(R.drawable.piece1000), res.getDrawable(R.drawable.piece1001),
                res.getDrawable(R.drawable.piece1010), res.getDrawable(R.drawable.piece1011),
                res.getDrawable(R.drawable.piece1100), res.getDrawable(R.drawable.piece1101),
                res.getDrawable(R.drawable.piece1110), res.getDrawable(R.drawable.piece1111),
        };
        gridBg = res.getDrawable(R.drawable.grid);
        checkWork = res.getDrawable(R.drawable.checkwork);
        viewLevels = res.getDrawable(R.drawable.viewlevels);
        checkMark = res.getDrawable(R.drawable.checkmark);
        exMark = res.getDrawable(R.drawable.exmark);
        showMark = 0;

        holder = view.getHolder();
        holder.addCallback(this);
        view.setOnTouchListener(this);

    }

    void drawLevel(Canvas canvas, int viewWidth, int viewHeight, int dpi, MotionEvent event){
        final int PIECE_DIM = (int) (this.PIECE_DIM * dpi);
        canvas.drawColor(0xFFCCCCCC);
        float evX = 0, evY = 0;
        if(event != null) {
            evX = event.getX();
            evY = event.getY();
        }
        int x, y;

        int r = 0;
        y = (viewHeight - PIECE_DIM * currentLevel.grid.length) / 2;
        for (byte[] row : currentLevel.grid) {
            int c = 0;
            x = (viewWidth - PIECE_DIM * row.length) / 2;
            for (byte piece : row) {
                if(event != null)
                    switch(event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_POINTER_DOWN:
                            if(x < evX && evX < x + PIECE_DIM && y < evY && evY < y + PIECE_DIM) {
                                currentLevel.rotatePiece(r, c);
                            }
                    }
                Drawable pieceIcon = pieceIcons[piece];
                gridBg.setBounds(x, y, x + PIECE_DIM, y + PIECE_DIM);
                pieceIcon.setBounds(x, y, x + PIECE_DIM, y + PIECE_DIM);
                gridBg.draw(canvas);
                pieceIcon.draw(canvas);
                c++;
                x += PIECE_DIM;
            }
            r++;
            y += PIECE_DIM;
        }

        int btnWidth = (int) (MARK_DIM * dpi);
        int btnHeight = btnWidth / 4;
        x = (viewWidth - btnWidth) / 2;
        y = btnHeight / 2;
        checkWork.setBounds(x, y, x + btnWidth, y + btnHeight);
        if(event != null)
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (x < evX && evX < x + btnWidth && y < evY && evY < y + btnHeight) {
                        showMark = System.currentTimeMillis();
                        mark = getMark();
                    }
            }
        checkWork.draw(canvas);
        y = (int) (viewHeight - btnHeight * 1.5);
        viewLevels.setBounds(x, y, x + btnWidth, y + btnHeight);
        if(event != null)
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (x < evX && evX < x + btnWidth && y < evY && evY < y + btnHeight)
                        currentLevel = null;
            }
        viewLevels.draw(canvas);
    }

    public interface CanvasOp {
        void apply(Canvas canvas, int viewWidth, int viewHeight, int dpi);
    }

    void drawOnCanvas(CanvasOp op) {
        Canvas canvas = null;
        while (canvas == null) canvas = holder.lockCanvas();

        synchronized (holder) {
            int viewHeight = view.getHeight();
            int viewWidth = view.getWidth();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            op.apply(canvas, viewWidth, viewHeight, metrics.densityDpi);
        }
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Thread thread = new Thread(() -> {
            long start;
            while(true) {
                start = System.currentTimeMillis();
                onTouch(view, null);
                while(System.currentTimeMillis() - start < 66);
            }
        });

        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        onTouch(view, null);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        drawOnCanvas((canvas, viewWidth, viewHeight, dpi) -> {
                if(currentLevel != null) {
                    drawLevel(canvas, viewWidth, viewHeight, dpi, event);
                    if(showMark != 0){
                        drawCheckOrEx(canvas, viewWidth, viewHeight, dpi);
                    }
                } else {
                    drawLevelList(canvas, viewWidth, viewHeight, dpi, event);
                }
        });
        return true;
    }

    void drawLevelList(Canvas canvas, int viewWidth, int viewHeight, int dpi, MotionEvent event){
        final int metric = (int) (0.04 * dpi);
        canvas.drawColor(0xFFCCCCCC);
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(0xFF666666);
        textPaint.setTextSize(4 * metric);
        Paint linePaint = new Paint();
        linePaint.setColor(0xFFFFFFFF);
        linePaint.setStrokeWidth(1);
        int y = 6 * metric;
        textPaint.setFakeBoldText(true);
        canvas.drawText("Choose a Level:", 2 * metric, y, textPaint);
        textPaint.setFakeBoldText(false);
        y += 3 * metric;
        canvas.drawLine(0, y, viewWidth, y, linePaint);
        int i = 1;
        int topY = y;
        y += 6 * metric;
        int bottomY;
        for(Level level : levelList){
            canvas.drawText("Level " + i, 2 * metric, y, textPaint);
            y += 3 * metric;
            bottomY = y;
            if(event != null)
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (topY < event.getY() && event.getY() < bottomY)
                            currentLevel = levelList[i - 1];
                }
            canvas.drawLine(0, y, viewWidth, y, linePaint);
            topY = y;
            y += 6 * metric;
            i++;
        }
    }

    void drawCheckOrEx(Canvas canvas, int viewWidth, int viewHeight, int dpi) {
        final int MARK_DIM = (int) (this.MARK_DIM * dpi);
        int xMargin = (viewWidth - MARK_DIM) / 2;
        int yMargin = (viewHeight - MARK_DIM) / 2;
        Rect bounds = new Rect(xMargin, yMargin, xMargin + MARK_DIM, yMargin + MARK_DIM);
        int alpha = (int) (255 - (System.currentTimeMillis() - showMark) * 255f / 500) ;
        mark.setBounds(bounds);
        if(alpha < 0){
            showMark = 0;
        } else {
            mark.setAlpha(alpha);
            mark.draw(canvas);
        }
    }

    Drawable getMark(){
        byte[][] grid = currentLevel.grid;
        for(int r = 1; r < grid.length - 1; r++){
            for(int c = 1; c < grid[r].length - 1; c++){
                if(grid[r][c] != (
                        ((grid[r - 1][c] & 0b0010) << 2) | ((grid[r][c + 1] & 0b0001) << 2) |
                                ((grid[r + 1][c] & 0b1000) >> 2) | ((grid[r][c - 1] & 0b0100) >> 2))) {
                    return exMark;
                }
            }
        }
        return checkMark;
    }

}
