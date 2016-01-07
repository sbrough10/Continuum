package broughton.stephen.continuum;

public class Level {

    byte[][] grid;

    Level(byte[][] grid) {
        this.grid = grid;
    }

    public void rotatePiece(int row, int col){
        if(row < grid.length && col < grid[row].length) {
            byte piece = grid[row][col];
            grid[row][col] = (byte) ((piece >> 1) | ((piece & 1) << 3));
        }
    }
}
